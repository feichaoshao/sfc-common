package com.bboss.cache.service.interfaces;

import com.bboss.cache.bean.valid.RuleDetail;
import com.bboss.cache.bean.valid.RuleDetailParam;
import com.bboss.cache.bean.valid.RuleSyncLog;
import com.bboss.cache.bean.valid.RuleSyncParam;
import com.bboss.cache.dto.rule.RuleSyncDto;
import com.bboss.pub.msg.BaseRspsMsg;
import com.github.pagehelper.PageInfo;

public interface RuleService {
    PageInfo<RuleSyncLog> pageList(RuleSyncParam param);
    PageInfo<RuleDetail> pageDetailList(RuleDetailParam param);
    BaseRspsMsg confirmRuleSync(String offerNum, String transIdo);
    void ruleSync(RuleSyncDto ruleSyncDto);
    void syncRuleCenterLog(String syncDate);
}
