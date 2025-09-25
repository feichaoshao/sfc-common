package com.bboss.cache.service.interfaces;

import com.bboss.cache.bean.approve.ApproveSyncLog;
import com.bboss.cache.dto.approval.QueryApprovalParam;
import com.bboss.common.bean.approveCenter.approveSync.ApprovalSyncDetail;
import com.github.pagehelper.PageInfo;

public interface ApprovalService {
    void approveInfoSync(ApprovalSyncDetail approvalSyncDto);

    void approvalHandRefresh(String operationId, String skuNum, String transID);

    PageInfo<ApproveSyncLog> queryApproveSyncLog(QueryApprovalParam queryApprovalParam);
}