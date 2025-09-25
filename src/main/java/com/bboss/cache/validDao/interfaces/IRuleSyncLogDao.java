package com.bboss.cache.validDao.interfaces;

import com.bboss.cache.bean.valid.RuleSyncParam;
import com.bboss.cache.bean.valid.RuleSyncLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IRuleSyncLogDao {

    List<RuleSyncLog> selectList(RuleSyncParam param);

    int insert(RuleSyncLog record);

    int batchInsert(List<RuleSyncLog> list);

    /**
     * 根据商品编码或者请求流水号查询对应的规则同步信息
     * @param offerNum 商品编码
     * @param transIdo 流水号
     * @return 规则同步记录信息
     */
    RuleSyncLog selectRuleSyncLogByTrans(@Param("offerNum") String offerNum,@Param("transIdo") String transIdo);

    int updateRuleSyncLog(RuleSyncLog ruleSyncLog);
}
