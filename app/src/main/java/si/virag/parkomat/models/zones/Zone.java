package si.virag.parkomat.models.zones;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("NullableProblems")
public class Zone {

    @SerializedName("type")
    @NonNull
    public String zoneType;

    @SerializedName("maxHours")
    public int maxHours;    // If this is 0, take value from the corresponding zone type.

}
