package com.trainlog.simple;
import java.util.HashMap;
import java.util.Map;
/** Selected excerpts transcribed from the user's illustrated yoga.docx.
 * If absent, the application must not claim to show original instructions.
 */
final class YogaBookText {
  private static final Map<Integer,String> ORIGINAL=new HashMap<>();
  static {
    ORIGINAL.put(1, "双脚并拢站立．双脚脚跟和大拇指相互触碰．跖骨头接触地面．伸展所有脚趾平放于地面。|不要把身体的重量只放在脚跟或者脚趾处，而要把身体的重要均匀分布在脚跟和脚趾上。");
    ORIGINAL.put(2, "以山式站立。|保持这个体式上几秒钟．深长的呼吸。");
    ORIGINAL.put(4, "右脚向右转90度．左脚稍转向右．左腿从内侧保持伸展，膝部绷直。|保持这个姿势半分钟到一分钟.均匀深长地呼吸。");
    ORIGINAL.put(6, "右脚向右转90度。左脚向右转60度。保持左腿伸展。膝盖绷直。|伸展肩部和肩脾骨。");
    ORIGINAL.put(8, "弯曲右腿直到大腿和小腿成直角，右大腿与地面平行。|胸部、臀部和腿部应该在一条直线上。");
    ORIGINAL.put(10, "在两侧保持同样的时间。|伸展身体时保持均匀深长的呼吸。");
    ORIGINAL.put(14, "脸、胸部和右膝应该与右脚朝向同一方向。|保持这个体式20秒到30秒，正常呼吸。");
    ORIGINAL.put(15, "双手向两侧尽量延伸，感觉好像有两个人从不同方向把你朝两边拽。|保持这个体式20到30秒，保持深长的呼吸。");
    ORIGINAL.put(17, "保持这个体式20到30秒，保持深长的呼吸。|尽量拉伸右大腿后部．伸展双臂和左腿。");
    ORIGINAL.put(19, "身体重量放在右脚和右臀上。右手只是作为保持身体平衡的支撑。|保持这个体式20到30秒，深长均匀地呼吸。");
    ORIGINAL.put(26, "假如你不能在身体背后双手合十，那么就先握住手腕做上面的体式。");
    ORIGINAL.put(33, "不要把身体的重量放在头上，双脚和双手以及头部应该在一条直线上。|保持这个体式半分钟，深长均匀地呼吸。");
    ORIGINAL.put(39, "保持这个体式30到60秒，正常地呼吸。|在两侧保持体式的时间要相同。");
    ORIGINAL.put(41, "手掌下压脚底，使头部向后，脊柱尽量向大腿方向推。|保持这个体式半分钟：正常地呼吸。");
    ORIGINAL.put(42, "身体不要向前弯曲，胸部尽量向后挺。正常地呼吸。|保持这个体式数秒钟，30秒就足够了。");
    ORIGINAL.put(44, "背部下压是从骨盆区域开始弯曲．而不是从肩部向下。");
    ORIGINAL.put(48, "试着将头抬起，伸展脊柱，臀部稍向前从而使腿部与地面垂直。");
    ORIGINAL.put(56, "现在你全部的平衡都在右腿上，这需要一些时间来掌握。|保持这个体式几秒钟，比如15到20秒，同时保持深长地呼吸。");
    ORIGINAL.put(60, "脸朝下俯卧。手臂向后伸展。|保持这个体式，正常地呼吸。");
    ORIGINAL.put(63, "抬双膝离地，拉动双腿向上离地．同时带动胸部离地。|保持20到60秒。");
    ORIGINAL.put(73, "保持这个体式20秒，正常地呼吸。|呼气，肘部弯曲．躯干重新放回地面上。");
    ORIGINAL.put(74, "身体的重量应该只放在脚趾和手掌上。|保持这个体式半分钟到一分钟，深长地呼吸。");
    ORIGINAL.put(75, "双掌放于胸侧，手指伸直，指向头部的方向。|保持这个体式一分钟，深长地呼吸。");
    ORIGINAL.put(78, "保持半分钟．正常地呼吸。|呼气．放下手臂，双腿回到地面上，躺下，放松。");
    ORIGINAL.put(79, "练习这个体式时，不要试图屏息。");
    ORIGINAL.put(89, "跪在地上，双膝相靠，双脚分开18英寸（约45厘米）。");
    ORIGINAL.put(96, "以英雄式坐下。|呼气，身体向后。双肘依次放在地面上。");
    ORIGINAL.put(109, "坐在地面上，双腿向前伸直。|张大口，尽可能地将舌头朝下巴伸展。");
    ORIGINAL.put(223, "此体式牵涉到肩颈部承重，需要结合原书全部功法与辅助工具说明查看。");
    ORIGINAL.put(592, "完成后以挺尸式躺下，放松身体。");
    ORIGINAL.put(597, "以任何身体感到舒适的姿势坐下。|保持后背挺直。头部向躯干放低。把下巴放在锁骨之间凹陷处。|闭上双眼，向内看。");
    ORIGINAL.put(599, "按照太阳呼吸控制法的技巧进行练习。|每一侧的吸气和呼气的时间应该相同。");
  }
  static String excerpt(int figure){ return ORIGINAL.get(figure); }
  private YogaBookText(){}
}
