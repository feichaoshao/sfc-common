package com.bboss.cache.config;

import com.bboss.cache.service.interfaces.HandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisCacheConfig implements ApplicationRunner {

    @Autowired
    private HandService handService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.delRdeisCache();
    }

    public void delRdeisCache() throws Exception {
        log.info("初始化操作开始刷新redis缓存开始...");
        handService.initRefresh();
        log.info("初始化操作开始刷新redis缓存结束...");
    }
}