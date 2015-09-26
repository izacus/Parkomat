package si.virag.parkomat.modules;

import android.content.Context;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.Locale;

import rx.Observable;
import rx.functions.Func0;

public class SmsHandler {

    private static final String PHONE_NUMBER = "+38641202010";
    private static final String LOG_TAG = "Parkomat.SmsHandler";

    @NonNull
    private final Context appContext;

    public SmsHandler(@NonNull Context applicationContext) {
        this.appContext = applicationContext;
    }

    public Observable<Void> payForParking(@NonNull final String zone, @NonNull final String carPlate, @NonNull final int hours) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                SmsManager manager = SmsManager.getDefault();
                String smsContent = String.format("%s %s %d", zone.toUpperCase(Locale.GERMAN), carPlate.toUpperCase(Locale.GERMAN), hours);
                Log.d(LOG_TAG, smsContent);
                manager.sendTextMessage(PHONE_NUMBER, null, smsContent, null, null);
                return Observable.empty();
            }
        });
    }
}
