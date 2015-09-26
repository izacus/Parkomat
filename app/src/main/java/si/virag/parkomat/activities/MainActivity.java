package si.virag.parkomat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import rx.Observer;
import si.virag.parkomat.ParkomatApplication;
import si.virag.parkomat.R;
import si.virag.parkomat.models.Car;
import si.virag.parkomat.models.CarsManager;
import si.virag.parkomat.models.ZoneManager;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.main_registration_plate)
    TextView registrationPlate;

    @Bind(R.id.main_car_name)
    TextView carName;

    @Bind(R.id.main_parking_zone)
    TextView zoneName;

    @Inject
    CarsManager carsManager;

    @Inject
    ZoneManager zoneManager;

    @State
    String currentlySelectedZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        ParkomatApplication.get(this).inject(this);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setZone(zoneManager.lastSelectedZone());
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }
}
