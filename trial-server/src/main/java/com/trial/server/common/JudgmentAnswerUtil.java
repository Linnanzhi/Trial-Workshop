package com.trial.server.common;

/**
 * 判断题答案标准化工具类
 */
public class JudgmentAnswerUtil {

    /**
     * 标准化判断题答案
     * 将各种表示"对"的答案统一为"对"
     * 将各种表示"错"的答案统一为"错"
     * 
     * @param answer 原始答案
     * @return 标准化后的答案："对" 或 "错"，如果无法识别则返回原答案
     */
    public static String normalizeJudgmentAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return answer;
        }
        
        String normalized = answer.trim().toLowerCase();
        
        // 表示"对"的各种形式
        if (normalized.equals("对") 
            || normalized.equals("正确") 
            || normalized.equals("true") 
            || normalized.equals("t")
            || normalized.equals("√")
            || normalized.equals("是")
            || normalized.equals("yes")
            || normalized.equals("y")
            || normalized.equals("1")) {
            return "对";
        }
        
        // 表示"错"的各种形式
        if (normalized.equals("错") 
            || normalized.equals("错误") 
            || normalized.equals("false") 
            || normalized.equals("f")
            || normalized.equals("×")
            || normalized.equals("否")
            || normalized.equals("no")
            || normalized.equals("n")
            || normalized.equals("0")) {
            return "错";
        }
        
        // 无法识别，返回原答案
        return answer;
    }
    
    /**
     * 比较两个判断题答案是否相等
     * 会先标准化再比较
     * 
     * @param answer1 答案1
     * @param answer2 答案2
     * @return 是否相等
     */
    public static boolean compareJudgmentAnswer(String answer1, String answer2) {
        String normalized1 = normalizeJudgmentAnswer(answer1);
        String normalized2 = normalizeJudgmentAnswer(answer2);
        return normalized1.equals(normalized2);
    }
    
    /**
     * 判断是否为判断题答案格式
     * 
     * @param answer 答案
     * @return 是否为判断题答案
     */
    public static boolean isJudgmentAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return false;
        }
        
        String normalized = normalizeJudgmentAnswer(answer);
        return normalized.equals("对") || normalized.equals("错");
    }
}
