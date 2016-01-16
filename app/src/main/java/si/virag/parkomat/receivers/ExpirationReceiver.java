package si.virag.parkomat.receivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import si.virag.parkomat.R;

public class ExpirationReceiver extends BroadcastReceiver {

    public static final String EXPIRATION_TIME_EXTRA = "ExpirationTime";
    public static final String CANCEL_NOTIFICATION_UPDATE = "CancelNotificationUpdate";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra(CANCEL_NOTIFICATION_UPDATE, false)) {
            cancelNotificationUpdates(context);
            return;
        }

        long expirationDate = intent.getLongExtra(EXPIRATION_TIME_EXTRA, 0);
        if (expirationDate == 0) return;
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(expirationDate), ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(ldt)) return;

        long minutesDiff = ChronoUnit.MINUTES.between(now, ldt);

        String ticker = String.format("Parkirnina preteče čez %d minut.", minutesDiff);
        String title = String.format("Parkirnina do %s.", DateTimeFormatter.ofPattern("HH:mm").format(ldt));
        String body = String.format("Parkirnina bo potekla čez %d minut.", minutesDiff);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setTicker(ticker);
        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        builder.setAutoCancel(true);
        builder.setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);
        builder.setSmallIcon(R.drawable.ic_notification);

        // Cancel notification updates if the notification is dismissed.
        Intent notificationCancelIntent = new Intent(context, ExpirationReceiver.class);
        notificationCancelIntent.putExtra(CANCEL_NOTIFICATION_UPDATE, true);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, notificationCancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(pi);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());

        // Refresh this notification every minute
        Intent notificationReceiverIntent = new Intent(context, ExpirationReceiver.class);
        notificationReceiverIntent.putExtra(ExpirationReceiver.EXPIRATION_TIME_EXTRA, expirationDate);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME, 60000, pendingIntent);
    }

    private void cancelNotificationUpdates(Context context) {
        Log.d("Parkomat", "Canceling notification updates.");
        Intent notificationReceiverIntent = new Intent(context, ExpirationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }
}
