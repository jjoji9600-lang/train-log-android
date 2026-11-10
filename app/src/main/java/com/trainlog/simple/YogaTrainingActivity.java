package com.trainlog.simple;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.content.ContentValues;
import android.os.Bundle;
import android.text.InputType;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Two complementary practice modes:
 *   1) Independent Iyengar practice based on the 20-week course index
 *      in 《艾扬格瑜伽》, printed pp.410-423 (PDF pages 412-425).
 *   2) Short post-strength recovery plans editorially adapted from the
 *      supported/restorative techniques in both user-supplied books.
 *
 * This app's pose-title checklist is a working index, NOT a word-for-word
 * copy of each printed table (especially the later weeks, which have
 * numerous props/variants). Original book pages always take precedence.
 */
public final class YogaTrainingActivity extends Activity {
  static final int INK=0xff233730, MUTED=0xff63776e, GREEN=0xff176b59;
  static final String[] FEELINGS={"舒适、呼吸自然","部分动作需要调整","比较疲劳，已降低强度","出现疼痛或其他不适"};
  static final String[] FOCUS={"上肢训练后 · 12分钟","下肢训练后 · 15分钟","全身训练后 · 15分钟"};
  static final String[] WEEK_TITLES={
    "靠墙山式","上伸手山式","十指相交山式","双手背后相叠山式","牛面式手臂山式",
    "三角伸展式","三角侧伸展式","战士一式","战士二式","半月式",
    "加强胸部伸展式","下犬式","加强腿部伸展式","站立前屈式","俯身加强背部伸展式",
    "手杖式","英雄式","仰卧屈膝式","吉祥坐","束角式","束角坐式","完全船式",
    "俯身英雄式","俯身盘腿式","辅助加强背部伸展式","头碰膝前屈式",
    "加强背部伸展式","巴拉瓦伽扭转式（椅侧）","巴拉瓦伽扭转式（跨椅背）",
    "巴拉瓦伽扭转式（坐姿）","玛里琪扭转式","加强玛里琪扭转式","英雄坐侧扭转式",
    "辅助倒手杖式","辅助骆驼式","仰卧束角式","卧英雄式",
    "仰卧手抓脚趾伸展式（带）","仰卧手抓脚趾伸展式（砖）",
    "头倒立（靠墙）","辅助肩倒立","辅助犁式","辅助桥式","倒剪式","挺尸式",
    "乌加依呼吸","间断呼吸控制法"
  };
  // The short lists below have been cross-checked against the first six
  // printed weekly tables. Later week checklists are an abbreviated index,
  // clearly marked as such, while the book's full tables remain authoritative.
  static final String[][] FIRST_WEEKS={
    {
      "靠墙山式","上伸手山式","十指相交山式","站立前屈式","下犬式","手杖式",
      "英雄式","俯身英雄式","辅助加强背部伸展式","巴拉瓦伽扭转式（坐姿）","辅助桥式","挺尸式"
    },
    {
      "靠墙山式","上伸手山式","十指相交山式","双手背后相叠山式","三角伸展式",
      "站立前屈式","下犬式","手杖式","英雄式","仰卧屈膝式","束角式",
      "俯身英雄式","辅助加强背部伸展式","巴拉瓦伽扭转式（椅侧）",
      "仰卧束角式","辅助桥式","挺尸式"
    },
    {
      "靠墙山式","上伸手山式","十指相交山式","双手背后相叠山式","三角伸展式",
      "站立前屈式","下犬式","手杖式","英雄式","仰卧屈膝式","束角式",
      "俯身英雄式","辅助加强背部伸展式","巴拉瓦伽扭转式（椅侧）",
      "加强玛里琪扭转式","仰卧束角式","辅助桥式","挺尸式"
    },
    {
      "靠墙山式","上伸手山式","十指相交山式","双手背后相叠山式","牛面式手臂山式",
      "三角伸展式","三角侧伸展式","站立前屈式","下犬式","手杖式",
      "英雄式","仰卧屈膝式","吉祥坐","束角式","束角坐式","俯身英雄式",
      "辅助加强背部伸展式","头碰膝前屈式","加强背部伸展式",
      "巴拉瓦伽扭转式（椅侧）","巴拉瓦伽扭转式（跨椅背）","加强玛里琪扭转式",
      "英雄坐侧扭转式","仰卧束角式","仰卧手抓脚趾伸展式（带）","辅助桥式","挺尸式"
    },
    {
      "靠墙山式","上伸手山式","十指相交山式","双手背后相叠山式","牛面式手臂山式",
      "三角伸展式","三角侧伸展式","战士一式","战士二式","下犬式",
      "加强腿部伸展式","站立前屈式","手杖式","英雄式","仰卧屈膝式","吉祥坐",
      "束角式","束角坐式","俯身英雄式","俯身盘腿式","辅助加强背部伸展式",
      "头碰膝前屈式","加强背部伸展式","巴拉瓦伽扭转式（椅侧）",
      "巴拉瓦伽扭转式（跨椅背）","巴拉瓦伽扭转式（坐姿）","加强玛里琪扭转式",
      "英雄坐侧扭转式","仰卧束角式","仰卧手抓脚趾伸展式（带）",
      "仰卧手抓脚趾伸展式（砖）","辅助桥式","挺尸式"
    },
    {
      "靠墙山式","上伸手山式","十指相交山式","双手背后相叠山式","牛面式手臂山式",
      "三角伸展式","三角侧伸展式","战士一式","战士二式","半月式","下犬式",
      "加强腿部伸展式","站立前屈式","俯身加强背部伸展式",
      "手杖式","英雄式","仰卧屈膝式","吉祥坐","束角式","束角坐式","完全船式",
      "俯身英雄式","俯身盘腿式","辅助加强背部伸展式","头碰膝前屈式",
      "加强背部伸展式","巴拉瓦伽扭转式（椅侧）",
      "巴拉瓦伽扭转式（跨椅背）","巴拉瓦伽扭转式（坐姿）",
      "玛里琪扭转式","加强玛里琪扭转式","英雄坐侧扭转式",
      "仰卧束角式","卧英雄式","仰卧手抓脚趾伸展式（带）",
      "仰卧手抓脚趾伸展式（砖）","辅助桥式","倒剪式","挺尸式"
    }
  };
  static final int[] COURSE_SIZES={12,17,18,27,33,39,41,41,41,42,42,42,43,43,45,46,46,46,46,47};
  static final String[][] RECOVERY={
    {
      "靠墙山式｜约1分钟",
      "扶墙半下犬式｜2次，每次约30秒",
      "手杖式（坐垫支撑）｜约1分钟",
      "仰卧束角式（枕头和膝下支撑）｜约3分钟",
      "挺尸式（舒适仰卧）｜约4分钟"
    },
    {
      "靠墙山式｜约1分钟",
      "仰卧手抓脚趾伸展式（瑜伽带）｜左右各约30秒",
      "辅助束角式｜约2分钟",
      "温和坐姿前屈（垫高坐骨）｜约1分钟",
      "挺尸式｜约5分钟"
    },
    {
      "靠墙山式｜约1分钟",
      "扶墙半下犬式｜2次，每次约30秒",
      "辅助坐姿扭转（小幅度）｜左右各约30秒",
      "仰卧束角式（充分支撑）｜约3分钟",
      "挺尸式｜约5分钟"
    }
  };
  Store store;
  LinearLayout root;
  TextView selectedLabel;
  final HashSet<Integer> done=new HashSet<>();
  EditText minutes, notes;
  Spinner feeling;
  String mode="after",planId="";
  String[] currentItems;
  int week=1,afterChoice=0;
  String day=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());

  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
  @Override public void onCreate(Bundle state){
    super.onCreate(state);store=new Store(this);
    week=getPreferences(0).getInt("week",1);
    if(getIntent().getBooleanExtra("after_strength",false))after();else home();
  }
  TextView text(String t,int size){
    TextView v=new TextView(this);v.setText(t);v.setTextColor(INK);
    v.setTextSize(size);v.setPadding(0,dp(6),0,dp(6));return v;
  }
  Button button(String t,Runnable r){
    Button b=new Button(this);b.setText(t);b.setAllCaps(false);b.setTextColor(GREEN);
    b.setOnClickListener(v->r.run());return b;
  }
  void add(View v){root.addView(v,new LinearLayout.LayoutParams(-1,-2));}
  void screen(String title){
    ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xfff6f7f5);
    root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(16),dp(14),dp(16),dp(36));
    scroll.addView(root);setContentView(scroll);
    TextView head=text(title,23);head.setTypeface(null,Typeface.BOLD);add(head);
  }
  void home(){
    mode="home";screen("瑜伽训练");
    add(text("两种模式共用体式参考资料，练完再记录。每天可以记多次，不会覆盖旧记录。",14));
    add(button("力量训练后 · 10～20分钟",this::after));
    add(text("依据当次练习负荷选择温和恢复，不做新的力量训练。",13));
    add(button("独立瑜伽训练 · 原书20周课程",this::weeks));
    add(text("同一周可以重复任意次数；不自动升级到下一周。",13));
    add(button("查看瑜伽训练历史",this::history));
    add(text("资料：《艾扬格瑜伽》原书课程表（纸书410～423页）；《精进习练指南》用于查询方法和辅助工具。",12));
  }
  void after(){
    mode="after";screen("力量训练后 · 温和恢复");
    add(button("← 返回瑜伽首页",this::home));
    add(text("App自编短课，不是原书官方“训练后”课程。疲劳、疼痛或眩晕时可直接休息，不强行拉伸。",13));
    int suggested=suggestFromStrength();
    if(suggested>=0){
      afterChoice=suggested;
      add(text("已参考今天保存的力量记录，优先显示："+FOCUS[suggested],13));
    }
    LinearLayout bar=new LinearLayout(this);
    for(int i=0;i<3;i++){
      final int idx=i;Button b=button(new String[]{"上肢","下肢","全身"}[i],()->{afterChoice=idx;after();});
      bar.addView(b,new LinearLayout.LayoutParams(0,dp(51),1));
    }
    add(bar);
    add(text("当前方案："+FOCUS[afterChoice],17));
    add(text("可自行跳过不舒服的动作；辅助工具需要稳定支撑。",13));
    buildChecklist(RECOVERY[afterChoice],"after-"+afterChoice);
  }
  int suggestFromStrength(){
    MainActivity.DB db=new MainActivity.DB(this);
    try{
      HashMap<String,String> r=db.get(day);
      int upper=count(r.get("s0"))+count(r.get("s1"));
      int lower=count(r.get("s2"));
      if(upper>0 && lower>0)return 2;
      if(lower>0)return 1;
      if(upper>0)return 0;
      return -1;
    }finally{db.close();}
  }
  int count(String raw){
    int n=0;try{JSONArray a=new JSONArray(raw);for(int i=0;i<a.length();i++)n+=Math.max(0,a.optInt(i));}catch(Exception ignored){}
    return n;
  }
  void weeks(){
    mode="weeks";screen("独立瑜伽 · 20周课程");
    add(button("← 返回瑜伽首页",this::home));
    add(text("原书课程表：纸书第410～423页（扫描PDF约第412～425页）。课程里有较多辅助工具、倒置与长序列。",13));
    add(text("第1～6周按原书顺序整理为可勾选清单；第7～20周是辅助记录用的体式索引摘要，完整顺序、辅助工具和增减仍以对应书页为准。",13));
    add(text("当前练习周次："+week+" / 20",18));
    LinearLayout changes=new LinearLayout(this);changes.setGravity(Gravity.CENTER);
    changes.addView(button("上一周",()->{week=Math.max(1,week-1);persistWeek();weeks();}),new LinearLayout.LayoutParams(0,dp(50),1));
    changes.addView(button("下一周",()->nextWeek()),new LinearLayout.LayoutParams(0,dp(50),1));
    add(changes);
    add(text("原书课程表所在页："+bookPages(week)+"；如果时间不足，可以只练一部分并记录。",13));
    if(week>=7)add(text("重要：本周勾选列表是基于体式名称整理的便捷索引，不代表书中每一项工具、变式和顺序。实际练习以原书课程表为准。",13));
    add(button("开始记录第"+week+"周",()->openWeek(week)));
    add(text("建议反复练习当前周，直到能够自然呼吸、稳定进入和退出动作；不必按日历强制升级。",13));
    add(button("查看同周历史记录",this::history));
  }
  String bookPages(int n){
    final String[] refs={"410","410","410","411","411～412","412","413","413～414","414～415","415","416","416～417","417～418","418～419","419","420","420～421","421～422","422～423","423"};
    return refs[Math.max(1,Math.min(20,n))-1];
  }
  void nextWeek(){
    LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(16),dp(10),dp(16),0);
    CheckBox known=new CheckBox(this);known.setText("已经熟悉本周的主要体式与辅助工具");
    CheckBox steady=new CheckBox(this);steady.setText("动作进出稳定，能自然呼吸");
    CheckBox recovery=new CheckBox(this);recovery.setText("没有持续疼痛或异常不适");
    l.addView(known);l.addView(steady);l.addView(recovery);
    new AlertDialog.Builder(this).setTitle("确认进入下一周？")
      .setMessage("这是自评提醒，不代表复杂倒立或其他高难度动作已经安全。")
      .setView(l).setNegativeButton("继续当前周",null)
      .setPositiveButton("确认升级",(d,w)->{
        if(known.isChecked()&&steady.isChecked()&&recovery.isChecked()){
          week=Math.min(20,week+1);persistWeek();weeks();
        }else Toast.makeText(this,"建议先继续当前周练习",Toast.LENGTH_LONG).show();
      }).show();
  }
  void persistWeek(){getPreferences(0).edit().putInt("week",week).apply();}
  String[] weekItems(int num){
    if(num<=FIRST_WEEKS.length)return FIRST_WEEKS[num-1];
    int length=COURSE_SIZES[num-1];
    return Arrays.copyOf(WEEK_TITLES,Math.min(length,WEEK_TITLES.length));
  }
  void openWeek(int n){
    mode="week";screen("原书课程 · 第"+n+"周");
    add(button("← 返回20周课程",this::weeks));
    add(text("原书对应纸书第"+bookPages(n)+"页；部分周次序列包含30～47项，并非要求一次全部做完。",13));
    if(n>=7)add(text("这是速记用的体式索引，书中的辅助工具、变式及实际位置仍需参照原书表格。",13));
    add(text("倒置或颈部承重体式请先由合格教师评估，不因列在课程中就自行练习。",13));
    buildChecklist(weekItems(n),"week-"+n);
  }
  void buildChecklist(String[] items,String id){
    planId=id;currentItems=items;done.clear();
    selectedLabel=text("已完成：0 / "+items.length,16);add(selectedLabel);
    for(int i=0;i<items.length;i++){
      final int index=i;
      String name=items[i];
      CheckBox box=new CheckBox(this);box.setText((i+1)+". "+name);
      box.setTextSize(15);box.setTextColor(INK);
      if(name.contains("倒立")||name.contains("犁式")||name.contains("头倒立"))box.setTextColor(0xff996239);
      box.setPadding(0,dp(3),0,dp(3));
      box.setOnCheckedChangeListener((v,yes)->{
        if(yes)done.add(index);else done.remove(index);
        selectedLabel.setText("已完成："+done.size()+" / "+items.length);
      });
      add(box);
      if(i<5||i%7==0){
        String hint=poseHint(name);
        if(hint!=null){TextView small=text(hint,12);small.setTextColor(MUTED);add(small);}
      }
    }
    add(text("练习时长（分钟，可不填）",14));
    minutes=new EditText(this);minutes.setInputType(InputType.TYPE_CLASS_NUMBER);
    minutes.setHint("如 15");add(minutes);
    add(text("练后感觉",14));
    feeling=new Spinner(this);
    ArrayAdapter<String> choices=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,FEELINGS);
    feeling.setAdapter(choices);add(feeling);
    notes=new EditText(this);notes.setMinLines(2);notes.setHint("可选：哪边紧？哪里代偿？有没有使用辅助工具？");add(notes);
    add(button("保存本次训练记录",this::save));
    add(text("每次点击保存都会新建记录；同一天练两次也不会覆盖。",12));
  }
  String poseHint(String name){
    if(name.contains("山式"))return "注意足部均匀受力、脊柱延长；参考《精进习练指南》山式。";
    if(name.contains("三角"))return "手可借助瑜伽砖；稳定腿部后再延长躯干。";
    if(name.contains("下犬"))return "肩腕有不适时可改扶墙版本，避免强压脚跟。";
    if(name.contains("束角"))return "可用支撑垫高膝盖，避免强压髋或膝。";
    if(name.contains("挺尸"))return "自然呼吸、放松；不必强迫固定呼吸次数。";
    return null;
  }
  void save(){
    if(done.isEmpty()){Toast.makeText(this,"请至少勾选一个实际完成动作",Toast.LENGTH_SHORT).show();return;}
    String raw=minutes.getText().toString().trim();int m=0;
    if(!raw.isEmpty())try{m=Integer.parseInt(raw);}catch(Exception e){Toast.makeText(this,"分钟数不正确",Toast.LENGTH_SHORT).show();return;}
    if(m<0||m>240){Toast.makeText(this,"分钟数应在0～240之间",Toast.LENGTH_SHORT).show();return;}
    ArrayList<Integer> keys=new ArrayList<>(done);Collections.sort(keys);
    JSONArray arr=new JSONArray();for(Integer k:keys)arr.put(currentItems[k]);
    String name=planId.startsWith("week-")?"第"+planId.substring(5)+"周课程":FOCUS[afterChoice];
    store.insert(day,planId,name,arr.toString(),m,FEELINGS[feeling.getSelectedItemPosition()],notes.getText().toString());
    Toast.makeText(this,"已保存本次瑜伽练习",Toast.LENGTH_SHORT).show();home();
  }
  void history(){
    mode="history";screen("瑜伽训练历史");
    add(button("← 返回瑜伽首页",this::home));
    try(Cursor c=store.getReadableDatabase().rawQuery(
        "SELECT id,day,plan_name,items,minutes,feeling,note FROM sessions ORDER BY id DESC LIMIT 70",null)){
      if(c.getCount()==0)add(text("暂无记录",14));
      while(c.moveToNext()){
        final long id=c.getLong(0);
        String d=c.getString(1),name=c.getString(2),raw=c.getString(3);
        int count=0;StringBuilder doneText=new StringBuilder();
        try{
          JSONArray arr=new JSONArray(raw);count=arr.length();
          for(int j=0;j<arr.length();j++)doneText.append(j==0?"":"、").append(arr.optString(j));
        }catch(Exception ignored){}
        add(text(d+" · "+name+" · "+count+"项 · "+c.getInt(4)+"分钟",16));
        add(text(doneText.toString(),13));
        add(text("感受："+c.getString(5),13));
        if(!c.getString(6).isEmpty())add(text("备注："+c.getString(6),13));
        add(button("删除这次记录（需确认）",()->new AlertDialog.Builder(this)
          .setTitle("删除该次训练记录？").setMessage("不可恢复，其他训练记录不会受影响。")
          .setNegativeButton("取消",null).setPositiveButton("删除",(dia,w)->{store.delete(id);history();}).show()));
    }
    }
  }
  @Override public void onBackPressed(){
    if("week".equals(mode))weeks();
    else if(!"home".equals(mode))home();
    else super.onBackPressed();
  }
  @Override protected void onDestroy(){store.close();super.onDestroy();}
  static class Store extends SQLiteOpenHelper {
    Store(Context c){super(c,"trainlog_yoga_sessions.db",null,1);}
    @Override public void onCreate(SQLiteDatabase d){
      d.execSQL("CREATE TABLE sessions (id INTEGER PRIMARY KEY AUTOINCREMENT,day TEXT NOT NULL,plan_id TEXT NOT NULL,plan_name TEXT NOT NULL,items TEXT NOT NULL,minutes INTEGER NOT NULL,feeling TEXT NOT NULL,note TEXT NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase d,int old,int now){}
    void insert(String day,String pid,String name,String done,int min,String feel,String note){
      ContentValues v=new ContentValues();
      v.put("day",day);v.put("plan_id",pid);v.put("plan_name",name);v.put("items",done);
      v.put("minutes",min);v.put("feeling",feel);v.put("note",note);
      getWritableDatabase().insertOrThrow("sessions",null,v);
    }
    void delete(long id){getWritableDatabase().delete("sessions","id=?",new String[]{String.valueOf(id)});}
  }
}
