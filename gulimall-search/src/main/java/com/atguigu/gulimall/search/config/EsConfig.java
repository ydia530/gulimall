package com.atguigu.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Yuan Diao
 * @date 2022/1/29
 */
@Configuration
public class EsConfig {

    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient() {

        RestClientBuilder builder = null;
        // 可以指定多个es
        builder = RestClient.builder(new HttpHost("81.70.10.9", 9200, "http"));

        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
}

