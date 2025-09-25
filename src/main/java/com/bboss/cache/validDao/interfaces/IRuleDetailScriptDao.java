package com.bboss.cache.validDao.interfaces;

import com.bboss.cache.bean.valid.RuleDetailScript;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

public interface IRuleDetailScriptDao {

    int insert(RuleDetailScript ruleDetailScript);

    int batchInsert(@Param("list") List<RuleDetailScript> list);

    int deleteRuleDetailScript(@Param("ruleDetailId") String ruleDetailId);

    List<RuleDetailScript> queryRuleDetailScriptById(@Param("ruleDetailId") String ruleDetailId);

    List<RuleDetailScript> queryRuleDetailScriptByKey(@Param("ruleDetailId") String ruleDetailId);

    int updateRuleDetailScript(RuleDetailScript ruleDetailScript);
}