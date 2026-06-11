package com.trial.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 试炼坊考试系统 - 主启动类
 */
@SpringBootApplication
@MapperScan("com.trial.server.mapper")
public class TrialServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrialServerApplication.class, args);
        System.out.println("============================================");
        System.out.println("      试炼坊考试系统 启动成功！");
        System.out.println("      API文档: http://localhost:8088/api/doc.html");
        System.out.println("============================================");
    }
}
