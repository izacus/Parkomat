package si.virag.parkomat.models.zones;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

@SuppressWarnings("NullableProblems")
public class ZoneInformation {

    @SerializedName("zoneTypes")
    @NonNull
    public Map<String, ZoneType> zoneTypes;

    @SerializedName("zones")
    @NonNull
    public Map<String, Zone> zones;

}
