package com.atguigu.gulimall.product;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void contextLoads(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello", "diaoyuan");
        String hello = ops.get("hello");
        System.out.println(hello);
    }


    @Test
    public void testRedisonClient(){
        System.out.println(redissonClient);
    }

}
