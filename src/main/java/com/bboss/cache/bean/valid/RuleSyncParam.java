package com.bboss.cache.bean.valid;


import lombok.Data;

@Data
public class RuleSyncParam {
    private static final long serialVersionUID = 1L;
    /**
     * 当前页数
     */
    private Integer pageNum = 1;
    /**
     * 每页展示条数
     */
    private Integer pageSize = 10;
    //商品编码
    private String offerNum;
    //请求流水
    private String transIDO;
}
