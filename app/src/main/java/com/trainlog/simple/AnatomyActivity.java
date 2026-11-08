package com.trainlog.simple;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * An anatomy-first companion based on the user's 解剖.docx (chapters 6-11).
 * Practice sequences are editorially constructed, NOT sequences from that book.
 */
public final class AnatomyActivity extends Activity {
  static final class Move {
    final String id,cn,latin,group,why,how,breath;
    Move(String id,String cn,String latin,String group,String why,String how,String breath){
      this.id=id;this.cn=cn;this.latin=latin;this.group=group;
      this.why=why;this.how=how;this.breath=breath;
    }
  }
  static final Move[] MOVES={
    new Move("tadasana","山式","Tadasana","站式",
      "原书解剖：足底通过脚跟、第一与第五跖骨形成三点支撑；足弓及足部肌肉参与维持站立稳定。",
      "App练习建议：站稳双脚，观察重量是否偏到脚跟或前掌；不要刻意绷紧全身。",
      "书中指出：双脚、双腿与脊柱获得稳定支撑时，胸廓和肩带可更自由地参与呼吸。"),
    new Move("utkatasana","椅式","Utkatasana","站式",
      "原书解剖：重力是主要阻力，骨盆过度前倾或腰椎过度拱起均可能发生。",
      "App练习建议：屈髋屈膝下坐到能控制的位置；膝盖顺脚尖方向，保持下背不过度挤压。",
      "书中指出：在身体轴向伸展和较大肌肉群工作时，需要有效呼吸。"),
    new Move("uttanasana","站立前屈式","Uttanasana","站式",
      "原书解剖：髋关节屈曲不足时，脊柱常代偿更多屈曲；腿后侧紧张可能造成过度主动下拉。",
      "App练习建议：腿后侧紧张时微屈膝，尝试从髋部折叠，不强迫手碰地。",
      "书中指出：深度髋屈与躯干屈曲会压缩腹部，呼吸更多依赖胸廓后侧活动。"),
    new Move("vrksasana","树式","Vrksasana","站式",
      "原书解剖：站立腿的外展肌、髋部旋转肌与足踝协同控制骨盆和单脚平衡。",
      "App练习建议：先扶墙，观察骨盆是否扭转；不必把抬起脚放得太高。",
      "书中指出：上肢位置会改变重心与呼吸运动的自由程度。"),
    new Move("warrior1","战士一式","Virabhadrasana I","站式",
      "原书解剖：前腿肌肉以离心活动控制屈曲，后腿和双脚对骨盆、脊柱构成支撑。",
      "App练习建议：缩短站距也可以；先保证双脚稳固，再调节骨盆和举臂幅度。",
      "书中强调：下肢获得足够支撑时，气息才更容易自由移动。"),
    new Move("warrior2","战士二式","Virabhadrasana II","站式",
      "原书解剖：前腿承重和外展肌控制十分关键；后髋的旋转幅度不应被孤立规定。",
      "App练习建议：膝盖顺前脚方向，不强求髋部打开到最大；如双臂发麻，应退出调整。",
      "书中指出：相较战士一式，较少的骨盆/脊柱扭转可能使呼吸更容易。"),
    new Move("warrior3","战士三式","Virabhadrasana III","站式",
      "原书解剖：单脚站立要求支撑腿及躯干完成更复杂的平衡控制。",
      "App练习建议：手扶墙进行，先保持骨盆稳定、呼吸自然，不追求抬腿高度。",
      "书中将其视为平衡姿势，稳定肌群用力可能影响呼吸自由度。"),
    new Move("trikonasana","三角伸展式","Utthita Trikonasana","站式",
      "原书解剖：前膝伸直时，股四头肌转为围绕关节的平衡活动；髋骨盆活动不足时膝内侧可能承受代偿压力。",
      "App练习建议：手扶砖，先保持两脚和膝盖稳定。膝盖出现疼痛或压迫感就退出调整。",
      "书中强调：胸廓、骨盆和脊柱旋转彼此有关，不必强行把胸口转到极限。"),
    new Move("sukhasana","简易坐","Sukhasana","坐式",
      "原书将坐姿视为恢复骨盆、下背部与地面接触关系的练习基础。",
      "App练习建议：坐在折叠毛毯上，必要时在膝下提供支撑，不强求盘腿深度。",
      "App练习建议：观察脊柱保持延伸时的自然呼吸，不刻意憋气。"),
    new Move("dandasana","手杖式","Dandasana","坐式",
      "原书解剖：臀腿紧张可使脊柱屈曲；手臂、躯干比例和肩胛位置也影响坐姿的脊柱延长。",
      "App练习建议：坐垫上坐稳，必要时微屈膝，先保持躯干舒适延伸。",
      "书中讨论在轴向伸展姿势下维持呼吸的挑战。"),
    new Move("baddhakonasana","束角式","Baddha Konasana","坐式",
      "原书归于坐姿体系，体式要求髋、膝、踝参与而不是单纯压低膝盖。",
      "App练习建议：坐高一些，在大腿/膝下垫支撑，不用手强压膝盖。",
      "App练习建议：保持平缓呼吸，膝或腹股沟疼痛时停止。"),
    new Move("balasana","婴儿式","Balasana","跪式",
      "原书把跪姿视作处在坐姿和站姿之间的支撑形式，并涉及髋、骨盆、膝和脚背的关系。",
      "App练习建议：膝盖、脚踝不舒服时增加支撑，额头可放在瑜伽砖或叠毯上。",
      "App练习建议：不要为了把臀部坐到脚跟而屏住呼吸。"),
    new Move("apanasana","下行气姿势","Apanasana","仰卧式",
      "原书特别指出它能把呼吸与身体的轻柔运动连接起来，并常作为理疗瑜伽工具。",
      "原书描述：仰卧，手放在膝盖上；吸气时腿远离身体，呼气时双腿向身体移动。",
      "移动配合呼吸，可非常缓慢轻柔地完成。"),
    new Move("savasana","挺卧式","Savasana","仰卧式",
      "原书把挺卧式描述为基本的仰卧姿势：身体背侧得到地面支撑，姿势肌可以减少抗重力用力。",
      "App练习建议：让身体舒适仰卧，膝下可垫支撑，避免腰部僵硬。",
      "观察自然呼吸；不追求特殊屏息或固定呼吸频率。"),
    new Move("bhujangasana","蛇式","Bhujangasana","俯卧式",
      "原书指出俯卧式常依靠身体后侧肌群，使躯干离开地面；胸背及肩部活动限制可能影响代偿。",
      "App练习建议：先做小幅度胸椎延展，腹部/骨盆接触地面，不借双臂强行把腰顶高。",
      "书中指出：俯卧姿势的腹部活动受承重约束，胸廓后侧需要更多运动。"),
    new Move("salabhasana","蝗虫式","Salabhasana","俯卧式",
      "原书将俯卧式中的脊柱及肢体伸展视为后侧肌群参与的重要类型。",
      "App练习建议：先小幅抬胸或抬腿，关注上下背协同，避免耸肩和甩动。",
      "App练习建议：动作幅度控制在可以自然呼吸的范围。"),
    new Move("downwarddog","下犬式","Adho Mukha Svanasana","臂架式",
      "原书指出与足部相比，手及腕部用于承重的结构比例较少，臂架体式需特别注意上肢支撑。",
      "App练习建议：双掌均匀推地，必要时屈膝；腕或肩不适可改扶墙式。",
      "App练习建议：保持肩部稳定，不为脚跟着地牺牲脊柱延伸。"),
    new Move("tabletop","四脚桌子式","Chatus Pada Pitham","臂架式",
      "原书指出：腘绳肌的限制或臀部用力过多可能妨碍髋关节伸展；胸部僵硬可能增加肩和脊柱代偿。",
      "App练习建议：坐姿屈膝、手撑身体后侧；先小幅抬髋，感觉颈肩或手腕不适时退出。",
      "书中指出：手臂伸展可能限制胸腔前侧活动，呼吸方式会相应改变。")
  };
  static final class Plan{
    final String id,name,desc,level;final String[] ids;
    Plan(String i,String n,String d,String l,String... a){id=i;name=n;desc=d;level=l;ids=a;}
  }
  static final Plan[] PLANS={
    new Plan("anatomy_foundations","A｜站姿与足部支撑","App根据《解剖.docx》第6章编排，按脚底支撑→站姿控制→舒展和放松推进。","基础",
      "tadasana","utkatasana","warrior2","trikonasana","uttanasana","savasana"),
    new Plan("anatomy_hip_breath","B｜髋部、坐姿与呼吸","App按第7–9章知识点编排，关注骨盆支撑、髋部活动和缓和呼吸。","基础",
      "tadasana","vrksasana","sukhasana","dandasana","baddhakonasana","apanasana","savasana"),
    new Plan("anatomy_back_shoulder","C｜背部与肩带协同","App根据第9–11章编排，关注后侧肌群、上肢承重和舒适放松。","基础/进阶可选",
      "tadasana","uttanasana","downwarddog","bhujangasana","salabhasana","tabletop","savasana"),
    new Plan("anatomy_warriors","D｜战士式稳定控制","App围绕单脚平衡、骨盆和前腿承重组织，动作难度略高。","进阶可选",
      "tadasana","warrior1","warrior2","trikonasana","warrior3","uttanasana","savasana")
  };
  private static final int GREEN=0xff176b59,INK=0xff263a32,MUTED=0xff61766c;
  LinearLayout root;
  Store db;
  String day;
  Plan plan;
  final Set<Integer> completed=new HashSet<>();
  EditText note;
  static final int SELECT_DOCX=47;
  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
  @Override public void onCreate(Bundle state){super.onCreate(state);db=new Store(this);day=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());home();}
  TextView text(String s,int n){TextView t=new TextView(this);t.setText(s);t.setTextColor(INK);t.setTextSize(n);t.setPadding(0,dp(6),0,dp(6));return t;}
  void add(View v){root.addView(v,new LinearLayout.LayoutParams(-1,-2));}
  Button button(String name,Runnable on){
    Button b=new Button(this);b.setText(name);b.setAllCaps(false);b.setTextColor(GREEN);b.setOnClickListener(v->on.run());return b;
  }
  void page(String title){
    ScrollView sc=new ScrollView(this);sc.setBackgroundColor(0xfff6f7f5);
    root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(18),dp(15),dp(18),dp(30));
    sc.addView(root);setContentView(sc);
    TextView t=text(title,22);t.setTypeface(null,Typeface.BOLD);add(t);
  }
  Move find(String id){for(Move m:MOVES)if(m.id.equals(id))return m;return null;}
  void home(){
    plan=null;page("瑜伽解剖 · 训练与动作");
    add(text("资料：《解剖.docx》第6–11章。原文是按身体姿势分类的解剖说明，不是按周训练课表。以下4套序列由App根据章节组织。",14));
    add(text("建议优先从A或B开始；不以拉伸深度衡量好坏。出现膝、腰、颈等关节疼痛时停止相应动作。",13));
    int ready=AnatomyImageStore.readyCount(this);
    add(text("配图：已内置 "+ready+"/18 个体式，每个2张参考图。无需选择 Word 文档，点击缩略图直接查看。",13));
    if(ready<18)add(text("有图片资源加载失败，请检查本版本 APK 是否完整。",13));
    add(button("查看解剖训练历史",this::history));
    for(Plan p:PLANS){
      add(button(p.name+" · "+p.ids.length+"个动作",()->open(p)));
      TextView label=text(p.level+" · "+p.desc,12);label.setTextColor(MUTED);add(label);
    }
    add(button("按书中六类浏览全部解剖动作",this::catalog));
  }
  void catalog(){
    page("按解剖姿势分类");add(button("返回训练序列",this::home));
    for(String cat:new String[]{"站式","坐式","跪式","仰卧式","俯卧式","臂架式"}){
      TextView head=text(cat,18);head.setTypeface(null,Typeface.BOLD);add(head);
      for(Move m:MOVES)if(m.group.equals(cat))add(button(m.cn+" · "+m.latin,()->details(m)));
    }
  }
  void open(Plan p){
    plan=p;completed.clear();
    JSONArray old=db.saved(p.id,day);if(old!=null)for(int i=0;i<old.length();i++)completed.add(old.optInt(i,-1));
    page(p.name);add(button("← 返回",this::home));add(text(p.desc,14));
    add(text("点击右侧动作缩略图，查看体式图、解剖／关节说明图和练习方法。练完勾选完成动作。",13));
    for(int i=0;i<p.ids.length;i++){
      final int index=i;Move m=find(p.ids[i]);if(m==null)continue;
      LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);
      CheckBox c=new CheckBox(this);c.setText((i+1)+". "+m.cn);c.setTextSize(15);c.setChecked(completed.contains(i));
      c.setOnCheckedChangeListener((v,checked)->{if(checked)completed.add(index);else completed.remove(index);});
      line.addView(c,new LinearLayout.LayoutParams(0,dp(56),1));
      if(AnatomyImageStore.ready(this,m.id)){
        Bitmap thumbnail=AnatomyImageStore.load(this,m.id,0,270);
        if(thumbnail!=null){
          ImageView image=new ImageView(this);
          image.setImageBitmap(thumbnail);image.setScaleType(ImageView.ScaleType.FIT_CENTER);
          image.setContentDescription("查看"+m.cn+"体式插图");
          image.setOnClickListener(v->details(m));
          line.addView(image,new LinearLayout.LayoutParams(dp(92),dp(82)));
        }
      }else{
        Button more=button("详情",()->details(m));more.setTextSize(12);
        line.addView(more,new LinearLayout.LayoutParams(dp(76),dp(52)));
      }
      add(line);
    }
    add(text("训练记录（选填）",15));
    note=new EditText(this);note.setHint("哪里稳定？哪里代偿？呼吸是否受限？");note.setMinLines(2);note.setText(db.note(p.id,day));add(note);
    add(button("保存今天已完成动作",()->{
      if(completed.isEmpty()){Toast.makeText(this,"先勾选实际完成的动作",Toast.LENGTH_SHORT).show();return;}
      JSONArray done=new JSONArray();ArrayList<Integer> order=new ArrayList<>(completed);Collections.sort(order);for(int i:order)done.put(i);
      db.save(p.id,day,done.toString(),note.getText().toString());
      Toast.makeText(this,"已保存",Toast.LENGTH_SHORT).show();home();
    }));
  }
  void details(Move m){
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(18),dp(8),dp(18),dp(10));
    TextView title=text(m.cn+" / "+m.latin,18);title.setTypeface(null,Typeface.BOLD);
    content.addView(title);content.addView(text("分类："+m.group+"  ·  来源：解剖.docx",12));
    if(AnatomyImageStore.ready(this,m.id)){
      LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER);
      ImageView image=new ImageView(this);
      image.setAdjustViewBounds(true);image.setScaleType(ImageView.ScaleType.FIT_CENTER);
      TextView caption=text("",12);
      Runnable posture=()->{
        Bitmap bm=AnatomyImageStore.load(this,m.id,0,1100);
        if(bm!=null)image.setImageBitmap(bm);
        caption.setText("动作外形（来自你提供的《解剖.docx》）");
      };
      Runnable anatomy=()->{
        Bitmap bm=AnatomyImageStore.load(this,m.id,1,1100);
        if(bm!=null)image.setImageBitmap(bm);
        caption.setText("解剖图或肌肉／关节动作表（来自《解剖.docx》）");
      };
      Button first=button("动作参考图",posture);
      Button second=button("解剖图／表",anatomy);
      first.setTextSize(12);second.setTextSize(12);
      tabs.addView(first,new LinearLayout.LayoutParams(0,dp(49),1f));
      tabs.addView(second,new LinearLayout.LayoutParams(0,dp(49),1f));
      content.addView(tabs);
      content.addView(caption);
      content.addView(image,new LinearLayout.LayoutParams(-1,-2));
      posture.run();
    }else{
      TextView missing=text("图片资源读取失败，请检查安装包。",13);
      missing.setTextColor(MUTED);content.addView(missing);
    }
    for(String[] section:new String[][]{{"书中解剖信息（提要）",m.why},{"练习方式（区分原书与App建议）",m.how},{"呼吸与观察",m.breath}}){
      TextView h=text(section[0],16);h.setTypeface(null,Typeface.BOLD);content.addView(h);
      content.addView(text(section[1],14));
    }
    content.addView(text("图1为姿势或肌肉动作示意；图2可能是骨骼／肌肉关节动作表，具体以原书内容为准。提要不能替代现场体式纠正。",12));
    ScrollView sc=new ScrollView(this);sc.addView(content);
    new AlertDialog.Builder(this).setView(sc).setPositiveButton("关闭",null).show();
  }
  void chooseDocx(){
    Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    startActivityForResult(intent,SELECT_DOCX);
  }
  @Override protected void onActivityResult(int request,int result,Intent data){
    super.onActivityResult(request,result,data);
    if(request!=SELECT_DOCX||result!=RESULT_OK||data==null||data.getData()==null)return;
    Uri file=data.getData();
    new AlertDialog.Builder(this).setTitle("导入解剖配图")
      .setMessage("会从这份 Word 中按动作名称对应18个体式，每个体式提取动作和解剖两张图。图片只保存在手机，不连接网络。")
      .setNegativeButton("取消",null).setPositiveButton("开始",(dialog,which)->importImages(file)).show();
  }
  void importImages(Uri file){
    ProgressDialog progress=new ProgressDialog(this);
    progress.setTitle("正在导入解剖配图");progress.setMessage("正在读取文件...");
    progress.setCancelable(false);progress.show();
    new Thread(()->{
      try{
        int count=AnatomyImageStore.importDocx(this,file,(done,total)->
          runOnUiThread(()->progress.setMessage("图片 "+done+"/"+total+" 已处理")));
        runOnUiThread(()->{
          progress.dismiss();
          new AlertDialog.Builder(this).setTitle("图片准备完成")
            .setMessage("已为 "+count+" 个体式保存动作图和解剖图。现在可以在训练计划中点击缩略图查看。")
            .setPositiveButton("查看",(d,w)->{if(plan!=null)open(plan);else home();}).show();
        });
      }catch(Exception ex){
        runOnUiThread(()->{
          progress.dismiss();
          new AlertDialog.Builder(this).setTitle("导入失败").setMessage(ex.getMessage())
            .setPositiveButton("确定",null).show();
        });
      }
    }).start();
  }
  void history(){
    page("解剖训练历史");add(button("返回",this::home));
    try(Cursor c=db.getReadableDatabase().rawQuery("SELECT day,plan_id,done,note FROM anatomy_sessions ORDER BY day DESC",null)){
      if(c.getCount()==0)add(text("暂无记录",14));
      while(c.moveToNext()){
        String date=c.getString(0),id=c.getString(1),raw=c.getString(2),n=c.getString(3);
        Plan found=null;for(Plan p:PLANS)if(p.id.equals(id))found=p;
        if(found==null)continue;
        add(text(date+" · "+found.name,16));
        StringBuilder done=new StringBuilder();
        try{JSONArray arr=new JSONArray(raw);for(int i=0;i<arr.length();i++){int k=arr.optInt(i,-1);if(k>=0&&k<found.ids.length){Move m=find(found.ids[k]);if(m!=null)done.append(" · ").append(m.cn);}}}catch(Exception ignore){}
        add(text("完成："+done.toString().replaceFirst("^ · ",""),13));
        if(n!=null&&!n.isEmpty())add(text("备注："+n,13));
      }
    }
  }
  @Override public void onBackPressed(){if(plan!=null)home();else super.onBackPressed();}
  @Override protected void onDestroy(){db.close();super.onDestroy();}
  static final class Store extends SQLiteOpenHelper{
    Store(Context c){super(c,"yoga_anatomy.db",null,1);}
    @Override public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE anatomy_sessions(day TEXT,plan_id TEXT,done TEXT,note TEXT,PRIMARY KEY(day,plan_id))");}
    @Override public void onUpgrade(SQLiteDatabase d,int a,int b){}
    JSONArray saved(String p,String day){try(Cursor c=getReadableDatabase().rawQuery("SELECT done FROM anatomy_sessions WHERE day=? AND plan_id=?",new String[]{day,p})){if(c.moveToFirst())return new JSONArray(c.getString(0));}catch(Exception e){}return null;}
    String note(String p,String day){try(Cursor c=getReadableDatabase().rawQuery("SELECT note FROM anatomy_sessions WHERE day=? AND plan_id=?",new String[]{day,p})){return c.moveToFirst()?c.getString(0):"";}}
    void save(String p,String day,String done,String note){ContentValues v=new ContentValues();v.put("plan_id",p);v.put("day",day);v.put("done",done);v.put("note",note);getWritableDatabase().insertWithOnConflict("anatomy_sessions",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
  }
}