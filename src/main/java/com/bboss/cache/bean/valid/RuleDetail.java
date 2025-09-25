package com.bboss.cache.bean.valid;

import lombok.Data;

import java.util.Date;
import java.util.List;

/*
 * @Description 规则同步详情表
 * @Author admin
 * @Date 2024-04-03
 * @Param
 **/
@Data
public class RuleDetail {
    //id
    private String id;
    //商品编码
    private String offerNum;
    //包编码
    private String packageNum;
    //产品编码
    private String skuNum;
    //产品操作编码同BPM配置操作
    private String businessNum;
    //组合规则标识
    private String combinationKey;
    //预留目标服务，业务对应校验服务
    private String targetService;
    //规则编码
    private String ruleNum;
    //规则名称
    private String ruleName;
    //规则详细描述
    private String ruleDesc;
    //规则模式（0服务，1脚本）
    private String ruleMode;
    //规则入口接口地址、类名、方法名
    private String ruleEntry;
    //规则脚本
    private String ruleContent;
    //规则类型（0：特殊规则1：通用规则）
    private String ruleType;
    //校验类型（0：商品级校验只走一次1：产品级校验按产品走
    private String checkType;
    //规则状态
    private String ruleStatus;
    //规则标签编码
    private String ruleLabel;
    //规则目录编码
    private String ruleDirectory;
    //规则执行顺序（规则中心没有，交付控制台可以使用）
    private String ruleExecuteSort;
    //系统来源
    private String systemNum;
    //规则场景（自定义但要结合业务：1：开通、2：注销、3：暂停、4：恢复、5：资费变更、9：业务变更、10：预受理、71：新增产品、72：注销产品、66：成员属性变更、61成员新增、62：成员删除）
    private String businessType;
    //规则类型（自定义但要结合业务：check：校验、before_prov：送开通前、after_prov：送开通后、audit：审批、archive_platform：归档平台、archive_billing：归档计费、archive_province：归档省、archive_settle：归档结算）
    private String businessScene;
    //描述
    private String description;
    //创建人编码
    private String createNum;
    //创建时间
    private Date createTime;
    //更新人编码
    private String updateNum;
    //更新时间
    private Date updateTime;
    //脚本可支持多个在交付控制台进行配置！仅供业务逻辑使用
    private List<RuleDetailScript> ruleDetailScriptList;
}
