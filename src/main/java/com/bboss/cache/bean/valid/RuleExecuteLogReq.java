package com.bboss.cache.bean.valid;

import com.bboss.pub.msg.JsonObj;
import lombok.Data;

@Data
public class RuleExecuteLogReq extends JsonObj {
    private String offerNum;
    private String skuNum;
    private String businessNum;
    private String ruleNum;
    private String ruleTotalCount;
    private String ruleSuccessCount;
    private String ruleErrorCount;
    private String createTime;
    private String errorMsg;
}