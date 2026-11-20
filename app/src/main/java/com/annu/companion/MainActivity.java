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
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
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
    private static final int OVERLAY_REQUEST=701, SPEECH_REQUEST=703, PERMISSION_REQUEST=704, VRM_PICK=705, AUDIO_PICK=706;
    private TextView status, assetStatus;
    private android.content.SharedPreferences appPrefs;
    private TextToSpeech tts;
    private WebView character;
    private WebViewAssetLoader assetLoader;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        appPrefs=getSharedPreferences("annu",MODE_PRIVATE);
        getWindow().setStatusBarColor(Color.rgb(35,22,57));getWindow().setNavigationBarColor(Color.rgb(35,22,57));
        assetLoader=AnnuWebAssets.create(this);
        copyBundledModelIfMissing();
        tts=new TextToSpeech(this,r->{if(r==TextToSpeech.SUCCESS){tts.setLanguage(new Locale("hi","IN"));tts.setSpeechRate(0.94f);}});
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(10,8,10,8);root.setBackgroundColor(Color.rgb(35,22,57));
        character=new WebView(this);setupWebView(character);character.setBackgroundColor(Color.TRANSPARENT);
        root.addView(character,new LinearLayout.LayoutParams(-1,0,1));
        TextView title=new TextView(this);title.setText("ANNU  💜");title.setTextColor(Color.WHITE);title.setTextSize(25);title.setGravity(Gravity.CENTER);title.setPadding(8,4,8,4);root.addView(title);
        status=new TextView(this);status.setText("Ji Boss! Main yahan hoon.");status.setTextColor(Color.WHITE);status.setTextSize(15);status.setGravity(Gravity.CENTER);status.setPadding(8,4,8,6);root.addView(status);
        assetStatus=new TextView(this);assetStatus.setTextColor(Color.LTGRAY);assetStatus.setTextSize(12);assetStatus.setGravity(Gravity.CENTER);root.addView(assetStatus);refreshAssetStatus();
        Button start=new Button(this);start.setText("FLOAT ANNU ON SCREEN");start.setOnClickListener(v->startAnnu());root.addView(start);
        Button speak=new Button(this);speak.setText("🎙  TALK TO ANNU");speak.setOnClickListener(v->listen());root.addView(speak);
        Button vrm=new Button(this);vrm.setText("IMPORT / CHANGE VRM MODEL");vrm.setOnClickListener(v->pickVrm());root.addView(vrm);
        Button audio=new Button(this);audio.setText("IMPORT VOICE / AUDIO FILE");audio.setOnClickListener(v->pickAudio());root.addView(audio);
        Button stop=new Button(this);stop.setText("STOP FLOATING ANNU");stop.setOnClickListener(v->{appPrefs.edit().putBoolean("floating_enabled",false).apply();stopService(new Intent(this,FloatingService.class));});root.addView(stop);
        Button smaller=new Button(this);smaller.setText("MAKE ANNU SMALLER");smaller.setOnClickListener(v->overlayAction("SIZE_SMALL"));root.addView(smaller);
        Button larger=new Button(this);larger.setText("MAKE ANNU LARGER");larger.setOnClickListener(v->overlayAction("SIZE_LARGE"));root.addView(larger);
        Button reset=new Button(this);reset.setText("RESET ANNU POSITION & SIZE");reset.setOnClickListener(v->overlayAction("SIZE_RESET"));root.addView(reset);
        setContentView(root);
        character.loadUrl("https://appassets.androidplatform.net/assets/annu.html");
        if(getIntent().getBooleanExtra("annu_incoming_call",false)){say("Boss, aapko call aa rahi hai. Kya aap baat karna chahenge?");getIntent().removeExtra("annu_incoming_call");}
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
        if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},PERMISSION_REQUEST);return;}
        appPrefs.edit().putBoolean("floating_enabled",true).apply();Intent service=new Intent(this,FloatingService.class);if(Build.VERSION.SDK_INT>=26)startForegroundService(service);else startService(service);say("Ji Boss! Floating mode shuru kar diya hai.");
    }
    private void overlayAction(String action){if(!appPrefs.getBoolean("floating_enabled",false)){say("Boss, pehle floating ANNU start kijiye.");return;}Intent i=new Intent(this,FloatingService.class).setAction(action);startService(i);say("Ji Boss, setting update kar di.");}
    private void listen(){Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"hi-IN");i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"hi-IN");i.putExtra(RecognizerIntent.EXTRA_PROMPT,"ANNU sun rahi hai...");try{startActivityForResult(i,SPEECH_REQUEST);}catch(Exception e){say("Voice recognition abhi available nahi hai.");}}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);
        if(req==OVERLAY_REQUEST&&Settings.canDrawOverlays(this))startAnnu();
        if(result==RESULT_OK&&data!=null&&data.getData()!=null&&(req==VRM_PICK||req==AUDIO_PICK)){
            try{if(req==VRM_PICK){importFile(data.getData(),new File(getFilesDir(),"annu.vrm"));say("Boss, VRM model save ho gaya. App ko dobara kholne par bhi rahega.");}else{File dir=new File(getFilesDir(),"audio");dir.mkdirs();String name="audio_"+System.currentTimeMillis();importFile(data.getData(),new File(dir,name));say("Boss, audio file phone mein save ho gayi.");}refreshAssetStatus();if(character!=null)character.reload();}
            catch(Exception e){say("Boss, file import nahi ho paayi.");android.util.Log.e("ANNU","Import failed",e);}
        }
        if(req==SPEECH_REQUEST&&result==RESULT_OK&&data!=null){ArrayList<String>a=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(a!=null&&!a.isEmpty())handleCommand(a.get(0).toLowerCase(Locale.ROOT));}
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){super.onRequestPermissionsResult(request,permissions,results);if(request==PERMISSION_REQUEST){boolean ok=true;for(int r:results)if(r!=PackageManager.PERMISSION_GRANTED)ok=false;if(ok)startAnnu();else say("Boss, permission allow karni hogi.");}}
    private void handleCommand(String c){
        if(c.contains("settings")||c.contains("सेटिंग")){say("Boss, settings khol rahi hoon.");startActivity(new Intent(Settings.ACTION_SETTINGS));return;}
        if(c.contains("camera")||c.contains("कैमरा")){launchIntent(new Intent("android.media.action.IMAGE_CAPTURE"),"Camera khol rahi hoon.");return;}
        if(c.contains("maps")||c.contains("map")||c.contains("नक्शा")){launchIntent(new Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q=")),"Maps khol rahi hoon.");return;}
        if(c.contains("bluetooth")){say("Boss, Bluetooth settings khol rahi hoon.");startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));return;}
        if(c.contains("wifi")||c.contains("wi-fi")){say("Boss, Wi-Fi settings khol rahi hoon.");startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));return;}
        if(c.contains("youtube")){launch("com.google.android.youtube","YouTube khol rahi hoon.");return;}
        if(c.contains("chrome")){launch("com.android.chrome","Chrome khol rahi hoon.");return;}
        if(c.contains("time")||c.contains("समय")||c.contains("kitne baje")){say("Boss, abhi "+new SimpleDateFormat("h:mm a",Locale.getDefault()).format(new Date())+" ho rahe hain.");return;}
        if(c.contains("dance")){say("Boss, dance animation abhi add nahi hui hai.");return;}
        if(c.contains("so ja")||c.contains("sleep")){say("Theek hai Boss, sleep animation agle phase mein add hogi.");return;}
        if(c.contains("call ka message")){say("Boss, abhi call message history available nahi hai.");return;}
        if(c.contains("hello")||c.contains("hi")||c.contains("namaste")||c.contains("नमस्ते")||c.contains("annu")){say("Ji Boss! Main yahan hoon. Bataiye kya karna hai?");return;}
        if(c.contains("stop")||c.contains("band ho ja")){stopService(new Intent(this,FloatingService.class));say("Theek hai Boss, floating mode band kar rahi hoon.");return;}
        say("Boss, ye command abhi mere basic version mein nahi hai.");
    }
    private void launchIntent(Intent intent,String response){try{say(response);startActivity(intent);}catch(Exception e){say("Boss, ye feature is phone par available nahi hai.");}}
    private void launch(String pkg,String response){Intent i=getPackageManager().getLaunchIntentForPackage(pkg);if(i==null){say("Boss, ye app phone mein nahi mili.");return;}say(response);startActivity(i);}
    private void say(String m){if(status!=null)status.setText(m);if(tts!=null)tts.speak(m,TextToSpeech.QUEUE_FLUSH,null,"annu_reply");}
    @Override protected void onResume(){super.onResume();if(appPrefs!=null&&appPrefs.getBoolean("floating_enabled",false))try{startService(new Intent(this,FloatingService.class).setAction("HIDE"));}catch(Exception ignored){}}
    @Override protected void onPause(){if(appPrefs!=null&&appPrefs.getBoolean("floating_enabled",false))try{startService(new Intent(this,FloatingService.class).setAction("SHOW"));}catch(Exception ignored){}super.onPause();}
    @Override protected void onDestroy(){if(character!=null)character.destroy();if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}