package com.bboss.cache.controller;

import com.alibaba.fastjson.JSONObject;
import com.bboss.cache.bean.approve.ApproveSyncLog;
import com.bboss.cache.dto.approval.QueryApprovalParam;
import com.bboss.cache.service.interfaces.ApprovalService;
import com.bboss.common.bean.approveCenter.approveSync.ApprovalSyncDetail;
import com.bboss.common.bean.jwt.JwtPayload;
import com.bboss.pub.msg.BaseRspsMsg;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/*
 * @Description 审批中心数据页面交互
 **/
@Slf4j
@RestController
@RequestMapping("/v1/ApprovalCenter/")
@Api(value = "审批数据与前端交互接口", tags = "审批数据与前端交互接口")
public class ApproveCenterPageController {
    @Autowired
    ApprovalService approvalService;

    /**
     * 查询审批中心同步记录
     */
    @PostMapping(value = "/queryApprovalSyncLogList")
    @ResponseBody
    @ApiOperation(value="审批中心列表查询接口", notes = "审批中心列表查询接口")
    public BaseRspsMsg queryApproveCenterSyncLogList(HttpServletRequest request, @RequestBody QueryApprovalParam queryApprovalParam) {
        JwtPayload jwtPayload = (JwtPayload) request.getAttribute(JwtPayload.class.getName());
        log.info("the jwtPayload is : {} ", jwtPayload.toJsonStr());
        BaseRspsMsg baseRspsMsg = null;
        try {
            PageInfo<ApproveSyncLog> pubInfo = approvalService.queryApproveSyncLog(queryApprovalParam);
            baseRspsMsg = BaseRspsMsg.ok(pubInfo);
        } catch (Exception e) {
            e.printStackTrace();
            baseRspsMsg = BaseRspsMsg.build(BaseRspsMsg.BIZ_CODE_00001_FAILED, e.getMessage());
        } finally {
            log.info(" 审批中心列表查询接口,查询结果 " + baseRspsMsg.toJsonStr());
        }
        return baseRspsMsg;
    }
}