package com.bboss.cache.dto.rule;

import com.bboss.common.bean.check.rule.RuleDetailDto;
import lombok.Data;

import java.util.List;

/*
 * @Description OS规则中心同步结构
 * @Author admin
 * @Date 2024-04-03
 * @Param
 **/
@Data
public class RuleSyncDto {
    //规则同步流水
    private String transIdo;
    //规则同步列表
    private List<RuleDetailDto> ruleDetailDto;
}