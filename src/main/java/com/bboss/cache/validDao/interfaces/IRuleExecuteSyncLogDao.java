package com.bboss.cache.validDao.interfaces;

import com.bboss.cache.bean.valid.RuleExecuteSyncLog;

public interface IRuleExecuteSyncLogDao {

    String selectSyncDate();

    int insert(RuleExecuteSyncLog ruleExecuteSyncLog);
}
