package com.bboss.cache.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.bboss.pub.interceptor.MyBatisEncryptionInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/*
 * @Description
 * @Author admin
 * @Date 2024-04-03
 * @Param SessionFactoryConfig 配置数据源及mybatis映射文件及接口位置
 **/
@Slf4j
@Configuration
@MapperScan(basePackages = SessionFactoryConfig.PACKAGE, sqlSessionFactoryRef = "SqlSessionFactory")
public class SessionFactoryConfig {

    /**
     * @Fields field:field:指代：mybatis-config.xml
     */
    private static String MYBATIS_CONFIG = "mybatis-config.xml";

    /**
     * @Fields field:dao层接口所在的位置
     */
    static final String PACKAGE = "com.bboss.cache.validDao";

    /**
     * @Fields field:mapper映射文件的位置
     */
    private static final String MAPPER_LOCATION = "com/bboss/cache/validDao/impl/*.xml";

    @Value("${spring.datasource.mysqljdbc.url}")
    private String url;

    @Value("${spring.datasource.mysqljdbc.username}")
    private String user;

    @Value("${spring.datasource.mysqljdbc.password}")
    private String password;

    @Value("${spring.datasource.mysqljdbc.driverClassName}")
    private String driverClass;

    @Value("${spring.datasource.mysqljdbc.maxActive}")
    private int maxActive;

    @Value("${spring.datasource.mysqljdbc.maxWait}")
    private long maxWait;

    @Value("${spring.datasource.mysqljdbc.initialSize}")
    private int initialSize;

    @Value("${spring.datasource.mysqljdbc.minIdle}")
    private int minIdle;

    @Value("${spring.datasource.mysqljdbc.timeBetweenEvictionRunsMillis}")
    private int timeBetweenEvictionRunsMillis;

    @Value("${spring.datasource.mysqljdbc.minEvictableIdleTimeMillis}")
    private int minEvictableIdleTimeMillis;

    @Value("${spring.datasource.mysqljdbc.validationQuery}")
    private String validationQuery;

    @Value("${spring.datasource.mysqljdbc.testWhileIdle}")
    private boolean testWhileIdle;

    @Value("${spring.datasource.mysqljdbc.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${spring.datasource.mysqljdbc.testOnReturn}")
    private boolean testOnReturn;

    @Value("${spring.datasource.mysqljdbc.connectionErrorRetryAttempts}")
    private int connectionErrorRetryAttempts;

    @Value("${spring.datasource.mysqljdbc.breakAfterAcquireFailure}")
    private boolean breakAfterAcquireFailure;

    /**
     * @param : 参数
     * @return DataSource    返回类型
     * @throws
     * @Title: orderDataSource
     * @Description: 数据库连接池的配置
     */
    @Bean(name = "DataSource")
    public DataSource orderDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxWait(maxWait);
        dataSource.setInitialSize(initialSize);
        dataSource.setMinIdle(minIdle);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestWhileIdle(testWhileIdle);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnReturn(testOnReturn);
        dataSource.setConnectionErrorRetryAttempts(connectionErrorRetryAttempts);
        dataSource.setBreakAfterAcquireFailure(breakAfterAcquireFailure);
        return dataSource;
    }

    /**
     * @param : 参数
     * @return DataSourceTransactionManager    返回类型
     * @throws
     * @Title: orderTransactionManager
     * @Description: 事物管理器的配置
     */
    @Bean(name = "TransactionManager")
    public DataSourceTransactionManager orderTransactionManager() {
        return new DataSourceTransactionManager(orderDataSource());
    }

    @Bean(name = "SqlSessionFactory")
    public SqlSessionFactory orderSqlSessionFactory(@Qualifier("DataSource") DataSource orderDataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setConfigLocation(new ClassPathResource(MYBATIS_CONFIG));
        sessionFactory.setDataSource(orderDataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(SessionFactoryConfig.MAPPER_LOCATION));
        /*SqlSessionFactory factory = sessionFactory.getObject();
        log.info("SqlSessionFactoryMysql obtained");

        factory.getConfiguration().addInterceptor(new MyBatisEncryptionInterceptor());
        log.info("MyBatisEncryptionInterceptor obtained");*/
        return sessionFactory.getObject();
    }
}