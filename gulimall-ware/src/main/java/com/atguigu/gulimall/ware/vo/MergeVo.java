package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Yuan Diao
 * @date 2022/1/23
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
