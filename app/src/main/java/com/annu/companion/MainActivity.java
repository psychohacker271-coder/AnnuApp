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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int OVERLAY_REQUEST = 701;
    private static final int SPEECH_REQUEST = 703;
    private TextView status;
    private TextToSpeech tts;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        tts = new TextToSpeech(this, result -> { if (result == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("en", "IN")); });
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER);
        root.setPadding(28, 36, 28, 36); root.setBackgroundColor(Color.rgb(35, 22, 57));
        TextView title = new TextView(this);
        title.setText("ANNU  💜"); title.setTextColor(Color.WHITE); title.setTextSize(34); title.setGravity(Gravity.CENTER);
        TextView info = new TextView(this);
        info.setText("Your floating anime companion\n\nStart ANNU, then tap her bubble and choose a voice command. This starter version supports a small set of safe commands; more abilities and your VRM model can be added next.");
        info.setTextColor(Color.WHITE); info.setTextSize(16); info.setGravity(Gravity.CENTER); info.setPadding(0,24,0,20);
        status = new TextView(this); status.setText("Ready when you are ✨"); status.setTextColor(Color.WHITE); status.setTextSize(16); status.setGravity(Gravity.CENTER); status.setPadding(0,8,0,16);
        Button start = new Button(this); start.setText("Allow permission & Start ANNU"); start.setOnClickListener(v -> startAnnu());
        Button speak = new Button(this); speak.setText("🎙 Speak to ANNU"); speak.setOnClickListener(v -> listen());
        Button stop = new Button(this); stop.setText("Stop ANNU"); stop.setOnClickListener(v -> stopService(new Intent(this, FloatingService.class)));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));
        root.addView(start, new LinearLayout.LayoutParams(-1, -2));
        root.addView(speak, new LinearLayout.LayoutParams(-1, -2));
        root.addView(stop, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }

    private void startAnnu() {
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), OVERLAY_REQUEST);
            return;
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 702);
        Intent service = new Intent(this, FloatingService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(service); else startService(service);
        say("I'm here! What shall we do?");
    }

    private void listen() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "ANNU is listening...");
        try { startActivityForResult(i, SPEECH_REQUEST); }
        catch (Exception e) { status.setText("Speech recognition is not available on this phone."); say("Voice recognition is not available right now."); }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQUEST && Settings.canDrawOverlays(this)) startAnnu();
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) handleCommand(matches.get(0).toLowerCase(Locale.ROOT));
        }
    }

    private void handleCommand(String command) {
        status.setText("You said: " + command);
        if (command.contains("youtube")) { launchApp("com.google.android.youtube", "Opening YouTube."); return; }
        if (command.contains("chrome")) { launchApp("com.android.chrome", "Opening Chrome."); return; }
        if (command.contains("time") || command.contains("समय") || command.contains("kitne baje")) {
            String now = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
            say("Abhi " + now + " ho rahe hain."); return;
        }
        if (command.contains("hello") || command.contains("hi") || command.contains("नमस्ते") || command.contains("annu")) {
            say("Hello! Main yahin hoon. Batao, kya karna hai?"); return;
        }
        if (command.contains("stop") || command.contains("band ho ja")) {
            stopService(new Intent(this, FloatingService.class)); say("Theek hai, floating mode band kar rahi hoon."); return;
        }
        say("Ye command abhi mere starter version mein available nahi hai. Main ise next update mein seekh sakti hoon.");
    }

    private void launchApp(String packageName, String response) {
        Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch == null) { say("Ye app installed nahi mil rahi."); return; }
        say(response); startActivity(launch);
    }

    private void say(String message) {
        status.setText(message);
        if (tts != null) tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "annu_reply");
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
