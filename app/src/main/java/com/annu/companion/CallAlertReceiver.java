package com.annu.companion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;

public class CallAlertReceiver extends BroadcastReceiver {
    private static final String CHANNEL="annu_calls";
    @Override public void onReceive(Context context,Intent intent){
        if(!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction()))return;
        String state=intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if(!TelephonyManager.EXTRA_STATE_RINGING.equals(state))return;
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel ch=new NotificationChannel(CHANNEL,"ANNU incoming call prompts",NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Alerts you when a call arrives while ANNU is enabled");
            nm.createNotificationChannel(ch);
        }
        Intent open=new Intent(context,MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        open.putExtra("annu_incoming_call",true);
        PendingIntent pi=PendingIntent.getActivity(context,82,open,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(context,CHANNEL):new Notification.Builder(context);
        Notification n=b.setSmallIcon(android.R.drawable.sym_call_incoming)
                .setContentTitle("ANNU 💜 — Boss, call aa rahi hai")
                .setContentText("Kya aap baat karna chahenge? Call screen kholne ke liye tap karein.")
                .setCategory(Notification.CATEGORY_CALL).setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true).setContentIntent(pi).build();
        nm.notify(82,n);
    }
}