package com.bboss.cache.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bboss.cache.bean.valid.RuleDetail;
import com.bboss.common.constants.RuleConstants;
import com.bboss.cache.service.interfaces.HandService;
import com.bboss.cache.validDao.interfaces.IRuleDetailDao;
import com.bboss.common.bean.check.rule.RuleDetailDto;
import com.bboss.common.util.CommonUtil;
import com.bboss.common.util.RedisUtil;
import com.bboss.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HandServiceImpl implements HandService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IRuleDetailDao iRuleDetailDao;
    @Autowired
    private CommonServiceImpl commonServiceImpl;

    /*
     * @Description 重启服务初始化全量业务缓存
     * @Author admin
     * @Date 2024-09-19
     * @Param []
     **/
    public void initRefresh() throws Exception {
        try {
            //再根据数据库同步表规则数据进行规则缓存
            List<String> offerList = iRuleDetailDao.getOfferRuleGroup();
            log.info("自动刷新缓存根据offerNum分组共:{}个.", offerList.size());
            //TODO 处理方式后续在优化直接暴力清理全部规则，并按照新的配置进行缓存。全部删除根据数据库最新规则缓存到redis，重启务必根据日志检查是否存在无规则商品
            if (CommonUtil.isNullList(offerList)) {
                log.error("%%%初始化全部商品规则异常，未获取到商品规则表配置数据！");
                throw new Exception("初始化全部商品规则异常！请检查");
            }
            //直接根据定义好的几类缓存批量清理
            String delPackageRuleKey = String.format(RuleConstants.REDIS_RULE_PACKAGE_KEY.substring(0, RuleConstants.REDIS_RULE_PACKAGE_KEY.length() - 3), "*");
            boolean delPackageRule = redisUtil.removePattern(delPackageRuleKey);
            log.info("del redis_rule_package_key :{}.result:{}.", delPackageRuleKey, delPackageRule);
            String delSkuRuleKey = String.format(RuleConstants.REDIS_RULE_SKU_KEY.substring(0, RuleConstants.REDIS_RULE_SKU_KEY.length() - 3), "*");
            boolean delSkuRule = redisUtil.removePattern(delSkuRuleKey);
            log.info("del redis_rule_sku_key :{}.result:{}.", delSkuRuleKey, delSkuRule);
            String delSkuBusinessRuleKey = String.format(RuleConstants.REDIS_RULE_SKUBUSINESS_KEY.substring(0, RuleConstants.REDIS_RULE_SKUBUSINESS_KEY.length() - 3), "*");
            boolean delSkuBusinessRule = redisUtil.removePattern(delSkuBusinessRuleKey);
            log.info("del redis_rule_skubusiness_key :{}.result:{}.", delSkuBusinessRuleKey, delSkuBusinessRule);
            String delCombinationRuleKey = String.format(RuleConstants.REDIS_RULE_COMBINATION_KEY, "*");
            boolean delCombinationRule = redisUtil.removePattern(delCombinationRuleKey);
            log.info("del redis_rule_combination_key :{}.result:{}.", delCombinationRuleKey, delCombinationRule);
            for (String num : offerList) {
                try {
                    this.refresh(num, null, null);
                } catch (Exception e) {
                    log.error("%%%初始化全部商品规则异常：{}.", e);
                    log.error("%%%%%%商品{}初始化规则缓存异常！请检查！开始处理下一个...", num);
                }
            }
        } catch (Exception e) {
            log.error("%%%%%%自动刷新缓存处理异常:{}.", e);
            throw new Exception(e);
        }
    }

    private void refresh(String offerNum, String skuNum, String businessNum) throws Exception {
        try {
            List<RuleDetail> ruleDetailList = iRuleDetailDao.getRuleDetail(offerNum, skuNum, businessNum);
            log.info("初始化商品规则:{}.数:{}.", offerNum, ruleDetailList.size());
            if (CommonUtil.isNullList(ruleDetailList)) {
                log.warn("初始化未获取到商品:{}规则缓存数据！", offerNum);
                throw new Exception("初始化商品规则未获取到缓存数据！");
            }
            List<RuleDetailDto> ruleDetailDTOList = ruleDetailList.stream().map(t -> {
                RuleDetailDto ruleDetailDto = new RuleDetailDto();
                BeanUtils.copyProperties(t, ruleDetailDto);
                return ruleDetailDto;
            }).collect(Collectors.toList());
            //商品PACKAGE规则缓存，规则中心配置时只配置到商品
            Map<String, List<RuleDetailDto>> packageRuleDetailDtoMap = ruleDetailDTOList.stream()
                    .filter(o -> StringUtils.isEmpty(o.getCombinationKey()) && RuleDetailDto.CHECK_TYPE_PACKAGE.equals(o.getCheckType()) && StringUtils.isNotEmpty(o.getOfferNum()))
                    .collect(Collectors.groupingBy(RuleDetailDto::getOfferNum));
            log.info("init packageRuleDetailDtoMap is {}.", packageRuleDetailDtoMap.size());
            if (packageRuleDetailDtoMap.size() > 0) {
                packageRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                    List<RuleDetailDto> ruleDetailValues = ruleDtoMap.getValue();
                    Map<String, List<RuleDetailDto>> ruleDetailMap = ruleDetailValues.stream().collect(Collectors.groupingBy(RuleDetailDto::getBusinessScene));
                    ruleDetailMap.entrySet().stream().forEach(ruleMap -> {
                        String setRuleKey = String.format(RuleConstants.REDIS_RULE_PACKAGE_KEY, ruleDtoMap.getKey(), ruleMap.getKey());
                        log.info("init business package ruleDetailDtoMap set businessKey:{}.ruleDetail:{}.", setRuleKey, ruleMap.getValue());
                        redisUtil.set(setRuleKey, JSONObject.toJSONString(ruleMap.getValue()));
                    });
                });
            }
            //产品SKU规则缓存
            Map<String, List<RuleDetailDto>> skuRuleDetailDtoMap = ruleDetailDTOList.stream()
                    .filter(o -> StringUtils.isEmpty(o.getCombinationKey()) && StringUtils.isEmpty(o.getBusinessNum()) && StringUtils.isNotEmpty(o.getSkuNum())
                            && RuleDetailDto.CHECK_TYPE_SKU.equals(o.getCheckType()))
                    .collect(Collectors.groupingBy(RuleDetailDto::getSkuNum));
            log.info("init skuRuleDetailDtoMap is {}.", skuRuleDetailDtoMap.size());
            if (skuRuleDetailDtoMap.size() > 0) {
                skuRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                    List<RuleDetailDto> ruleDetailValues = ruleDtoMap.getValue();
                    Map<String, List<RuleDetailDto>> ruleDetailMap = ruleDetailValues.stream().collect(Collectors.groupingBy(RuleDetailDto::getBusinessScene));
                    ruleDetailMap.entrySet().stream().forEach(ruleMap -> {
                        String ruleKey = String.format(RuleConstants.REDIS_RULE_SKU_KEY, ruleDtoMap.getKey(), ruleMap.getKey());
                        log.info("init business sku ruleDetailDtoMap set businessKey:{}.ruleDetail:{}.", ruleKey, ruleMap.getValue());
                        redisUtil.set(ruleKey, JSONObject.toJSONString(ruleMap.getValue()));
                    });
                });
            }
            //产品SKU操作规则缓存
            Map<String, List<RuleDetailDto>> skuBusinessRuleDetailDtoMap = ruleDetailDTOList.stream()
                    .filter(o -> StringUtils.isEmpty(o.getCombinationKey()) && StringUtils.isNotEmpty(o.getBusinessNum())
                            && RuleDetailDto.CHECK_TYPE_SKU.equals(o.getCheckType()))
                    .collect(Collectors.groupingBy(RuleDetailDto::getBusinessNum));
            log.info("init skuBusinessRuleDetailDtoMap is {}.", skuBusinessRuleDetailDtoMap.size());
            if (skuBusinessRuleDetailDtoMap.size() > 0) {
                skuBusinessRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                    List<RuleDetailDto> ruleDetailValues = ruleDtoMap.getValue();
                    Map<String, List<RuleDetailDto>> ruleDetailMap = ruleDetailValues.stream().collect(Collectors.groupingBy(RuleDetailDto::getBusinessScene));
                    ruleDetailMap.entrySet().stream().forEach(ruleMap -> {
                        String ruleKey = String.format(RuleConstants.REDIS_RULE_SKUBUSINESS_KEY, ruleDtoMap.getKey(), ruleMap.getKey());
                        log.info("init business skuBusiness ruleDetailDtoMap set businessKey:{}.ruleDetail:{}.", ruleKey, ruleMap.getValue());
                        redisUtil.set(ruleKey, JSONObject.toJSONString(ruleMap.getValue()));
                    });
                });
            }
            //把特殊规则缓存，特殊场景暂无缓存失效功能
            Map<String, List<RuleDetailDto>> combinationKeyRuleDetailDtoMap = ruleDetailDTOList.stream().filter(o -> StringUtils.isNotEmpty(o.getCombinationKey())).collect(Collectors.groupingBy(RuleDetailDto::getCombinationKey));
            log.info("init combinationKeyRuleDetailDtoMap is {}.", combinationKeyRuleDetailDtoMap.size());
            if (combinationKeyRuleDetailDtoMap.size() > 0) {
                combinationKeyRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                    String ruleKey = String.format(RuleConstants.REDIS_RULE_COMBINATION_KEY, ruleDtoMap.getKey());
                    log.info("init combinationKey ruleDetailDtoMap set combinationKey:{}.ruleDetail:{}.", ruleKey, ruleDtoMap.getValue());
                    redisUtil.set(ruleKey, JSONObject.toJSONString(ruleDtoMap.getValue()));
                });
            }
        } catch (Exception e) {
            log.error("%%%%%%init rule offerNum:{}.缓存异常:{}.",offerNum, e);
            throw new Exception("初始化缓存刷新异常！");
        }
    }

    public void handRefresh(String offerNum, String skuNum, String businessNum) throws Exception {
        try {
            List<RuleDetail> ruleDetailList = iRuleDetailDao.getRuleDetail(offerNum, skuNum, businessNum);
            log.info("handRefresh 获取到规则数:{}.", ruleDetailList.size());
            if (CommonUtil.isNullList(ruleDetailList)) {
                log.warn("手动刷新商品规则未获取到商品:{}规则缓存数据！", offerNum);
                throw new Exception("手动刷新商品规则未获取到缓存数据！");
            }
            List<RuleDetailDto> ruleDetailDTOList = ruleDetailList.stream().map(t -> {
                RuleDetailDto ruleDetailDto = new RuleDetailDto();
                BeanUtils.copyProperties(t, ruleDetailDto);
                return ruleDetailDto;
            }).collect(Collectors.toList());
            /**
             * TODO 20250429 业务规则开始使用了 业务规则优先使用内存规则进行处理
             * 各个业务多实例部署，缓存刷新方式1.直接重启根据订单路由归属服务获取offer规则2.redis监听暂不使用3.统一由cache服务进行管理下发，下游服务不需要重启
             * 由于缓存的rediskey比较多 本次根据offerNum进行整个业务规则缓存
             */
            commonServiceImpl.setOfferRule(offerNum, ruleDetailDTOList);
            log.info("===>业务offerNum:{}.规则同步redis完成...", offerNum);

            //商品PACKAGE规则缓存，规则中心配置时只配置到商品
            Map<String, List<RuleDetailDto>> packageRuleDetailDtoMap = ruleDetailDTOList.stream()
                    .filter(o -> StringUtils.isEmpty(o.getCombinationKey()) && RuleDetailDto.CHECK_TYPE_PACKAGE.equals(o.getCheckType()) && StringUtils.isNotEmpty(o.getOfferNum()))
                    .collect(Collectors.groupingBy(RuleDetailDto::getOfferNum));
            log.info("hand packageRuleDetailDtoMap is {}.", packageRuleDetailDtoMap.size());
            if (packageRuleDetailDtoMap.size() > 0) {
                packageRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                    String deleteRuleKey = String.format(RuleConstants.REDIS_RULE_PACKAGE_KEY, ruleDtoMap.getKey(), "*");
                    log.info("hand business package ruleDetailDtoMap delete businessKey:{}.", deleteRuleKey);
                    if (redisUtil.removePattern(deleteRuleKey)) {
                        List<RuleDetailDto> ruleDetailValues = ruleDtoMap.getValue();
                        Map<String, List<RuleDetailDto>> ruleDetailMap = ruleDetailValues.stream().collect(Collectors.groupingBy(RuleDetailDto::getBusinessScene));
                        ruleDetailMap.entrySet().stream().forEach(ruleMap -> {
                            String setRuleKey = String.format(RuleConstants.REDIS_RULE_PACKAGE_KEY, ruleDtoMap.getKey(), ruleMap.getKey());
                            log.info("hand business package ruleDetailDtoMap set businessKey:{}.ruleDetail:{}.", setRuleKey, ruleMap.getValue());
                            redisUtil.set(setRuleKey, JSONObject.toJSONString(ruleMap.getValue()));
                        });
                    }
                });
            }
            //产品SKU规则缓存
            Map<String, List<RuleDetailDto>> skuRuleDetailDtoMap = ruleDetailDTOList.stream()
                    .filter(o -> StringUtils.isEmpty(o.getCombinationKey()) && StringUtils.isEmpty(o.getBusinessNum()) && StringUtils.isNotEmpty(o.getSkuNum())
                            && RuleDetailDto.CHECK_TYPE_SKU.equals(o.getCheckType()))
                    .collect(Collectors.groupingBy(RuleDetailDto::getSkuNum));
            log.info("hand skuRuleDetailDtoMap is {}.", skuRuleDetailDtoMap.size());
            if (skuRuleDetailDtoMap.size() > 0) {
                skuRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                    String deleteRuleKey = String.format(RuleConstants.REDIS_RULE_SKU_KEY, ruleDtoMap.getKey(), "*");
                    log.info("hand business sku ruleDetailDtoMap delete businessKey:{}.", deleteRuleKey);
                    if (redisUtil.removePattern(deleteRuleKey)) {
                        List<RuleDetailDto> ruleDetailValues = ruleDtoMap.getValue();
                        Map<String, List<RuleDetailDto>> ruleDetailMap = ruleDetailValues.stream().collect(Collectors.groupingBy(RuleDetailDto::getBusinessScene));
                        ruleDetailMap.entrySet().stream().forEach(ruleMap -> {
                            String ruleKey = String.format(RuleConstants.REDIS_RULE_SKU_KEY, ruleDtoMap.getKey(), ruleMap.getKey());
                            log.info("hand business sku ruleDetailDtoMap set businessKey:{}.ruleDetail:{}.", ruleKey, ruleMap.getValue());
                            redisUtil.set(ruleKey, JSONObject.toJSONString(ruleMap.getValue()));
                        });
                    }
                });
            }
            //产品SKU操作规则缓存
            Map<String, List<RuleDetailDto>> skuBusinessRuleDetailDtoMap = ruleDetailDTOList.stream()
                    .filter(o -> StringUtils.isEmpty(o.getCombinationKey()) && StringUtils.isNotEmpty(o.getBusinessNum())
                            && RuleDetailDto.CHECK_TYPE_SKU.equals(o.getCheckType()))
                    .collect(Collectors.groupingBy(RuleDetailDto::getBusinessNum));
            log.info("hand skuBusinessRuleDetailDtoMap is {}.", skuBusinessRuleDetailDtoMap.size());
            if (skuBusinessRuleDetailDtoMap.size() > 0) {
                skuBusinessRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                    String deleteRuleKey = String.format(RuleConstants.REDIS_RULE_SKUBUSINESS_KEY, ruleDtoMap.getKey(), "*");
                    log.info("hand business skuBusiness ruleDetailDtoMap delete businessKey:{}.", deleteRuleKey);
                    if (redisUtil.removePattern(deleteRuleKey)) {
                        List<RuleDetailDto> ruleDetailValues = ruleDtoMap.getValue();
                        Map<String, List<RuleDetailDto>> ruleDetailMap = ruleDetailValues.stream().collect(Collectors.groupingBy(RuleDetailDto::getBusinessScene));
                        ruleDetailMap.entrySet().stream().forEach(ruleMap -> {
                            String ruleKey = String.format(RuleConstants.REDIS_RULE_SKUBUSINESS_KEY, ruleDtoMap.getKey(), ruleMap.getKey());
                            log.info("hand business skuBusiness ruleDetailDtoMap set businessKey:{}.ruleDetail:{}.", ruleKey, ruleMap.getValue());
                            redisUtil.set(ruleKey, JSONObject.toJSONString(ruleMap.getValue()));
                        });
                    }
                });
            }
            //把特殊规则缓存，特殊场景暂无缓存失效功能
            Map<String, List<RuleDetailDto>> combinationKeyRuleDetailDtoMap = ruleDetailDTOList.stream().filter(o -> StringUtils.isNotEmpty(o.getCombinationKey())).collect(Collectors.groupingBy(RuleDetailDto::getCombinationKey));
            log.info("hand combinationKeyRuleDetailDtoMap is {}.", combinationKeyRuleDetailDtoMap.size());
            if (combinationKeyRuleDetailDtoMap.size() > 0) {
                combinationKeyRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                    String ruleKey = String.format(RuleConstants.REDIS_RULE_COMBINATION_KEY, ruleDtoMap.getKey());
                    log.info("hand combinationKey ruleDetailDtoMap set combinationKey:{}.ruleDetail:{}.", ruleKey, ruleDtoMap.getValue());
                    redisUtil.set(ruleKey, JSONObject.toJSONString(ruleDtoMap.getValue()));
                });
            }
        } catch (Exception e) {
            log.error("%%%%%%hand rule offerNum:{}.缓存异常:{}.", offerNum, e);
            throw new Exception("手动缓存刷新异常！");
        }
    }

    @Override
    public String handGetKey(String redisKey) throws Exception {
        String value = null;
        try {
            value = redisUtil.get(redisKey);
        } catch (Exception e) {
            log.error("%%%%%%手动获取redis error:{}.", e);
            throw new Exception(e);
        }
        return value;
    }

    @Override
    public String handDeleteKey(String redisKey) throws Exception {
        String value = null;
        boolean flag = false;
        try {
            flag = redisUtil.delete(redisKey);
        } catch (Exception e) {
            log.error("%%%%%%手动获取redis error:{}.", e);
            throw new Exception(e);
        } finally {
            if (flag) {
                value = "删除成功key：" + redisKey;
            }
        }
        return value;
    }

    @Override
    public String handKeys(String redisKey) throws Exception {
        String value = null;
        try {
            Set<Object> keys = redisUtil.hashKeys(redisKey);
            if (keys.size() > 0) {
                value = keys.toString();
            } else {
                value = "未获取到keys:" + redisKey;
            }
        } catch (Exception e) {
            log.error("%%%%%%手动获取keys error:{}.", e);
            throw new Exception(e);
        }
        return value;
    }
}