package com.bboss.cache.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @Description 铭文密码加密解密接口
 * @Author admin
 * @Date 2024-04-03
 **/
@Slf4j
@Api(value = "cabs-cache Encrypt")
@RestController
@RequestMapping(value = "/business")
public class Encrypt {
    @Autowired
    private StringEncryptor stringEncryptor;

    @PostMapping(value = "/encrypt")
    public String encrypt(@RequestBody String s) {
        String encrypt = stringEncryptor.encrypt(s);
        log.info("encrypt:{}", encrypt);
        // 解秘
        log.info("decrypt:{}", stringEncryptor.decrypt(encrypt));
        return encrypt;
    }

    @PostMapping(value = "/decrypt")
    public String decrypt(@RequestBody String s) {
        String decrypt = stringEncryptor.decrypt(s);
        // 解秘
        log.info("decrypt：{}", decrypt);
        return decrypt;
    }


}
