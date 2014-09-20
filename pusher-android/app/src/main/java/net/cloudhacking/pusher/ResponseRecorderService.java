package net.cloudhacking.pusher;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


public class ResponseRecorderService extends IntentService {
    private static final String TAG = "ResponseRecorderService";
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_RECORD_RESPONSE = "net.cloudhacking.pusher.action.RECORD_RESPONSE";

    public static final String EXTRA_QUESTION_ID = "net.cloudhacking.pusher.extra.QUESTION_ID";
    public static final String EXTRA_RESPONSE_CODE = "net.cloudhacking.pusher.extra.RESPONSE_CODE";

    public ResponseRecorderService() {
        super("ResponseRecorderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Recording response...");
        /*if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }*/
    }
}
