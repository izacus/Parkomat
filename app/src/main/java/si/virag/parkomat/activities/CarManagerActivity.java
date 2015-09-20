package si.virag.parkomat.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import si.virag.parkomat.ParkomatApplication;
import si.virag.parkomat.R;
import si.virag.parkomat.models.Car;
import si.virag.parkomat.models.CarsManager;

public class CarManagerActivity extends AppCompatActivity implements CarListAdapter.OnCarClickedListener {

    @Bind(R.id.carmanager_list)
    protected RecyclerView carList;

    @Inject
    protected CarsManager carsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_manager);

        ButterKnife.bind(this);
        ParkomatApplication.get(this).inject(this);

        // Setup list
        carList.setHasFixedSize(true);
        carList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCarList();
    }

    @OnClick(R.id.carmanager_add)
    protected void onAddClick(FloatingActionButton button) {
        new MaterialDialog.Builder(this)
                          .title("Add car")
                          .customView(R.layout.dialog_car_add, false)
                          .positiveText("Add")
                          .negativeText("Cancel")
                          .callback(new MaterialDialog.ButtonCallback() {
                              @Override
                              public void onPositive(MaterialDialog dialog) {
                                  super.onPositive(dialog);
                                  final View views = dialog.getCustomView();
                                  final String name = ((EditText)views.findViewById(R.id.dialog_caradd_carname)).getText().toString();
                                  final String licensePlate = ((EditText)views.findViewById(R.id.dialog_caradd_carplate)).getText().toString();
                                  addCar(name, licensePlate);
                              }
                          })
                          .show();
    }

    @Override
    public void onCarClicked(final Car car, final int position) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                                    .title("Edit car")
                                    .customView(R.layout.dialog_car_add, false)
                                    .positiveText("Save")
                                    .negativeText("Cancel")
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            super.onPositive(dialog);
                                            final View views = dialog.getCustomView();
                                            car.name = ((EditText)views.findViewById(R.id.dialog_caradd_carname)).getText().toString();
                                            car.registrationPlate = ((EditText)views.findViewById(R.id.dialog_caradd_carplate)).getText().toString();
                                            car.save();
                                            updateCarList();
                                        }
                                    })
                                    .show();

        final View views = dialog.getCustomView();
        ((EditText)views.findViewById(R.id.dialog_caradd_carname)).setText(car.name);
        ((EditText)views.findViewById(R.id.dialog_caradd_carplate)).setText(car.registrationPlate);
    }

    /**
     * Adds a new car to the database
     */
    protected void addCar(@NonNull final String name, @NonNull final String licensePlate) {
        carsManager.addCar(name, licensePlate);
    }

    protected void updateCarList() {
        carsManager.getCars()
                   .toList()
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(new Observer<List<Car>>() {
                       @Override
                       public void onCompleted() {

                       }

                       @Override
                       public void onError(Throwable e) {

                       }

                       @Override
                       public void onNext(List<Car> cars) {
                            carList.setAdapter(new CarListAdapter(cars, CarManagerActivity.this));
                       }
                   });
    }

}
