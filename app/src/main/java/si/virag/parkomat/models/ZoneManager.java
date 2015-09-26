package si.virag.parkomat.models;

import android.content.Context;
import android.support.annotation.NonNull;

import si.virag.parkomat.R;

public class ZoneManager {

    private final Context appContext;

    @NonNull
    private final String[] zoneNames;

    public ZoneManager(Context context) {
        this.appContext = context;

        zoneNames = appContext.getResources().getStringArray(R.array.zones);
    }

    public String lastSelectedZone() {
        return zoneNames[0];
    }
}
