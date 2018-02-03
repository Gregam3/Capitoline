//package com.greg.configuration;
//
//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.orm.hibernate4.HibernateTransactionManager;
//import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//import java.util.Properties;
//
//@Configuration
//@Component
//@EnableTransactionManagement
//public class JdbcConfiguration {
//    private static final String USERNAME = "SYSTEM";
//    private static final String PASSWORD = "password";
//    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
//    private static final String URL = "localhost";
//    private static final int PORT = 1521;
//    private static final String SID = "XE";
//
//    private static final String ENTITYMANAGER_PACKAGES_TO_SCAN = "com.greg.entity";
//    private static final String HIBERNATE_DIALECT = "org.hibernate.dialect.Oracle11gDialect";
//    private static final String HIBERNATE_DEFAULT_SCHEMA = "SYSTEM";
//    private static final boolean HIBERNATE_SHOW_SQL = true;
//
////    @ConfigurationProperties(prefix = "spring.datasource")
////    @Bean
////    @Primary
////    public DataSource dataSource() {
////        return DataSourceBuilder.create()
////                .username(USERNAME)
////                .password(PASSWORD)
////                .driverClassName(DRIVER)
////                .url("jdbc:oracle:thin@" + URL + ":" +PORT + ":" +SID)
////                .build();
////    }
//
//    @Bean
//    public LocalSessionFactoryBean sessionFactory() {
//        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
//        sessionFactoryBean.setDataSource(dataSource());
//        sessionFactoryBean.setPackagesToScan(ENTITYMANAGER_PACKAGES_TO_SCAN);
//        Properties hibernateProperties = new Properties();
//        hibernateProperties.put("hibernate.dialect", HIBERNATE_DIALECT);
//        hibernateProperties.put("hibernate.show_sql", HIBERNATE_SHOW_SQL);
//        hibernateProperties.put("hibernate.default_schema", HIBERNATE_DEFAULT_SCHEMA);
////        hibernateProperties.put("hibernate.hbm2ddl.auto",
////                HIBERNATE_HBM2DDL_AUTO);
//        sessionFactoryBean.setHibernateProperties(hibernateProperties);
//
//        return sessionFactoryBean;
//    }
//
//    @Bean
//    public HibernateTransactionManager transactionManager() {
//        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
//        transactionManager.setSessionFactory(sessionFactory().getObject());
//        return transactionManager;
//    }
//}