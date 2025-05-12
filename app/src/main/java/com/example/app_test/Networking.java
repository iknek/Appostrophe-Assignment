package com.example.app_test;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import org.chromium.net.CronetEngine;
import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Networking {

    private static final ByteArrayOutputStream responseData = new ByteArrayOutputStream();
    private static boolean isinit = true;
    private static List<String> stickerImageUrls = new ArrayList<>();

    public interface UrlsCallback {
        void onUrlsReady(List<String> urls);
    }

    public void setUpNet(Context context, UrlsCallback callback){
        CronetEngine.Builder myBuilder = new CronetEngine.Builder(context);
        CronetEngine cronetEngine = myBuilder.build();
        Executor executor = Executors.newSingleThreadExecutor();

        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(
                "https://appostropheanalytics.herokuapp.com/scrl/test/overlays",
                new MyUrlRequestCallback(callback),
                executor
        );

        UrlRequest request = requestBuilder.build();
        request.start();
    }


    static class MyUrlRequestCallback extends UrlRequest.Callback {
        private static final String TAG = "MyUrlRequestCallback";
        private final UrlsCallback callback;
        MyUrlRequestCallback(UrlsCallback callback) {
            this.callback = callback;
        }
        @Override
        public void onRedirectReceived(UrlRequest request, UrlResponseInfo info, String newLocationUrl) {
            request.cancel();
            Log.i(TAG,"redirect recieved");
        }

        @Override
        public void onResponseStarted(UrlRequest request, UrlResponseInfo info) {
            request.read(ByteBuffer.allocateDirect(102400));
        }

        @Override
        public void onReadCompleted(UrlRequest request, UrlResponseInfo info, ByteBuffer byteBuffer) {
            Log.i(TAG, "onReadCompleted method called.");
            byteBuffer.flip();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            responseData.write(bytes, 0, bytes.length);
            byteBuffer.clear();
            request.read(byteBuffer);
        }

        @Override
        public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
            try {
                getUrls();
                callback.onUrlsReady(stickerImageUrls);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailed(UrlRequest request, UrlResponseInfo info, CronetException error) {

        }
    }

    private static void getUrls() throws UnsupportedEncodingException, JSONException {
        String jsonString = responseData.toString("UTF-8");
        JSONArray categories = new JSONArray(jsonString);

        for (int i = 0; i < categories.length(); i++) {
            JSONObject category = categories.getJSONObject(i);
            if (category.has("items")) {
                JSONArray items = category.getJSONArray("items");
                for (int j = 0; j < items.length(); j++) {
                    JSONObject item = items.getJSONObject(j);
                    if (item.has("source_url")) {
                        Log.i(TAG, "added: " + item.getString("source_url"));
                        stickerImageUrls.add(item.getString("source_url"));
                        if(isinit && i==7){
                            return;
                        }
                    }
                }
            }
        }
        isinit = false;
    }

    public static List<String> getStickerImageUrls() throws UnsupportedEncodingException, JSONException {
        getUrls();
        return stickerImageUrls;

    }

}
