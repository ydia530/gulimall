package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.VO.AttrResponseVo;
import com.atguigu.gulimall.product.VO.AttrVo;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Long id, Map<String, Object> params) {
        //先判断有没有关键词
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        String keyword = (String) params.get("key");
        if (StringUtils.isNotBlank(keyword)){
            wrapper.and((obj)->
                    obj.eq("attr_id", keyword).or().like("attr_name", keyword).or().eq("catelog_id", keyword)
            );
        }

        //如果当前目录为0，返回所有分类的属性
        if(id == 0){
            IPage<AttrEntity> page =this.page(new Query<AttrEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }

        wrapper.eq("catelog_id", id);
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);

        //先查询所有的分类名，组成map
        List<Long> categoryIds = page.getRecords().stream().map(attrEntity -> attrEntity.getCatelogId()).collect(Collectors.toList());
        Map<Long, String> categoryDict = categoryDao.selectBatchIds(categoryIds).stream().
                collect(Collectors.toMap(categoryEntity -> categoryEntity.getCatId(), categoryEntity -> categoryEntity.getName()));

        List<AttrResponseVo> responseVos = page.getRecords().stream().map(attrEntity -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity,attrResponseVo);
            attrResponseVo.setCatelogName(categoryDict.get(attrEntity.getCatelogId()));
            return attrResponseVo;
        }).collect(Collectors.toList());
        responseVos.forEach(System.out::println);
        pageUtils.setList(responseVos);
        return pageUtils;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
        attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
        attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
    }

}