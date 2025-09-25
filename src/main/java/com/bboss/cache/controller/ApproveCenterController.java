package com.bboss.cache.controller;

import com.alibaba.fastjson.JSONObject;
import com.bboss.cache.service.interfaces.ApprovalService;
import com.bboss.common.bean.approveCenter.approveSync.ApprovalSyncDetail;
import com.bboss.pub.msg.BaseRspsMsg;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/*
 * @Description OS审批中心交互接口
 * @Author admin
 * @Date 2024-05-25
 **/
@Slf4j
@RestController
@RequestMapping("/business")
@Api(value = "校验服务与审批中心交互接口", tags = "校验服务与审批中心交互")
public class ApproveCenterController {
    @Autowired
    ApprovalService approvalService;

    @RequestMapping(value = "/approveSync", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "审批中心同步", notes = "审批中心同步")
    public BaseRspsMsg receiveRuleInfo(@RequestBody ApprovalSyncDetail approvalSyncDto) {
        long startTime = System.currentTimeMillis();
        log.info("【审批中心同步审批规则信息】:{}.", JSONObject.toJSONString(approvalSyncDto));
        BaseRspsMsg baseRspsMsg = new BaseRspsMsg();
        baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00000_SUCCESS);
        baseRspsMsg.setBizDesc("校验服务接收审批中心同步规则成功！");
        try {
            approvalService.approveInfoSync(approvalSyncDto);
        } catch (Exception e) {
            log.error("【审批中心同步流程规则信息处理异常】:{}", e);
            baseRspsMsg.setBizDesc("审批中心同步流程规则信息处理异常，请联系管理员！");
            baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
        }
        log.info("【审批中心同步流程规则缓存处理完成,总用时】:{}，结束！", System.currentTimeMillis() - startTime);
        return baseRspsMsg;
    }

    /**
     * 查询redis中的审批数据
     */
    @RequestMapping(value = "/getRedisApprove", method = RequestMethod.GET)
    @ApiOperation(value = "",notes = "")
    public BaseRspsMsg getRedisApprove(){
        BaseRspsMsg baseRspsMsg = new BaseRspsMsg();
        baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00000_SUCCESS);
        baseRspsMsg.setBizDesc("校验服务接收审批中心同步规则成功！");
        return baseRspsMsg;
    }
}