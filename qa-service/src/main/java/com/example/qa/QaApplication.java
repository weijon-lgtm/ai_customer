package com.example.qa;

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,                    // 排除数据源自动配置
        DataSourceTransactionManagerAutoConfiguration.class, // 排除数据源事务管理器
        MybatisAutoConfiguration.class,                      // 排除MyBatis自动配置
        HibernateJpaAutoConfiguration.class                  // 排除JPA自动配置
})
@EnableFeignClients
public class QaApplication {
    public static void main(String[] args) {
        SpringApplication.run(QaApplication.class, args);
    }
}