package com.bboss.cache.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bboss.cache.bean.valid.*;
import com.bboss.cache.dto.rule.RuleSyncDto;
import com.bboss.cache.service.interfaces.RuleService;
import com.bboss.cache.validDao.interfaces.IRuleDetailDao;
import com.bboss.cache.validDao.interfaces.IRuleExecuteDao;
import com.bboss.cache.validDao.interfaces.IRuleExecuteSyncLogDao;
import com.bboss.cache.validDao.interfaces.IRuleSyncLogDao;
import com.bboss.common.bean.check.rule.RuleDetailDto;
import com.bboss.common.constants.RuleConstants;
import com.bboss.common.feign.IFeignShparm;
import com.bboss.common.util.*;
import com.bboss.pub.msg.BaseRspsMsg;
import com.bboss.pub.msg.RspMsgDto;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RefreshScope
public class RuleServiceImpl implements RuleService {
    @Autowired
    private IRuleSyncLogDao iRuleSyncLogDao;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IRuleDetailDao iRuleDetailDao;
    @Autowired
    private IRuleExecuteDao iRuleExecuteDao;
    @Autowired
    private IRuleExecuteSyncLogDao iRuleExecuteSyncLogDao;
    @Autowired
    private IFeignShparm iFeignShparm;
    @Autowired
    private HttpClientUtil httpClientUtil;
    @Autowired
    private CommonServiceImpl commonServiceImpl;

    @Value("${ruleCenter.eaiUrl:1}")
    private String eaiUrl;
    @Value("${ruleCenter.reqId:2025031701}")
    private String reqId;
    @Value("${ruleCenter.reqType:1}")
    private String reqType;
    @Value("${ruleCenter.reqUrl:1}")
    private String reqUrl;
    @Value("${ruleCenter.reqDateTime:10}")
    private int reqDateTime;

    private static String SYNC_SUCCESS_STATUS = "1";
    private static String SYNC_SUCCESS_FAIL = "0";

    @Override
    public PageInfo<RuleSyncLog> pageList(RuleSyncParam param) {
        PageHelper.startPage(param.getPageNum(), param.getPageSize(), true);
        List<RuleSyncLog> pageList = iRuleSyncLogDao.selectList(param);
        PageInfo pageInfo = new PageInfo<>(pageList);
        return pageInfo;
    }

    public PageInfo<RuleDetail> pageDetailList(RuleDetailParam param) {
        PageHelper.startPage(param.getPageNum(), param.getPageSize(), true);
        List<RuleDetail> pageList = iRuleDetailDao.getRuleDetail(param.getOfferNum(), param.getSkuNum(), param.getBusinessNum());
        PageInfo pageInfo = new PageInfo<>(pageList);
        return pageInfo;
    }

    /**
     * 确定规则同步信息接口
     * @param offerNum 商品编码
     * @param transIdo 流水号
     * @return
     */
    public BaseRspsMsg confirmRuleSync(String offerNum, String transIdo) {
        BaseRspsMsg baseRspsMsg = BaseRspsMsg.ok();
        RuleSyncLog ruleSyncLog = new RuleSyncLog();
        try {
            log.info("确定规则同步信息接口offerNum:{}.transIdo:{}.", offerNum, transIdo);
            if (StringUtils.isBlank(offerNum) && StringUtils.isBlank(transIdo)) {
                throw new RuntimeException("请求参数不能同时为空");
            }
            if (StringUtils.isNotBlank(transIdo)) {
                offerNum = null;
            }
            ruleSyncLog = iRuleSyncLogDao.selectRuleSyncLogByTrans(offerNum, transIdo);

            Assert.notNull(ruleSyncLog, "未查询到需要同步的规则同步日志记录信息");

            RuleSyncDto ruleSyncDto = JSON.parseObject(ruleSyncLog.getReqMsg(), RuleSyncDto.class);

            this.syncRuleDetail(ruleSyncDto);

            if (ruleSyncLog != null) {
                ruleSyncLog.setStatus("2");
            }
        } catch (Exception e) {
            ruleSyncLog.setStatus("0");
            log.error("确认规则同步信息接口异常：{}",e);
            baseRspsMsg = BaseRspsMsg.error("确认规则同步入库异常：" + e.getMessage());
        } finally {
            if (ruleSyncLog != null) {
                iRuleSyncLogDao.updateRuleSyncLog(ruleSyncLog);
            }
        }
        return baseRspsMsg;
    }

    /**
     * 同步规则明细信息
     * @param ruleSyncDto 规则同步参数信息
     */
    @Transactional
    public void syncRuleDetail(RuleSyncDto ruleSyncDto){
        Map<String, List<RuleDetailDto>> packageRuleDetailDtoMap = new HashMap<>();
        Map<String, List<RuleDetailDto>> skuRuleDetailDtoMap = new HashMap<>();
        List<RuleDetailDto> ruleDetailDTOList = ruleSyncDto.getRuleDetailDto();
        String offerNum = ruleDetailDTOList.get(0).getOfferNum();
        if(CommonUtil.isNullList(ruleDetailDTOList)){
            log.warn("规则中心同步规则信息为空！直接return...");
            return;
        }

        this.saveRuleDetail(offerNum, ruleDetailDTOList);
        log.info("===>规则入库完成开始按offerNum:{}.缓存商品下所有规则...", offerNum);

        /**
         * TODO 20250429 业务规则开始使用了 业务规则优先使用内存规则进行处理
         * 各个业务多实例部署，缓存刷新方式1.直接重启根据订单路由归属服务获取offer规则2.redis监听暂不使用3.统一由cache服务进行管理下发，下游服务不需要重启
         * 由于缓存的rediskey比较多 本次根据offerNum进行整个业务规则缓存
         */
        commonServiceImpl.setOfferRule(offerNum, ruleDetailDTOList);
        log.info("===>业务offerNum:{}.规则同步redis完成...", offerNum);

        //商品PACKAGE规则缓存，规则中心配置时只配置到商品
        packageRuleDetailDtoMap = ruleDetailDTOList.stream()
                .filter(o -> StringUtils.isEmpty(o.getCombinationKey())
                        && StringUtils.isNotEmpty(o.getOfferNum())
                        && RuleDetailDto.CHECK_TYPE_PACKAGE.equals(o.getCheckType()))
                .collect(Collectors.groupingBy(RuleDetailDto::getOfferNum));
        log.info("init packageRuleDetailDtoMap is {}.", packageRuleDetailDtoMap.size());

        if (packageRuleDetailDtoMap.size() > 0) {
            //入库
            packageRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                String deleteRuleKey = String.format(RuleConstants.REDIS_RULE_PACKAGE_KEY, ruleDtoMap.getKey(), "*");
                log.info("business package ruleDetailDtoMap delete businessKey:{}.", deleteRuleKey);
                if (redisUtil.removePattern(deleteRuleKey)) {
                    List<RuleDetailDto> ruleDetailValues = ruleDtoMap.getValue();
                    Map<String, List<RuleDetailDto>> ruleDetailMap = ruleDetailValues.stream().collect(Collectors.groupingBy(RuleDetailDto::getBusinessScene));
                    ruleDetailMap.entrySet().stream().forEach(ruleMap -> {
                        String setRuleKey = String.format(RuleConstants.REDIS_RULE_PACKAGE_KEY, ruleDtoMap.getKey(), ruleMap.getKey());
                        log.info("business package ruleDetailDtoMap set businessKey:{}.ruleDetail:{}.", setRuleKey, ruleMap.getValue());
                        redisUtil.set(setRuleKey, JSONObject.toJSONString(ruleMap.getValue()));
                    });
                }
            });
        }
        //产品SKU规则缓存
        skuRuleDetailDtoMap = ruleDetailDTOList.stream()
                .filter(o -> StringUtils.isEmpty(o.getCombinationKey())
                        && StringUtils.isNotEmpty(o.getSkuNum())
                        && StringUtils.isEmpty(o.getBusinessNum())
                        && RuleDetailDto.CHECK_TYPE_SKU.equals(o.getCheckType()))
                .collect(Collectors.groupingBy(RuleDetailDto::getSkuNum));
        log.info("init skuRuleDetailDtoMap is {}.", skuRuleDetailDtoMap.size());
        if (skuRuleDetailDtoMap.size() > 0) {
            skuRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                String deleteRuleKey = String.format(RuleConstants.REDIS_RULE_SKU_KEY, ruleDtoMap.getKey(), "*");
                log.info("business sku ruleDetailDtoMap delete businessKey:{}.", deleteRuleKey);
                if (redisUtil.removePattern(deleteRuleKey)) {
                    List<RuleDetailDto> ruleDetailValues = ruleDtoMap.getValue();
                    Map<String, List<RuleDetailDto>> ruleDetailMap = ruleDetailValues.stream().collect(Collectors.groupingBy(RuleDetailDto::getBusinessScene));
                    ruleDetailMap.entrySet().stream().forEach(ruleMap -> {
                        String ruleKey = String.format(RuleConstants.REDIS_RULE_SKU_KEY, ruleDtoMap.getKey(), ruleMap.getKey());
                        log.info("business sku ruleDetailDtoMap set businessKey:{}.ruleDetail:{}.", ruleKey, ruleMap.getValue());
                        redisUtil.set(ruleKey, JSONObject.toJSONString(ruleMap.getValue()));
                    });
                }
            });
        }
        //产品SKU操作规则缓存（由于货架没有产品操作层business_num只能规则中心特殊匹配：产品编码+_商品操作+产品操作来区分）
        Map<String, List<RuleDetailDto>> skuBusinessRuleDetailDtoMap = ruleDetailDTOList.stream()
                .filter(o -> StringUtils.isEmpty(o.getCombinationKey())
                        && StringUtils.isNotEmpty(o.getBusinessNum())
                        && RuleDetailDto.CHECK_TYPE_SKU.equals(o.getCheckType()))
                .collect(Collectors.groupingBy(RuleDetailDto::getBusinessNum));
        log.info("init skuBusinessRuleDetailDtoMap is {}.", skuBusinessRuleDetailDtoMap.size());
        if (skuBusinessRuleDetailDtoMap.size() > 0) {
            skuBusinessRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                String deleteRuleKey = String.format(RuleConstants.REDIS_RULE_SKUBUSINESS_KEY, ruleDtoMap.getKey(), "*");
                log.info("business skuBusiness ruleDetailDtoMap delete businessKey:{}.", deleteRuleKey);
                if (redisUtil.removePattern(deleteRuleKey)) {
                    List<RuleDetailDto> ruleDetailValues = ruleDtoMap.getValue();
                    Map<String, List<RuleDetailDto>> ruleDetailMap = ruleDetailValues.stream().collect(Collectors.groupingBy(RuleDetailDto::getBusinessScene));
                    ruleDetailMap.entrySet().stream().forEach(ruleMap -> {
                        String ruleKey = String.format(RuleConstants.REDIS_RULE_SKUBUSINESS_KEY, ruleDtoMap.getKey(), ruleMap.getKey());
                        log.info("business skuBusiness ruleDetailDtoMap set businessKey:{}.ruleDetail:{}.", ruleKey, ruleMap.getValue());
                        redisUtil.set(ruleKey, JSONObject.toJSONString(ruleMap.getValue()));
                    });
                }
            });
        }
        //把特殊规则缓存，特殊场景暂无缓存失效功能
        Map<String, List<RuleDetailDto>> combinationKeyRuleDetailDtoMap = ruleDetailDTOList.stream().filter(o -> StringUtils.isNotEmpty(o.getCombinationKey())).collect(Collectors.groupingBy(RuleDetailDto::getCombinationKey));
        log.info("init combinationKeyRuleDetailDtoMap is {}.", combinationKeyRuleDetailDtoMap.size());
        if (combinationKeyRuleDetailDtoMap.size() > 0) {
            combinationKeyRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                String ruleKey = String.format(RuleConstants.REDIS_RULE_COMBINATION_KEY, ruleDtoMap.getKey());
                log.info("combinationKey ruleDetailDtoMap set combinationKey:{}.ruleDetail:{}.", ruleKey, ruleDtoMap.getValue());
                redisUtil.set(ruleKey, JSONObject.toJSONString(ruleDtoMap.getValue()));
            });
        }
    }

    /*
     * 经过分析准备留两个口子说明如下
     * 1.最初设计规则挂在产品操作上，规则会区分商产品类型来触发各业务对应的商产品规则。
     * 2.产品操作粒度细方便管理，也是规划所要求的，那么对业务要求拆分的也得细，改造粒度场景也会比较乱
     * 3.本次新增的两个口子：可以不根据产品操作进行配置，规则可以挂在商品上和产品上，迁移配置时需要注意划分
     * @Description
     * @Author admin
     * @Date 2024-05-13
     * @Param [ruleSyncDto]
     **/
    @Override
    @Transactional
    public void ruleSync(RuleSyncDto ruleSyncDto) {
        RuleSyncLog ruleSyncLog = new RuleSyncLog(CommonUtil.getUid(), ruleSyncDto.getTransIdo(), JSONObject.toJSONString(ruleSyncDto), SYNC_SUCCESS_STATUS);
        List<RuleDetailDto> ruleDetailDTOList = ruleSyncDto.getRuleDetailDto();
        ruleSyncLog.setOfferNum(ruleDetailDTOList.get(0).getOfferNum());
        iRuleSyncLogDao.insert(ruleSyncLog);
    }

    /**
     * 保证数据准确性.每次生成数据记录一个执行时间位点
     * 每次从位点中获取一个时间+10分钟
     *
     * @param syncDate
     */
    @Async("syncTaskPool")
    public void syncRuleCenterLog(String syncDate) {
        RuleExecuteSyncLog ruleExecuteSyncLog = new RuleExecuteSyncLog();
        try {
            String startDate = iRuleExecuteSyncLogDao.selectSyncDate();
            log.info("数据库同步时间位点:{}.", startDate);
            if (StringUtils.isBlank(startDate)) {
                startDate = DateUtil.toString(new Date(), DateUtil.YYYY_MM_DDHHMMSS);
                ruleExecuteSyncLog = new RuleExecuteSyncLog(CommonUtil.getUid(), startDate, RuleExecuteSyncLog.BIZ_CODE_00_SUCCESS);
            }
            log.info("开始执行时间范围数据:{}.", startDate);
            Date endTime = DateUtil.addMinute(DateUtil.getDateByString(startDate, DateUtil.YYYY_MM_DDHHMMSS), reqDateTime);
            if (endTime.after(new Date())) {
                log.warn("执行时间大于当前时间本次跳过！");
                return;
            }
            String endDate = DateUtil.toString(endTime, DateUtil.YYYY_MM_DDHHMMSS);
            log.info("同步的数据范围:{}==>{}.", startDate, endDate);
            ruleExecuteSyncLog = new RuleExecuteSyncLog(CommonUtil.getUid(), endDate, RuleExecuteSyncLog.BIZ_CODE_00_SUCCESS);
            String rsp = iFeignShparm.searchOsReqHeaderInfo(reqId, reqType);
            log.info("请求OS获取请求头参数配置信息：{}", rsp);
            List<RuleExecuteLogReq> ruleExecuteLogReqList = iRuleExecuteDao.queryExecuteRuleLog(startDate, endDate);
            log.info("请求OS同步信息：{}", ruleExecuteLogReqList.size());
            String headerInfos = JSON.parseObject(rsp).getString("data");
            HashMap<String, String> reqHeaderInfo = JSON.parseObject(headerInfos, new TypeReference<HashMap<String, String>>() {
            });
            SyncRuleCenterParam syncRuleCenterParam = new SyncRuleCenterParam();
            syncRuleCenterParam.setTransIdo(CommonUtil.getUid());
            syncRuleCenterParam.setRuleExecuteDetails(ruleExecuteLogReqList);
            log.info("请求OS报文:{}.", JSONObject.toJSONString(syncRuleCenterParam));
            RspMsgDto rspMsgDto = httpClientUtil.sendJSONDataByPostForOs(eaiUrl + reqUrl, JSONObject.toJSONString(syncRuleCenterParam), null, reqHeaderInfo, syncRuleCenterParam.getTransIdo());
            log.info("请求OS反馈:{}.", rspMsgDto.toJsonStr());
            String errCode = rspMsgDto.getErrCode();
            if (!StringUtils.equals("200", errCode)) {
                ruleExecuteSyncLog.setSyncStatus(RuleExecuteSyncLog.BIZ_CODE_99_ERROR);
            }
            ruleExecuteSyncLog.setSyncResp(rspMsgDto.toJsonStr());
        } catch (Exception e) {
            ruleExecuteSyncLog.setSyncStatus(RuleExecuteSyncLog.BIZ_CODE_99_ERROR);
            ruleExecuteSyncLog.setSyncReq(e.getMessage());
            log.error("同步OS规则记录异常:{}.", e);
        } finally {
            if (StringUtils.isNotBlank(ruleExecuteSyncLog.getId())) {
                log.info("insert ruleExecuteSyncLog...");
                iRuleExecuteSyncLogDao.insert(ruleExecuteSyncLog);
            }
        }
    }

    /*
     * @Description 把规则中心同步数据保存入库
     * @Author admin
     * @Date 2024-05-18
     * @Param [packageRuleDetailDtoMap, skuRuleDetailDtoMap, ruleDetailDTOList]
     **/
    private void saveRuleDetail(String offerNum, List<RuleDetailDto> ruleDetailDTOList) {
        List<RuleDetail> ruleDetailList = new ArrayList<>();

        int deleteCount = iRuleDetailDao.deleteRuleDetailByPackage(offerNum, null);
        log.info("---OS规则中心同步按商品先删除生效老数据:{}.", deleteCount);

        for (RuleDetailDto ruleDetailDto : ruleDetailDTOList) {
            RuleDetail ruleDetail = new RuleDetail();
            //id
            ruleDetail.setId(CommonUtil.getUid());
            //商品编码
            ruleDetail.setOfferNum(ruleDetailDto.getOfferNum());
            //包编码
            ruleDetail.setPackageNum(ruleDetailDto.getPackageNum());
            //产品编码
            ruleDetail.setSkuNum(ruleDetailDto.getSkuNum());
            //产品操作编码同BPM配置操作
            ruleDetail.setBusinessNum(ruleDetailDto.getBusinessNum());
            //组合规则标识
            ruleDetail.setCombinationKey(ruleDetailDto.getCombinationKey());
            //预留目标服务，业务对应校验服务
            ruleDetail.setTargetService(ruleDetailDto.getTargetService());
            //规则编码
            ruleDetail.setRuleNum(ruleDetailDto.getRuleNum());
            //规则名称
            ruleDetail.setRuleName(ruleDetailDto.getRuleName());
            //规则详细描述
            ruleDetail.setRuleDesc(ruleDetailDto.getRuleDesc());
            //规则模式
            ruleDetail.setRuleMode(ruleDetailDto.getRuleMode());
            //规则入口接口地址、类名、方法名
            ruleDetail.setRuleEntry(ruleDetailDto.getRuleEntry());
            //规则脚本
            ruleDetail.setRuleContent(ruleDetailDto.getRuleContent());
            //规则类型（0：特殊规则1：通用规则）
            ruleDetail.setRuleType(ruleDetailDto.getRuleType());
            //校验类型（0：商品级校验只走一次1：产品级校验按产品走
            ruleDetail.setCheckType(ruleDetailDto.getCheckType());
            //规则状态
            ruleDetail.setRuleStatus(ruleDetailDto.getRuleStatus());
            //规则标签编码
            ruleDetail.setRuleLabel(ruleDetailDto.getRuleLabel());
            //规则目录编码
            ruleDetail.setRuleDirectory(ruleDetailDto.getRuleDirectory());
            //规则执行顺序
            ruleDetail.setRuleExecuteSort(StringUtils.isEmpty(ruleDetailDto.getRuleExecuteSort())?"1":ruleDetailDto.getRuleExecuteSort());
            //系统来源
            ruleDetail.setSystemNum(ruleDetailDto.getSystemNum());
            //规则场景（自定义但要结合业务：1：开通、2：注销、3：暂停、4：恢复、5：资费变更、9：业务变更、10：预受理、71：新增产品、72：注销产品、66：成员属性变更、61成员新增、62：成员删除）
            ruleDetail.setBusinessType(ruleDetailDto.getBusinessType());
            //规则类型（自定义但要结合业务：check：校验、before_prov：送开通前、after_prov：送开通后、audit：审批、archive_platform：归档平台、archive_billing：归档计费、archive_province：归档省、archive_settle：归档结算）
            ruleDetail.setBusinessScene(ruleDetailDto.getBusinessScene());
            //描述
            ruleDetail.setDescription(ruleDetailDto.getDescription());
            //创建人编码
            ruleDetail.setCreateNum("OS");
            //更新人编码
            ruleDetail.setUpdateNum("OS");
            ruleDetailList.add(ruleDetail);
        }
        log.info("---OS规则中心同步数据：{}.封装完成数据：{}.开始保存...");
        iRuleDetailDao.batchInsert(ruleDetailList);
        log.info("---OS规则中心同步数据保存完成,开始缓存...");
    }

    /*
     * @Description 第一版备份
     * @Author admin
     * @Date 2024-05-13
     * @Param [ruleSyncDto]
     **/
    public void ruleSyncBack(RuleSyncDto ruleSyncDto) {
        RuleSyncLog ruleSyncLog = new RuleSyncLog(CommonUtil.getUid(), ruleSyncDto.getTransIdo(), JSONObject.toJSONString(ruleSyncDto), SYNC_SUCCESS_STATUS);
        try {
            List<RuleDetailDto> ruleDetailDTOList = ruleSyncDto.getRuleDetailDto();
            //商品PACKAGE规则缓存
            Map<String, List<RuleDetailDto>> packageRuleDetailDtoMap = ruleDetailDTOList.stream()
                    .filter(o -> StringUtils.isEmpty(o.getCombinationKey()) && RuleDetailDto.CHECK_TYPE_PACKAGE.equals(o.getCheckType()))
                    .collect(Collectors.groupingBy(RuleDetailDto::getPackageNum));
            packageRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                String deleteRuleKey = String.format(RuleConstants.REDIS_RULE_PACKAGE_KEY, ruleDtoMap.getKey(), "*");
                log.info("business package ruleDetailDtoMap delete businessKey:{}.", deleteRuleKey);
                if(redisUtil.removePattern(deleteRuleKey)) {
                    List<RuleDetailDto> ruleDetailValues = ruleDtoMap.getValue();
                    Map<String, List<RuleDetailDto>> ruleDetailMap = ruleDetailValues.stream().collect(Collectors.groupingBy(RuleDetailDto::getBusinessScene));
                    ruleDetailMap.entrySet().stream().forEach(ruleMap -> {
                        String setRuleKey = String.format(RuleConstants.REDIS_RULE_PACKAGE_KEY, ruleDtoMap.getKey(), ruleMap.getKey());
                        log.info("business package ruleDetailDtoMap set businessKey:{}.ruleDetail:{}.", setRuleKey, ruleMap.getValue());
                        redisUtil.set(setRuleKey, JSONObject.toJSONString(ruleMap.getValue()));
                    });
                }
            });
            //产品SKU操作规则缓存
            Map<String, List<RuleDetailDto>> skuBusinessRuleDetailDtoMap = ruleDetailDTOList.stream()
                    .filter(o -> StringUtils.isEmpty(o.getCombinationKey()) && RuleDetailDto.CHECK_TYPE_SKU.equals(o.getCheckType()))
                    .collect(Collectors.groupingBy(RuleDetailDto::getBusinessNum));
            skuBusinessRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                String deleteRuleKey = String.format(RuleConstants.REDIS_RULE_SKUBUSINESS_KEY, ruleDtoMap.getKey(), "*");
                log.info("business sku ruleDetailDtoMap delete businessKey:{}.", deleteRuleKey);
                if(redisUtil.removePattern(deleteRuleKey)) {
                    List<RuleDetailDto> ruleDetailValues = ruleDtoMap.getValue();
                    Map<String, List<RuleDetailDto>> ruleDetailMap = ruleDetailValues.stream().collect(Collectors.groupingBy(RuleDetailDto::getBusinessScene));
                    ruleDetailMap.entrySet().stream().forEach(ruleMap -> {
                        String ruleKey = String.format(RuleConstants.REDIS_RULE_SKUBUSINESS_KEY, ruleDtoMap.getKey(), ruleMap.getKey());
                        log.info("business sku ruleDetailDtoMap set businessKey:{}.ruleDetail:{}.", ruleKey, ruleMap.getValue());
                        redisUtil.set(ruleKey, JSONObject.toJSONString(ruleMap.getValue()));
                    });
                }
            });
            //把特殊规则缓存，特殊场景暂无缓存失效功能
            Map<String, List<RuleDetailDto>> combinationKeyRuleDetailDtoMap = ruleDetailDTOList.stream().filter(o -> StringUtils.isNotEmpty(o.getCombinationKey())).collect(Collectors.groupingBy(RuleDetailDto::getCombinationKey));
            combinationKeyRuleDetailDtoMap.entrySet().stream().forEach(ruleDtoMap -> {
                String ruleKey = String.format(RuleConstants.REDIS_RULE_COMBINATION_KEY, ruleDtoMap.getKey());
                log.info("combinationKey ruleDetailDtoMap set combinationKey:{}.ruleDetail:{}.", ruleKey, ruleDtoMap.getValue());
                redisUtil.set(ruleKey, JSONObject.toJSONString(ruleDtoMap.getValue()));
            });
        } catch (Exception e) {
            ruleSyncLog.setStatus(SYNC_SUCCESS_FAIL);
            log.error("%%%%%%ruleSync error:{}.", e);
        } finally {
            iRuleSyncLogDao.insert(ruleSyncLog);
        }
    }
}
