package si.virag.parkomat.modules;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import si.virag.parkomat.R;
import si.virag.parkomat.receivers.ExpirationReceiver;
import si.virag.parkomat.receivers.SmsReceiver;

public class SmsHandler {
    public static final String PHONE_NUMBER = "+38641202010";
    private static final String LOG_TAG = "Parkomat.SmsHandler";

    @NonNull
    private final Context appContext;

    public SmsHandler(@NonNull Context applicationContext) {
        this.appContext = applicationContext;
    }

    public Observable<Void> payForParking(@NonNull final Activity owner, @NonNull final String zone, @NonNull final String carPlate, @NonNull final int hours) {
        return showSmsSendDialog(owner, zone, carPlate, hours)
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean send) {
                        return send;
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean allow) {
                        return RxPermissions.getInstance(owner).request(Manifest.permission.SEND_SMS);
                    }
                })
                .map(new Func1<Boolean, Void>() {
                    @Override
                    public Void call(Boolean permissionGranted) {
                        if (!permissionGranted) throw new SecurityException("Permission was not granted.");
                        SmsManager manager = SmsManager.getDefault();
                        String smsContent = String.format("%s %s %d", zone.toUpperCase(Locale.GERMAN), carPlate.toUpperCase(Locale.GERMAN), hours);
                        Log.d(LOG_TAG, smsContent);
                        manager.sendTextMessage(PHONE_NUMBER, null, smsContent, null, null);
                        return null;
                    }
                });
    }

    private Observable<Boolean> showSmsSendDialog(@NonNull final Activity owner, @NonNull final String zone, @NonNull final String carPlate, @NonNull final int hours) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                MaterialDialog dialog = new MaterialDialog.Builder(owner)
                        .customView(R.layout.dialog_confirm_sms, false)
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                subscriber.onCompleted();
                            }
                        })
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                subscriber.onNext(true);
                            }
                        })
                        .title(R.string.dialog_sendsms_title)
                        .positiveText(R.string.dialog_sendsms_send)
                        .negativeText(R.string.dialog_cancel)
                        .show();
                View views = dialog.getCustomView();
                ((TextView)views.findViewById(R.id.dialog_sms_zone)).setText(zone);
                ((TextView)views.findViewById(R.id.dialog_sms_hours)).setText(String.valueOf(hours));
                ((TextView)views.findViewById(R.id.dialog_sms_plate)).setText(carPlate);
            }
        });
    }

    public Observable<Void> checkForFunds(@NonNull final Activity owner) {
        return showFundsSendDialog(owner)
                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean aBoolean) {
                        return RxPermissions.getInstance(owner).request(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS);
                    }
                })
                .map(new Func1<Boolean, Void>() {
                    @Override
                    public Void call(Boolean permissionGranted) {
                        if (!permissionGranted) throw new SecurityException("Permission was not granted.");
                        SmsManager manager = SmsManager.getDefault();
                        String smsContent = "STANJE";
                        Log.d(LOG_TAG, smsContent);
                        manager.sendTextMessage(PHONE_NUMBER, null, smsContent, null, null);
                        return null;
                    }
                });
    }

    private Observable<Boolean> showFundsSendDialog(@NonNull final Activity owner) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                new MaterialDialog.Builder(owner)
                        .title(R.string.dialog_sendsms_title)
                        .content(R.string.dialog_check_funds)
                        .positiveText(R.string.dialog_sendsms_send)
                        .negativeText(R.string.dialog_cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                subscriber.onNext(true);
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

    public void handleReceivedMessage(@NonNull final Activity owner, @NonNull final SmsReceiver.ReceivedSmsMessage message) {
        if (message.body.contains("Stanje na SMS Parking")) {
            showFundsDialog(owner, message.body);
        } else if (message.body.contains("Placilo parkirnine za") && message.body.contains("uspesno.")) {
            registerParkingExpirationNotification(message.body);
        }
    }

    private void registerParkingExpirationNotification(@NonNull final String smsBody) {
        LocalDateTime expirationDate = getDateTimeFromParkingSMS(smsBody);
        if (expirationDate == null) return;
        LocalDateTime notificationTime = expirationDate.minus(10, ChronoUnit.MINUTES);
        LocalDateTime now = LocalDateTime.now();
        if (notificationTime.isBefore(now)) return;

        long notificationEpoch = notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long expirationEpoch = expirationDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        AlarmManager am = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        Intent notificationReceiverIntent = new Intent(appContext, ExpirationReceiver.class);
        notificationReceiverIntent.putExtra(ExpirationReceiver.EXPIRATION_TIME_EXTRA, expirationEpoch);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, notificationReceiverIntent, PendingIntent.FLAG_ONE_SHOT);

        // Sigh.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            am.set(AlarmManager.RTC_WAKEUP, notificationEpoch, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            am.setExact(AlarmManager.RTC_WAKEUP, notificationEpoch, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationEpoch, pendingIntent);
        }
    }

    private void showFundsDialog(Activity owner, String body) {
        Pattern pattern = Pattern.compile("Stanje na SMS Parking racunu je ([0-9,.]+) EUR.");
        Matcher matcher = pattern.matcher(body);
        if (!matcher.matches()) return;

        float value = Float.parseFloat(matcher.group(1).replace(',', '.'));
        new MaterialDialog.Builder(owner)
                .title(R.string.dialog_funds_title)
                .content(owner.getString(R.string.dialog_funds_content, value))
                .positiveText(R.string.dialog_ok)
                .show();
    }


    @Nullable
    @VisibleForTesting LocalDateTime getDateTimeFromParkingSMS(String smsBody) {
        Pattern p = Pattern.compile("Veljavnost: ([0-9:]+) \\(([0-9\\.]+)\\)");
        Matcher m = p.matcher(smsBody);
        if (!m.find()) return null;

        String timeString = m.group(1);
        String dateString = m.group(2);

        return LocalDateTime.parse(timeString + " " + dateString, DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy"));
    }
}
