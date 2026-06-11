package com.trial.server.dto;

import lombok.Data;
import java.util.List;

/**
 * 文本解析结果
 */
@Data
public class ParseResult {
    
    /**
     * 成功解析的题目列表
     */
    private List<QuestionDTO> questions;
    
    /**
     * 未解析的文本块列表
     */
    private List<UnparsedBlock> unparsedBlocks;
    
    @Data
    public static class UnparsedBlock {
        /**
         * 未解析的文本内容
         */
        private String text;
        
        /**
         * 在原文中的起始位置
         */
        private Integer startIndex;
        
        /**
         * 在原文中的结束位置
         */
        private Integer endIndex;
        
        public UnparsedBlock(String text, Integer startIndex, Integer endIndex) {
            this.text = text;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }
}
