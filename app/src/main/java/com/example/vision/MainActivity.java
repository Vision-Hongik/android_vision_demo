package com.example.vision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    private LocationRequest locationRequest;
    private MyGps myGps;
    private Service service;
    private Voice voice;
    private Voice serviceVoice;

    private Button apiButton;
    private Button sttButton;
    private Button ttsButton;
    private Button initButton;
    private EditText srcEdit;
    private EditText dstEdit;
    private EditText sttText;
    private EditText ttsText;
    private EditText stt_statusText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setContenteView후에 셋팅해야함!! 그전에는 널값임
        sttButton = (Button)findViewById(R.id.STT);
        ttsButton = (Button)findViewById(R.id.TTS);
        apiButton = (Button) findViewById(R.id.api);
        initButton = (Button)findViewById(R.id.init_button);

        sttText = (EditText)findViewById(R.id.sttEdit);
        ttsText = (EditText)findViewById(R.id.ttsEdit);
        stt_statusText = (EditText)findViewById(R.id.stt_statusEdit);
        srcEdit = (EditText)findViewById(R.id.source_edit);
        dstEdit = (EditText)findViewById(R.id.dest_edit);


        // GPS가 꺼져있다면 On Dialog
        createLocationRequest();
        turn_on_GPS_dialog();

        // Gps
        myGps = new MyGps(MainActivity.this,locationListener);
        myGps.startGps();

        // Voice
        voice = new Voice(this,voiceListener);
        serviceVoice = new Voice(this,null);
        // API Server
        requestQueue = Volley.newRequestQueue(MainActivity.this);  // 전송 큐

        // Service
        service = new Service();


        // 전송 버튼!! 리스너
        apiButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String temp = "sangsu"; // Map 전송쪽에 보낼 메세지
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ocrtest); //OCR에 보낼 비트맵
                Log.e("t", "Send! ");

                // Map api에 전송
                MapRequest jsonRequest = new MapRequest(temp, jsonArrayListener);
                requestQueue.add(jsonRequest);

                // OCR api에 전송
                OcrRequest ocrRequest = new OcrRequest(bitmap,ocrListener);
                requestQueue.add(ocrRequest);
            }
        });


        sttButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("v", "service 음성인식 시작!");
                voice.setRecognitionListener(voiceListener);
                voice.STT(); // 음성 인식 시작!! 리스너에 따라 행동함.

            }
        });


        ttsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("v", "service 음성으로 변환!!");
                voice.TTS(ttsText.getText().toString()); // 음성 인식 시작!! 리스너에 따라 행동함.
            }
        });


        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp;
                Log.e("v", "음성으로 초기화 값 입력 시작!");

                voice.TTS("어디 역에서 출발 하시나요?");
                voice.setRecognitionListener(sourceStationVoiceListener);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                voice.STT();

            }
        });

    } // oncreate end


//--Listener----------------------------------------------------------------------------------------------------------------------------------------


    // Server Volley 전송시 리스너 객체
    // Response received from the server 서버에서 내용을 받았을때 처리할 내용!
    final Response.Listener<JSONArray> jsonArrayListener = new Response.Listener<JSONArray>() {

        @Override
        public void onResponse(JSONArray response) {
            ArrayList<Sector> tmpMapdataList = new ArrayList<Sector>();

            for(int i = 0; i< response.length(); i++){
                try {
                    tmpMapdataList.add(new Sector(response.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            service.setSectorArrayList(tmpMapdataList);
            Log.e("h", "length " + service.getMapdataArrayList().size());

            for(int i=0; i < service.getMapdataArrayList().size(); i++){
                Log.e("h", "onResponse: " + service.getMapdataArrayList().get(i).getName());
            }

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

            service.setLatitude(location.getLatitude());
            service.setLongitude(location.getLongitude());

            Log.e("t", "service 위도: " + service.getLatitude());
            Log.e("t", "service 경도: " + service.getLongitude()+ "\n..\n");

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


    private RecognitionListener voiceListener= new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            stt_statusText.setText("onReadyForSpeech..........." + "\r\n" + stt_statusText.getText());
        }

        @Override
        public void onBeginningOfSpeech() {
            stt_statusText.setText("지금부터 말을 해주세요..........." + "\r\n" + stt_statusText.getText());
        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            stt_statusText.setText("onBufferReceived..........." + "\r\n" + stt_statusText.getText());
        }

        @Override
        public void onEndOfSpeech() {
            stt_statusText.setText("onEndOfSpeech..........." + "\r\n" + stt_statusText.getText());
        }

        @Override
        public void onError(int i) {
            stt_statusText.setText("천천히 다시 말해 주세요..........." + "\r\n" + stt_statusText.getText());
        }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            stt_statusText.setText("onResult..." + "\r\n" + sttText.getText());
            sttText.setText(rs[0] + "\r\n" + sttText.getText());
            FuncVoiceOrderCheck(rs[0]);
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            stt_statusText.setText("onPartialResults..........."+"\r\n"+stt_statusText.getText());
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
            stt_statusText.setText("onEvent..........."+"\r\n"+stt_statusText.getText());
        }
    };


    private RecognitionListener sourceStationVoiceListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int i) {
            voice.TTS("음성 에러 5초후 다시 말씀해주세요!");
            String message;

            switch (i) {

                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;

                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;

                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션없음";
                    break;

                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;

                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;

                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을수 없음";;
                    break;

                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "바쁘대";
                    break;

                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버이상";;
                    break;

                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;

                default:
                    message = "알수없음";
                    break;
            }
            Log.e("GoogleActivity", "SPEECH ERROR : " + message);
        }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);

            service.setSource_Station(mResult.get(0));
            Log.e("v", "Start Station onResults: " + service.getSource_Station() );

            srcEdit.setText(mResult.get(0));

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            voice.TTS("어디 역으로 가시나요?");
            voice.setRecognitionListener(destStationVoiceListener);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            voice.STT();
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }

    };


    private RecognitionListener destStationVoiceListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int i) {
            voice.TTS("음성 에러 5초후 다시 말씀해주세요!");
            String message;

            switch (i) {

                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;

                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;

                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션없음";
                    break;

                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;

                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;

                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을수 없음";;
                    break;

                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "바쁘대";
                    break;

                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버이상";;
                    break;

                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;

                default:
                    message = "알수없음";
                    break;
            }
            Log.e("GoogleActivity", "SPEECH ERROR : " + message);
        }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);

            service.setDest_Station(mResult.get(0));
            Log.e("v", "End Station onResults: " + service.getDest_Station());

            dstEdit.setText(mResult.get(0));

            try {
                Thread.sleep(2000);
                voice.TTS("출발지는 " + service.getSource_Station() + "도착지는 " + service.getDest_Station()+ "이 맞습니까? 네 아니요로 대답해주세요.");
                voice.setRecognitionListener(confirmVoiceListener);
                Thread.sleep(6000);
                voice.STT();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }

    };


    private RecognitionListener confirmVoiceListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int i) {
            voice.TTS("음성 에러 5초후 다시 말씀해주세요!");
            String message;

            switch (i) {

                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;

                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;

                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션없음";
                    break;

                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;

                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;

                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을수 없음";;
                    break;

                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "바쁘대";
                    break;

                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버이상";;
                    break;

                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;

                default:
                    message = "알수없음";
                    break;
            }
            Log.e("GoogleActivity", "SPEECH ERROR : " + message);
        }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);

            String answer = mResult.get(0);
            Log.e("v", "answer: " + answer);

            try {
                Thread.sleep(2000);
                if(answer.charAt(0) != '네' && answer.charAt(0) != '내'){
                    voice.TTS("어디 역에서 출발 하시나요?");
                    voice.setRecognitionListener(sourceStationVoiceListener);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    voice.STT();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }

    };



//--Function----------------------------------------------------------------------------------------------------------------------------------------


    private void FuncVoiceOrderCheck(String VoiceMsg){
        if(VoiceMsg.length()<1)return;

        VoiceMsg=VoiceMsg.replace(" ","");//공백제거

        if(VoiceMsg.indexOf("카카오톡")>-1 || VoiceMsg.indexOf("카톡")>-1){
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.kakao.talk");
            startActivity(launchIntent);
            onDestroy();
        }//카카오톡 어플로 이동
        if(VoiceMsg.indexOf("전동꺼")>-1 || VoiceMsg.indexOf("불꺼")>-1){
            // FuncVoiceOut("전등을 끕니다");//전등을 끕니다 라는 음성 출력
            voice.TTS("전등을 끕니다.");
        }
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        voice.close();
    }
}
