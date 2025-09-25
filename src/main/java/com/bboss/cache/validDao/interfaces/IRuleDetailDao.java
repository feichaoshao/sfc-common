package com.bboss.cache.validDao.interfaces;

import com.bboss.cache.bean.valid.RuleDetail;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

public interface IRuleDetailDao {

    int insert(RuleDetail record);

    int batchInsert(@Param("list") List<RuleDetail> list);

    int deleteRuleDetailByPackage(@Param("offerNum") String offerNum, @Param("skuNum") String skuNum);

    List<String> getOfferRuleGroup();

    List<RuleDetail> getRuleDetail(@Param("offerNum") String offerNum, @Param("skuNum") String skuNum, @Param("businessNum") String businessNum);
}
