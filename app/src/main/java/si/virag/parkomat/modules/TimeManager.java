package si.virag.parkomat.modules;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.ChronoUnit;

import rx.Observable;
import rx.Subscriber;

/**
 * Handles parking time related things
 */
public class TimeManager {

    @NonNull
    private final Context appContext;

    public TimeManager(@NonNull Context applicationContext) {
        this.appContext = applicationContext;
    }

    public LocalTime initialDisplayedTime() {
        return LocalTime.now().plus(1, ChronoUnit.HOURS);
    }

    public Observable<LocalTime> pickTime(@NonNull final Activity owner, @NonNull final LocalTime currentlySelected) {
        return Observable.create(new Observable.OnSubscribe<LocalTime>() {
            @Override
            public void call(final Subscriber<? super LocalTime> subscriber) {
                TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
                        LocalTime instant = LocalTime.now()
                                                     .with(ChronoField.HOUR_OF_DAY, hourOfDay)
                                                     .with(ChronoField.MINUTE_OF_HOUR, minute);
                        subscriber.onNext(instant);
                    }
                };

                DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        subscriber.onCompleted();
                    }
                };

                TimePickerDialog dialog = TimePickerDialog.newInstance(listener, currentlySelected.get(ChronoField.HOUR_OF_DAY), currentlySelected.get(ChronoField.MINUTE_OF_HOUR), true);
                dialog.setOnDismissListener(dismissListener);
                dialog.show(owner.getFragmentManager(), "TimePicker");
            }
        });

    }

}
