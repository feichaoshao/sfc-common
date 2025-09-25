package com.bboss.cache.dto.approval;

import com.bboss.common.bean.approveCenter.approveSync.ApprovalSyncDetail;
import lombok.Data;

import java.util.List;

/**
 * @Description OS 审批中心同步给业务系统审批数据
 * @Author zhaohf
 * @Date 2024-05-27
 */
@Data
public class ApprovalSyncDto {
    //审批数据同步流水
    private String transIdo;
    //审批数据详情
    private List<ApprovalSyncDetail> approvalSyncDetail;
}