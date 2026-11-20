package com.annu.companion;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BrainClient {
    public interface Callback { void done(String answer, String error); }
    private static final ExecutorService IO=Executors.newSingleThreadExecutor();
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private BrainClient(){}

    public static boolean configured(Context c){
        return !c.getSharedPreferences("annu",Context.MODE_PRIVATE).getString("brain_url","").trim().isEmpty();
    }

    public static void ask(Context context,String userText,Callback callback){
        SharedPreferences prefs=context.getSharedPreferences("annu",Context.MODE_PRIVATE);
        String endpoint=prefs.getString("brain_url","").trim();
        String model=prefs.getString("brain_model","qwen3.5:0.8b").trim();
        if(endpoint.isEmpty()){callback.done(null,"AI brain connect nahi hai. App mein FREE LOCAL AI setup karein.");return;}
        IO.execute(()->{
            HttpURLConnection conn=null;
            try{
                if(!endpoint.startsWith("http://")&&!endpoint.startsWith("https://"))throw new Exception("Endpoint http:// ya https:// se shuru hona chahiye.");
                while(endpoint.endsWith("/"))endpoint=endpoint.substring(0,endpoint.length()-1);
                if(!endpoint.endsWith("/api/chat"))endpoint+="/api/chat";
                URL url=new URL(endpoint);
                conn=(HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(45000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json; charset=utf-8");
                JSONObject body=new JSONObject();
                body.put("model",model.isEmpty()?"qwen3.5:0.8b":model);
                body.put("stream",false);
                JSONArray messages=new JSONArray();
                messages.put(new JSONObject().put("role","system").put("content","You are ANNU, a friendly, polite, helpful Indian AI companion. Speak concise Hindi or Hinglish unless the user writes English. Use feminine Hindi grammar. Address the user as Boss. Never claim to be human or physically present. You cannot control phone functions unless the Android app explicitly offers that command. Be honest about limitations and prioritize safety."));
                messages.put(new JSONObject().put("role","user").put("content",userText));
                body.put("messages",messages);
                byte[] payload=body.toString().getBytes(StandardCharsets.UTF_8);
                try(OutputStream out=conn.getOutputStream()){out.write(payload);}
                int status=conn.getResponseCode();
                InputStream stream=status>=200&&status<300?conn.getInputStream():conn.getErrorStream();
                StringBuilder raw=new StringBuilder();
                try(BufferedReader reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))){String line;while((line=reader.readLine())!=null)raw.append(line);}
                if(status<200||status>=300)throw new Exception("AI server error "+status+": "+raw.toString().substring(0,Math.min(220,raw.length())));
                JSONObject result=new JSONObject(raw.toString());
                String answer=result.optJSONObject("message")!=null?result.getJSONObject("message").optString("content",""):result.optString("response","");
                if(answer.trim().isEmpty())throw new Exception("AI server ne empty reply diya.");
                String finalAnswer=answer.trim();
                MAIN.post(()->callback.done(finalAnswer,null));
            }catch(Exception e){String message=e.getMessage()==null?"Connection failed":e.getMessage();MAIN.post(()->callback.done(null,message));}
            finally{if(conn!=null)conn.disconnect();}
        });
    }
}
