package com.bboss.cache.controller;

import com.bboss.cache.service.interfaces.ApprovalService;
import com.bboss.cache.service.interfaces.HandService;
import com.bboss.common.util.StringUtils;
import com.bboss.pub.msg.BaseRspsMsg;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/*
 * @Description 方便测试手动触发缓存相关接口
 * @Author admin
 * @Date 2024-04-03
 **/
@Slf4j
@RestController
@RequestMapping("/business")
@Api(value = "手动脚本", tags = "手动脚本")
public class HandController {
    @Autowired
    private HandService handService;
    @Autowired
    ApprovalService approvalService;

    @RequestMapping(value = "/handRefresh", method = RequestMethod.GET)
    @ApiOperation(value = "手动刷缓存", notes = "手动刷缓存")
    public BaseRspsMsg handRefresh(@RequestParam(value = "offerNum", required = false) String offerNum,
                                   @RequestParam(value = "skuNum", required = false) String skuNum,
                                   @RequestParam(value = "businessNum", required = false) String businessNum) {
        long startTime = System.currentTimeMillis();
        BaseRspsMsg baseRspsMsg = new BaseRspsMsg();
        baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00000_SUCCESS);
        baseRspsMsg.setBizDesc("手动刷新缓存成功");
        try {
            if (StringUtils.isEmpty(offerNum)) {
                baseRspsMsg.setBizDesc("手动刷新缓存请求参数offerNum不能为空！");
                baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
                return baseRspsMsg;
            }
            log.info("手动刷新缓存offerNum:{}.skuNum:{}.businessNum:{}.", offerNum, skuNum, businessNum);
            handService.handRefresh(offerNum, skuNum, businessNum);
        } catch (Exception e) {
            log.error("【手动刷新缓存处理异常】:{}.", e);
            baseRspsMsg.setBizDesc("手动刷新缓存异常:" + e.getMessage());
            baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
        }
        log.info("【手动刷新缓存完成,总用时】:{}，结束！", System.currentTimeMillis() - startTime);
        return baseRspsMsg;
    }

    @RequestMapping(value = "/handGetKeyValue", method = RequestMethod.GET)
    @ApiOperation(value = "获取key缓存", notes = "获取key缓存")
    public BaseRspsMsg handGetKey(@RequestParam("redisKey") String redisKey) {
        long startTime = System.currentTimeMillis();
        BaseRspsMsg baseRspsMsg = new BaseRspsMsg();
        baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00000_SUCCESS);
        try {
            if (StringUtils.isEmpty(redisKey)) {
                baseRspsMsg.setBizDesc("请输入redisKey！");
                baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
                return baseRspsMsg;
            }
            baseRspsMsg.setBizDesc(handService.handGetKey(redisKey));;
        } catch (Exception e) {
            log.error("【手动获取key缓存异常】:{}.", e);
            baseRspsMsg.setBizDesc("手动获取key缓存异常！");
            baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
        }
        log.info("【手动获取key缓存完成,总用时】:{}，结束！", System.currentTimeMillis() - startTime);
        return baseRspsMsg;
    }

    @RequestMapping(value = "/handDeleteKeyValue", method = RequestMethod.GET)
    @ApiOperation(value = "删除key缓存", notes = "删除key缓存")
    public BaseRspsMsg handDeleteKey(@RequestParam("redisKey") String redisKey) {
        long startTime = System.currentTimeMillis();
        BaseRspsMsg baseRspsMsg = new BaseRspsMsg();
        baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00000_SUCCESS);
        try {
            if (StringUtils.isEmpty(redisKey)) {
                baseRspsMsg.setBizDesc("请输入redisKey！");
                baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
                return baseRspsMsg;
            }
            baseRspsMsg.setBizDesc(handService.handDeleteKey(redisKey));;
        } catch (Exception e) {
            log.error("【手动删除key缓存异常】:{}.", e);
            baseRspsMsg.setBizDesc("手动删除key缓存异常！");
            baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
        }
        log.info("【手动删除key缓存完成,总用时】:{}，结束！", System.currentTimeMillis() - startTime);
        return baseRspsMsg;
    }

    @RequestMapping(value = "/handGetKeys", method = RequestMethod.GET)
    @ApiOperation(value = "手动获取keys", notes = "手动获取keys")
    public BaseRspsMsg handGetKeys(@RequestParam("redisKey") String redisKey) {
        long startTime = System.currentTimeMillis();
        BaseRspsMsg baseRspsMsg = new BaseRspsMsg();
        baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00000_SUCCESS);
        try {
            if (StringUtils.isEmpty(redisKey)) {
                baseRspsMsg.setBizDesc("请输入redisKey！");
                baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
                return baseRspsMsg;
            }
            baseRspsMsg.setBizDesc(handService.handKeys(redisKey));;
        } catch (Exception e) {
            log.error("【手动获取keys异常】:{}.", e);
            baseRspsMsg.setBizDesc("手动获取keys异常！");
            baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
        }
        log.info("【手动获取keys完成,总用时】:{}，结束！", System.currentTimeMillis() - startTime);
        return baseRspsMsg;
    }

    /**
     * 审批数据同步到redis
     */

    @RequestMapping(value = "/approval/handRefresh", method = RequestMethod.GET)
    @ApiOperation(value = "手动刷缓存", notes = "手动刷缓存")
    public BaseRspsMsg approvalHandRefresh(@RequestParam(value = "operationId", required = false) String operationId,
                                           @RequestParam(value = "skuNum", required = false) String skuNum,
                                           @RequestParam(value = "transId", required = false) String transId) {
        long startTime = System.currentTimeMillis();
        BaseRspsMsg baseRspsMsg = new BaseRspsMsg();
        baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00000_SUCCESS);
        baseRspsMsg.setBizDesc("手动刷新缓存成功");
        try {
            if (StringUtils.isEmpty(operationId) && StringUtils.isEmpty(skuNum) && StringUtils.isEmpty(transId)) {
                baseRspsMsg.setBizDesc("手动刷新缓存请求参数operationId、skuNum、operationId不能为空！");
                baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
                return baseRspsMsg;
            }
            approvalService.approvalHandRefresh(operationId, skuNum, transId);
        } catch (Exception e) {
            log.error("【手动刷新缓存处理异常】:{}.", e);
            baseRspsMsg.setBizDesc("手动刷新缓存异常:" + e.getMessage());
            baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
        }
        log.info("【手动刷新缓存完成,总用时】:{}，结束！", System.currentTimeMillis() - startTime);
        return baseRspsMsg;
    }
}