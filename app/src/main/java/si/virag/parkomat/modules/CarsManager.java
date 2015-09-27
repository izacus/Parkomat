package si.virag.parkomat.modules;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.Locale;

import rx.Observable;
import rx.Observer;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import si.virag.parkomat.models.Car;
import si.virag.parkomat.models.Car$Table;


/**
 * Singleton cars manager
 */
public class CarsManager {

    private static final String LOG_TAG = "Parkomat.CarsManager";
    private final Context context;

    public CarsManager(Context context) {
        this.context = context;
    }

    public Observable<Car> addCar(@NonNull final String name, @NonNull final String licensePlate) {
        return Observable.defer(new Func0<Observable<Car>>() {
            @Override
            public Observable<Car> call() {
                final Car car = new Car();
                car.name = name;
                car.registrationPlate = prettyfyPlateNumber(licensePlate);
                car.save();
                return Observable.just(car);
            }
        });
    }

    public Observable<Car> getCars() {
        return Observable.defer(new Func0<Observable<Car>>() {
            @Override
            public Observable<Car> call() {
                return Observable.from(new Select().from(Car.class).where(Condition.column(Car$Table.DELETED).eq(false)).queryList());
            }
        });
    }

    public Observable<Void> deleteCar(final Car car) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                car.deleted = true;
                car.save();
                return Observable.empty();
            }
        });
    }

    public Observable<Void> undeleteCar(final Car car) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                car.deleted = false;
                car.save();
                return Observable.empty();
            }
        });
    }

    public String prettyfyPlateNumber(@NonNull String plate) {
        plate = plate.replaceAll("[^A-Za-z0-9]", "");
        if (plate.length() != 7) return plate;
        return String.format("%s %s-%s", plate.substring(0, 2), plate.substring(2, 4), plate.substring(4)).toUpperCase(Locale.GERMAN);
    }

    public void pruneCarsAsync() {
        Observable.defer(new Func0<Observable<Car>>() {
            @Override
            public Observable<Car> call() {
                return Observable.from(new Select().from(Car.class).where(Condition.column(Car$Table.DELETED).eq(true)).queryList());
            }
        })
        .flatMap(new Func1<Car, Observable<?>>() {
            @Override
            public Observable<?> call(Car car) {
                car.delete();
                return Observable.empty();
            }
        })
        .subscribeOn(Schedulers.io())
        .subscribe(new Observer<Object>() {
            @Override
            public void onCompleted() {
                Log.d(LOG_TAG, "Car database pruned.");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(LOG_TAG, "Failed to prune car database.");
            }

            @Override
            public void onNext(Object o) {

            }
        });
    }

    public boolean hasCars() {
        return new Select().from(Car.class).where(Condition.column(Car$Table.DELETED).eq(false)).count() > 0;
    }
}
