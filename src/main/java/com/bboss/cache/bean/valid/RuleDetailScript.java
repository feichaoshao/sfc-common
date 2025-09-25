package com.bboss.cache.bean.valid;

import lombok.Data;

import java.util.Date;

@Data
public class RuleDetailScript {
    //脚本名称唯一标识
    private String scriptName;
    //规则id
    private String ruleDetailId;
    //公共规则标识-提供特殊处理使用
    private String combinationKey;
    //脚本内容
    private String scriptContent;
    //脚本类型1：js 2：python 3：groovy
    private int scriptType;
    //脚本状态1：生效
    private int scriptStatus;
    //描述
    private String description;
    //执行顺序由小到大
    private int displayOrder;
    //创建人编码
    private String createNum;
    //创建时间
    private Date createTime;
    //更新人编码
    private String updateNum;
    //更新时间
    private Date updateTime;
}