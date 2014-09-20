package net.cloudhacking.pusher;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by wcauchois on 9/20/14.
 */
public class PusherApi {
    public static final String TAG = "PusherApi";
    public static final String API_ROOT = "http://pusher.cloudhacking.net/api";

    private static void sendPostRequest(String endpoint, NameValuePair[] pairs) throws IOException {
        HttpClient client = new DefaultHttpClient();
        String url = API_ROOT + endpoint;
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(Arrays.asList(pairs)));

        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() != 200) {
            Log.e(TAG, "Received non-200 response from endpoint");
            throw new IOException("Received non-200 response from endpoint");
        }
    }

    public static void recordResponse(String questionId, int code) throws IOException {
        sendPostRequest("/recordresponse", new BasicNameValuePair[] {
                new BasicNameValuePair("questionId", questionId),
                new BasicNameValuePair("code", "" + code)
        });
    }
}
