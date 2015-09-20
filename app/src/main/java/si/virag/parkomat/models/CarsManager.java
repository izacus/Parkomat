package si.virag.parkomat.models;

import android.content.Context;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.Select;

import rx.Observable;
import rx.functions.Func0;


/**
 * Singleton cars manager
 */
public class CarsManager {

    private static final String LOG_TAG = "Parkomat.CarsManager";

    public CarsManager(Context context) {

    }

    public void addCar(@NonNull final String name, @NonNull final String licensePlate) {
        final Car car = new Car();
        car.name = name;
        car.registrationPlate = licensePlate;

        car.save();
    }

    public Observable<Car> getCars() {
        return Observable.defer(new Func0<Observable<Car>>() {
            @Override
            public Observable<Car> call() {
                return Observable.from(new Select().from(Car.class).queryList());
            }
        });
    }
}
