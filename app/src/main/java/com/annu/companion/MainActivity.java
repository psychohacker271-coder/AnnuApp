package com.annu.companion;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import androidx.webkit.WebViewAssetLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int OVERLAY_REQUEST=701, SPEECH_REQUEST=703, PERMISSION_REQUEST=704, VRM_PICK=705, AUDIO_PICK=706, CALL_PERMISSION_REQUEST=707, WAKE_PERMISSION_REQUEST=708, WAKE_NOTIFICATION_REQUEST=709;
    private TextView status, assetStatus;
    private android.content.SharedPreferences appPrefs;
    private TextToSpeech tts;
    private WebView character;
    private WebViewAssetLoader assetLoader;
    private MediaPlayer importedPlayer;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        appPrefs=getSharedPreferences("annu",MODE_PRIVATE);
        getWindow().setStatusBarColor(Color.rgb(35,22,57));getWindow().setNavigationBarColor(Color.rgb(35,22,57));
        assetLoader=AnnuWebAssets.create(this);
        copyBundledModelIfMissing();
        tts=new TextToSpeech(this,r->{if(r==TextToSpeech.SUCCESS){tts.setLanguage(appPrefs.getString("speech_language","hi-IN").equals("hi-IN")?new Locale("hi","IN"):new Locale("en","IN"));tts.setSpeechRate(0.94f);tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){@Override public void onStart(String id){runOnUiThread(()->setSpeechState(true));}@Override public void onDone(String id){runOnUiThread(()->setSpeechState(false));}@Override public void onError(String id){runOnUiThread(()->setSpeechState(false));}});}});
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(10,8,10,8);root.setBackgroundColor(Color.rgb(35,22,57));
        character=new WebView(this);setupWebView(character);character.setBackgroundColor(Color.TRANSPARENT);
        root.addView(character,new LinearLayout.LayoutParams(-1,0,1));
        TextView title=new TextView(this);title.setText("ANNU  💜");title.setTextColor(Color.WHITE);title.setTextSize(25);title.setGravity(Gravity.CENTER);title.setPadding(8,4,8,4);root.addView(title);
        status=new TextView(this);status.setText("Ji Boss! Main yahan hoon.");status.setTextColor(Color.WHITE);status.setTextSize(15);status.setGravity(Gravity.CENTER);status.setPadding(8,4,8,6);root.addView(status);
        assetStatus=new TextView(this);assetStatus.setTextColor(Color.LTGRAY);assetStatus.setTextSize(12);assetStatus.setGravity(Gravity.CENTER);root.addView(assetStatus);refreshAssetStatus();
        EditText memoryInput=new EditText(this);memoryInput.setSingleLine(false);memoryInput.setMinLines(1);memoryInput.setMaxLines(3);memoryInput.setHint("ANNU ke liye local note likhein…");memoryInput.setTextColor(Color.WHITE);memoryInput.setHintTextColor(Color.LTGRAY);memoryInput.setText(appPrefs.getString("user_note",""));root.addView(memoryInput);
        Button saveMemory=new Button(this);saveMemory.setText("SAVE PRIVATE NOTE ON THIS PHONE");saveMemory.setOnClickListener(v->{String note=memoryInput.getText().toString().trim();appPrefs.edit().putString("user_note",note).apply();say(note.isEmpty()?"Boss, note clear kar diya.":"Ji Boss, note isi phone par save kar diya.");});root.addView(saveMemory);
        Button start=new Button(this);start.setText("FLOAT ANNU ON SCREEN");start.setOnClickListener(v->startAnnu());root.addView(start);
        Button speak=new Button(this);speak.setText("🎙  TALK TO ANNU");speak.setOnClickListener(v->listen());root.addView(speak);
        Button wake=new Button(this);wake.setText("ENABLE HEY ANNU WAKE LISTENING");wake.setOnClickListener(v->enableWakeWord());root.addView(wake);
        Button stopWake=new Button(this);stopWake.setText("STOP HEY ANNU LISTENING");stopWake.setOnClickListener(v->{stopService(new Intent(this,WakeWordService.class));say("Boss, wake phrase listening band kar di.");});root.addView(stopWake);
        Button language=new Button(this);language.setText(appPrefs.getString("speech_language","hi-IN").equals("hi-IN")?"VOICE LANGUAGE: HINDI":"VOICE LANGUAGE: ENGLISH");language.setOnClickListener(v->{String next=appPrefs.getString("speech_language","hi-IN").equals("hi-IN")?"en-IN":"hi-IN";appPrefs.edit().putString("speech_language",next).apply();if(tts!=null)tts.setLanguage(next.equals("hi-IN")?new Locale("hi","IN"):new Locale("en","IN"));language.setText(next.equals("hi-IN")?"VOICE LANGUAGE: HINDI":"VOICE LANGUAGE: ENGLISH");say(next.equals("hi-IN")?"Hindi voice select ho gayi.":"English voice selected.");});root.addView(language);
        Button vrm=new Button(this);vrm.setText("IMPORT / CHANGE VRM MODEL");vrm.setOnClickListener(v->pickVrm());root.addView(vrm);
        Button audio=new Button(this);audio.setText("IMPORT AUDIO SAMPLE");audio.setOnClickListener(v->pickAudio());root.addView(audio);
        Button playAudio=new Button(this);playAudio.setText("PLAY IMPORTED AUDIO SAMPLE");playAudio.setOnClickListener(v->playImportedAudio());root.addView(playAudio);
        Button stopAudio=new Button(this);stopAudio.setText("STOP AUDIO SAMPLE");stopAudio.setOnClickListener(v->stopImportedAudio());root.addView(stopAudio);
        Button brain=new Button(this);brain.setText("CONNECT FREE LOCAL AI (OLLAMA)");brain.setOnClickListener(v->configureBrain());root.addView(brain);
        Button calls=new Button(this);calls.setText("ENABLE INCOMING CALL ALERTS (OPTIONAL)");calls.setOnClickListener(v->enableCallAlerts());root.addView(calls);
        Button stop=new Button(this);stop.setText("STOP FLOATING ANNU");stop.setOnClickListener(v->{appPrefs.edit().putBoolean("floating_enabled",false).apply();stopService(new Intent(this,FloatingService.class));});root.addView(stop);
        Button smaller=new Button(this);smaller.setText("MAKE ANNU SMALLER");smaller.setOnClickListener(v->overlayAction("SIZE_SMALL"));root.addView(smaller);
        Button larger=new Button(this);larger.setText("MAKE ANNU LARGER");larger.setOnClickListener(v->overlayAction("SIZE_LARGE"));root.addView(larger);
        Button reset=new Button(this);reset.setText("RESET ANNU POSITION & SIZE");reset.setOnClickListener(v->overlayAction("SIZE_RESET"));root.addView(reset);
        setContentView(root);
        character.loadUrl("https://appassets.androidplatform.net/assets/annu.html");
        if(getIntent().getBooleanExtra("annu_incoming_call",false)){say("Boss, aapko call aa rahi hai. Kya aap baat karna chahenge?");getIntent().removeExtra("annu_incoming_call");}
        if(getIntent().getBooleanExtra("annu_wake",false)){getIntent().removeExtra("annu_wake");status.postDelayed(()->{say("Ji Boss, main sun rahi hoon.");listen();},700);}
    }
    private void stopImportedAudio(){if(importedPlayer!=null){try{importedPlayer.stop();}catch(Exception ignored){}importedPlayer.release();importedPlayer=null;}if(status!=null)status.setText("Audio sample stopped.");}
    private void playImportedAudio(){
        String path=appPrefs.getString("latest_audio_path","");
        if(path.isEmpty()||!new File(path).isFile()){say("Boss, pehle audio file import kijiye.");return;}
        try{
            if(importedPlayer!=null){importedPlayer.release();importedPlayer=null;}
            importedPlayer=new MediaPlayer();
            importedPlayer.setDataSource(path);
            importedPlayer.setOnPreparedListener(mp->{mp.start();if(status!=null)status.setText("Imported audio sample playing…");});
            importedPlayer.setOnCompletionListener(mp->{mp.release();if(importedPlayer==mp)importedPlayer=null;});
            importedPlayer.setOnErrorListener((mp,what,extra)->{mp.release();importedPlayer=null;say("Boss, ye audio format play nahi ho paaya.");return true;});
            importedPlayer.prepareAsync();
        }catch(Exception e){say("Boss, audio play nahi ho paaya.");android.util.Log.e("ANNU","Audio playback failed",e);}
    }
    private void configureBrain(){
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(28,12,28,4);
        EditText endpoint=new EditText(this);endpoint.setSingleLine(true);endpoint.setHint("http://192.168.1.10:11434");endpoint.setText(appPrefs.getString("brain_url",""));panel.addView(endpoint);
        EditText model=new EditText(this);model.setSingleLine(true);model.setHint("qwen3.5:0.8b");model.setText(appPrefs.getString("brain_model","qwen3.5:0.8b"));panel.addView(model);
        new android.app.AlertDialog.Builder(this).setTitle("ANNU — Free Local AI")
            .setMessage("Ollama laptop/PC par chalna chahiye aur phone same Wi-Fi par ho. Ollama ka LAN address yahan daalein, example http://192.168.1.10:11434. Model pehle PC par download hona chahiye. Is connection par cloud API fee nahi lagti; PC on rehna zaroori hai.")
            .setView(panel)
            .setPositiveButton("SAVE",(d,w)->{String url=endpoint.getText().toString().trim();String name=model.getText().toString().trim();appPrefs.edit().putString("brain_url",url).putString("brain_model",name.isEmpty()?"qwen3.5:0.8b":name).apply();say(url.isEmpty()?"Boss, local AI connection clear kar diya.":"Boss, AI server address save ho gaya. Ab us PC par Ollama chalayein.");})
            .setNeutralButton("TEST CONNECTION",(d,w)->{String url=endpoint.getText().toString().trim();String name=model.getText().toString().trim();appPrefs.edit().putString("brain_url",url).putString("brain_model",name.isEmpty()?"qwen3.5:0.8b":name).apply();BrainClient.ask(this,"Reply only: ANNU connected.",(a,e)->say(a!=null?"AI connection successful. "+a:"AI connection failed: "+e));})
            .setNegativeButton("CANCEL",null).show();
    }
    private void copyBundledModelIfMissing(){
        File target=new File(getFilesDir(),"annu.vrm");if(target.exists())return;
        try(InputStream in=getAssets().open("annu.vrm");FileOutputStream out=new FileOutputStream(target)){byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}catch(Exception e){android.util.Log.e("ANNU","Bundled VRM copy failed",e);}
    }
    private void setupWebView(WebView w){WebSettings s=w.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setLoadsImagesAutomatically(true);w.setWebViewClient(new android.webkit.WebViewClient(){@Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view,android.webkit.WebResourceRequest request){return assetLoader.shouldInterceptRequest(request.getUrl());}});w.setWebChromeClient(new android.webkit.WebChromeClient());w.setLayerType(View.LAYER_TYPE_HARDWARE,null);}
    private void pickVrm(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"model/gltf-binary","application/octet-stream"});i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,VRM_PICK);}
    private void pickAudio(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("audio/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,AUDIO_PICK);}
    private void importFile(Uri uri,File target)throws Exception{try(InputStream in=getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(target)){if(in==null)throw new Exception("File open nahi hua");byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}}
    private void refreshAssetStatus(){File dir=new File(getFilesDir(),"audio");int count=dir.exists()&&dir.list()!=null?dir.list().length:0;assetStatus.setText("Saved model: "+(new File(getFilesDir(),"annu.vrm").exists()?"Ready":"Missing")+"  •  Saved audio files: "+count);}
    private void startAnnu(){
        if(!Settings.canDrawOverlays(this)){startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())),OVERLAY_REQUEST);return;}
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},PERMISSION_REQUEST);return;}
        appPrefs.edit().putBoolean("floating_enabled",true).apply();Intent service=new Intent(this,FloatingService.class);if(Build.VERSION.SDK_INT>=26)startForegroundService(service);else startService(service);say("Ji Boss! Floating mode shuru kar diya hai.");
    }
    private void overlayAction(String action){if(!appPrefs.getBoolean("floating_enabled",false)){say("Boss, pehle floating ANNU start kijiye.");return;}Intent i=new Intent(this,FloatingService.class).setAction(action);startService(i);say("Ji Boss, setting update kar di.");}
    private void listen(){Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);String lang=appPrefs.getString("speech_language","hi-IN");i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,lang);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,lang);i.putExtra(RecognizerIntent.EXTRA_PROMPT,"ANNU sun rahi hai...");try{startActivityForResult(i,SPEECH_REQUEST);}catch(Exception e){say("Voice recognition abhi available nahi hai.");}}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);
        if(req==OVERLAY_REQUEST&&Settings.canDrawOverlays(this))startAnnu();
        if(result==RESULT_OK&&data!=null&&data.getData()!=null&&(req==VRM_PICK||req==AUDIO_PICK)){
            try{if(req==VRM_PICK){importFile(data.getData(),new File(getFilesDir(),"annu.vrm"));say("Boss, VRM model save ho gaya. App ko dobara kholne par bhi rahega.");}else{File dir=new File(getFilesDir(),"audio");dir.mkdirs();String mime=getContentResolver().getType(data.getData());String ext=mime!=null&&mime.contains("mpeg")?".mp3":mime!=null&&mime.contains("wav")?".wav":mime!=null&&mime.contains("mp4")?".m4a":mime!=null&&mime.contains("ogg")?".ogg":mime!=null&&mime.contains("aac")?".aac":".audio";File saved=new File(dir,"audio_"+System.currentTimeMillis()+ext);importFile(data.getData(),saved);appPrefs.edit().putString("latest_audio_path",saved.getAbsolutePath()).apply();say("Boss, audio file phone mein save ho gayi. Aap Play Imported Audio se sun sakte hain.");}refreshAssetStatus();if(character!=null)character.reload();}
            catch(Exception e){say("Boss, file import nahi ho paayi.");android.util.Log.e("ANNU","Import failed",e);}
        }
        if(req==SPEECH_REQUEST&&result==RESULT_OK&&data!=null){ArrayList<String>a=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(a!=null&&!a.isEmpty())handleCommand(a.get(0).toLowerCase(Locale.ROOT));}
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){super.onRequestPermissionsResult(request,permissions,results);if(request==CALL_PERMISSION_REQUEST){say(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED?"Boss, incoming call alerts enable ho gaye.":"Boss, call alerts ke liye phone permission nahi mili.");return;}if(request==WAKE_PERMISSION_REQUEST){if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)startWakeWord();else say("Boss, wake phrase ke liye microphone permission zaroori hai.");return;}if(request==WAKE_NOTIFICATION_REQUEST){if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)startWakeWord();else say("Boss, wake alert dikhane ke liye notification permission allow karein.");return;}if(request==PERMISSION_REQUEST){boolean ok=true;for(int r:results)if(r!=PackageManager.PERMISSION_GRANTED)ok=false;if(ok)startAnnu();else say("Boss, floating mode ke liye notification permission allow karein.");}}
    private void enableWakeWord(){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},WAKE_PERMISSION_REQUEST);return;}
        startWakeWord();
    }
    private void startWakeWord(){
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){say("Boss, pehle notification permission allow karein.");requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},WAKE_NOTIFICATION_REQUEST);return;}
        Intent i=new Intent(this,WakeWordService.class).putExtra("language",appPrefs.getString("speech_language","hi-IN"));
        if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);
        say("Boss, Hey ANNU wake listening start ho gayi. Notification se kabhi bhi band kar sakte hain.");
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);if(intent!=null&&intent.getBooleanExtra("annu_wake",false)){intent.removeExtra("annu_wake");say("Ji Boss, main sun rahi hoon.");listen();}}
    private void enableCallAlerts(){if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED){say("Boss, incoming call alerts already enabled hain.");return;}requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},CALL_PERMISSION_REQUEST);}
    private void handleCommand(String c){
        if(c.contains("settings")||c.contains("सेटिंग")){say("Boss, settings khol rahi hoon.");startActivity(new Intent(Settings.ACTION_SETTINGS));return;}
        if(c.contains("camera")||c.contains("कैमरा")){launchIntent(new Intent("android.media.action.IMAGE_CAPTURE"),"Camera khol rahi hoon.");return;}
        if(c.contains("maps")||c.contains("map")||c.contains("नक्शा")){launchIntent(new Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q=")),"Maps khol rahi hoon.");return;}
        if(c.contains("bluetooth")){say("Boss, Bluetooth settings khol rahi hoon.");startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));return;}
        if(c.contains("wifi")||c.contains("wi-fi")){say("Boss, Wi-Fi settings khol rahi hoon.");startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));return;}
        if(c.contains("youtube")){launch("com.google.android.youtube","YouTube khol rahi hoon.");return;}
        if(c.contains("chrome")){launch("com.android.chrome","Chrome khol rahi hoon.");return;}
        if(c.contains("time")||c.contains("समय")||c.contains("kitne baje")){say("Boss, abhi "+new SimpleDateFormat("h:mm a",Locale.getDefault()).format(new Date())+" ho rahe hain.");return;}
        if(c.contains("battery")||c.contains("बैटरी")){Intent b=registerReceiver(null,new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));if(b!=null){int level=b.getIntExtra("level",-1),scale=b.getIntExtra("scale",100);say(level>=0?"Boss, battery "+(level*100/Math.max(scale,1))+" percent hai.":"Boss, battery status nahi mil paaya.");}else say("Boss, battery status nahi mil paaya.");return;}
        if(c.contains("date")||c.contains("tarikh")||c.contains("तारीख")){say("Boss, aaj "+new SimpleDateFormat("EEEE, d MMMM yyyy",Locale.getDefault()).format(new Date())+" hai.");return;}
        if(c.contains("dance")){animate("DANCE","Ji Boss, dance mode!");return;}
        if(c.contains("walk")||c.contains("चल")||c.contains("चलो")){animate("WALK","Boss, walking animation shuru!");return;}
        if(c.contains("sit")||c.contains("बैठ")||c.contains("baith")){animate("SIT","Ji Boss, sitting pose!");return;}
        if(c.contains("nod")||c.contains("haan mein")||c.contains("सिर हिला")){animate("NOD","Ji Boss!");return;}
        if(c.contains("think")||c.contains("सोच")||c.contains("soch")){animate("THINK","Hmm Boss, main soch rahi hoon.");return;}
        if(c.contains("happy")||c.contains("खुश")||c.contains("khush")){animate("HAPPY","Yay Boss! 💜");return;}

        if(c.contains("wave")||c.contains("haath hila")){animate("WAVE","Boss, hello!");return;}
        if(c.contains("so ja")||c.contains("sleep")){animate("SLEEP","Good night Boss.");return;}
        if(c.contains("wake")||c.contains("uth ja")||c.contains("jaag")){animate("WAKE","Ji Boss, main jag gayi!");return;}
        if(c.contains("kya yaad")||c.contains("what do you remember")||c.contains("mera note")||c.contains("note bata")){String note=appPrefs.getString("user_note","");say(note.isEmpty()?"Boss, abhi koi private note save nahi hai.":"Boss, aapka saved note: "+note);return;}
        if(c.contains("play audio")||c.contains("voice sample")||c.contains("audio chala")||c.contains("audio sun")){playImportedAudio();return;}
        if(c.contains("call ka message")){say("Boss, abhi call message history available nahi hai.");return;}
        if(c.contains("hello")||c.contains("hi")||c.contains("namaste")||c.contains("नमस्ते")||c.contains("annu")){say("Ji Boss! Main yahan hoon. Bataiye kya karna hai?");return;}
        if(c.contains("stop")||c.contains("band ho ja")){appPrefs.edit().putBoolean("floating_enabled",false).apply();stopService(new Intent(this,FloatingService.class));say("Theek hai Boss, floating mode band kar rahi hoon.");return;}
        BrainClient.ask(this,c,(answer,error)->{if(answer!=null)say(answer);else say("Boss, AI se jawab nahi mila. "+error);});
    }
    private void animate(String mode,String response){if(!appPrefs.getBoolean("floating_enabled",false)){say("Boss, pehle floating ANNU start kijiye.");return;}startService(new Intent(this,FloatingService.class).setAction("ANIM_"+mode));say(response);}
    private void launchIntent(Intent intent,String response){try{say(response);startActivity(intent);}catch(Exception e){say("Boss, ye feature is phone par available nahi hai.");}}
    private void launch(String pkg,String response){Intent i=getPackageManager().getLaunchIntentForPackage(pkg);if(i==null){say("Boss, ye app phone mein nahi mili.");return;}say(response);startActivity(i);}
    private void setSpeechState(boolean active){if(appPrefs!=null&&appPrefs.getBoolean("floating_enabled",false)){try{startService(new Intent(this,FloatingService.class).setAction(active?"SPEAK_ON":"SPEAK_OFF"));}catch(Exception ignored){}}}
    private void say(String m){if(status!=null)status.setText(m);if(tts!=null)tts.speak(m,TextToSpeech.QUEUE_FLUSH,null,"annu_reply");}
    @Override protected void onResume(){super.onResume();if(appPrefs!=null&&appPrefs.getBoolean("floating_enabled",false))try{startService(new Intent(this,FloatingService.class).setAction("HIDE"));}catch(Exception ignored){}}
    @Override protected void onPause(){if(appPrefs!=null&&appPrefs.getBoolean("floating_enabled",false))try{startService(new Intent(this,FloatingService.class).setAction("SHOW"));}catch(Exception ignored){}super.onPause();}
    @Override protected void onDestroy(){if(character!=null)character.destroy();if(importedPlayer!=null){importedPlayer.release();importedPlayer=null;}if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}