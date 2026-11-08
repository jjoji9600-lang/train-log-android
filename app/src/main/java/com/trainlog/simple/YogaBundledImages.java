package com.trainlog.simple;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** Compressed illustrations bundled from the user's own yoga.docx. No online dependency. */
final class YogaBundledImages {
  private static final Map<String,String> images = new HashMap<>();
  private static boolean loaded=false;
  private YogaBundledImages(){}
  private static synchronized void ensureLoaded(Context context){
    if(loaded)return;
    for(int i=0;i<6;i++){
      String file=String.format(java.util.Locale.US,"pose_images/part_%02d.json",i);
      try(InputStream in=context.getAssets().open(file);
          ByteArrayOutputStream out=new ByteArrayOutputStream()){
        byte[] buf=new byte[16384];int n;
        while((n=in.read(buf))!=-1)out.write(buf,0,n);
        JSONObject o=new JSONObject(out.toString("UTF-8"));
        java.util.Iterator<String> it=o.keys();
        while(it.hasNext()){String key=it.next();images.put(key,o.getString(key));}
      }catch(Exception ignored){}
    }
    loaded=true;
  }
  static boolean has(Context context,String pose){
    ensureLoaded(context);
    return images.containsKey(String.valueOf(YogaBookImages.firstFigure(pose)));
  }
  static Bitmap load(Context context,String pose){
    ensureLoaded(context);
    String b64=images.get(String.valueOf(YogaBookImages.firstFigure(pose)));
    if(b64==null)return null;
    try{
      byte[] data=Base64.decode(b64,Base64.DEFAULT);
      return BitmapFactory.decodeByteArray(data,0,data.length);
    }catch(Exception ignored){return null;}
  }
}
