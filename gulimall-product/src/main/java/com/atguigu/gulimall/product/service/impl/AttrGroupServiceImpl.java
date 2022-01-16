package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.VO.AttrGroupWIthAttrsVo;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.service.AttrService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;


/**
 * @author diaoyuan
 */
@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;



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

        //先判断有没有关键词
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        String keyword = (String) params.get("key");
        if (StringUtils.isNotBlank(keyword)){
            wrapper.and((obj)->
                    obj.eq("attr_group_id", keyword).or().like("attr_group_name", keyword)
            );
        }

        //如果当前目录为0，返回所有分类的属性
        if(id == 0){
            IPage<AttrGroupEntity> page =this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }

        wrapper.eq("catelog_id", id);
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    /**
     * 根据分类id 查出所有属性分组及属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWIthAttrsVo> getAttrGroupWithAttrsByCategoryId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities =
                this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        List<AttrGroupWIthAttrsVo> collect = attrGroupEntities.stream().map(group -> {
            AttrGroupWIthAttrsVo attrGroupWIthAttrsVo = new AttrGroupWIthAttrsVo();
            BeanUtils.copyProperties(group, attrGroupWIthAttrsVo);
            attrGroupWIthAttrsVo.setAttrs(attrService.getRelationAttr(attrGroupWIthAttrsVo.getAttrGroupId()));
            return attrGroupWIthAttrsVo;
        }).collect(Collectors.toList());
        return collect;
    }

}
