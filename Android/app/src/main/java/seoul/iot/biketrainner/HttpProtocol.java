package seoul.iot.biketrainner;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by maru5 on 2017-05-09.
 */

public class HttpProtocol {
    private static final String mServerAddress = "http://13.124.228.81:8080";
    private String mResponse;

    public void post(String url, JSONObject body) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, body.toString());
        Request request = new Request.Builder()
                .url(mServerAddress + url)
                .post(requestBody)
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "9f321e22-0c77-3e92-e315-493293174615")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    mResponse = response.body().string();
                    Log.d("KimDC", mResponse);
                    //응답 왔을때 처리
                }
            }
        });
    }

    public void get(String url) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(mServerAddress + url)
                .get()
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "4d9324a7-0db1-b170-3852-b15b7ea6910a")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    mResponse = response.toString();
                    Log.d("KimDC", mResponse);
                    mResponse = response.headers().toString();
                    Log.d("KimDC", mResponse);
                    mResponse = response.body().string();
                    Log.d("KimDC", mResponse);
                    //mResponse = response.body().string();
                    //응답 왔을때 처리
                }
            }
        });
    }

    /*public void apGet(String url) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                //.get()
                //.addHeader("cache-control", "no-cache")
                //.addHeader("postman-token", "4d9324a7-0db1-b170-3852-b15b7ea6910a")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    mResponse = response.toString();
                    Log.d("KimDC", mResponse);
                    mResponse = response.headers().toString();
                    Log.d("KimDC", mResponse);
                    mResponse = response.message();
                    Log.d("KimDC", mResponse);
                    mResponse = response.body().string();
                    Log.d("KimDC", mResponse);
                    //응답 왔을때 처리
                }
            }
        });
    }*/
}
