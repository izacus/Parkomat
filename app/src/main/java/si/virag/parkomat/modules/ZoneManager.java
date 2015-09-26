package si.virag.parkomat.modules;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.config.NaturalOrderComparator;

import org.threeten.bp.LocalTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import rx.Observable;
import rx.Subscriber;
import si.virag.parkomat.models.zones.Zone;
import si.virag.parkomat.models.zones.ZoneInformation;

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

    public int getValidHoursToPayFromThisMoment(LocalTime desiredTimeToPayTo, String zone) {
        long diffMinutes = ChronoUnit.MINUTES.between(LocalTime.now(), desiredTimeToPayTo);
        int hours = (int)Math.ceil(diffMinutes / 60.0);
        int maxHoursInZone = maxHoursForZone(zone);
        return Math.min(hours, maxHoursInZone);
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
}
