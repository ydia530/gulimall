package com.atguigu.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author Yuan Diao
 * @date 2022/2/15
 */
@Configuration
public class myRedissonConfig {
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException{
        Config config = new Config();
        config.useSingleServer().setAddress("redis://81.70.10.9:6379//");
        return Redisson.create(config);
    }
}
