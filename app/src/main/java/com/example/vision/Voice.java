package com.example.vision;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class Voice {

    //음성 인식용
    private Intent SttIntent;
    private SpeechRecognizer mRecognizer;
    private SpeechRecognizer baseRecognizer;
    private String resBaseSTT;

    //음성 출력용
    private TextToSpeech tts;
    private AppCompatActivity activity;

    public Voice(AppCompatActivity cThis, RecognitionListener listener){

        this.activity = cThis;

        //STT 설정
        SttIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.activity.getApplicationContext().getPackageName());
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");//한국어 사용
        mRecognizer=SpeechRecognizer.createSpeechRecognizer(this.activity);
        mRecognizer.setRecognitionListener(listener);
        baseRecognizer=SpeechRecognizer.createSpeechRecognizer(this.activity);
        baseRecognizer.setRecognitionListener(this.serviceVoiceListener);

        //TTS 설정
        this.tts=new TextToSpeech(this.activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!= TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

    }

    public void STT(){
        if(ContextCompat.checkSelfPermission(this.activity, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this.activity,new String[]{Manifest.permission.RECORD_AUDIO},1);
            //권한을 허용하지 않는 경우
        }else{
            //권한을 허용한 경우
            try {
                mRecognizer.startListening(this.SttIntent);
            }catch (SecurityException e){e.printStackTrace();}
        }
    }

    public String baseSTT(){
        if(ContextCompat.checkSelfPermission(this.activity, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this.activity,new String[]{Manifest.permission.RECORD_AUDIO},1);
            //권한을 허용하지 않는 경우
        }else{
            //권한을 허용한 경우
            try {
                baseRecognizer.startListening(this.SttIntent);
                return resBaseSTT;
            }catch (SecurityException e){e.printStackTrace();}
        }
        return "";
    }

    public void TTS(String OutMsg){
        if(OutMsg.length()<1)return;

        tts.setPitch(1.0f);//목소리 톤1.0
        tts.setSpeechRate(1.0f);//목소리 속도
        tts.speak(OutMsg,TextToSpeech.QUEUE_FLUSH,null,null);
        //어플이 종료할때는 완전히 제거
    }

    public void close(){
        if(this.tts!=null){
            this.tts.stop();
            this.tts.shutdown();
            this.tts=null;
        }
        if(this.mRecognizer!=null){
            this.mRecognizer.destroy();
            this.mRecognizer.cancel();
            this.mRecognizer=null;
        }
    }


    private RecognitionListener serviceVoiceListener= new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Voice.this.TTS("지금 말해주세요");
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
            Voice.this.TTS("음성 에러. 음성 재인식 시작");
            Voice.this.resBaseSTT = Voice.this.baseSTT();
        }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);

            Voice.this.resBaseSTT = mResult.get(0);
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }

    };

}

