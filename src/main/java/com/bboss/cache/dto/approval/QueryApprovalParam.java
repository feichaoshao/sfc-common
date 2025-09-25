package com.bboss.cache.dto.approval;

import com.bboss.pub.msg.JsonObj;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "查询参数列表",description = "查询参数列表")
public class QueryApprovalParam extends JsonObj {
	@ApiModelProperty(value = "流水号",required = false)
    private String transId;
	@ApiModelProperty(value = "当前页码",required = false)
    private String currentPage;
	@ApiModelProperty(value = "每页条数",required = false)
    private String pageSize;

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }
}
