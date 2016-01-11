package si.virag.parkomat.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import si.virag.parkomat.R;

public class ExpirationReceiver extends BroadcastReceiver {

    public static final String EXPIRATION_TIME_EXTRA = "ExpirationTime";

    @Override
    public void onReceive(Context context, Intent intent) {
        long expirationDate = intent.getLongExtra(EXPIRATION_TIME_EXTRA, 0);
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

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}
