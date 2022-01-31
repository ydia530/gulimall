package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Yuan Diao
 * @date 2022/1/21
 */
@Data
public class SpuBoundsDto {
    private Long id;

    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
