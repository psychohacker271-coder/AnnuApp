package com.annu.companion;

import android.content.Intent;
import android.service.voice.VoiceInteractionService;

public class AnnuVoiceInteractionService extends VoiceInteractionService {
    @Override public void onReady() { super.onReady(); }
    @Override public void onLaunchVoiceAssistFromKeyguard() {
        Intent i=new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra("annu_voice_assist",true);
        startActivity(i);
    }
}