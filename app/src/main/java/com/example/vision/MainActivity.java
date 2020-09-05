package com.example.vision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.vision.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private RequestQueue queue;
    private LocationRequest locationRequest;
    private MyGps myGps;
    private Service service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // GPS가 꺼져있다면 On Dialog
        createLocationRequest();
        turn_on_GPS_dialog();

        //service = new Service();
        //Gps set listener
        myGps = new MyGps(MainActivity.this,locationListener);

        //start!
        myGps.startGps();

        service = new Service();


        // activity 객체들
        Button bLogin = (Button) findViewById(R.id.bSignIn);
        // 전송 버튼!! 리스너
        bLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String temp = "hello!";
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ocrtest);
                Log.e("t", "Send! ");
                // EditText에 들어온 ID와 PW로 로그인 시도!
                JsonRequest jsonRequest = new JsonRequest(temp.getBytes(), jsonArrayListener);
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(jsonRequest);

                OcrRequest ocrRequest = new OcrRequest(bitmap,ocrListener);
                queue.add(ocrRequest);
            }
        });

    } // oncreate end


//--Listener----------------------------------------------------------------------------------------------------------------------------------------


    // Server Volley 전송시 리스너 객체
    // Response received from the server 서버에서 내용을 받았을때 처리할 내용!
    final Response.Listener<JSONArray> jsonArrayListener = new Response.Listener<JSONArray>() {

        @Override
        public void onResponse(JSONArray response) {
            Log.e("h","Resonps!!");
            Log.e("h", "Response: " + response.toString());
        } //onResponse
    };

    final Response.Listener<JSONObject> ocrListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.e("h", "Response: " + response.toString());
        }
    };


    // GPS Location 정보 획득시 리스너 객체
    final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            Log.e("t", "startGps:" + location.getProvider());
            Log.e("t", "startGps:" + location.getLongitude());
            Log.e("t", "startGps:" + location.getLatitude());
            Log.e("t", "정확" + location.getAccuracy());
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e("t", "startGps: 상태변화");
        }
        @Override
        public void onProviderEnabled(String provider) {
            Log.e("t", "startGps: 사용가능");
            myGps.startGps();
        }
        @Override
        public void onProviderDisabled(String provider) {
            Log.e("t", "startGps: 사용불가");
        }
    };




//--Function----------------------------------------------------------------------------------------------------------------------------------------

    // GPS 꺼져있을 경우 alert dialog
    protected void createLocationRequest()
    {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //  GPS 켜는 dialog 뛰우기
    protected void turn_on_GPS_dialog()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(MainActivity.this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        //GPS get에 실패시 (GPS가 꺼져있는 경우)
        task.addOnFailureListener(MainActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException)
                {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try
                    {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                0x1);
                    }
                    catch (IntentSender.SendIntentException sendEx)
                    {
                        // Ignore the error.
                    }
                    finally {

                        myGps.startGps();
                        // GPS를 켜고나면 다시 재부팅하라는 안내가 있어야함
                        // GPS를 중간에
                    }
                }
            }
        });
    }//turn_on_gps end

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
    }
}
