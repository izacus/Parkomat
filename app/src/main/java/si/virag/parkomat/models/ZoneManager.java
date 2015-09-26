package si.virag.parkomat.models;

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

import java.io.IOException;
import java.io.InputStreamReader;
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

    private ZoneInformation zoneInformation;

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
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not load zone data!", e);
        }
    }

    public Observable<String> pickZone(final Activity owner, String currentZone) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                new MaterialDialog.Builder(owner)
                        .title("Izberi cono")
                        .items(zoneInformation.zones.keySet().toArray(new String[zoneInformation.zones.size()]))
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

    public int maxHoursForZone(@NonNull String zoneName) {
        Zone zone = zoneInformation.zones.get(zoneName);
        if (zone.maxHours != 0) return zone.maxHours;
        return zoneInformation.zoneTypes.get(zone.zoneType).maxHours;
    }

    public String lastSelectedZone() {
        return preferences.getString(PREF_LAST_CHOSEN, zoneInformation.zones.keySet().iterator().next());
    }
}
