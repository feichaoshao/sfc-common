package com.bboss.cache.service.interfaces;


public interface HandService {
    void handRefresh(String offerNum, String skuNum, String businessNum) throws Exception;
    void initRefresh() throws Exception;
    String handGetKey(String redisKey) throws Exception;
    String handDeleteKey(String redisKey) throws Exception;
    String handKeys(String redisKey) throws Exception;

}
