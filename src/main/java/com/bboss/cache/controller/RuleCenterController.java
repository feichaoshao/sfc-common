package com.bboss.cache.controller;

import com.alibaba.fastjson.JSON;
import com.bboss.cache.bean.valid.RuleDetail;
import com.bboss.cache.bean.valid.RuleDetailParam;
import com.bboss.cache.bean.valid.RuleSyncLog;
import com.bboss.cache.bean.valid.RuleSyncParam;
import com.bboss.cache.dto.rule.RuleSyncDto;
import com.bboss.cache.service.interfaces.RuleService;
import com.bboss.common.bean.check.rule.RuleDetailDto;
import com.bboss.common.util.CommonUtil;
import com.bboss.pub.msg.BaseRspsMsg;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * @Description OS规则中心交互接口
 * @Author admin
 * @Date 2024-04-03
 **/
@Slf4j
@RestController
@RequestMapping("/business")
@Api(value = "校验服务与规则中心交互接口", tags = "校验服务与规则中心交互")
public class RuleCenterController {
    @Autowired
    private RuleService ruleService;

    /**
     * 查询 分页查询
     **/
    @PostMapping("/ruleSync/pageList")
    public BaseRspsMsg pageList(@RequestBody RuleSyncParam param) {
        BaseRspsMsg result = BaseRspsMsg.ok();
        try{
            PageInfo<RuleSyncLog> ruleSyncLogPageInfo = ruleService.pageList(param);
            result.setData(ruleSyncLogPageInfo);
        }catch (Exception e){
            log.error("查询规则同步信息接口异常：{}",e);
            result = BaseRspsMsg.error("操作失败!");
        }

        return result;
    }

    /**
     * 查询 分页查询
     **/
    @PostMapping("/ruleSync/pageDetailList")
    public BaseRspsMsg pageDetailList(@RequestBody RuleDetailParam param) {
        BaseRspsMsg result = BaseRspsMsg.ok();
        try{
            PageInfo<RuleDetail> ruleDetatilPageInfo = ruleService.pageDetailList(param);
            result.setData(ruleDetatilPageInfo);
        }catch (Exception e){
            log.error("查询规则列表信息接口异常：{}",e);
            result = BaseRspsMsg.error("操作失败!");
        }

        return result;
    }

    /**
     * 确定规则同步信息接口
     * @param offerNum 商品编码
     * @param transIdo 流水号
     * @return 确认规则同步信息同步结果信息
     */
    @RequestMapping(value = "/confirmRuleSync", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public BaseRspsMsg confirmRuleSync(@RequestParam(value = "offerNum", required = false) String offerNum,
                                       @RequestParam(value = "transIdo", required = false) String transIdo){
            return ruleService.confirmRuleSync(offerNum,transIdo);
    }

    @RequestMapping(value = "/ruleSync", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "规则中心同步", notes = "规则中心同步")
    public BaseRspsMsg receiveRuleInfo(@RequestBody RuleSyncDto ruleSyncDto) {
        long startTime = System.currentTimeMillis();
        log.info("【规则中心同步业务规则信息】:{}.", JSON.toJSONString(ruleSyncDto));
        BaseRspsMsg baseRspsMsg = new BaseRspsMsg();
        baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00000_SUCCESS);
        baseRspsMsg.setBizDesc("校验服务接收规则中心同步规则成功！");
        try {
            List<RuleDetailDto> ruleList = ruleSyncDto.getRuleDetailDto();
            if (CommonUtil.isNullList(ruleList)) {
                baseRspsMsg.setBizDesc("规则中心同步数据为空，缓存失败！");
                baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
                return baseRspsMsg;
            }
            ruleService.ruleSync(ruleSyncDto);
        } catch (Exception e) {
            log.error("【规则中心同步业务规则信息处理异常】:{}", e);
            baseRspsMsg.setBizDesc("规则中心同步业务规则信息处理异常，请联系管理员！");
            baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
        }
        log.info("【规则中心同步业务规则缓存处理完成,总用时】:{}，结束！", System.currentTimeMillis() - startTime);
        return baseRspsMsg;
    }

    /**
     * 同步OS规则中心记录定时器接口
     * 参数YYYY-MM-DD HH:MM:SS
     *
     * @param syncDate
     * @return
     */
    @RequestMapping(value = "/syncRuleCenter", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public BaseRspsMsg syncRuleCenter(String syncDate) {
        long startTime = System.currentTimeMillis();
        log.info("定时同步规则数据开始...");
        BaseRspsMsg baseRspsMsg = new BaseRspsMsg();
        baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00000_SUCCESS);
        baseRspsMsg.setBizDesc("业务规则执行记录同步OS规则中心调度成功！");
        try {
            ruleService.syncRuleCenterLog(syncDate);
        } catch (Exception e) {
            log.error("定时同步规则数据异常:{}", e);
            baseRspsMsg.setBizDesc("定时同步规则数据异常！" + e.getMessage());
            baseRspsMsg.setBizCode(BaseRspsMsg.BIZ_CODE_00001_FAILED);
        }
        log.info("定时同步规则数据完成,总用时:{}，结束！", System.currentTimeMillis() - startTime);
        return baseRspsMsg;
    }
}