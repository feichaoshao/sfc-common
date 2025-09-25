package com.bboss.cache.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bboss.cache.dto.approval.QueryApprovalParam;
import com.bboss.common.bean.order.export.ExportOrder;
import com.bboss.common.constants.ApprovalConstants;
import com.bboss.cache.bean.approve.ApproveSyncLog;
import com.bboss.cache.service.interfaces.ApprovalService;
import com.bboss.cache.validDao.interfaces.IApproveSyncLogDao;
import com.bboss.common.bean.approveCenter.approveSync.ApprovalSyncDetail;
import com.bboss.common.util.CommonUtil;
import com.bboss.common.util.DateUtil;
import com.bboss.common.util.PubMethod;
import com.bboss.common.util.RedisUtil;
import com.bboss.common.util.uid.utils.DateUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ApprovalServiceImpl implements ApprovalService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IApproveSyncLogDao iApproveSyncLogDao;

    private static String SYNC_SUCCESS_STATUS = "1";
    private static String SYNC_SUCCESS_FAIL = "0";

    /*
     * 缓存说明如下
     * 1.审批中心同步审批数据，审批数据是按操作+产品配置的，所以这里缓的用 APPROVALCENTER:SKU:KEY:操作编码:产品编码 作为key来缓存对应的数据
     * @Description
     * @Author admin
     * @Date 2024-05-27
     * @Param [approvalSyncDto]
     **/
    @Override
    public void approveInfoSync(ApprovalSyncDetail approvalSyncDto) {
        String transId = DateUtils.formatDate(new Date(), DateUtil.YYYYMMDDHHMMSSSSS);
        ApproveSyncLog approveSyncLog = new ApproveSyncLog(CommonUtil.getUid(), transId, JSONObject.toJSONString(approvalSyncDto), SYNC_SUCCESS_STATUS);
        try{
            String redisKey = String.format(ApprovalConstants.REDIS_APPROVE_SKU_KEY,approvalSyncDto.getProductOpera() + approvalSyncDto.getProductId());
            log.info("approveInfoSync redisKey:{}, redisValue:{}", redisKey, JSONObject.toJSONString(approvalSyncDto));
            redisUtil.set(redisKey, JSONObject.toJSONString(approvalSyncDto));
        }catch (Exception e){
            approveSyncLog.setStatus(SYNC_SUCCESS_FAIL);
            log.error("%%%%%%approveInfoSync error:{}.", e);
        }finally {
            iApproveSyncLogDao.insert(approveSyncLog);
        }
    }

    @Override
    public void approvalHandRefresh(String operationId, String skuNum, String transID){
        String redisKey = String.format(ApprovalConstants.REDIS_APPROVE_SKU_KEY,operationId.trim()+skuNum.trim());
        try {
            List<ApproveSyncLog> approveSyncLogs = iApproveSyncLogDao.selectApproveSyncLogByTrans(transID);
            if(CommonUtil.checkList(approveSyncLogs)){
                approveSyncLogs.stream().forEach(approveSyncLog -> {
                    ApprovalSyncDetail approvalSyncDetail = JSONObject.parseObject(approveSyncLog.getReqMsg(), ApprovalSyncDetail.class);
                    if(operationId.equals(approvalSyncDetail.getProductOpera()) && skuNum.equals(approvalSyncDetail.getProductId()) ){
                        redisUtil.set(redisKey, approveSyncLog.getReqMsg());
                    }
                });
            }
        } catch (Exception e) {
            log.error("%%%%%%手动同步审批数据缓存 error:{}.", e);
        }
    }

    @Override
    public PageInfo<ApproveSyncLog> queryApproveSyncLog(QueryApprovalParam queryApprovalParam) {
        PubMethod.setPageHelper(1, 20, queryApprovalParam.getCurrentPage(), queryApprovalParam.getPageSize());

        List<ApproveSyncLog> syncLogs = iApproveSyncLogDao.selectApproveSyncLogs(queryApprovalParam.getTransId());

        PageInfo<ApproveSyncLog> pageInfo = new PageInfo<ApproveSyncLog>(syncLogs);

        return pageInfo;
    }
}
