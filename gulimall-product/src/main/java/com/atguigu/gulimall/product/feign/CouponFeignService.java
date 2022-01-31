package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionDto;
import com.atguigu.common.to.SpuBoundsDto;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Yuan Diao
 * @date 2022/1/21
 */

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsDto spuBoundsDto);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionDto skuReductionDto);
}
