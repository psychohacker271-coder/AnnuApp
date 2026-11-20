package com.annu.companion;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import android.speech.*;
import java.util.*;

public class WakeWordService extends Service {
    private static final String CHANNEL="annu_wake";
    private SpeechRecognizer recognizer;
    private Handler handler;
    private boolean stopping=false, triggered=false;
    private String language="hi-IN";

    @Override public void onCreate(){
        super.onCreate();
        handler=new Handler(Looper.getMainLooper());
        NotificationChannel channel=new NotificationChannel(CHANNEL,"ANNU wake phrase",NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Microphone is active while ANNU listens for the wake phrase");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification n=buildNotification("Listening for Hey ANNU. Tap Stop to turn off.");
        if(Build.VERSION.SDK_INT>=29)startForeground(43,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        else startForeground(43,n);
    }

    private Notification buildNotification(String message){
        Intent open=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("annu_wake",true);
        PendingIntent openPi=PendingIntent.getActivity(this,43,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent stop=new Intent(this,WakeWordService.class).setAction("STOP_WAKE");
        PendingIntent stopPi=PendingIntent.getService(this,44,stop,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("ANNU wake listening 💜").setContentText(message).setContentIntent(openPi)
            .setOngoing(true).addAction(android.R.drawable.ic_media_pause,"Stop listening",stopPi).build();
    }

    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null&&"STOP_WAKE".equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
        if(intent!=null)language=intent.getStringExtra("language")!=null?intent.getStringExtra("language"):"hi-IN";
        stopping=false;triggered=false;
        handler.post(this::beginListening);
        return START_STICKY;
    }

    private void beginListening(){
        if(stopping||triggered)return;
        if(!SpeechRecognizer.isRecognitionAvailable(this)){stopSelf();return;}
        if(recognizer!=null){try{recognizer.destroy();}catch(Exception ignored){}}
        recognizer=SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener(){
            @Override public void onReadyForSpeech(Bundle p){}
            @Override public void onBeginningOfSpeech(){}
            @Override public void onRmsChanged(float rms){}
            @Override public void onBufferReceived(byte[] b){}
            @Override public void onEndOfSpeech(){}
            @Override public void onError(int error){if(!stopping&&!triggered)handler.postDelayed(WakeWordService.this::beginListening,900);}
            @Override public void onResults(Bundle b){check(b);if(!stopping&&!triggered)handler.postDelayed(WakeWordService.this::beginListening,500);}
            @Override public void onPartialResults(Bundle b){check(b);}
            @Override public void onEvent(int event,Bundle p){}
        });
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,language);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);
        try{recognizer.startListening(i);}catch(Exception e){handler.postDelayed(this::beginListening,1200);}
    }

    private void check(Bundle result){
        if(result==null||triggered)return;
        ArrayList<String> matches=result.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(matches==null)return;
        for(String phrase:matches){
            String s=phrase.toLowerCase(Locale.ROOT).replaceAll("[^a-z\\u0900-\\u097f ]"," ").trim();
            if(s.contains("hey annu")||s.contains("hey anu")||s.contains("हे अनु")||s.contains("हे अन्नू")||s.equals("annu")||s.equals("अनु")){
                triggered=true;
                try{if(recognizer!=null)recognizer.cancel();}catch(Exception ignored){}
                NotificationManager nm=getSystemService(NotificationManager.class);
                // Use a fresh activity PendingIntent for the wake notification.
                Intent open=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("annu_wake",true);
                PendingIntent pi=PendingIntent.getActivity(this,45,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
                alert=new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_btn_speak_now)
                    .setContentTitle("ANNU heard you 💜").setContentText("Boss, main sun rahi hoon. Tap to talk.")
                    .setContentIntent(pi).setAutoCancel(true).build();
                nm.notify(45,alert);
                stopSelf();
                return;
            }
        }
    }

    @Override public void onDestroy(){
        stopping=true;
        if(handler!=null)handler.removeCallbacksAndMessages(null);
        if(recognizer!=null){try{recognizer.cancel();recognizer.destroy();}catch(Exception ignored){}recognizer=null;}
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent){return null;}
}
