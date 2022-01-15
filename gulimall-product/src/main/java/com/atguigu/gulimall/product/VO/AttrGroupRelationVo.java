package com.atguigu.gulimall.product.VO;

import lombok.Data;

/**
 * @author Yuan Diao
 * @date 2022/1/15
 */
@Data
public class AttrGroupRelationVo {

    /**
     * 分组id
     */
    private Long attrGroupId;

    /**
     * 属性id
     */
    private Long attrId;
}
