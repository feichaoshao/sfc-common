package com.bboss.cache;

import com.bboss.pub.filter.JwtFilter;
import com.bboss.pub.filter.TrcFilter;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;

import java.security.NoSuchAlgorithmException;

@EnableEncryptableProperties
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableDiscoveryClient
@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = {"com.bboss.cache","com.bboss.pub","com.bboss.common"}, exclude = DataSourceAutoConfiguration.class)
@EnableFeignClients(basePackages = {"com.bboss.common.feign","com.bboss.cache"})
public class CacheApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder().sources(CacheApplication.class).run(args);
    }

    @Bean
    public FilterRegistrationBean<GenericFilterBean> trcFilter() {
        FilterRegistrationBean<GenericFilterBean> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TrcFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<GenericFilterBean> jwtFilter() throws NoSuchAlgorithmException {
        FilterRegistrationBean<GenericFilterBean> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtFilter());
        //订单详情接口
        registration.addUrlPatterns("/v1/ApprovalCenter/queryApprovalSyncLogList");
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
