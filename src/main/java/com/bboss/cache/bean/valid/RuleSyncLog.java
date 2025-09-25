package com.bboss.cache.bean.valid;

import lombok.Data;

import java.util.Date;

/*
 * @Description 规则中心同步记录表
 * @Author admin
 * @Date 2024-04-03
 * @Param
 **/
@Data
public class RuleSyncLog {
    //id
    private String id;
    //商品编码
    private String offerNum;
    //请求流水
    private String transIDO;
    //请求报文
    private String reqMsg;
    //处理状态
    private String status;
    //创建时间
    private Date createDate;

    public RuleSyncLog() {
    }

    public RuleSyncLog(String id, String transIDO, String reqMsg, String status) {
        this.id = id;
        this.transIDO = transIDO;
        this.reqMsg = reqMsg;
        this.status = status;
    }
}
