package com.annu.companion;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;

public class MainActivity extends Activity {
    private static final int OVERLAY_REQUEST = 701;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 36, 28, 36);
        root.setBackgroundColor(Color.rgb(35, 22, 57));
        TextView title = new TextView(this);
        title.setText("ANNU  💜"); title.setTextColor(Color.WHITE); title.setTextSize(34); title.setGravity(Gravity.CENTER);
        TextView info = new TextView(this);
        info.setText("Your floating anime companion\n\nAllow 'Display over other apps', then start ANNU.\n\nThis first build uses a temporary character bubble. Your VRM model will be added in the next step.");
        info.setTextColor(Color.WHITE); info.setTextSize(16); info.setGravity(Gravity.CENTER); info.setPadding(0,24,0,28);
        Button start = new Button(this); start.setText("Allow permission & Start ANNU");
        start.setOnClickListener(v -> startAnnu());
        Button stop = new Button(this); stop.setText("Stop ANNU");
        stop.setOnClickListener(v -> stopService(new Intent(this, FloatingService.class)));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));
        root.addView(start, new LinearLayout.LayoutParams(-1, -2));
        root.addView(stop, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }
    private void startAnnu() {
        if (!Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(i, OVERLAY_REQUEST);
            return;
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 702);
        }
        Intent service = new Intent(this, FloatingService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(service); else startService(service);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQUEST && Settings.canDrawOverlays(this)) startAnnu();
    }
}
