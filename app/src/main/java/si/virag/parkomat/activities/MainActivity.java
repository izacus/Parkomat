package si.virag.parkomat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;
import org.threeten.bp.temporal.ChronoUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import rx.Observer;
import rx.functions.Action1;
import si.virag.parkomat.ParkomatApplication;
import si.virag.parkomat.R;
import si.virag.parkomat.models.Car;
import si.virag.parkomat.models.CarsManager;
import si.virag.parkomat.models.TimeManager;
import si.virag.parkomat.models.ZoneManager;

public class MainActivity extends AppCompatActivity {

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm ").withZone(ZoneId.systemDefault());

    @Bind(R.id.main_registration_plate)
    TextView registrationPlate;

    @Bind(R.id.main_car_name)
    TextView carName;

    @Bind(R.id.main_parking_zone_txt)
    TextView zoneName;

    @Bind(R.id.main_parking_time_txt)
    TextView timeName;

    @Inject
    CarsManager carsManager;

    @Inject
    ZoneManager zoneManager;

    @Inject
    TimeManager timeManager;

    @State
    String currentlySelectedZone;

    @State
    LocalTime currentlySelectedTime;

    @State
    int calculatedHoursToPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        ParkomatApplication.get(this).inject(this);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setZone(zoneManager.lastSelectedZone());
        setTime(timeManager.initialDisplayedTime());
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCar(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_main_carmanager:
                Intent carManagerIntent = new Intent(this, CarManagerActivity.class);
                startActivity(carManagerIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setZone(@NonNull String zone) {
        zoneName.setText(zone);
        currentlySelectedZone = zone;
    }

    private void setTime(@NonNull LocalTime time) {
        currentlySelectedTime = time;

        // Normalize time to max hours
        long diffMinutes = ChronoUnit.MINUTES.between(LocalTime.now(), time);
        int hours = (int)Math.ceil(diffMinutes / 60.0);
        int maxHoursInZone = zoneManager.maxHoursForZone(currentlySelectedZone);
        calculatedHoursToPay = Math.min(maxHoursInZone, hours);

        LocalTime timeToDisplay = LocalTime.now().plus(calculatedHoursToPay, ChronoUnit.HOURS);

        SpannableStringBuilder string = new SpannableStringBuilder(timeFormatter.format(timeToDisplay));
        String hourDifferenceString = "(" + (calculatedHoursToPay == maxHoursInZone ? "max " : "") + calculatedHoursToPay + " ur)";
        string.append(hourDifferenceString);
        string.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), string.length() - hourDifferenceString.length(), string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        string.setSpan(new AbsoluteSizeSpan(14, true), string.length() - hourDifferenceString.length(), string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        timeName.setText(string);
    }

    private void showCar(int index) {
        carsManager.getCars().elementAt(index).single().subscribe(new Observer<Car>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Car car) {
                carName.setText(car.name);
                registrationPlate.setText(car.registrationPlate);
            }
        });
    }

    @OnClick(R.id.main_parking_time)
    public void onParkingTimeClick() {
        timeManager.pickTime(this, currentlySelectedTime).subscribe(new Action1<LocalTime>() {
            @Override
            public void call(LocalTime instant) {
                setTime(instant);
            }
        });
    }

    @OnClick(R.id.main_parking_zone)
    public void onParkingZoneClick() {
        zoneManager.pickZone(this, currentlySelectedZone).subscribe(new Action1<String>() {
            @Override
            public void call(String selectedZone) {
                setZone(selectedZone);
                setTime(currentlySelectedTime);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }
}
