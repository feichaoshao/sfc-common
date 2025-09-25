package com.bboss.cache.bean.valid;

import lombok.Data;

import java.util.Date;

@Data
public class RuleExecuteSyncLog {
    public static final String BIZ_CODE_00_SUCCESS = "00";//成功
    public static final String BIZ_CODE_99_ERROR = "99";//失败
    private String id;
    private String syncDate;
    private String syncStatus;
    private String syncReq;
    private String syncResp;
    //创建时间
    private Date createDate;

    public RuleExecuteSyncLog() {
    }

    public RuleExecuteSyncLog(String id, String syncDate, String syncStatus) {
        this.id = id;
        this.syncDate = syncDate;
        this.syncStatus = syncStatus;
    }
}
