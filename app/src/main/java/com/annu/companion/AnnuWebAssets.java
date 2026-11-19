package com.annu.companion;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceResponse;
import androidx.webkit.WebViewAssetLoader;
import java.io.File;
import java.io.FileInputStream;

public final class AnnuWebAssets {
    private AnnuWebAssets(){}
    public static WebViewAssetLoader create(Context context){
        return new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/model/",uri->{
                if(!"/model/annu.vrm".equals(uri.getPath()))return null;
                File f=new File(context.getFilesDir(),"annu.vrm");
                if(!f.isFile())return null;
                try{return new WebResourceResponse("model/gltf-binary","binary",new FileInputStream(f));}
                catch(Exception e){return null;}
            }).build();
    }
}