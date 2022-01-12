package com.atguigu.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import org.w3c.dom.Attr;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Integer id) {
        //如果当前目录为0，返回所有分类的属性
        if(id == 0){
            IPage<AttrGroupEntity> page =this.page(new Query<AttrGroupEntity>().getPage(params));
            return new PageUtils(page);
        }
        //先判断有没有关键词
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        String keyword = (String) params.get("key");
        if (StringUtils.isNotBlank(keyword)){
            wrapper.and((obj)->
                    obj.eq("attr_group_id", keyword).or().like("attr_group_name", keyword)
            );
        }

        wrapper.eq("catelog_id", id);
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }
}