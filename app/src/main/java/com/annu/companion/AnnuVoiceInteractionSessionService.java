package com.annu.companion;

import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSessionService;

public class AnnuVoiceInteractionSessionService extends VoiceInteractionSessionService {
    @Override public VoiceInteractionSession onNewSession(Bundle args) {
        return new AnnuVoiceInteractionSession(this);
    }
    static class AnnuVoiceInteractionSession extends VoiceInteractionSession {
        AnnuVoiceInteractionSession(VoiceInteractionSessionService service) { super(service); }
        @Override public void onShow(Bundle args,int showFlags) {
            super.onShow(args,showFlags);
            Intent i=new Intent(getContext(),MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra("annu_voice_assist",true);
            getContext().startActivity(i);
            hide();
        }
    }
}