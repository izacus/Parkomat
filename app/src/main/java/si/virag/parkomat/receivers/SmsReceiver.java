package si.virag.parkomat.receivers;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;
import de.greenrobot.event.EventBus;

public class SmsReceiver extends BroadcastReceiver {

    public static class ReceivedSmsMessage {
        @NonNull
        public final String body;

        public ReceivedSmsMessage(@NonNull String body) {
            this.body = body;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;
            Object[] pdus = (Object[]) bundle.get("pdus");

            for (Object pdu : pdus) {

                SmsMessage message = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? SmsMessage.createFromPdu((byte[]) pdu, "3gpp")
                                                                                    : SmsMessage.createFromPdu((byte[]) pdu);
                String sourceNumber = message.getOriginatingAddress();
                if (!sourceNumber.contains("41202010")) continue;
                String body = message.getMessageBody();
                if (body == null) continue;
                EventBus.getDefault().post(new ReceivedSmsMessage(body));
                abortBroadcast();
            }
        }
    }
}
