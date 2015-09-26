package si.virag.parkomat.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

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

    public String lastSelectedZone() {
        return preferences.getString(PREF_LAST_CHOSEN, zoneInformation.zones.keySet().iterator().next());
    }
}
