package si.virag.parkomat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observer;
import si.virag.parkomat.ParkomatApplication;
import si.virag.parkomat.R;
import si.virag.parkomat.models.Car;
import si.virag.parkomat.models.CarsManager;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.main_registration_plate)
    TextView registrationPlate;

    @Bind(R.id.main_car_name)
    TextView carName;

    @Inject
    CarsManager carsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        ParkomatApplication.get(this).inject(this);
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
}
