package net.cloudhacking.pusher;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;

public class ResponseRecorderReceiver extends BroadcastReceiver {
    private static final String TAG = "ResponseRecorderReceiver";

    public static final String EXTRA_QUESTION_ID = "net.cloudhacking.pusher.extra.QUESTION_ID";
    public static final String EXTRA_RESPONSE_CODE = "net.cloudhacking.pusher.extra.RESPONSE_CODE";
    public static final String EXTRA_NOTIFICATION_ID = "net.cloudhacking.pusher.extra.NOTIFICATION_ID";

    public static final String INTENT_RECORD_RESPONSE = "net.cloudhacking.pusher.intent.RECORD_RESPONSE";

    public ResponseRecorderReceiver() {
    }

    private enum RecordingResult {
        SUCCESS, FAIL
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Recording response...");
        final String questionId = intent.getStringExtra(EXTRA_QUESTION_ID);
        final int code = intent.getIntExtra(EXTRA_RESPONSE_CODE, -1);
        final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        new AsyncTask<Void, Void, RecordingResult>() {
            @Override
            protected RecordingResult doInBackground(Void... params) {
                String msg = "";
                try {
                    PusherApi.recordResponse(questionId, code);
                    return RecordingResult.SUCCESS;
                } catch (IOException ex) {
                    Log.e(TAG, "Failed to record result: " + ex.getMessage());
                    return RecordingResult.FAIL;
                }
            }

            @Override
            protected void onPostExecute(RecordingResult result) {
                if (notificationId != -1) {
                    NotificationManager notificationManager = (NotificationManager)
                            context.getSystemService(Context.NOTIFICATION_SERVICE);
                    String message = "";
                    if (result == RecordingResult.SUCCESS) {
                        message = "Success";
                    } else if (result == RecordingResult.FAIL) {
                        message = "Failed";
                    }
                    NotificationCompat.Builder notificationBuilder =
                            new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentText(message);
                    notificationManager.notify(notificationId, notificationBuilder.build());
                }

            }
        }.execute();
        /*
        try {
            PusherApi.recordResponse(
                    intent.getExtras().getString(EXTRA_QUESTION_ID),
                    intent.getExtras().getInt(EXTRA_RESPONSE_CODE)
            );

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class), 0);
            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.remote_undo);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("Pusher")
                            .setContentText("Submitted");
            mBuilder.setContent(contentView);
            notificationManager.notify(intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1), mBuilder.build());
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to record response " + e.getMessage());
        }*/
    }
}
