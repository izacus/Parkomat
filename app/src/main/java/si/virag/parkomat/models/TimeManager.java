package si.virag.parkomat.models;

import android.content.Context;
import android.support.annotation.NonNull;

import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;

/**
 * Handles parking time related things
 */
public class TimeManager {

    @NonNull
    private final Context appContext;

    public TimeManager(@NonNull Context applicationContext) {
        this.appContext = applicationContext;
    }

    public Instant initialDisplayedTime() {
        return Instant.now().plus(1, ChronoUnit.HOURS);
    }
}
