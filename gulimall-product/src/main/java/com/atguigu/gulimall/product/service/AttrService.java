package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.VO.AttrResponseVo;
import com.atguigu.gulimall.product.VO.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author DY
 * @email ydia530@aucklanduni.ac.nz
 * @date 2022-01-08 13:42:58
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Long id, Map<String, Object> params, String attrType);

    void saveAttr(AttrVo attr);

    AttrResponseVo getInfoById(Long attrId);

    void updateInfo(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId, String attrType);

    void deleteAttr(List<Long> asList);
}

