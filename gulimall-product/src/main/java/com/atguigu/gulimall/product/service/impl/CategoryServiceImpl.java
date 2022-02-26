package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.VO.Catalog2Vo;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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
     * 级联更新所有数据			[分区名默认是就是缓存的前缀] SpringCache: 不加锁
     * @CacheEvict: 缓存失效模式		--- 页面一修改 然后就清除这两个缓存
     * key = "'getLevel1Categorys'" : 记得加单引号 [子解析字符串]
     * @Caching: 同时进行多种缓存操作
     * @CacheEvict(value = {"category"}, allEntries = true) : 删除这个分区所有数据
     * @CachePut: 这次查询操作写入缓存
     */
//    @CacheEvict(value = {"category"}, allEntries = true)
//	@CachePut
//    @Caching(evict = {
//            @CacheEvict(value = {"category"}, key = "'getLevel1Categorys'"),
//            @CacheEvict(value = {"category"}, key = "'getCatelogJson'")
//    })
//

    /**
     * @Cacheable: 当前方法的结果需要缓存 并指定缓存名字
     *  缓存的value值 默认使用jdk序列化
     *  默认ttl时间 -1
     *	key: 里面默认会解析表达式 字符串用 ''
     *
     *  自定义:
     *  	1.指定生成缓存使用的key
     *  	2.指定缓存数据存活时间	[配置文件中修改]
     *  	3.将数据保存为json格式
     *
     *  sync = true: --- 开启同步锁
     *
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
        // 测试能否缓存null值
//		return null;
    }


    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatelogJson() {
        String catelog = stringRedisTemplate.opsForValue().get("catelog");
        if (StringUtils.isEmpty(catelog)){
            Map<String, List<Catalog2Vo>> catelogs = getCatelogJsonFromDb();
            stringRedisTemplate.opsForValue().set("catelog", JSON.toJSONString(catelogs), 1, TimeUnit.DAYS);
            return catelogs;
        }
        System.out.println("缓存命中");
        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catelog, new TypeReference<Map<String, List<Catalog2Vo>>>(){});
        return result;
    }

    public Map<String, List<Catalog2Vo>> getCatelogJsonFromDb() {

        List<CategoryEntity> categoryEntities = this.list();
        //查出所有一级分类
        List<CategoryEntity> level1Categories = getCategoryByParentCid(categoryEntities, 0L);
        Map<String, List<Catalog2Vo>> listMap = level1Categories.stream().collect(Collectors.toMap(k->k.getCatId().toString(), v -> {
            //遍历查找出二级分类
            List<CategoryEntity> level2Categories = getCategoryByParentCid(categoryEntities, v.getCatId());
            List<Catalog2Vo> catalog2Vos=null;
            if (level2Categories!=null){
                //封装二级分类到vo并且查出其中的三级分类
                catalog2Vos = level2Categories.stream().map(cat -> {
                    //遍历查出三级分类并封装
                    List<CategoryEntity> level3Catagories = getCategoryByParentCid(categoryEntities, cat.getCatId());
                    List<Catalog2Vo.Catalog3Vo> catalog3Vos = null;
                    if (level3Catagories != null) {
                        catalog3Vos = level3Catagories.stream()
                                .map(level3 -> new Catalog2Vo.Catalog3Vo(level3.getParentCid().toString(), level3.getCatId().toString(), level3.getName()))
                                .collect(Collectors.toList());
                    }
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), cat.getCatId().toString(), cat.getName(), catalog3Vos);
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        return listMap;
    }

    private List<CategoryEntity> getCategoryByParentCid(List<CategoryEntity> categoryEntities, long l) {
        List<CategoryEntity> collect = categoryEntities.stream().filter(cat -> cat.getParentCid() == l).collect(Collectors.toList());
        return collect;
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
