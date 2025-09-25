package com.bboss.cache.validDao.interfaces;

import com.bboss.cache.bean.valid.RuleExecuteLog;
import com.bboss.cache.bean.valid.RuleExecuteLogReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IRuleExecuteDao {
    int insert(RuleExecuteLog RuleExecuteLog);

    List<RuleExecuteLogReq> queryExecuteRuleLog(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
