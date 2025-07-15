package com.kh21.khpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.kh21.khpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class KhPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(KhPictureBackendApplication.class, args);
    }

}
