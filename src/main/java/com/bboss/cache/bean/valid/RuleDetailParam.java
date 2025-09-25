package com.bboss.cache.bean.valid;


import lombok.Data;

@Data
public class RuleDetailParam {
    private static final long serialVersionUID = 1L;
    /**
     * 当前页数
     */
    private Integer pageNum = 1;
    /**
     * 每页展示条数
     */
    private Integer pageSize = 10;
    private String offerNum;
    private String skuNum;
    private String businessNum;
}
