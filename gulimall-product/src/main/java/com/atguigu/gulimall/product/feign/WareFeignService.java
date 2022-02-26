package com.atguigu.gulimall.product.feign;

import com.atguigu.gulimall.product.VO.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author Yuan Diao
 * @date 2022/2/2
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    List<SkuHasStockVo> getSkuHasStock(List<Long> skuids);
}
