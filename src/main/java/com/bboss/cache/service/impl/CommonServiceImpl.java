package com.bboss.cache.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bboss.common.bean.check.rule.RuleDetailDto;
import com.bboss.common.bean.order.OrderRoute;
import com.bboss.common.constants.RuleConstants;
import com.bboss.common.feign.IFeignCabsOrder;
import com.bboss.common.util.*;
import com.bboss.pub.msg.BaseRspsMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommonServiceImpl {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IFeignCabsOrder iFeignCabsOrder;
    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 业务规则缓存到redis供给业务使用
     *
     * @param offerNum
     * @param ruleDetailDtoList
     */
    public void setOfferRule(String offerNum, List<RuleDetailDto> ruleDetailDtoList) {
        try {
            //重新对规则进行缓存,先删后存
            String allRuleKey = String.format(RuleConstants.REDIS_ALL_RULE_KEY, offerNum);
            if (redisUtil.delete(allRuleKey)) {
                log.info("allRuleKey {} delete success.", allRuleKey);
                redisUtil.set(allRuleKey, JSONObject.toJSONString(ruleDetailDtoList));
                log.info("allRuleKey {} set success.", allRuleKey);
            }
            //商品规则保存完成开始通知各个业务服务刷新缓存
            BaseRspsMsg baseRspsMsg = iFeignCabsOrder.getOfferRoute(offerNum);
            log.info("根据offerNum:{}.获取业务归属路由服务反馈:{}.", offerNum, baseRspsMsg.toJsonStr());
            if (!StringUtils.equals(BaseRspsMsg.BIZ_CODE_00000_SUCCESS, baseRspsMsg.getBizCode())) {
                log.error("根据offerNum:{}.获取业务归属路由服务失败！", offerNum);
                return;
            }
            List<OrderRoute> orderRouteList = JSON.parseArray(JSON.toJSONString(baseRspsMsg.getData()), OrderRoute.class);
            Map<String, List<OrderRoute>> orderRouteListMap = orderRouteList.stream()
                    .collect(Collectors.groupingBy(OrderRoute::getOfferNum));
            List<OrderRoute> orderRoutes = orderRouteListMap.get(offerNum);
            if (CommonUtil.isNullList(orderRoutes)) {
                log.error("根据offerNum:{}.未获取到业务归属路由服务！", offerNum);
                return;
            }
            log.info("根据offerNum:{}获取到的路由服务数:{}.开始遍历通知各个实例...", offerNum, orderRoutes.size());
            for (OrderRoute orderRoute : orderRoutes) {
                List<ServiceInstance> instances = discoveryClient.getInstances(orderRoute.getServiceCode());
                log.info("根据路由服务:{}.获取到注册中心服务注册信息数:{}.", orderRoute.getServiceCode(), instances.size());
                if (CommonUtil.isNullList(instances)) {
                    log.warn("根据路由服务:{}.无法触发服务规则刷新！", orderRoute.getServiceCode());
                    return;
                }
                for (ServiceInstance instance : instances) {
                    String url = instance.getUri() + "/business/rule/refresh?offerNum=" + offerNum;
                    log.info("服务实例的地址: {}.", url);
                    try {
                        String response = restTemplate.getForObject(url, String.class);
                        log.info("业务规则调用服务成功：{}.响应：{}. ", url, response);
                    } catch (Exception e) {
                        log.error("业务规则调用服务异常：{}.报错：{}. ", url, e);
                    }
                }
            }
        } catch (Exception e) {
            //TODO 不妨碍别人使用！错误先不抛！测试的时候检查日志！
            log.error("业务规则缓存异常:{}.", e);
        }
    }
}