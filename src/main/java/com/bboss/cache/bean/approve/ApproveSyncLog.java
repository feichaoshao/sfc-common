package com.bboss.cache.bean.approve;

import lombok.Data;

import java.util.Date;

/*
 * @Description 审批中心同步记录表
 * @Author admin
 * @Date 2024-05-27
 * @Param
 **/
@Data
public class ApproveSyncLog {
    //id
    private String id;
    //请求流水
    private String transIDO;
    //请求报文
    private String reqMsg;
    //处理状态
    private String status;
    //创建时间
    private Date createDate;

    public ApproveSyncLog() {
    }

    public ApproveSyncLog(String id, String transIDO, String reqMsg, String status) {
        this.id = id;
        this.transIDO = transIDO;
        this.reqMsg = reqMsg;
        this.status = status;
    }
}
