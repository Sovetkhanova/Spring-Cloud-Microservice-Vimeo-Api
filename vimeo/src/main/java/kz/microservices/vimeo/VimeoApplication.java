package kz.microservices.vimeo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableCaching
@EnableEurekaClient
@EnableFeignClients
@SpringBootApplication
public class VimeoApplication {

    public static void main(String[] args) {
        SpringApplication.run(VimeoApplication.class, args);
    }

}