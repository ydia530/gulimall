package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @author Yuan Diao
 * @date 2022/1/24
 */
@Data
public class PurchaseItemDoneVo {

    private Long itemId;
    private Integer status;
    private String reason;
}
