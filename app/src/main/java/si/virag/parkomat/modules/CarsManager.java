package si.virag.parkomat.modules;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import si.virag.parkomat.R;
import si.virag.parkomat.models.Car;
import si.virag.parkomat.models.Car$Table;


/**
 * Singleton cars manager
 */
public class CarsManager {

    private static final String LOG_TAG = "Parkomat.CarsManager";

    private static final String PREF_LAST_CHOSEN_CAR = "Last.Car";

    private final Context context;

    @NonNull
    private final SharedPreferences preferences;

    public CarsManager(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
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

    public Observable<Integer> pickCar(final Activity owner) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                String[] cars = getCars()
                                    .map(new Func1<Car, String>() {
                                        @Override
                                        public String call(Car car) {
                                            return car.name + " (" + car.registrationPlate + ")";
                                        }
                                    })
                                    .toList()
                                    .map(new Func1<List<String>, String[]>() {
                                        @Override
                                        public String[] call(List<String> strings) {
                                            return strings.toArray(new String[strings.size()]);
                                        }
                                    })
                                    .toBlocking()
                                    .single();

                new MaterialDialog.Builder(owner)
                                   .title("Izberite avto")
                                   .titleColorRes(R.color.colorAccent)
                                   .items(cars)
                                   .itemsCallback(new MaterialDialog.ListCallback() {
                                       @Override
                                       public void onSelection(MaterialDialog materialDialog, View view, int position, CharSequence name) {
                                           preferences.edit().putInt(PREF_LAST_CHOSEN_CAR, position).apply();
                                           subscriber.onNext(position);
                                       }
                                   })
                                   .dismissListener(new DialogInterface.OnDismissListener() {
                                       @Override
                                       public void onDismiss(DialogInterface dialog) {
                                           subscriber.onCompleted();
                                       }
                                   })
                                   .show();
            }
        });
    }

    public boolean hasCars() {
        return new Select().from(Car.class).where(Condition.column(Car$Table.DELETED).eq(false)).count() > 0;
    }

    public int lastSelectedCarIndex() {
        return preferences.getInt(PREF_LAST_CHOSEN_CAR, 0);
    }
}
