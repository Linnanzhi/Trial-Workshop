package com.trial.server.common;

import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageQuery {

    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页大小 */
    private Integer pageSize = 10;

    /** 搜索关键词 */
    private String keyword;
}
