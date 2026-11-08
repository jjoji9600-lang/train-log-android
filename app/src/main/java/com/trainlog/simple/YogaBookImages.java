package com.trainlog.simple;

import android.content.Context;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.regex.*;

/**
 * Imports illustrations from the owner's original yoga.docx, not from the network.
 * Figure-to-media mapping is specific to the user-provided illustrated edition.
 * Only selected entries are read; images are downscaled before local storage.
 */
final class YogaBookImages {
  static final int[] FIG_MEDIA={
    1,1,2,2,4,3,6,4,8,5,10,6,14,8,15,11,17,12,19,13,26,18,
    33,22,35,24,39,25,41,26,42,27,44,28,46,29,48,30,49,31,56,34,
    60,38,63,41,73,47,74,49,75,50,78,52,79,54,80,55,83,57,89,60,
    96,65,97,68,102,70,104,72,107,75,108,76,109,77,113,80,125,92,
    127,93,135,100,139,103,144,107,160,114,168,129,184,137,202,155,
    208,160,211,163,218,165,223,166,235,176,236,177,244,182,246,183,
    247,185,249,186,250,187,251,188,261,197,269,201,274,205,275,208,
    276,209,303,223,311,227,592,444
  };
  private static final Pattern FIGURE=Pattern.compile("图\\s*(\\d+)");
  static int firstFigure(String title){
    Matcher m=FIGURE.matcher(title);
    return m.find()?Integer.parseInt(m.group(1)):-1;
  }
  static File path(Context ctx,int figure){
    File dir=new File(ctx.getFilesDir(),"yoga_book_figures");
    return new File(dir,figure+".jpg");
  }
  static boolean has(Context ctx,String text){return path(ctx,firstFigure(text)).isFile();}
  static Bitmap load(Context ctx,String text){
    File f=path(ctx,firstFigure(text));if(!f.isFile())return null;
    BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;
    BitmapFactory.decodeFile(f.getAbsolutePath(),o);
    o.inJustDecodeBounds=false;o.inSampleSize=Math.max(1,Math.max(o.outWidth,o.outHeight)/1600);
    return BitmapFactory.decodeFile(f.getAbsolutePath(),o);
  }
  interface Progress{void update(int done,int total);}
  static int importDocument(Context ctx,Uri uri, Progress progress) throws Exception {
    HashMap<String,ArrayList<Integer>> wanted=new HashMap<>();
    for(int i=0;i<FIG_MEDIA.length;i+=2){
      String entry="word/media/image"+FIG_MEDIA[i+1]+".png";
      ArrayList<Integer> ids=wanted.get(entry);
      if(ids==null){ids=new ArrayList<>();wanted.put(entry,ids);}
      ids.add(FIG_MEDIA[i]);
    }
    File dir=new File(ctx.getFilesDir(),"yoga_book_figures");
    if(!dir.exists()&&!dir.mkdirs())throw new IOException("无法创建图片目录");
    int total=FIG_MEDIA.length/2,done=0,found=0;
    try(InputStream raw=ctx.getContentResolver().openInputStream(uri)){
      if(raw==null)throw new IOException("无法读取文件");
      try(ZipInputStream zin=new ZipInputStream(new BufferedInputStream(raw,32768))){
        ZipEntry entry;
        byte[] buffer=new byte[32768];
        while((entry=zin.getNextEntry())!=null){
          ArrayList<Integer> ids=wanted.get(entry.getName());
          if(ids!=null){
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            int n;
            while((n=zin.read(buffer))!=-1){
              if(bytes.size()+n>12000000)throw new IOException("单张图片太大");
              bytes.write(buffer,0,n);
            }
            byte[] data=bytes.toByteArray();
            BitmapFactory.Options o=new BitmapFactory.Options();
            o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,o);
            if(o.outWidth<1||o.outHeight<1)throw new IOException("图片格式错误");
            o.inJustDecodeBounds=false;
            o.inSampleSize=1;
            while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>1100)o.inSampleSize*=2;
            Bitmap bm=BitmapFactory.decodeByteArray(data,0,data.length,o);
            if(bm==null)throw new IOException("图片解码失败");
            try{
              for(int fig:ids){
                File tmp=new File(dir,fig+".tmp");
                try(FileOutputStream f=new FileOutputStream(tmp)){
                  if(!bm.compress(Bitmap.CompressFormat.JPEG,84,f))throw new IOException("保存图片失败");
                  f.getFD().sync();
                }
                File target=path(ctx,fig);
                if(target.exists()&&!target.delete())throw new IOException("无法替换旧图");
                if(!tmp.renameTo(target))throw new IOException("无法保存图片");
                done++;
              }
            }finally{bm.recycle();}
            found++;
            if(progress!=null)progress.update(done,total);
          }
          zin.closeEntry();
        }
      }
    }
    if(found<15)throw new IOException("没有识别到对应版本的图片，请确认选择的是原始 yoga.docx");
    return done;
  }
}
