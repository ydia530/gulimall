package com.atguigu.gulimall.product.VO;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class SpuItemAttrGroup{
    private String groupName;

    /** 两个属性attrName、attrValue */
    private List<SpuBaseAttrVo> attrs;
}
