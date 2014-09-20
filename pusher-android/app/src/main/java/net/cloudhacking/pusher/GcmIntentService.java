package net.cloudhacking.pusher;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {

    static final String TAG = "GcmIntentService";

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.e(TAG, "Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.d(TAG, "Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                Log.i(TAG, "Received: " + extras.toString());
                sendNotification(extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Bundle dataFromServer) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        /*String msg = dataFromServer.getString("question");
        Bundle responseOptionDict = dataFromServer.getBundle("options");*/
        String msg = "hey";

        QuestionNotificationModel model = QuestionNotificationModel.fromJson(
                dataFromServer.getString("json_data"));

        //Log.i(TAG, "Got question model: " + model);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Pusher")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(model.getQuestion()))
                        .setContentText(model.getQuestion())
                        .setVibrate(new long[] { 0, 500 });

        for (QuestionNotificationModel.ResponseOption option : model.getOptions()) {
            Intent recorderIntent = new Intent(GcmIntentService.this,
                    ResponseRecorderService.class);
            recorderIntent.putExtra(ResponseRecorderService.EXTRA_RESPONSE_CODE,
                    option.getCode());
            recorderIntent.putExtra(ResponseRecorderService.EXTRA_QUESTION_ID,
                    model.getQuestionId());
            PendingIntent recorderPendingIntent = PendingIntent.getService(
                    GcmIntentService.this, 0, recorderIntent, 0);
            mBuilder.addAction(0, option.getAnswer(), recorderPendingIntent);
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}

