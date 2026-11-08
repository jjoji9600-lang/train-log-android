package com.trainlog.simple;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * A one-time OFFLINE importer for the user's anatomy DOCX.
 * This mapping is based on verified positions in the uploaded 解剖.docx.
 * Each pair is: posture drawing, skeletal/muscular illustration.
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
  private static String path(Context c,String id,int slot){
    return new File(new File(c.getFilesDir(),"anatomy"),id+"_"+slot+".jpg").getAbsolutePath();
  }
  static boolean ready(Context c,String id){
    return new File(path(c,id,0)).isFile()&&new File(path(c,id,1)).isFile();
  }
  static int readyCount(Context c){
    int sum=0;for(String[] row:MAP)if(ready(c,row[0]))sum++;return sum;
  }
  static Bitmap load(Context c,String id,int slot,int edge){
    String path=path(c,id,slot);
    BitmapFactory.Options o=new BitmapFactory.Options();
    o.inJustDecodeBounds=true;BitmapFactory.decodeFile(path,o);
    if(o.outWidth<1||o.outHeight<1)return null;
    o.inJustDecodeBounds=false;o.inSampleSize=1;
    while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>edge)o.inSampleSize*=2;
    try{return BitmapFactory.decodeFile(path,o);}catch(OutOfMemoryError error){return null;}
  }
  interface Progress{void report(int imported,int total);}
  static int importDocx(Context c, Uri doc, Progress progress) throws Exception{
    final HashMap<String,ArrayList<String[]>> needed=new HashMap<>();
    for(String[] pair:MAP)for(int slot=0;slot<2;slot++){
      String key="word/media/image"+pair[slot+1]+".jpg";
      ArrayList<String[]> values=needed.get(key);
      if(values==null){values=new ArrayList<>();needed.put(key,values);}
      values.add(new String[]{pair[0],String.valueOf(slot)});
    }
    File directory=new File(c.getFilesDir(),"anatomy");
    if(!directory.exists()&&!directory.mkdirs())throw new IOException("无法创建图片目录");
    int done=0;
    try(InputStream raw=c.getContentResolver().openInputStream(doc)){
      if(raw==null)throw new IOException("无法读取 Word 文档");
      try(ZipInputStream zin=new ZipInputStream(new BufferedInputStream(raw,65536))){
        ZipEntry entry;
        byte[] buffer=new byte[32768];
        while((entry=zin.getNextEntry())!=null){
          ArrayList<String[]> target=needed.get(entry.getName());
          if(target!=null){
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            int n;
            while((n=zin.read(buffer))!=-1){
              if(bytes.size()+n>12_000_000)throw new IOException("单张插图过大");
              bytes.write(buffer,0,n);
            }
            byte[] data=bytes.toByteArray();
            BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;
            BitmapFactory.decodeByteArray(data,0,data.length,o);
            if(o.outWidth<1||o.outHeight<1)throw new IOException("无法识别 Word 中的插图");
            o.inJustDecodeBounds=false;o.inSampleSize=1;
            while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>1000)o.inSampleSize*=2;
            Bitmap bitmap=BitmapFactory.decodeByteArray(data,0,data.length,o);
            if(bitmap==null)throw new IOException("图片解码失败");
            try{
              for(String[] dst:target){
                File temp=new File(directory,dst[0]+"_"+dst[1]+".tmp");
                try(FileOutputStream stream=new FileOutputStream(temp)){
                  if(!bitmap.compress(Bitmap.CompressFormat.JPEG,85,stream))throw new IOException("压缩图片失败");
                }
                File file=new File(path(c,dst[0],Integer.parseInt(dst[1])));
                if(file.exists()&&!file.delete())throw new IOException("旧图片无法替换");
                if(!temp.renameTo(file))throw new IOException("无法保存图片");
                done++;
              }
            }finally{bitmap.recycle();}
            if(progress!=null)progress.report(done,MAP.length*2);
          }
          zin.closeEntry();
        }
      }
    }
    if(done<20)throw new IOException("图片匹配不足：请确认选择的是你上传的《解剖.docx》");
    return readyCount(c);
  }
}
