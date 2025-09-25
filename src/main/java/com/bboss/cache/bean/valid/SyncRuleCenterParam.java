package com.bboss.cache.bean.valid;

import com.bboss.pub.msg.JsonObj;
import lombok.Data;

import java.util.List;

@Data
public class SyncRuleCenterParam extends JsonObj {
    private String transIdo;
    private String systemNum = "01";
    private List<RuleExecuteLogReq> ruleExecuteDetails;
}