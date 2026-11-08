package com.trainlog.simple;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import java.text.SimpleDateFormat;
import java.util.*;

/** 《瑜伽之光》中文版附录一：体式课程。PDF第509–514页，书内起始页373。 */
public final class YogaPlansActivity extends Activity {
  static final class Plan {
    final String id,title,pages,note; final String[] poses;
    Plan(String id,String title,String pages,String note,String list) {
      this.id=id;this.title=title;this.pages=pages;this.note=note;this.poses=list.split("\\|");
    }
  }
  static final Plan[] PLANS={
new Plan("c1-1-2","课程一｜第1–2周","509","按书中顺序。肩倒立和犁式涉及颈部承重，未由老师检查前只浏览、不练习。", "山式（图1）|树式第一式（图2）|三角伸展式（图4–5）|三角侧伸展式（图8–9）|战士第一式（图14）|战士第二式（图15）|加强侧伸展式（图26）|⚠ 肩倒立第一式（图223）|⚠ 犁式（图244）|挺尸式（图592）"),
new Plan("c1-3-4","课程一｜第3–4周","509","强化站姿，增加三角扭转和双角第一式。", "三角伸展式（图4–5）|三角侧伸展式（图8–9）|战士第一式（图14）|战士第二式（图15）|三角扭转伸展式（图6–7）|加强侧伸展式（图26）|双角第一式（图33–34）|⚠ 肩倒立第一式（图223）|⚠ 犁式（图244）|挺尸式（图592）"),
new Plan("c1-5-6","课程一｜第5–6周","509","加入腿部核心训练和乌加依呼吸；不自主练习屏息。", "三角伸展式（图4–5）|三角侧伸展式（图8–9）|战士第一式（图14）|战士第二式（图15）|三角扭转伸展式（图6–7）|加强侧伸展式（图26）|双角第一式（图33–34）|上伸腿式（图276–279）|完全船式（图78）|半船式（图79）|⚠ 肩倒立第一式（图223）|⚠ 犁式（图244）|乌加依呼吸（第三部分203）|挺尸式（图592，5分钟）"),
new Plan("c1-7","课程一｜第7周（巩固）","509","书中要求巩固前述体式、适当延长保持时间。以下沿用第5–6周序列供记录。", "三角伸展式（图4–5）|三角侧伸展式（图8–9）|战士第一式（图14）|战士第二式（图15）|三角扭转伸展式（图6–7）|加强侧伸展式（图26）|双角第一式（图33–34）|上伸腿式（图276–279）|完全船式（图78）|半船式（图79）|⚠ 肩倒立第一式（图223）|⚠ 犁式（图244）|乌加依呼吸（第三部分203）|挺尸式（图592）"),
new Plan("c1-8","课程一｜第8周","509","加入战士第三式、半月式和双角第二式。", "三角伸展式（图4–5）|三角侧伸展式（图8–9）|战士第一式（图14）|战士第二式（图15）|战士第三式（图17）|半月式（图19）|三角扭转伸展式（图6–7）|加强侧伸展式（图26）|双角第一式（图33–34）|双角第二式（图35–36）|上伸腿式（图276–279）|完全船式（图78）|半船式（图79）|⚠ 肩倒立第一式（图223）|⚠ 犁式（图244）|乌加依呼吸（第三部分203）|挺尸式（图592，5分钟）"),
new Plan("c1-9-10","课程一｜第9–10周","510","加入门闩式和较多肩倒立变化式。", "三角伸展式（图4–5）|三角侧伸展式（图8–9）|战士第一式（图14）|战士第二式（图15）|战士第三式（图17）|半月式（图19）|三角扭转伸展式（图6–7）|三角扭转侧伸展式（图10–11）|加强侧伸展式（图26）|双角第一式（图33–34）|双角第二式（图35–36）|门闩式（图39）|上伸腿式（图276–279）|完全船式（图78）|半船式（图79）|⚠ 肩倒立第一式（图223）|⚠ 犁式（图244）|⚠ 膝碰耳犁式（图246）|⚠ 单腿肩倒立式（图250）|卧扭转放松式（图274–275）|乌加依呼吸（第三部分203）|挺尸式（图592，5分钟）"),
new Plan("c1-11-12","课程一｜第11–12周","510","继续增加站姿前屈；包含倒置变化式，须逐项评估。", "三角伸展式（图4–5）|三角扭转伸展式（图6–7）|三角侧伸展式（图8–9）|三角扭转侧伸展式（图10–11）|战士第一式（图14）|战士第二式（图15）|战士第三式（图17）|半月式（图19）|加强侧伸展式（图26）|双角第一式（图33–34）|双角第二式（图35–36）|手抓脚趾站立伸展式（图44）|手碰脚前屈式（图46）|加强脊柱伸展式（图48）|门闩式（图39）|上伸腿式（图276–279）|完全船式（图78）|半船式（图79）|⚠ 肩倒立第一式（图223）|⚠ 犁式（图244）|⚠ 膝碰耳犁式（图246）|⚠ 单腿肩倒立式（图250）|卧扭转放松式（图274–275）|乌加依呼吸（第三部分203）|挺尸式（图592）"),
new Plan("c1-13","课程一｜第13周（复习）","510","书中没有重新列动作，要求重复、巩固前述体式；这里引用第11–12周完整序列，允许延长复习周数。", "三角伸展式（图4–5）|三角扭转伸展式（图6–7）|三角侧伸展式（图8–9）|三角扭转侧伸展式（图10–11）|战士第一式（图14）|战士第二式（图15）|战士第三式（图17）|半月式（图19）|加强侧伸展式（图26）|双角第一式（图33–34）|双角第二式（图35–36）|手抓脚趾站立伸展式（图44）|手碰脚前屈式（图46）|加强脊柱伸展式（图48）|门闩式（图39）|上伸腿式（图276–279）|完全船式（图78）|半船式（图79）|⚠ 肩倒立第一式（图223）|⚠ 犁式（图244）|⚠ 膝碰耳犁式（图246）|⚠ 单腿肩倒立式（图250）|卧扭转放松式（图274–275）|乌加依呼吸（第三部分203）|挺尸式（图592）"),
new Plan("c1-day1","课程一｜三日课程·第1天","513","书中针对已熟悉课程一的人开列的三日课程，非零基础建议。保持时间见各动作。", "⚠ 头倒立第一式（图184，10分钟）|⚠ 肩倒立第一式（图223，10分钟）|⚠ 犁式（图244，5分钟）|卧扭转放松式（图275，两侧各30秒）|完全船式（图78，1分钟）|半船式（图79，20–30秒）|背部伸展式（图160，3–5分钟）|圣哲玛里琪第三式（图303–304，两侧各30秒）|半鱼王第一式（图311–312，两侧各30秒；困难时可改巴拉瓦伽式）|坐山式（图107，1分钟）|鱼式（图113，20–30秒）|蝗虫式（图60，20–30秒）|弓式（图63，30秒）|上犬式（图74，20–30秒）|下犬式（图75，1分钟）|加强脊柱伸展式（图48，1–2分钟）|挺尸式（图592，5分钟）|⚠ 经络清洁呼吸（第三部分205，书中含屏息和乌迪雅那）|挺尸式（图592）"),
new Plan("c1-day2","课程一｜三日课程·第2天","513–514","书中倒置变化较多，风险较高；建议作为进阶查阅计划，不作为自动推荐训练。", "⚠ 头倒立第一式（图184，10分钟）|⚠ 扭转侧倒立式（图202–203）|⚠ 单腿倒立式（图208–209）|⚠ 上莲花倒立式（图211）|⚠ 胎儿倒立式（图218）|⚠ 肩倒立第一式（图223，8–10分钟）|⚠ 肩倒立第二式（图235）|⚠ 无支撑肩倒立第一式、第二式（图236–237）|⚠ 犁式（图244，5分钟）|⚠ 膝碰耳犁式（图246）|⚠ 双角犁式（图247）|⚠ 侧犁式（图249）|⚠ 单腿肩倒立式（图250）|⚠ 单腿侧着地肩倒立式（图251）|⚠ 上莲花肩倒立式（图261）|⚠ 胎儿肩倒立式（图269）|卧扭转放松式（图275）|上伸腿式（图276–279）|内女式（图125）|头碰膝前屈式（图127）|半莲花加强背部前屈式（图135）|半英雄面式前屈（图139）|圣哲玛里琪第一、第二、第三式（图144、146、147）|背部伸展式（图160，3分钟）|脸朝上背部伸展第一式（图168）|圣哲玛里琪第三式（图303–304）|半鱼王第一式（图311–312）|束角式（图102）|加强脊柱伸展式（图48，2分钟）|挺尸式（图592，5分钟）|乌加依或经络清洁呼吸（第三部分203或205，8分钟）|挺尸式（图592）"),
new Plan("c1-day3","课程一｜三日课程·第3天","514","书中站姿、后弯、坐姿组合；不熟悉的莲花相关体式可仅浏览。", "⚠ 头倒立第一式（图184，10分钟）|三角伸展式（图4–5）|三角扭转伸展式（图6–7）|三角侧伸展式（图8–9）|三角扭转侧伸展式（图10–11）|战士第一式（图14）|战士第二式（图15）|战士第三式（图17）|半月式（图19）|加强侧伸展式（图26）|双角第一式（图33–34）|手抓脚趾站立伸展式（图44）|手碰脚前屈式（图46）|加强脊柱伸展式（图48）|单腿脊柱前屈伸展式（图49）|鸟王式（图56）|幻椅式（图42）|门闩式（图39）|骆驼式（图41）|眼镜蛇第一式（图73）|英雄式（图89）|卧英雄式（图96）|榻式（图97）|莲花式（图104）|坐山式（图107）|莲花支撑式（图108）|鱼式（图113）|牛面式（图80）|支撑摇摆式（图83）|狮子第一式（图109）|背部伸展式（图160，3–5分钟）|乌加依或经络清洁呼吸（第三部分203或205，10分钟；不屏息）|挺尸式（图592，5分钟）")
  };
  private static final int INK=0xff253630,GRAY=0xff62766b,TEAL=0xff176b59;
  private LinearLayout body;
  private Plan selected;
  private final Set<Integer> checked = new HashSet<>();
  private EditText remarks;
  private Data db;
  private String date;
  private static final int PICK_YOGA_BOOK=43;
  private int dp(int a){return (int)(a*getResources().getDisplayMetrics().density+.5f);}
  @Override public void onCreate(Bundle state){super.onCreate(state);db=new Data(this);date=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());home();}
  private TextView text(String t,int sp){TextView v=new TextView(this);v.setText(t);v.setTextSize(sp);v.setTextColor(INK);v.setPadding(0,dp(6),0,dp(6));return v;}
  private void add(View v){body.addView(v,new LinearLayout.LayoutParams(-1,-2));}
  private void screen(String title){
    ScrollView s=new ScrollView(this);s.setBackgroundColor(0xfff6f7f5);
    body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(18),dp(18),dp(18),dp(32));
    s.addView(body);setContentView(s);
    TextView head=text(title,23);head.setTypeface(null,Typeface.BOLD);add(head);
  }
  private Button btn(String title,Runnable r){
    Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextColor(TEAL);b.setOnClickListener(v->r.run());return b;
  }
  private void home(){
    selected=null;screen("《瑜伽之光》训练序列");
    add(text("已内置68张来自你提供文档的体式参考图。无需额外导入，打开任何训练序列即可看到缩略图，点图可放大。",14));
    add(text("⚠ 原书初期即包含肩倒立、犁式。你可以只查阅这些动作；没有老师确认颈肩对位前不要按书中时长自行练习。",13));
    add(btn("可选：从 yoga.docx 重新导入或替换图片",()->chooseDocument()));
    add(text("图片已预装。只有希望重新导入时，才需要选择原始 Word 文档。",12));
    add(btn("查看已保存的序列记录",()->history()));
    for(Plan p:PLANS){
      add(btn(p.title+"  ·  "+p.poses.length+"项",()->detail(p)));
      TextView subtitle=text("PDF第"+p.pages+"页 · "+p.note,12);subtitle.setTextColor(GRAY);add(subtitle);
    }
  }
  private void detail(Plan p){
    selected=p;checked.clear();screen(p.title);
    add(btn("← 返回计划库",()->home()));
    add(text("《瑜伽之光》PDF第"+p.pages+"页 · "+p.note,14));
    add(text("勾选的是实际完成项目，不必照单全做；未完成的保持空白。",13));
    add(text("点击缩略图阅读书中“功法”原文；OCR文字与额外安全提示会明确区分。",13));
    JSONArray previous=db.get(p.id,date);
    if(previous!=null){for(int i=0;i<previous.length();i++) if(previous.optInt(i,-1)>=0)checked.add(previous.optInt(i));}
    for(int i=0;i<p.poses.length;i++){
      final int n=i;
      CheckBox cb=new CheckBox(this);cb.setText((i+1)+". "+p.poses[i]);cb.setTextSize(15);
      cb.setPadding(0,dp(5),0,dp(5));cb.setChecked(checked.contains(i));
      if(p.poses[i].startsWith("⚠"))cb.setTextColor(0xff9b653a);
      cb.setOnCheckedChangeListener((v,yes)->{if(yes)checked.add(n);else checked.remove(n);});
      LinearLayout row=new LinearLayout(this);
      row.setGravity(Gravity.CENTER_VERTICAL);
      row.addView(cb,new LinearLayout.LayoutParams(0,-2,1f));
      final String pose=p.poses[i];
      Bitmap thumbnail=YogaBookImages.load(this,pose);
      if(thumbnail!=null){
        ImageView preview=new ImageView(this);
        preview.setImageBitmap(thumbnail);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setContentDescription("点击查看"+pose+"参考图");
        preview.setPadding(dp(3),dp(3),dp(3),dp(3));
        row.addView(preview,new LinearLayout.LayoutParams(dp(86),dp(88)));
        preview.setOnClickListener(v->showPose(pose));
      }else{
        Button view=btn("查看",()->showPose(pose));
        view.setTextSize(12);
        row.addView(view,new LinearLayout.LayoutParams(dp(78),dp(46)));
      }
      add(row);
    }
    add(text("复盘备注（可空）",14));
    remarks=new EditText(this);remarks.setMinLines(2);remarks.setHint("例如今天哪些体式不稳定、肩膝是否不适");
    remarks.setText(db.note(p.id,date));add(remarks);
    add(btn("保存 "+date+" 的实际完成情况",()->{
      JSONArray vals=new JSONArray();List<Integer> indices=new ArrayList<>(checked);Collections.sort(indices);
      for(Integer i:indices)vals.put(i);
      if(vals.length()==0){Toast.makeText(this,"至少勾选一个实际完成项目",Toast.LENGTH_SHORT).show();return;}
      db.save(p.id,date,vals.toString(),remarks.getText().toString());
      Toast.makeText(this,"已保存，未勾选项目不会计为完成",Toast.LENGTH_SHORT).show();home();
    }));
  }

  private void chooseDocument(){
    Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    startActivityForResult(intent,PICK_YOGA_BOOK);
  }
  @Override protected void onActivityResult(int request,int result,Intent data){
    super.onActivityResult(request,result,data);
    if(request!=PICK_YOGA_BOOK||result!=RESULT_OK||data==null||data.getData()==null)return;
    Uri uri=data.getData();
    new AlertDialog.Builder(this).setTitle("导入书中的参考图")
      .setMessage("将从选中的 Word 文档中提取与训练序列匹配的约68张图，压缩保存在手机。不会修改训练记录，也不会上传文件。")
      .setNegativeButton("取消",null)
      .setPositiveButton("开始导入",(d,w)->runImport(uri)).show();
  }
  private void runImport(Uri uri){
    final ProgressDialog dialog=new ProgressDialog(this);
    dialog.setTitle("正在整理体式图");
    dialog.setMessage("正在读取原始文档，请不要关闭应用…");
    dialog.setCancelable(false);dialog.show();
    new Thread(()->{
      try{
        int count=YogaBookImages.importDocument(this,uri,(done,total)->
          runOnUiThread(()->dialog.setMessage("已整理 "+done+" / "+total+" 张图…")));
        runOnUiThread(()->{
          dialog.dismiss();
          new AlertDialog.Builder(this).setTitle("导入完成")
            .setMessage("已保存 "+count+" 张参考图。现在进入训练序列，点击动作右侧的“参考图”即可查看。")
            .setPositiveButton("好的",(d,w)->{if(selected!=null)detail(selected);else home();}).show();
        });
      }catch(Exception e){
        runOnUiThread(()->{dialog.dismiss();new AlertDialog.Builder(this)
          .setTitle("导入未完成").setMessage(e.getMessage())
          .setPositiveButton("确定",null).show();});
      }
    }).start();
  }
  private String poseHint(int fig){
    switch(fig){
      case 1:return "检查：双脚均匀着地；膝盖自然伸展；颈部与脊柱保持延长。";
      case 2:return "检查：单脚稳定着地；两侧保持时间接近；必要时扶墙练习。";
      case 4:return "检查：两腿稳定；先延长躯干；手可扶瑜伽砖，不必强行触地。";
      case 6:return "检查：先拉长脊柱再扭转；后脚稳定，不用蛮力拧腰。";
      case 8:return "检查：前膝朝向脚尖，后腿有支撑，胸部向外延伸。";
      case 14:return "检查：前膝与脚尖方向一致；后脚站稳；不为了举手而压腰。";
      case 15:return "检查：前膝保持稳定；两臂向两边延伸；骨盆不过度前倾。";
      case 17:return "检查：支撑腿稳定；背和抬起的腿向两端延伸；可扶墙。";
      case 19:return "检查：保持平衡；下方手可扶砖；不要为了转胸而扭腰。";
      case 33:return "检查：重心由双脚承担；双腿稳定；不要把身体重量压在头顶。";
      case 41:return "检查：优先延长胸部，避免颈部过度后仰或腰部挤压。";
      case 60:return "检查：腰背与腿部协同发力，动作控制，不要突然弹起。";
      case 73:return "检查：胸部向前上方延长，避免仅从腰椎挤出高度。";
      case 74:return "检查：手掌稳定；肩膀远离耳朵，颈部不要僵硬。";
      case 75:return "检查：手推地、背部延长；必要时屈膝，不必强压脚跟。";
      case 78:return "检查：背部延长，腿部可先屈膝，保持自然呼吸。";
      case 79:return "检查：避免靠甩动形成姿势，注意腰部是否受压。";
      case 223:case 244:case 184:return "⚠ 颈部承重和进出动作有风险。参考图无法判断你的受力是否安全，建议由合格教师现场检查。";
      case 592:return "检查：身体自然放松，不刻意屏息，也不需要强迫入静。";
      default:return "观察：双侧是否均衡、呼吸是否自然、关节是否有挤压或疼痛。仅作自检提示，不代表动作已被纠正。";
    }
  }
  private void showPose(String pose){
    int figure=YogaBookImages.firstFigure(pose);
    Bitmap bitmap=YogaBookImages.load(this,pose);
    LinearLayout l=new LinearLayout(this);
    l.setOrientation(LinearLayout.VERTICAL);
    l.setPadding(dp(18),dp(10),dp(18),dp(14));
    TextView t=text(pose.replace("⚠ ",""),17);t.setTypeface(null,Typeface.BOLD);
    l.addView(t);
    if(bitmap!=null){
      ImageView img=new ImageView(this);
      img.setAdjustViewBounds(true);
      img.setScaleType(ImageView.ScaleType.FIT_CENTER);
      img.setImageBitmap(bitmap);
      l.addView(img,new LinearLayout.LayoutParams(-1,-2));
      if(figure==599)l.addView(text("参考图599展示的是鼻孔呼吸的手势，不是气息流动示意。",12));
    }else{
      l.addView(text("这项目前未匹配到原书照片；下面仍可查看书中的功法文字。",13));
    }
    String original=YogaBookOriginal.text(this,figure);
    l.addView(text("《瑜伽之光》功法原文（DOCX文字识别摘录）",16));
    if(original!=null&&!original.isEmpty()){
      TextView src=text(original,14);
      src.setTextIsSelectable(true);
      l.addView(src);
      l.addView(text("以上是你提供文档中的识别文字，可能存在OCR错字或缺行，重要动作请结合原书图片核对。",12));
    }else{
      l.addView(text("该动作暂未成功提取到功法原文，未用其他资料代替。",13));
    }
    if(pose.startsWith("⚠") || pose.contains("肩倒立") || pose.contains("头倒立") || pose.contains("犁式")){
      l.addView(text("额外安全提醒（非书中原文）：涉及颈部承重，建议由合格教师现场检查受力和进出动作。",13));
    } else if(pose.contains("经络清洁")||pose.contains("乌加依")){
      l.addView(text("额外安全提醒（非书中原文）：初学者不宜自行练习长时间屏息或收束法，出现头晕及时停止。",13));
    }
    if(figure>0)l.addView(text("文档参考图号："+figure,12));
    ScrollView scroll=new ScrollView(this);scroll.addView(l);
    new AlertDialog.Builder(this).setView(scroll)
      .setPositiveButton("关闭",null).show();
  }
  private void history(){
    screen("序列练习记录");add(btn("← 返回计划库",()->home()));
    try(Cursor c=db.getReadableDatabase().rawQuery("SELECT day,plan_id,done,note FROM plan_sessions ORDER BY day DESC,plan_id",null)){
      if(c.getCount()==0)add(text("尚无记录",15));
      while(c.moveToNext()){
        String day=c.getString(0),id=c.getString(1),done=c.getString(2),note=c.getString(3);
        Plan p=null;for(Plan t:PLANS)if(t.id.equals(id))p=t;
        if(p==null)continue;
        StringBuilder completed=new StringBuilder();
        try{JSONArray arr=new JSONArray(done);for(int j=0;j<arr.length();j++){int i=arr.optInt(j,-1);if(i>=0&&i<p.poses.length)completed.append("\n").append(p.poses[i]);}}catch(Exception ignored){}
        add(text(day+" · "+p.title+completed.toString(),14));
        if(note!=null&&!note.isEmpty())add(text("复盘："+note,12));
        final Plan show=p;
        add(btn("查看或修改此计划（今天记录）",()->detail(show)));
      }
    }
  }
  @Override public void onBackPressed(){if(selected!=null)home();else super.onBackPressed();}
  @Override protected void onDestroy(){if(db!=null)db.close();super.onDestroy();}
  static class Data extends SQLiteOpenHelper {
    Data(Activity a){super(a,"yoga_plans.db",null,1);}
    @Override public void onCreate(SQLiteDatabase db){db.execSQL("CREATE TABLE plan_sessions(day TEXT NOT NULL, plan_id TEXT NOT NULL, done TEXT NOT NULL, note TEXT NOT NULL, PRIMARY KEY(day,plan_id))");}
    @Override public void onUpgrade(SQLiteDatabase d,int old,int now){}
    JSONArray get(String p,String date){
      try(Cursor c=getReadableDatabase().rawQuery("SELECT done FROM plan_sessions WHERE day=? AND plan_id=?",new String[]{date,p})){
        if(c.moveToFirst())return new JSONArray(c.getString(0));
      }catch(Exception ignored){}
      return null;
    }
    String note(String p,String date){
      try(Cursor c=getReadableDatabase().rawQuery("SELECT note FROM plan_sessions WHERE day=? AND plan_id=?",new String[]{date,p})){
        if(c.moveToFirst())return c.getString(0);
      }
      return "";
    }
    void save(String p,String date,String items,String note){
      ContentValues v=new ContentValues();v.put("day",date);v.put("plan_id",p);v.put("done",items);v.put("note",note);
      getWritableDatabase().insertWithOnConflict("plan_sessions",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
  }
}
