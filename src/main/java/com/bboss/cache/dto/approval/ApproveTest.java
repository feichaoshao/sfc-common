package com.bboss.cache.dto.approval;

import lombok.Data;

/**
 * @Description OS 审批中心同步给业务系统审批数据
 * @Author zhaohf
 * @Date 2024-05-27
 */
@Data
public class ApproveTest {
    private String type;
    private String hostCompany;
    private String poordPackageNum;
    private String prodordSkuNum;
    private String skuBusinessNum;
    private String taskId;
    private String firstName;
    private String businessCode;
}