package com.bboss.cache.bean.valid;

import lombok.Data;

import java.util.Date;

/*
 * @Description 规则执行记录表
 * @Author admin
 * @Date 2024-04-03
 * @Param
 **/
@Data
public class RuleExecuteLog {
    private String id;
    private String preCheckTransIdo;//预校验接口请求流水
    private String webCheckTransIdo;//创建订单拼接一个标识
    private String offerNum;//商品编码
    private String packageNum;//包编码
    private String skuNum;//产品编码
    private String ruleNum;//触发规则编码
    private String ruleName;//触发规则名称
    private String checkType;//触发规则类型0商品级1产品级
    private String checkCode;//触发规则结果00成功99失败
    private String checkDesc;//规则校验结果
    private Date createDate;//创建时间

    public RuleExecuteLog() {
    }

    public RuleExecuteLog(String id, String preCheckTransIdo, String webCheckTransIdo, String offerNum, String packageNum, String skuNum,
                          String ruleNum, String ruleName, String checkType, String checkCode, String checkDesc) {
        this.id = id;
        this.preCheckTransIdo = preCheckTransIdo;
        this.webCheckTransIdo = webCheckTransIdo;
        this.offerNum = offerNum;
        this.packageNum = packageNum;
        this.skuNum = skuNum;
        this.ruleNum = ruleNum;
        this.ruleName = ruleName;
        this.checkType = checkType;
        this.checkCode = checkCode;
        this.checkDesc = checkDesc;
    }
}