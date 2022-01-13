package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查处所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //找到一级分类，并组装成树
        List<CategoryEntity> firstLevelMenus = categoryEntities.stream()
                .filter(categoryEntity -> categoryEntity.getCatLevel().equals(1)).collect(Collectors.toList());

        return firstLevelMenus.stream()
                .map(parentMenu -> {
                        parentMenu.setChildren(consutructTree(parentMenu, categoryEntities));
                        return parentMenu;
                    })
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenusByIds(List<Long> asList) {
        //TODO 需要检查相关引用
        baseMapper.deleteBatchIds(asList);

    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        LinkedList<Long> path = new LinkedList<>();
        path.add(catelogId);
        CategoryEntity category = baseMapper.selectById(catelogId);
        while ( category.getParentCid() != 0L){
            category = baseMapper.selectById(category.getParentCid());
            path.addFirst(category.getCatId());
        }
        return path.toArray(new Long[0]);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

    }

    /**
     * 递归查找子菜单,构建结构树
     */
    private List<CategoryEntity> consutructTree(CategoryEntity parentMenu, List<CategoryEntity> categoryEntities) {
        return categoryEntities.stream().
                filter(categoryEntity -> categoryEntity.getParentCid().equals(parentMenu.getCatId()))
                .map(categoryEntity -> {categoryEntity.setChildren(consutructTree(categoryEntity, categoryEntities)); return categoryEntity;})
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }


}
