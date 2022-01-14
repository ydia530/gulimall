package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.VO.AttrResponseVo;
import com.atguigu.gulimall.product.VO.AttrVo;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryService categoryService;


    @Override
    public PageUtils queryPage(Long id, Map<String, Object> params, String attrType) {
        //先判断有没有关键词
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();

        //根据属性类型判断
        wrapper.eq("attr_type", attrType.equals("base") ?1:0);
        String keyword = (String) params.get("key");
        if (StringUtils.isNotBlank(keyword)){
            wrapper.and((obj)->
                    obj.eq("attr_id", keyword).or().like("attr_name", keyword).or().eq("catelog_id", keyword)
            );
        }

        IPage<AttrEntity> page = null;
        //如果当前目录为0，返回所有分类的属性
        if(id == 0){
            page =this.page(new Query<AttrEntity>().getPage(params), wrapper);
        } else{
            wrapper.eq("catelog_id", id);
            page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        }
        PageUtils pageUtils = new PageUtils(page);
        //先查询所有的分类名，组成map
        List<Long> attrIds = page.getRecords().stream().map(attrEntity -> attrEntity.getAttrId()).collect(Collectors.toList());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in("attr_id", attrIds);
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(queryWrapper);

        Map<Long, Long> relationDict = relationEntities.stream().collect(Collectors.toMap(relationEntity ->
                relationEntity.getAttrId(), relationEntity -> relationEntity.getAttrGroupId()));

        Map<Long, String> categoryDict;
        Map<Long, String> groupDict;


        if ("base".equals(attrType)){
            List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectBatchIds(relationDict.values());

            groupDict = attrGroupEntities.stream()
                    .collect(Collectors.toMap(attrGroupEntity -> attrGroupEntity.getAttrGroupId(), attrGroupEntity -> attrGroupEntity.getAttrGroupName()));

            categoryDict = categoryDao.selectBatchIds(
                    attrGroupEntities.stream().map(attrGroupEntity -> attrGroupEntity.getCatelogId())
                            .collect(Collectors.toList())).stream().
                    collect(Collectors.toMap(categoryEntity -> categoryEntity.getCatId(), categoryEntity -> categoryEntity.getName()));


        } else {
            categoryDict = categoryDao.selectBatchIds(page.getRecords().stream()
                    .map(attrEntity -> attrEntity.getCatelogId()).collect(Collectors.toList())).stream().
                    collect(Collectors.toMap(categoryEntity -> categoryEntity.getCatId(), categoryEntity -> categoryEntity.getName()));
            groupDict = null;
        }

        List<AttrResponseVo> responseVos = page.getRecords().stream().map(attrEntity -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity,attrResponseVo);
            attrResponseVo.setCatelogName(categoryDict.get(attrEntity.getCatelogId()));
            if ("base".equals(attrType)){
                attrResponseVo.setGroupName(groupDict.get(relationDict.get(attrEntity.getAttrId())));
            }
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
        if ("1".equals(attr.getAttrType())){
            System.out.println("+++++++++++++++++++");
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrSort(0);
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public AttrResponseVo getInfoById(Long attrId) {
        AttrEntity attr = baseMapper.selectById(attrId);
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        BeanUtils.copyProperties(attr, attrResponseVo);
        attrResponseVo.setCatelogPath(categoryService.findCatelogPath(attr.getCatelogId()));
        AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.
                selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_id", attrResponseVo.getAttrId()));
        attrResponseVo.setAttrGroupId(relationEntity.getAttrGroupId());
        attrResponseVo.setGroupName(attrGroupDao.selectById(relationEntity.getAttrGroupId()).getAttrGroupName());
        return attrResponseVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(AttrVo attr) {
        //先更新主体
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        //更新属性与属性分组关联
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrGroupId(attr.getAttrGroupId());
        attrAttrgroupRelationDao.update(
                relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));

    }

}
