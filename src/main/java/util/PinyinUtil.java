package util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Arrays;

public class PinyinUtil {
    // 定义汉语拼音的配置 - 全局常量：必须在定义时初始化，全局唯一
    // 这些配置表示将汉字字符转为拼音字符串时的一些初始化设置
    private static final HanyuPinyinOutputFormat FORMAT;

    // 代码块就是在进行一些项目配置的初始化操作
    static {
        // 当PinyinUtil类加载时执行静态块，除了产生对象外，还可以进行一些配置相关的工作
        FORMAT = new HanyuPinyinOutputFormat();
        // 设置转换后的英文字母为全小写 好 -> hao
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        // 设置转换后的英文字母不带音调
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        // 特殊拼音用v替代 绿 -> lv
        FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    /**
     * 传入任意的文件名称，就能将该文件名称转为字母字符串和首字母小写字符串
     * eg：文件名为 你好 -> nihao / nh
     * 若文件名中包含了其他字符，英文数字等不需要做处理，直接保存
     * eg：你好hello -> nihaohello123 / nhhello123
     * @param fileName
     * @return
     */
    public static String[] getPinyinByFileName(String fileName) {
        // 第一个字符串为文件名全拼
        // 第二个字符串为首字母
        String[] ret = new String[2];
        // 核心操作就是遍历文件名中的每个字符，碰到非中文直接保留，中文处理
        StringBuilder allNameAppender = new StringBuilder();
        StringBuilder firstCaseAppender = new StringBuilder();
        for(char c : fileName.toCharArray()) {
            //
            try {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                if(pinyins == null || pinyins.length == 0) {
                    // 碰到非中文字符，直接保留
                    allNameAppender.append(c);
                    firstCaseAppender.append(c);
                }else {
                    // 碰到中文字符，取第一个多音字的返回值
                    allNameAppender.append(pinyins[0]);
                    firstCaseAppender.append(pinyins[0].charAt(0));
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                allNameAppender.append(c);
                firstCaseAppender.append(c);
            }
        }
        ret[0] = allNameAppender.toString();
        ret[1] = firstCaseAppender.toString();
        return ret;
    }

    public static void main(String[] args) throws BadHanyuPinyinOutputFormatCombination {
        String str1 = "和面";
        System.out.println(Arrays.toString(getPinyinByFileName(str1)));
        System.out.println("--------------------------分割线--------------------------");
        String str2 = "我是♂字符串♂";
        System.out.println(Arrays.toString(getPinyinByFileName(str2)));
    }
}
