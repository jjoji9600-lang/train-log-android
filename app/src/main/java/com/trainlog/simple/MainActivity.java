package com.trainlog.simple;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.*;
import android.net.Uri;
import android.text.*;
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
  static final String[] LEGACY_IDS={"tadasana","trikonasana","virabhadrasana2","virabhadrasana1","ardha_adho_mukha_wall","savasana"};
  static final String[] YOGA_CATEGORIES={"全部","站姿","坐姿","前屈","后弯","扭转","倒置","放松"};
  static class Pose {
    final String id,name,category,tip;
    Pose(String i,String n,String c,String t){id=i;name=n;category=c;tip=t;}
  }
  static final Pose[] POSES={
    new Pose("tadasana","山式","站姿","脚掌均匀受力，膝盖自然伸展，脊柱向上延伸。"),
    new Pose("urdhva_hastasana","上举手式","站姿","手臂向上伸展时保持肋骨稳定，不刻意塌腰。"),
    new Pose("utkatasana","幻椅式","站姿","双脚扎根，膝盖跟随脚尖，胸廓保持自然。"),
    new Pose("trikonasana","三角伸展式","站姿","重心落在两脚，手可扶瑜伽砖，先延长躯干再转胸。"),
    new Pose("parivrtta_trikonasana","扭转三角式","扭转","先伸长脊柱，再小幅扭转；用瑜伽砖，不强迫转胸。"),
    new Pose("utthita_parsvakonasana","侧角伸展式","站姿","前膝朝脚尖方向，后脚稳定，躯干向侧面延伸。"),
    new Pose("parivrtta_parsvakonasana","扭转侧角式","扭转","保持腿部稳定，先延长身体再旋转，避免强拧腰部。"),
    new Pose("virabhadrasana1","战士一式","站姿","可缩短站距，后脚踩稳，举臂时避免挤压腰部。"),
    new Pose("virabhadrasana2","战士二式","站姿","前膝朝脚尖，后腿稳定，骨盆与胸口自然展开。"),
    new Pose("virabhadrasana3","战士三式","站姿","先扶墙练平衡，支撑腿不锁膝，身体与抬起腿向两端延伸。"),
    new Pose("ardha_chandrasana","半月式","站姿","一手扶瑜伽砖或墙，先求稳定再尝试打开胸口。"),
    new Pose("parsvottanasana","加强侧伸展式","站姿","两脚稳定，髋部向前折叠，背部延长，不强压腿后侧。"),
    new Pose("prasarita_padottanasana","双角式","前屈","宽站距，髋部折叠，手扶砖；头晕时缓慢退出。"),
    new Pose("uttanasana","站立前屈式","前屈","膝盖可微弯，重心稳定，让脊柱自然延长。"),
    new Pose("adho_mukha_svanasana","下犬式","前屈","手掌推地，膝盖可弯，优先拉长脊柱而非脚跟着地。"),
    new Pose("ardha_adho_mukha_wall","靠墙半下犬式","前屈","双手扶墙，髋部折叠，背部舒展，不挤压肩关节。"),
    new Pose("dandasana","杖式","坐姿","坐骨均匀着地，必要时垫毯，背部向上延展。"),
    new Pose("sukhasana","简易坐","坐姿","臀部坐高一点，膝下可垫支撑，不强求盘腿。"),
    new Pose("baddha_konasana","束角式","坐姿","坐骨稳，膝下可垫砖或毯，避免强压膝盖。"),
    new Pose("virasana","英雄坐","坐姿","臀部可坐砖，膝或脚踝不舒服就退出，不硬压。"),
    new Pose("gomukhasana","牛面式","坐姿","坐稳，双肩保持舒适；手够不到可用瑜伽带。"),
    new Pose("upavistha_konasana","坐角式","坐姿","坐高、脊柱延伸，双腿打开到能稳定呼吸的幅度。"),
    new Pose("paschimottanasana","坐姿前屈式","前屈","坐在毯上，先延长躯干再折髋，不拉拽颈部。"),
    new Pose("janu_sirsasana","单腿头碰膝式","前屈","弯腿膝盖充分支撑，沿伸直腿方向折髋而非强弓背。"),
    new Pose("trianga_mukhaikapada","半英雄坐前屈式","前屈","弯腿侧膝踝无痛再练，垫高坐骨，不强压。"),
    new Pose("marichyasana1","玛里琪一式","坐姿","保持坐骨稳定，先把胸口抬起，不追求抱紧腿。"),
    new Pose("bharadvajasana","巴拉瓦伽式","扭转","坐高并延伸脊柱，轻柔旋转，不借手臂猛拧。"),
    new Pose("marichyasana3","玛里琪三式","扭转","先延伸脊柱，再随呼气温和转体，不挤压腹部。"),
    new Pose("ardha_matsyendrasana","半鱼王式","扭转","坐骨稳，旋转均匀，膝腰不适时减小幅度。"),
    new Pose("balasana","婴儿式","放松","额头可垫毯，膝盖和脚踝舒适为先。"),
    new Pose("savasana","摊尸式","放松","仰卧自然呼吸，膝下或头下可垫支撑。"),
    new Pose("supta_baddha_konasana","仰卧束角式","放松","膝下提供支撑，胸口柔和舒展，不强行外开髋部。"),
    new Pose("supta_padangusthasana","仰卧手抓大脚趾式","前屈","可用瑜伽带，骨盆稳定，不拉直膝到疼痛。"),
    new Pose("setu_bandha","桥式","后弯","双脚稳、膝盖同向，避免用颈部发力顶起身体。"),
    new Pose("salabhasana","蝗虫式","后弯","延展胸部和腿，动作小而稳定，颈部保持自然。"),
    new Pose("bhujangasana","眼镜蛇式","后弯","耻骨和腿部稳定，主要从上背延展，不硬压腰部。"),
    new Pose("urdhva_mukha_svanasana","上犬式","后弯","胸口向前上方延展，肩远离耳朵，腰不舒服即减轻。"),
    new Pose("ustrasana","骆驼式","后弯","可手扶骨盆，腿部稳，先伸展胸椎再向后弯。"),
    new Pose("dhanurasana","弓式","后弯","两侧均衡伸展，避免强拉脚踝或挤压腰部。"),
    new Pose("urdhva_dhanurasana","轮式","后弯","进阶体式，建议教师检查肩、腕和腰部后再练。"),
    new Pose("halasana","犁式","倒置","颈部可能承重；无现场指导时先只查阅，不单独练。"),
    new Pose("salamba_sarvangasana","肩倒立","倒置","颈部受力有风险；建议合格教师现场评估支撑与退出方式。"),
    new Pose("salamba_sirsasana","头倒立","倒置","颈部承重风险；仅作查阅，先由教师检查对位及进出动作。"),
    new Pose("viparita_karani","靠墙抬腿式","放松","臀部离墙可留距离，腿放松，头颈保持舒适。")
  };
  static final int GREEN=0xff176b59, DARK=0xff21332f, MUTED=0xff687b74, BG=0xfff6f7f5;
  DB db;
  LinearLayout root;
  TextView[][] counts=new TextView[3][3];
  CheckBox popping,pain;
  final Set<String> selectedYoga=new LinkedHashSet<>();
  String yogaCategory="全部", pendingYogaImageId=null;
  LinearLayout yogaList;
  TextView yogaSelectedLabel;
  EditText yogaSearch;
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
    HorizontalScrollView navScroller=new HorizontalScrollView(this);
    navScroller.setHorizontalScrollBarEnabled(false);
    LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);
    for(String item:new String[]{"力量","瑜伽","序列","解剖","趋势","历史"}){
      Button b=button(item);b.setTextSize(12);
      nav.addView(b,new LinearLayout.LayoutParams(dp(72),dp(50)));
      b.setOnClickListener(v->{
        if(item.equals("力量"))record();
        else if(item.equals("瑜伽"))yoga();
        else if(item.equals("序列"))startActivity(new Intent(this,YogaPlansActivity.class));
        else if(item.equals("解剖"))startActivity(new Intent(this,AnatomyActivity.class));
        else if(item.equals("趋势"))progress();
        else history();
      });
    }
    navScroller.addView(nav);add(root,navScroller);add(root,label(title,20));
  }
  void record(){
    start("今天 · "+today);
    HashMap<String,String> existing=db.get(today);
    add(root,label("做完一组，点右侧 + 或 − 调整次数；点击数字可以直接输入。未练的组保持0。",13));
    for(int e=0;e<3;e++){
      add(root,label(NAMES[e],17));
      JSONArray saved=null;
      try{saved=new JSONArray(existing.getOrDefault("s"+e,"[]"));}catch(Exception ignored){}
      for(int k=0;k<3;k++){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
        TextView name=label("第 "+(k+1)+" 组",15);
        row.addView(name,new LinearLayout.LayoutParams(0,dp(50),1));
        LinearLayout stepper=new LinearLayout(this);stepper.setGravity(Gravity.CENTER_VERTICAL);
        TextView number=label(saved!=null&&k<saved.length()?String.valueOf(saved.optInt(k,0)):"0",19);
        number.setGravity(Gravity.CENTER);number.setTextColor(GREEN);
        counts[e][k]=number;
        Button minus=button("−");minus.setTextSize(18);minus.setMinWidth(0);minus.setMinimumWidth(0);
        Button plus=button("+");plus.setTextSize(18);plus.setMinWidth(0);plus.setMinimumWidth(0);
        minus.setOnClickListener(v->{int n=Integer.parseInt(number.getText().toString());number.setText(String.valueOf(Math.max(0,n-1)));});
        plus.setOnClickListener(v->{int n=Integer.parseInt(number.getText().toString());number.setText(String.valueOf(Math.min(999,n+1)));});
        number.setOnClickListener(v->{
          EditText editor=new EditText(this);editor.setInputType(2);editor.setSingleLine(true);
          editor.setText(number.getText());editor.setSelectAllOnFocus(true);
          new AlertDialog.Builder(this).setTitle("修改本组次数").setView(editor)
            .setNegativeButton("取消",null).setPositiveButton("确定",(d,w)->{
              try{int val=Integer.parseInt(editor.getText().toString().trim());
                if(val<0||val>999)throw new NumberFormatException();
                number.setText(String.valueOf(val));
              }catch(Exception ex){toast("请输入0～999之间的次数");}
            }).show();
        });
        stepper.addView(minus,new LinearLayout.LayoutParams(dp(45),dp(46)));
        stepper.addView(number,new LinearLayout.LayoutParams(dp(52),dp(46)));
        stepper.addView(plus,new LinearLayout.LayoutParams(dp(45),dp(46)));
        row.addView(stepper);add(root,row);
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
  Pose yogaPose(String id){
    for(Pose p:POSES)if(p.id.equals(id))return p;
    return null;
  }
  ArrayList<String> yogaIds(String raw){
    ArrayList<String> ids=new ArrayList<>();
    try {
      JSONArray arr=new JSONArray(raw==null?"[]":raw);
      if(arr.length()>0 && arr.opt(0) instanceof Boolean){
        for(int i=0;i<Math.min(arr.length(),LEGACY_IDS.length);i++)
          if(arr.optBoolean(i,false))ids.add(LEGACY_IDS[i]);
      }else for(int i=0;i<arr.length();i++){
        String id=arr.optString(i,"");
        if(yogaPose(id)!=null&&!ids.contains(id))ids.add(id);
      }
    }catch(Exception ignored){}
    return ids;
  }
  void yoga(){
    start("瑜伽体式库 · "+today);
    HashMap<String,String> old=db.getYoga(today);
    selectedYoga.clear();selectedYoga.addAll(yogaIds(old.get("completed")));
    yogaCategory="全部";
    add(root,label("按分类或名称找体式。已预装常用体式参考图；点“看图/要点”直接查看，不需要自己导图。",14));
    yogaSelectedLabel=label("",16);add(root,yogaSelectedLabel);
    add(root,label("练习分钟数（可留空）",14));
    yogaMinutes=new EditText(this);yogaMinutes.setInputType(2);yogaMinutes.setSingleLine(true);
    yogaMinutes.setHint("选填");String minuteSaved=old.getOrDefault("minutes","");
    yogaMinutes.setText("0".equals(minuteSaved)?"":minuteSaved);add(root,yogaMinutes);
    yogaPain=new CheckBox(this);yogaPain.setText("本次出现疼痛、头晕或麻木");
    yogaPain.setChecked("1".equals(old.get("pain")));add(root,yogaPain);
    yogaNotes=new EditText(this);yogaNotes.setHint("感受、左右差异、身体对位（可不填）");
    yogaNotes.setMinLines(2);yogaNotes.setText(old.getOrDefault("note",""));add(root,yogaNotes);
    Button save=button("保存本次瑜伽记录");add(root,save);save.setOnClickListener(v->saveYoga());
    add(root,label("近28天记录："+db.yogaCount(28)+"天。参考图从手机导入后，只存在本机。",13));
    yogaSearch=new EditText(this);yogaSearch.setSingleLine(true);yogaSearch.setHint("搜索体式名称");
    add(root,yogaSearch);
    yogaSearch.addTextChangedListener(new TextWatcher(){
      public void beforeTextChanged(CharSequence x,int a,int b,int c){}
      public void onTextChanged(CharSequence x,int a,int b,int c){refreshYogaList();}
      public void afterTextChanged(Editable x){}
    });
    HorizontalScrollView scroller=new HorizontalScrollView(this);
    LinearLayout tabs=new LinearLayout(this);tabs.setOrientation(LinearLayout.HORIZONTAL);
    for(String cat:YOGA_CATEGORIES){
      Button b=button(cat);tabs.addView(b,new LinearLayout.LayoutParams(dp(78),dp(50)));
      b.setOnClickListener(v->{yogaCategory=cat;refreshYogaList();});
    }
    scroller.addView(tabs);add(root,scroller);
    yogaList=column();add(root,yogaList);
    refreshYogaList();
    Button bottomSave=button("保存本次瑜伽记录");add(root,bottomSave);bottomSave.setOnClickListener(v->saveYoga());
    Button csv=button("导出瑜伽记录CSV");add(root,csv);csv.setOnClickListener(v->exportYoga());
    add(root,label("头倒立、肩倒立等倒置动作仅供查阅；没有教师检查前不建议自行反复练。",13));
  }
  void refreshYogaList(){
    if(yogaList==null)return;
    yogaList.removeAllViews();
    if(yogaSelectedLabel!=null)yogaSelectedLabel.setText("本次已选："+selectedYoga.size()+" 个体式");
    String filter=yogaSearch==null?"":yogaSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
    int found=0;
    for(Pose p:POSES){
      if(!"全部".equals(yogaCategory)&&!p.category.equals(yogaCategory))continue;
      if(!p.name.toLowerCase(Locale.ROOT).contains(filter)&&!p.id.contains(filter))continue;
      found++;
      LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
      CheckBox check=new CheckBox(this);check.setText(p.name);check.setTextColor(DARK);check.setTextSize(16);
      check.setChecked(selectedYoga.contains(p.id));
      check.setOnCheckedChangeListener((v,checked)->{
        if(checked)selectedYoga.add(p.id);else selectedYoga.remove(p.id);
        yogaSelectedLabel.setText("本次已选："+selectedYoga.size()+" 个体式");
      });
      row.addView(check,new LinearLayout.LayoutParams(0,dp(54),1));
      Button detail=button((hasYogaImage(p.id)||builtInYogaFigure(p.id)>0)?"看图/要点":"添加图/要点");
      detail.setTextSize(12);row.addView(detail,new LinearLayout.LayoutParams(dp(112),dp(52)));
      detail.setOnClickListener(v->showYogaPose(p));
      add(yogaList,row);
      TextView kind=label(p.category+" · "+p.tip,12);kind.setTextColor(MUTED);
      kind.setPadding(dp(12),0,dp(5),dp(10));add(yogaList,kind);
    }
    if(found==0)add(yogaList,label("没有匹配的体式",14));
  }
  boolean hasYogaImage(String id){
    return getSharedPreferences("yoga_images",MODE_PRIVATE).contains(id);
  }
  int builtInYogaFigure(String id){
    switch(id){
      case "tadasana": return 1;
      case "utkatasana": return 42;
      case "trikonasana": return 4;
      case "parivrtta_trikonasana": return 6;
      case "utthita_parsvakonasana": return 8;
      case "parivrtta_parsvakonasana": return 10;
      case "virabhadrasana1": return 14;
      case "virabhadrasana2": return 15;
      case "virabhadrasana3": return 17;
      case "ardha_chandrasana": return 19;
      case "parsvottanasana": return 26;
      case "prasarita_padottanasana": return 33;
      case "uttanasana": return 48;
      case "adho_mukha_svanasana": return 75;
      case "baddha_konasana": return 102;
      case "virasana": return 89;
      case "gomukhasana": return 80;
      case "paschimottanasana": return 160;
      case "janu_sirsasana": return 127;
      case "trianga_mukhaikapada": return 139;
      case "marichyasana1": return 144;
      case "marichyasana3": return 303;
      case "ardha_matsyendrasana": return 311;
      case "savasana": return 592;
      case "salabhasana": return 60;
      case "bhujangasana": return 73;
      case "urdhva_mukha_svanasana": return 74;
      case "ustrasana": return 41;
      case "dhanurasana": return 63;
      case "halasana": return 244;
      case "salamba_sarvangasana": return 223;
      case "salamba_sirsasana": return 184;
      default:return -1;
    }
  }
  void showYogaPose(Pose p){
    LinearLayout container=column();container.setPadding(dp(18),0,dp(18),0);
    add(container,label("分类："+p.category,14));
    String ref=getSharedPreferences("yoga_images",MODE_PRIVATE).getString(p.id,"");
    if(!ref.isEmpty()){
      ImageView picture=new ImageView(this);picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
      picture.setAdjustViewBounds(true);picture.setMinimumHeight(dp(150));
      picture.setMaxHeight(dp(330));
      try {
        Uri uri=Uri.parse(ref);
        BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;
        try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,bounds);}
        int max=Math.max(bounds.outWidth,bounds.outHeight),sample=1;
        while(max/sample>1200)sample*=2;
        BitmapFactory.Options opts=new BitmapFactory.Options();opts.inSampleSize=sample;
        try(InputStream in=getContentResolver().openInputStream(uri)){picture.setImageBitmap(BitmapFactory.decodeStream(in,null,opts));}
        add(container,picture);
      }catch(Exception ex){add(container,label("图片暂时无法读取，可以重新选择。",13));}
    }else{
      int figure=builtInYogaFigure(p.id);
      Bitmap built=figure>0?YogaBundledImages.load(this,"图"+figure):null;
      if(built!=null){
        ImageView image=new ImageView(this);
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setMaxHeight(dp(400));
        image.setImageBitmap(built);
        add(container,image);
        add(container,label("《瑜伽之光》内置参考图 · 图"+figure,12));
      }else add(container,label("该体式本版暂无内置参考图，可从手机选择替换。",13));
    }
    int sourceFigure=builtInYogaFigure(p.id);
    String original=sourceFigure>0?YogaBookOriginal.text(this,sourceFigure):"";
    if(!original.isEmpty()){
      add(container,label("《瑜伽之光》功法原文（DOCX识别文本）",16));
      TextView source=label(original,14);source.setTextIsSelectable(true);add(container,source);
      add(container,label("原文可能存在OCR错字或缺行，请结合原书图片核对。",12));
    }else{
      add(container,label("此体式暂未成功匹配功法原文。",12));
    }
    add(container,label("辅助提示（非书中原文）："+p.tip,14));
    if(p.category.equals("倒置"))add(container,label("注意：颈部承重体式需要现场指导，不依靠一张参考图判断安全。",13));
    ScrollView scroll=new ScrollView(this);scroll.addView(container);
    new AlertDialog.Builder(this).setTitle(p.name).setView(scroll)
      .setNegativeButton("关闭",null)
      .setPositiveButton("导入/替换图片",(d,w)->pickYogaImage(p.id)).show();
  }
  void pickYogaImage(String id){
    pendingYogaImageId=id;
    Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
    i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");
    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    startActivityForResult(i,25);
  }
  void saveYoga(){
    if(selectedYoga.isEmpty()){toast("至少勾选一个练过的体式");return;}
    String raw=yogaMinutes.getText().toString().trim();
    int minutes=0;
    try{if(!raw.isEmpty())minutes=Integer.parseInt(raw);}catch(Exception e){toast("分钟数格式不正确");return;}
    if(minutes<0||minutes>240){toast("分钟数请填0到240，或留空");return;}
    JSONArray ids=new JSONArray();for(String id:selectedYoga)ids.put(id);
    JSONObject j=new JSONObject();
    try{
      j.put("completed",ids.toString());j.put("minutes",minutes);
      j.put("pain",yogaPain.isChecked()?"1":"0");j.put("note",yogaNotes.getText().toString());
    }catch(Exception ex){toast("保存失败："+ex.getMessage());return;}
    Runnable action=()->{db.putYoga(today,j);toast("瑜伽记录已保存");yoga();};
    if(db.hasYoga(today))new AlertDialog.Builder(this).setTitle("更新今天的瑜伽记录？")
      .setMessage("将以本次勾选内容替换今天原有的瑜伽记录。")
      .setNegativeButton("取消",null).setPositiveButton("保存",(d,w)->action.run()).show();
    else action.run();
  }
  void exportYoga(){
    Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
    i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/csv");
    i.putExtra(Intent.EXTRA_TITLE,"瑜伽记录.csv");startActivityForResult(i,23);
  }
  void writeYogaCsv(Uri uri){
    StringBuilder out=new StringBuilder("date,pose,completed,minutes,pain,note\n");
    for(HashMap<String,String> r:db.allYoga()){
      for(String id:yogaIds(r.get("completed"))){
        Pose p=yogaPose(id);if(p==null)continue;
        out.append(csv(r.get("day"))).append(',').append(csv(p.name)).append(",1,");
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
      ArrayList<String> done=new ArrayList<>();
      for(String id:yogaIds(y.get("completed"))){Pose p=yogaPose(id);if(p!=null)done.add(p.name);}
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
    if(req==25){
      if(pendingYogaImageId==null)return;
      try{
        Uri selected=data.getData();
        getContentResolver().takePersistableUriPermission(selected,Intent.FLAG_GRANT_READ_URI_PERMISSION);
        getSharedPreferences("yoga_images",MODE_PRIVATE).edit().putString(pendingYogaImageId,selected.toString()).apply();
        toast("参考图已保存到本机");
        refreshYogaList();
      }catch(Exception ex){toast("图片导入失败："+ex.getMessage());}
      finally{pendingYogaImageId=null;}
      return;
    }
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
