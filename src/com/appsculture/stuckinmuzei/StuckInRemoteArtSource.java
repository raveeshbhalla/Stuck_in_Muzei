package com.appsculture.stuckinmuzei;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Random;

/**
 * Created by Raveesh on 09/04/14.
 */
public class StuckInRemoteArtSource extends RemoteMuzeiArtSource {
    private static final String TAG = "StuckInRemoteArtSouce";
    private static final String source_name = "Stuck in Muzei";
    private static final long ROTATE_TIME_MILLIS = 1000 * 60 * 60;
    public static String ALBUM_URL = "https://picasaweb.google.com/data/feed/api/user/105237212888595777019/albumid/5999658280921050609/?alt=json&fields=entry(title,content,gphoto%3Aid)";
    public static String IMAGE_BASE = "https://plus.google.com/photos/+TreyRatcliff/albums/5999658280921050609/";

    private SharedPreferences mPrefs;


    /*
     * Actions
     */
    public static final String ACTION_RESET_UPDATE = "reset_update";
    public static final String ACTION_SUBSCRIBE = "com.google.android.apps.muzei.api.action.SUBSCRIBE";

    public StuckInRemoteArtSource() {
        super(source_name);
    }

    public StuckInRemoteArtSource(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Handling Intent");
        if (intent == null) {
            super.onHandleIntent(intent);
            return;
        }
        String action = intent.getAction();
        Log.d(TAG, action);
        if (action.equals(ACTION_RESET_UPDATE)) {
            unscheduleUpdate();
            setNextUpdate();
        } else {
            super.onHandleIntent(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Inside onCreate");
        Context context = this;
        mPrefs = context.getSharedPreferences("stuckinmuzei", context.MODE_PRIVATE);
    }

    @Override
    protected void onTryUpdate(int i) throws RetryException {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Log.d(TAG, "Trying to update");
        if (!mPrefs.getBoolean("wifionly", true) || mWifi.isConnected()) {
            getNextArtwork();
        } else {
            throw new RetryException();
        }
    }

    /**
     * 
     */
    private void getNextArtwork() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ALBUM_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    onFeedObtained(jsonObject.getJSONObject("feed").getJSONArray("entry"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }
        );

        queue.add(request);
    }

    /**
     * @param feed
     * @throws JSONException
     */
    private void onFeedObtained(JSONArray feed) throws JSONException {
        JSONObject obj = feed.getJSONObject(getRandomNumber(0, feed.length()));
        String title = obj.getJSONObject("title").getString("$t").split("\\.")[0];
        if (title.contains(" ")) {
            displayArtwork(obj);
        } else {
            onFeedObtained(feed);
        }
    }

    /**
     * @param art
     * @throws JSONException
     */
    private void displayArtwork(JSONObject art) throws JSONException {
        String imageURL = art.getJSONObject("content").getString("src");
        Log.d(TAG, imageURL);
        Artwork artwork = new Artwork.Builder()
                .imageUri(Uri.parse(imageURL))
                .title(art.getJSONObject("title").getString("$t").split("\\.")[0])
                .byline("Trey Ratcliff")
                .viewIntent(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(IMAGE_BASE + art.getJSONObject("gphoto$id").getString("$t"))))
                .build();
        SharedPreferences.Editor edit = mPrefs.edit();
    	edit.putLong("lastupdate", System.currentTimeMillis());
    	edit.commit();
        publishArtwork(artwork);
        setNextUpdate();
    }

    /**
     * 
     */
    private void setNextUpdate() {
        Long nextUpdate = mPrefs.getLong("lastupdate", System.currentTimeMillis()) + (ROTATE_TIME_MILLIS * mPrefs.getInt("update", 24));
        scheduleUpdate(nextUpdate);
    }

    /**
     * Generates a random number
     *
     * @param min Integer Minimum value for the random number
     * @param max Integer Maximum value for the random number
     * @return Integer Random number between min and max, inclusive of both
     * values
     */
    private static int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
