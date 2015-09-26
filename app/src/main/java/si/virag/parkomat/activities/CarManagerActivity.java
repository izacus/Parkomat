package si.virag.parkomat.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import si.virag.parkomat.ParkomatApplication;
import si.virag.parkomat.R;
import si.virag.parkomat.models.Car;
import si.virag.parkomat.modules.CarsManager;

public class CarManagerActivity extends AppCompatActivity implements CarListAdapter.OnCarClickedListener {

    @Bind(R.id.carmanager_container)
    protected View container;

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

        // Setup swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                deleteCar(((CarListAdapter.ViewHolder)viewHolder).getCar());
            }
        };

        ItemTouchHelper helper = new ItemTouchHelper(itemTouchCallback);
        helper.attachToRecyclerView(carList);
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
                                  final DialogViewAdapter views = new DialogViewAdapter(dialog.getCustomView());
                                  final String name = views.name.getText().toString();
                                  final String licensePlate = views.registrationPlate.getText().toString();
                                  addCar(name, licensePlate);
                                  updateCarList();
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
                                            final DialogViewAdapter views = new DialogViewAdapter(dialog.getCustomView());
                                            car.name = views.name.getText().toString();
                                            car.registrationPlate = views.registrationPlate.getText().toString();
                                            car.save();
                                            updateCarList();
                                        }
                                    })
                                    .show();

        final DialogViewAdapter views = new DialogViewAdapter(dialog.getCustomView());
        views.name.setText(car.name);
        views.registrationPlate.setText(car.registrationPlate);
    }

    /**
     * Adds a new car to the database
     */
    protected void addCar(@NonNull final String name, @NonNull final String licensePlate) {
        carsManager.addCar(name, licensePlate).subscribe(new Action1<Car>() {
            @Override
            public void call(Car car) {
                updateCarList();
            }
        });
    }

    protected void deleteCar(@NonNull final Car car) {
        carsManager.deleteCar(car).subscribe(new Observer<Void>() {
            @Override
            public void onCompleted() {
                updateCarList();

                Snackbar.make(container, car.name + " deleted.", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                carsManager.undeleteCar(car).subscribe(new Subscriber<Void>() {
                                    @Override
                                    public void onCompleted() {
                                        updateCarList();
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onNext(Void aVoid) {

                                    }
                                });
                            }
                        }).show();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Void aVoid) {

            }
        });
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
                           if (carList.getAdapter() != null) {
                               CarListAdapter adapter = (CarListAdapter) carList.getAdapter();
                               adapter.setCars(cars);
                           } else {
                               carList.setAdapter(new CarListAdapter(cars, CarManagerActivity.this));
                           }
                       }
                   });
    }

    /**
     * Helper to make dialog handling code less verbose
     */
    private class DialogViewAdapter {

        private final EditText name;
        private final EditText registrationPlate;

        public DialogViewAdapter(View views) {
            name = ((EditText)views.findViewById(R.id.dialog_caradd_carname));
            registrationPlate = (EditText)views.findViewById(R.id.dialog_caradd_carplate);
        }
    }
}
