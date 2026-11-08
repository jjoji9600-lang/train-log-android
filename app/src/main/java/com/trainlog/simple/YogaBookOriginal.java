package com.trainlog.simple;

import android.content.Context;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Original exercise instructions extracted from the user-provided yoga.docx.
 * Sections are verbatim OCR text including possible source OCR errors.
 */
final class YogaBookOriginal {
  private static JSONObject book;
  private YogaBookOriginal(){}
  static synchronized String text(Context ctx,int figure){
    if(book==null){
      try(InputStream in=ctx.getAssets().open("pose_text/original.json");
          ByteArrayOutputStream out=new ByteArrayOutputStream()){
        byte[] buffer=new byte[8192];int n;
        while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
        book=new JSONObject(out.toString("UTF-8"));
      }catch(Exception e){book=new JSONObject();}
    }
    JSONObject section=book.optJSONObject(String.valueOf(figure));
    return section!=null?section.optString("text",""):"";
  }
}