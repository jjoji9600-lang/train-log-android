package com.trainlog.simple;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
  static final String[] NAMES={"引体向上","俯卧撑","徒手深蹲"};
  static final String[] YOGA_NAMES={"山式","三角伸展式","战士二式","战士一式","靠墙半下犬式","摊尸式"};
  static final String[] YOGA_HINTS={
    "60秒 · 双脚均匀受力，头顶向上，不僵硬锁膝",
    "左右各2次×20～30秒 · 手扶瑜伽砖，重视躯干伸长",
    "左右各2次×20～30秒 · 前膝顺脚尖，后脚稳定",
    "左右各1次×20～30秒 · 可缩短站距，不压腰",
    "2次×30秒 · 双手扶墙，髋部折叠，舒展背部",
    "3～5分钟 · 仰卧自然呼吸，完全放松"
  };
  static final int GREEN=0xff176b59, DARK=0xff21332f, MUTED=0xff687b74, BG=0xfff6f7f5;
  DB db;
  LinearLayout root;
  EditText[][] counts=new EditText[3][3];
  CheckBox popping,pain;
  CheckBox[] yogaChecks=new CheckBox[YOGA_NAMES.length];
  CheckBox yogaPain;
  EditText yogaMinutes,yogaNotes;
  EditText notes;
  String today=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
  int exercise=0;
  int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
  @Override public void onCreate(Bundle b){super.onCreate(b);db=new DB(this);record();}
  LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextColor(DARK);t.setTextSize(size);t.setPadding(0,dp(5),0,dp(5));return t;}
  Button button(String s){Button b=new Button(this);b.setAllCaps(false);b.setText(s);b.setTextColor(GREEN);return b;}
  void add(LinearLayout l,View v){l.addView(v,new LinearLayout.LayoutParams(-1,-2));}
  void start(String title){
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);
    root=column();root.setPadding(dp(16),dp(20),dp(16),dp(35));scroll.addView(root);setContentView(scroll);
    add(root,label("练一练",27));add(root,label("训练日志 · 安全优先 · 离线保存",13));
    LinearLayout nav=new LinearLayout(this);
    for(String item:new String[]{"力量","瑜伽","趋势","历史"}){
      Button b=button(item);nav.addView(b,new LinearLayout.LayoutParams(0,dp(52),1));
      b.setOnClickListener(v->{if(item.equals("力量"))record();else if(item.equals("瑜伽"))yoga();else if(item.equals("趋势"))progress();else history();});
    }
    add(root,nav);add(root,label(title,20));
  }
  void record(){
    start("今天 · "+today);
    HashMap<String,String> existing=db.get(today);
    for(int e=0;e<3;e++){
      add(root,label(NAMES[e],17));
      JSONArray saved=null;
      try{saved=new JSONArray(existing.getOrDefault("s"+e,"[]"));}catch(Exception ignored){}
      for(int k=0;k<3;k++){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
        TextView name=label("第 "+(k+1)+" 组",15);
        row.addView(name,new LinearLayout.LayoutParams(0,dp(50),1));
        EditText input=new EditText(this);input.setSingleLine(true);
        input.setInputType(2);input.setGravity(Gravity.CENTER);
        input.setText(saved!=null&&k<saved.length()?String.valueOf(saved.optInt(k,0)):"0");
        counts[e][k]=input;
        row.addView(input,new LinearLayout.LayoutParams(dp(110),dp(50)));add(root,row);
      }
    }
    add(root,label("关节状况",17));
    popping=new CheckBox(this);popping.setText("有弹响");popping.setChecked("1".equals(existing.get("popping")));add(root,popping);
    pain=new CheckBox(this);pain.setText("有疼痛、肿胀或不稳定");pain.setChecked("1".equals(existing.get("pain")));add(root,pain);
    notes=new EditText(this);notes.setHint("备注：训练感觉、恢复、动作问题");notes.setMinLines(2);notes.setText(existing.getOrDefault("note",""));add(root,notes);
    Button save=button("保存今日训练");add(root,save);
    save.setOnClickListener(v->save());
    add(root,label("每组量力而行，动作质量优先；疼痛、肿胀或卡住时停止相应动作。",12));
  }
  void save(){
    JSONObject data=new JSONObject();int total=0;
    try{
      for(int i=0;i<3;i++){
        JSONArray a=new JSONArray();
        for(int j=0;j<3;j++){
          String raw=counts[i][j].getText().toString().trim();
          int num=raw.isEmpty()?0:Integer.parseInt(raw);
          if(num<0||num>999)throw new IllegalArgumentException("次数必须在0到999之间");
          a.put(num);total+=num;
        }
        data.put("s"+i,a.toString());
      }
      if(total==0){toast("请至少记录一次动作");return;}
      data.put("popping",popping.isChecked()?"1":"0");
      data.put("pain",pain.isChecked()?"1":"0");
      data.put("note",notes.getText().toString());
      Runnable action=()->{db.put(today,data);toast("已保存");progress();};
      if(db.exists(today))new AlertDialog.Builder(this).setTitle("覆盖今天的记录？")
        .setMessage("新记录将替换今天原有数据。")
        .setNegativeButton("取消",null).setPositiveButton("确定",(d,w)->action.run()).show();
      else action.run();
    }catch(Exception ex){toast("输入不正确："+ex.getMessage());}
  }
  void yoga(){
    start("艾扬格基础 · "+today);
    add(root,label("建议每周2～3次，约15～20分钟；以动作稳定、自然呼吸为目标，不追求更深的拉伸。",14));
    add(root,label("这是基础居家序列，不是认证教师指导。自学阶段暂不安排头倒立和肩倒立。",13));
    HashMap<String,String> old=db.getYoga(today);
    JSONArray completed=null;
    try{completed=new JSONArray(old.getOrDefault("completed","[]"));}catch(Exception ignored){}
    for(int i=0;i<YOGA_NAMES.length;i++){
      CheckBox c=new CheckBox(this);c.setText((i+1)+". "+YOGA_NAMES[i]);
      c.setTextSize(17);c.setTextColor(DARK);c.setChecked(completed!=null&&completed.optBoolean(i,false));
      yogaChecks[i]=c;add(root,c);
      TextView tip=label(YOGA_HINTS[i],13);tip.setTextColor(MUTED);tip.setPadding(dp(28),0,0,dp(13));add(root,tip);
    }
    add(root,label("今天实际练习多少分钟？",15));
    yogaMinutes=new EditText(this);yogaMinutes.setInputType(2);yogaMinutes.setSingleLine(true);
    yogaMinutes.setText(old.getOrDefault("minutes","20"));add(root,yogaMinutes);
    yogaPain=new CheckBox(this);yogaPain.setText("出现关节疼痛、头晕、麻木等不适");yogaPain.setChecked("1".equals(old.get("pain")));add(root,yogaPain);
    yogaNotes=new EditText(this);yogaNotes.setHint("动作观察：左右差异、稳定性、呼吸、使用的瑜伽砖等");
    yogaNotes.setMinLines(2);yogaNotes.setText(old.getOrDefault("note",""));add(root,yogaNotes);
    Button save=button("保存今日瑜伽");add(root,save);save.setOnClickListener(v->saveYoga());
    add(root,label("最近28天完成 "+db.yogaCount(28)+" 次；短期目标：稳定每周2次，持续12周。",14));
    add(root,label("如果某动作疼痛或让你无法自然呼吸，及时退出或降低难度；不要靠强撑计时。",13));
    Button csv=button("导出瑜伽记录CSV");add(root,csv);csv.setOnClickListener(v->exportYoga());
  }
  void saveYoga(){
    int total=0;JSONArray items=new JSONArray();
    for(CheckBox c:yogaChecks){boolean checked=c!=null&&c.isChecked();items.put(checked);if(checked)total++;}
    if(total==0){toast("请至少完成一个体式后再保存");return;}
    int minutes;
    try{minutes=Integer.parseInt(yogaMinutes.getText().toString().trim());}
    catch(Exception e){toast("请输入有效的分钟数");return;}
    if(minutes<1||minutes>240){toast("练习分钟数应在1～240之间");return;}
    JSONObject j=new JSONObject();
    try{
      j.put("completed",items.toString());j.put("minutes",minutes);
      j.put("pain",yogaPain.isChecked()?"1":"0");j.put("note",yogaNotes.getText().toString());
    }catch(Exception ex){toast("保存失败："+ex.getMessage());return;}
    Runnable save=()->{db.putYoga(today,j);toast("已保存瑜伽记录");yoga();};
    if(db.hasYoga(today))new AlertDialog.Builder(this).setTitle("覆盖今天的瑜伽记录？")
      .setMessage("今天的旧瑜伽数据会被替换。")
      .setNegativeButton("取消",null).setPositiveButton("确定",(d,w)->save.run()).show();
    else save.run();
  }
  void exportYoga(){
    Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
    i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/csv");
    i.putExtra(Intent.EXTRA_TITLE,"瑜伽记录.csv");startActivityForResult(i,23);
  }
  void writeYogaCsv(Uri uri){
    StringBuilder out=new StringBuilder("date,pose,completed,minutes,pain,note\n");
    for(HashMap<String,String> r:db.allYoga()){
      JSONArray a;
      try{a=new JSONArray(r.get("completed"));}catch(Exception e){continue;}
      for(int i=0;i<YOGA_NAMES.length;i++)if(a.optBoolean(i,false)){
        out.append(csv(r.get("day"))).append(',').append(csv(YOGA_NAMES[i])).append(",1,");
        out.append(r.getOrDefault("minutes","")).append(',');
        out.append(csv(r.get("pain"))).append(',').append(csv(r.get("note"))).append('\n');
      }
    }
    try(OutputStream os=getContentResolver().openOutputStream(uri)){
      if(os==null)throw new IOException("无法打开输出文件");
      os.write(new byte[]{(byte)0xef,(byte)0xbb,(byte)0xbf});
      os.write(out.toString().getBytes(StandardCharsets.UTF_8));toast("瑜伽记录已导出");
    }catch(Exception ex){toast("导出失败："+ex.getMessage());}
  }
  void progress(){
    start("训练趋势");
    ArrayList<HashMap<String,String>> all=db.all();
    add(root,label("共记录 "+all.size()+" 个训练日。曲线为每次训练的最佳单组次数。",14));
    LinearLayout row=new LinearLayout(this);
    for(int i=0;i<3;i++){final int k=i;Button b=button(NAMES[i]);row.addView(b,new LinearLayout.LayoutParams(0,dp(50),1));b.setOnClickListener(v->{exercise=k;progress();});}
    add(root,row);add(root,label("当前项目："+NAMES[exercise],16));
    Graph graph=new Graph(this,all,exercise);add(root,graph);
    add(root,label("参考目标：引体向上从5次逐步提高到8次。不要为了次数牺牲动作质量。",13));
    add(root,label("瑜伽：最近28天练习 "+db.yogaCount(28)+" 次；目标每周2次（28天约8次）。",14));
    Button export=button("导出CSV备份");add(root,export);export.setOnClickListener(v->export());
  }
  void history(){
    start("历史记录");
    ArrayList<HashMap<String,String>> all=db.all();
    if(all.isEmpty())add(root,label("尚无训练记录",15));
    for(HashMap<String,String> r:all){
      add(root,label("日期："+r.get("day"),17));
      for(int i=0;i<3;i++)add(root,label(NAMES[i]+"："+format(r.get("s"+i)),14));
      String info="弹响："+("1".equals(r.get("popping"))?"是":"否")+"，疼痛/不适："+("1".equals(r.get("pain"))?"是":"否");
      add(root,label(info,13));
      if(!r.getOrDefault("note","").isEmpty())add(root,label("备注："+r.get("note"),13));
      Button del=button("删除此日（需确认）");add(root,del);String date=r.get("day");
      del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("删除 "+date+"？")
        .setMessage("删除后无法恢复，请考虑先导出CSV。")
        .setNegativeButton("取消",null).setPositiveButton("删除",(d,w)->{db.delete(date);history();}).show());
    }
    for(HashMap<String,String> y:db.allYoga()){
      add(root,label("瑜伽 "+y.get("day")+" · "+y.get("minutes")+"分钟",16));
      JSONArray a;
      try{a=new JSONArray(y.get("completed"));}catch(Exception ex){a=new JSONArray();}
      ArrayList<String> done=new ArrayList<>();
      for(int i=0;i<YOGA_NAMES.length;i++)if(a.optBoolean(i,false))done.add(YOGA_NAMES[i]);
      add(root,label("体式："+android.text.TextUtils.join("、",done),13));
      add(root,label("不适："+("1".equals(y.get("pain"))?"有":"无"),13));
      if(!y.getOrDefault("note","").isEmpty())add(root,label("观察："+y.get("note"),13));
    }
    Button b=button("导出CSV备份");add(root,b);b.setOnClickListener(v->export());
    Button y=button("导出瑜伽CSV");add(root,y);y.setOnClickListener(v->exportYoga());
  }
  String format(String raw){try{JSONArray a=new JSONArray(raw);ArrayList<String> parts=new ArrayList<>();for(int i=0;i<a.length();i++)if(a.optInt(i)>0)parts.add(String.valueOf(a.optInt(i)));return parts.isEmpty()?"未记录":android.text.TextUtils.join(" / ",parts);}catch(Exception e){return "未记录";}}
  void export(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/csv");i.putExtra(Intent.EXTRA_TITLE,"训练记录.csv");startActivityForResult(i,22);}
  @Override protected void onActivityResult(int req,int result,Intent data){
    super.onActivityResult(req,result,data);
    if(result!=RESULT_OK||data==null||data.getData()==null)return;
    if(req==23){writeYogaCsv(data.getData());return;}
    if(req!=22)return;
    StringBuilder out=new StringBuilder("date,exercise,set,reps,popping,pain,note\n");
    for(HashMap<String,String> r:db.all()){
      for(int e=0;e<3;e++)try{JSONArray a=new JSONArray(r.get("s"+e));
        for(int j=0;j<a.length();j++)if(a.optInt(j)>0){
          out.append(csv(r.get("day"))).append(',').append(csv(NAMES[e])).append(',')
            .append(j+1).append(',').append(a.optInt(j)).append(',')
            .append(csv(r.get("popping"))).append(',').append(csv(r.get("pain"))).append(',')
            .append(csv(r.get("note"))).append('\n');
        }
      }catch(Exception ignored){}
    }
    try(OutputStream os=getContentResolver().openOutputStream(data.getData())){
      if(os==null)throw new IOException("无法打开输出文件");
      os.write(new byte[]{(byte)0xef,(byte)0xbb,(byte)0xbf});
      os.write(out.toString().getBytes(StandardCharsets.UTF_8));toast("导出完成");
    }catch(Exception ex){toast("导出失败："+ex.getMessage());}
  }
  String csv(String s){return "\"" +(s==null?"":s.replace("\"","\"\""))+"\"";}
  void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
  @Override protected void onDestroy(){db.close();super.onDestroy();}

  static class DB extends SQLiteOpenHelper {
    DB(Context c){super(c,"trainlog.db",null,2);}
    @Override public void onCreate(SQLiteDatabase d){
      d.execSQL("CREATE TABLE training (day TEXT PRIMARY KEY,s0 TEXT,s1 TEXT,s2 TEXT,popping TEXT,pain TEXT,note TEXT)");
      d.execSQL("CREATE TABLE yoga (day TEXT PRIMARY KEY,completed TEXT,minutes TEXT,pain TEXT,note TEXT)");
    }
    @Override public void onUpgrade(SQLiteDatabase d,int a,int b){
      if(a<2)d.execSQL("CREATE TABLE IF NOT EXISTS yoga (day TEXT PRIMARY KEY,completed TEXT,minutes TEXT,pain TEXT,note TEXT)");
    }
    boolean exists(String day){try(Cursor c=getReadableDatabase().rawQuery("SELECT day FROM training WHERE day=?",new String[]{day})){return c.moveToFirst();}}
    HashMap<String,String> get(String day){
      try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM training WHERE day=?",new String[]{day})){
        if(c.moveToFirst())return row(c);
      }
      return new HashMap<>();
    }
    void put(String day,JSONObject j){
      ContentValues v=new ContentValues();v.put("day",day);
      for(String key:new String[]{"s0","s1","s2","popping","pain","note"})v.put(key,j.optString(key,""));
      getWritableDatabase().insertWithOnConflict("training",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    void delete(String day){getWritableDatabase().delete("training","day=?",new String[]{day});}
    ArrayList<HashMap<String,String>> all(){
      ArrayList<HashMap<String,String>> result=new ArrayList<>();
      try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM training ORDER BY day DESC",null)){
        while(c.moveToNext())result.add(row(c));
      }
      return result;
    }
    boolean hasYoga(String day){
      try(Cursor c=getReadableDatabase().rawQuery("SELECT day FROM yoga WHERE day=?",new String[]{day})){return c.moveToFirst();}
    }
    HashMap<String,String> getYoga(String day){
      try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM yoga WHERE day=?",new String[]{day})){
        if(c.moveToFirst())return row(c);
      }
      return new HashMap<>();
    }
    void putYoga(String day,JSONObject j){
      ContentValues v=new ContentValues();v.put("day",day);
      for(String key:new String[]{"completed","minutes","pain","note"})v.put(key,j.optString(key,""));
      getWritableDatabase().insertWithOnConflict("yoga",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    ArrayList<HashMap<String,String>> allYoga(){
      ArrayList<HashMap<String,String>> result=new ArrayList<>();
      try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM yoga ORDER BY day DESC",null)){
        while(c.moveToNext())result.add(row(c));
      }
      return result;
    }
    int yogaCount(int days){
      Calendar cal=Calendar.getInstance();cal.add(Calendar.DAY_OF_YEAR,-days+1);
      String since=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.getTime());
      try(Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM yoga WHERE day>=?",new String[]{since})){
        return c.moveToFirst()?c.getInt(0):0;
      }
    }
    HashMap<String,String> row(Cursor c){
      HashMap<String,String> m=new HashMap<>();
      for(int i=0;i<c.getColumnCount();i++)m.put(c.getColumnName(i),c.isNull(i)?"":c.getString(i));
      return m;
    }
  }

  static class Graph extends View{
    Paint paint=new Paint(3);
    ArrayList<Integer> values=new ArrayList<>();
    float density;
    Graph(Context c,ArrayList<HashMap<String,String>> rows,int exercise){
      super(c);density=getResources().getDisplayMetrics().density;
      for(int i=rows.size()-1;i>=0;i--){try{JSONArray arr=new JSONArray(rows.get(i).get("s"+exercise));int max=0;
        for(int j=0;j<arr.length();j++)max=Math.max(max,arr.optInt(j));if(max>0)values.add(max);
      }catch(Exception ignored){}}
      if(values.size()>30)values=new ArrayList<>(values.subList(values.size()-30,values.size()));
    }
    @Override protected void onMeasure(int w,int h){setMeasuredDimension(MeasureSpec.getSize(w),(int)(240*density));}
    @Override protected void onDraw(Canvas c){
      super.onDraw(c);float w=getWidth(),h=getHeight(),l=36*density,r=w-18*density,t=20*density,b=h-34*density;
      paint.setStyle(Paint.Style.FILL);paint.setTextSize(13*density);paint.setColor(MUTED);
      if(values.isEmpty()){c.drawText("保存训练后显示折线图",l,h/2,paint);return;}
      int max=5;for(int value:values)max=Math.max(max,value);max=((max+4)/5)*5;
      for(int i=0;i<=4;i++){
        float y=b-(b-t)*i/4f;paint.setColor(0xffdce5e0);paint.setStrokeWidth(density);c.drawLine(l,y,r,y,paint);
        paint.setColor(MUTED);c.drawText(String.valueOf(max*i/4),2*density,y,paint);
      }
      Path line=new Path();int n=values.size();
      for(int i=0;i<n;i++){float x=n==1?(l+r)/2:l+(r-l)*i/(n-1);float y=b-(b-t)*values.get(i)/max;
        if(i==0)line.moveTo(x,y);else line.lineTo(x,y);
      }
      paint.setColor(GREEN);paint.setStrokeWidth(3*density);paint.setStyle(Paint.Style.STROKE);c.drawPath(line,paint);
      paint.setStyle(Paint.Style.FILL);
      for(int i=0;i<n;i++){float x=n==1?(l+r)/2:l+(r-l)*i/(n-1);float y=b-(b-t)*values.get(i)/max;c.drawCircle(x,y,3.5f*density,paint);}
      paint.setColor(MUTED);paint.setTextSize(12*density);c.drawText("最近 "+n+" 次有效训练",l,b+24*density,paint);
    }
  }
}
