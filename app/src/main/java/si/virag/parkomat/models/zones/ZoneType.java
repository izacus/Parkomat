package si.virag.parkomat.models.zones;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

@SuppressWarnings("NullableProblems")
public class ZoneType {

    @SerializedName("maxHours")
    public int maxHours;

    @SerializedName("price")
    public float pricePerHour;

    @SerializedName("times")
    @NonNull
    public Map<String, ParkingTime> times;

    public static class ParkingTime {
        @SerializedName("from")
        public int fromHour;

        @SerializedName("to")
        public int toHour;
    }
}
