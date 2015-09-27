package si.virag.parkomat.modules;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.config.NaturalOrderComparator;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import rx.Observable;
import rx.Subscriber;
import si.virag.parkomat.R;
import si.virag.parkomat.models.zones.Zone;
import si.virag.parkomat.models.zones.ZoneInformation;
import si.virag.parkomat.models.zones.ZoneType;

public class ZoneManager {
    private static final String LOG_TAG = "Parkomat.Zones";

    private static final String PREF_LAST_CHOSEN = "Last.Zone";

    @NonNull
    private final Context appContext;
    @NonNull
    private final SharedPreferences preferences;

    @NonNull
    private ZoneInformation zoneInformation;

    @NonNull
    private String[] zoneNames;

    public ZoneManager(@NonNull Context context) {
        this.appContext = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        loadZoneData();
    }

    private void loadZoneData() {
        Log.d(LOG_TAG, "Loading zone data...");

        try {
            GZIPInputStream inputStream = new GZIPInputStream(appContext.getAssets().open("data"));
            Gson gson = new Gson();
            zoneInformation = gson.fromJson(new InputStreamReader(inputStream), ZoneInformation.class);

            List<String> zoneNames = new ArrayList<>(zoneInformation.zones.keySet());
            Collections.sort(zoneNames, new NaturalOrderComparator());
            this.zoneNames = zoneNames.toArray(new String[zoneNames.size()]);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not load zone data!", e);
        }
    }

    public Observable<String> pickZone(final Activity owner) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                new MaterialDialog.Builder(owner)
                        .title("Izberi cono")
                        .items(zoneNames)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                final String zoneName = charSequence.toString();
                                preferences.edit().putString(PREF_LAST_CHOSEN, zoneName).apply();
                                subscriber.onNext(zoneName);
                            }
                        })
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                subscriber.onCompleted();
                            }
                        })
                        .show();
            }
        });
    }

    public int getValidHoursToPayFromThisMoment(@NonNull final LocalTime desiredTimeToPayTo, @NonNull final String zone) {
        long diffMinutes = ChronoUnit.MINUTES.between(LocalTime.now(), desiredTimeToPayTo);

        // Check for max parking time in the zone
        ZoneType.ParkingTime time = getCurrentlyValidParkingTimeForZone(zone);

        // Parking is free today!
        if (time == null) return -1;

        diffMinutes = Math.min(diffMinutes, ChronoUnit.MINUTES.between(LocalTime.now(), LocalTime.now().with(ChronoField.HOUR_OF_DAY, time.toHour)));

        int hours = (int)Math.ceil(diffMinutes / 60.0);
        int maxHoursInZone = maxHoursForZone(zone);
        return Math.min(hours, maxHoursInZone);
    }

    @Nullable
    private ZoneType.ParkingTime getCurrentlyValidParkingTimeForZone(@NonNull final String zone) {
        LocalDateTime now = LocalDateTime.now();
        ZoneType zoneType = zoneInformation.zoneTypes.get(zoneInformation.zones.get(zone).zoneType);

        // Parking is free on sundays
        if (now.getDayOfWeek().equals(DayOfWeek.SUNDAY)) return null;
        if (now.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            if (zoneType.times.containsKey("sat")) {
                return zoneType.times.get("sat");
            } else {
                return null;
            }
        }

        return zoneType.times.get("week");
    }

    public float getPriceForZone(@NonNull String zoneName, int hours) {
        Zone zone = zoneInformation.zones.get(zoneName);
        return zoneInformation.zoneTypes.get(zone.zoneType).pricePerHour * hours;
    }

    public int maxHoursForZone(@NonNull String zoneName) {
        Zone zone = zoneInformation.zones.get(zoneName);
        if (zone.maxHours != 0) return zone.maxHours;
        return zoneInformation.zoneTypes.get(zone.zoneType).maxHours;
    }

    public String lastSelectedZone() {
        return preferences.getString(PREF_LAST_CHOSEN, zoneInformation.zones.keySet().iterator().next());
    }

    public String getZoneInfoString(@NonNull final String zone) {
        LocalDate date = LocalDate.now();
        if (date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) return "Parkiranje je v nedeljo brezplačno.";

        Zone z = zoneInformation.zones.get(zone);
        ZoneType zoneType = zoneInformation.zoneTypes.get(z.zoneType);

        StringBuilder str = new StringBuilder();
        str.append("Cona ");
        str.append(zone);
        str.append(", ");

        if (date.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            if (zoneType.times.containsKey("sat")) {
                ZoneType.ParkingTime pt = zoneType.times.get("sat");
                str.append("sob. ");
                str.append(String.valueOf(pt.fromHour) + ":00 - ");
                str.append(String.valueOf(pt.toHour) + ":00, ");
            } else {
                str.append("parkiranje v soboto brezplačno.");
                return str.toString();
            }
        } else {
            ZoneType.ParkingTime pt = zoneType.times.get("week");
            str.append("pon-pet. ");
            str.append(String.valueOf(pt.fromHour) + ":00 - ");
            str.append(String.valueOf(pt.toHour) + ":00, ");
        }

        str.append("cena " + String.format(Locale.GERMAN, "%.2f", zoneType.pricePerHour) + "€/h, ");
        int maxHours = z.maxHours == 0 ? zoneType.maxHours : z.maxHours;
        str.append("največ " + appContext.getResources().getQuantityString(R.plurals.hours_hint, maxHours, maxHours) + ".");
        return str.toString();
    }
}
