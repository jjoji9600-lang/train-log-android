package com.trainlog.simple;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * Built-in illustrated anatomy reference from the user's 解剖.docx.
 * All 18 poses ship with two bundled illustrations each.
 * Existing local import is retained as a fallback and for future replacements.
 */
final class AnatomyImageStore {
  private static final String[][] MAP={
    {"tadasana","1","3"},{"utkatasana","12","14"},
    {"uttanasana","15","17"},{"vrksasana","23","25"},
    {"warrior1","33","35"},{"warrior2","39","41"},
    {"warrior3","43","45"},{"trikonasana","52","54"},
    {"sukhasana","71","73"},{"dandasana","79","81"},
    {"baddhakonasana","98","100"},{"balasana","122","124"},
    {"apanasana","147","149"},{"savasana","145","146"},
    {"bhujangasana","185","187"},{"salabhasana","193","195"},
    {"downwarddog","199","201"},{"tabletop","242","244"}
  };
  private static final HashMap<String,Integer> PARTS=new HashMap<>();
  private static final HashMap<Integer,JSONObject> ASSETS=new HashMap<>();
  static {
    for(String id:new String[]{"tadasana","utkatasana","uttanasana","vrksasana"})PARTS.put(id,0);
    for(String id:new String[]{"warrior1","warrior2","warrior3","trikonasana"})PARTS.put(id,1);
    for(String id:new String[]{"sukhasana","dandasana","baddhakonasana","balasana"})PARTS.put(id,2);
    for(String id:new String[]{"apanasana","savasana","bhujangasana","salabhasana","downwarddog"})PARTS.put(id,3);
    PARTS.put("tabletop",4);
  }
  private static String localPath(Context c,String id,int slot){
    return new File(new File(c.getFilesDir(),"anatomy"),id+"_"+slot+".jpg").getAbsolutePath();
  }
  private static synchronized JSONObject part(Context c,int number) {
    if(ASSETS.containsKey(number))return ASSETS.get(number);
    String path=String.format(Locale.US,"anatomy_images/part_%02d.json",number);
    try(InputStream in=c.getAssets().open(path);
        ByteArrayOutputStream out=new ByteArrayOutputStream()){
      byte[] buffer=new byte[8192];int n;
      while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
      JSONObject obj=new JSONObject(out.toString("UTF-8"));
      ASSETS.put(number,obj);
      return obj;
    }catch(Exception e){return null;}
  }
  private static String encoded(Context c,String id,int slot){
    Integer index=PARTS.get(id);
    if(index==null||slot<0||slot>1)return null;
    JSONObject data=part(c,index);
    if(data==null)return null;
    JSONArray pair=data.optJSONArray(id);
    return pair!=null&&pair.length()>slot?pair.optString(slot,null):null;
  }
  static boolean ready(Context c,String id){
    if(PARTS.containsKey(id)){
      Integer index=PARTS.get(id);
      JSONObject assets=part(c,index);
      JSONArray imgs=assets==null?null:assets.optJSONArray(id);
      if(imgs!=null&&imgs.length()>=2&&!imgs.optString(0,"").isEmpty()&&!imgs.optString(1,"").isEmpty())return true;
    }
    return new File(localPath(c,id,0)).isFile()&&new File(localPath(c,id,1)).isFile();
  }
  static int readyCount(Context c){
    int sum=0;for(String[] r:MAP)if(ready(c,r[0]))sum++;return sum;
  }
  static Bitmap load(Context c,String id,int slot,int maxEdge){
    try{
      String b64=encoded(c,id,slot);
      if(b64!=null&&!b64.isEmpty()){
        byte[] raw=Base64.decode(b64,Base64.DEFAULT);
        BitmapFactory.Options opts=new BitmapFactory.Options();
        opts.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(raw,0,raw.length,opts);
        opts.inJustDecodeBounds=false;opts.inSampleSize=1;
        int edge=Math.max(1,maxEdge);
        while(Math.max(opts.outWidth,opts.outHeight)/opts.inSampleSize>edge)opts.inSampleSize*=2;
        Bitmap image=BitmapFactory.decodeByteArray(raw,0,raw.length,opts);
        if(image!=null)return image;
      }
    }catch(Exception|OutOfMemoryError ignored){}
    String path=localPath(c,id,slot);
    BitmapFactory.Options o=new BitmapFactory.Options();
    o.inJustDecodeBounds=true;BitmapFactory.decodeFile(path,o);
    if(o.outWidth<1||o.outHeight<1)return null;
    o.inJustDecodeBounds=false;o.inSampleSize=1;
    while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>Math.max(1,maxEdge))o.inSampleSize*=2;
    try{return BitmapFactory.decodeFile(path,o);}catch(Exception|OutOfMemoryError error){return null;}
  }
  interface Progress {void report(int imported,int total);}
  /** Optional replacement workflow for existing users; bundled images work without it. */
  static int importDocx(Context c,Uri doc,Progress progress) throws Exception {
    HashMap<String,ArrayList<String[]>> needed=new HashMap<>();
    for(String[] row:MAP)for(int slot=0;slot<2;slot++){
      String entry="word/media/image"+row[slot+1]+".jpg";
      if(!needed.containsKey(entry))needed.put(entry,new ArrayList<>());
      needed.get(entry).add(new String[]{row[0],String.valueOf(slot)});
    }
    File dir=new File(c.getFilesDir(),"anatomy");
    if(!dir.exists()&&!dir.mkdirs())throw new IOException("无法创建图片目录");
    int done=0;
    try(InputStream raw=c.getContentResolver().openInputStream(doc)){
      if(raw==null)throw new IOException("无法读取Word文档");
      try(ZipInputStream zin=new ZipInputStream(new BufferedInputStream(raw,32768))){
        ZipEntry entry;byte[] buffer=new byte[16384];
        while((entry=zin.getNextEntry())!=null){
          ArrayList<String[]> dst=needed.get(entry.getName());
          if(dst!=null){
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            int n;while((n=zin.read(buffer))!=-1){
              if(bytes.size()+n>12000000)throw new IOException("单张图片过大");
              bytes.write(buffer,0,n);
            }
            byte[] data=bytes.toByteArray();
            BitmapFactory.Options o=new BitmapFactory.Options();
            o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,o);
            if(o.outWidth<1||o.outHeight<1)throw new IOException("无法识别图片");
            o.inJustDecodeBounds=false;o.inSampleSize=1;
            while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>1000)o.inSampleSize*=2;
            Bitmap bitmap=BitmapFactory.decodeByteArray(data,0,data.length,o);
            if(bitmap==null)throw new IOException("图片解码失败");
            try{
              for(String[] target:dst){
                File file=new File(localPath(c,target[0],Integer.parseInt(target[1])));
                try(FileOutputStream stream=new FileOutputStream(file)){
                  if(!bitmap.compress(Bitmap.CompressFormat.JPEG,85,stream))throw new IOException("图片保存失败");
                }
                done++;
              }
            }finally{bitmap.recycle();}
            if(progress!=null)progress.report(done,MAP.length*2);
          }
          zin.closeEntry();
        }
      }
    }
    if(done<20)throw new IOException("图片匹配不足，请确认原始Word文档");
    return readyCount(c);
  }
}
