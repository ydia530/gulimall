package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.VO.ItemSaleAttrVo;
import com.atguigu.gulimall.product.VO.SkuItemVo;
import com.atguigu.gulimall.product.VO.SpuItemAttrGroup;
import com.atguigu.gulimall.product.config.MyThreadConfig;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * SKU 区间模糊查询
     * key: 华为
     * catelogId: 225
     * brandId: 2
     * min: 2
     * max: 2
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w -> w.eq("sku_id", key).or().like("sku_name", key));
        }
        // 三级id没选择不应该拼这个条件  没选应该查询所有
        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(min)){
            // gt : 大于;  ge: 大于等于
            wrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if(!StringUtils.isEmpty(max)){
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal("0")) == 1){
                    // le: 小于等于
                    wrapper.le("price", max);
                }
            } catch (Exception e) {
                System.out.println("SkuInfoServiceImpl：前端传来非数字字符");
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuInfoEntities;
    }


    /**
     * 查询页面详细内容
     */
    @Override // SkuInfoServiceImpl
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1 sku基本信息
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //3 获取spu销售属性组合 list
            List<ItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBuSpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(res -> {
            //4 获取spu介绍
            SpuInfoDescEntity spuInfo = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfo);
        }, executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //5 获取spu规格参数信息
            List<SpuItemAttrGroup> attrGroups = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroups);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //2 sku图片信息
            List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);

        CompletableFuture.allOf(imageFuture,saleAttrFuture,descFuture,baseAttrFuture).get();



//        CompletableFuture<SkuInfoEntity> infoFutrue = CompletableFuture.supplyAsync(() -> {
//            //1 sku基本信息
//            SkuInfoEntity info = getById(skuId);
//            skuItemVo.setInfo(info);
//            return info;
//        }, executor);
        // 无需获取返回值
//        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
//            //2 sku图片信息
//            List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
//            skuItemVo.setImages(images);
//        }, executor);
        // 在1之后
//        CompletableFuture<Void> saleAttrFuture = infoFutrue.thenAcceptAsync(res -> {
//            //3 获取spu销售属性组合 list
//            List<ItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBuSpuId(res.getSpuId());
//            skuItemVo.setSaleAttr(saleAttrVos);
//        },executor);
        // 在1之后
//        CompletableFuture<Void> descFuture = infoFutrue.thenAcceptAsync(res -> {
//            //4 获取spu介绍
//            SpuInfoDescEntity spuInfo = spuInfoDescService.getById(res.getSpuId());
//            skuItemVo.setDesc(spuInfo);
//        },executor);
        // 在1之后
//        CompletableFuture<Void> baseAttrFuture = infoFutrue.thenAcceptAsync(res -> {
//            //5 获取spu规格参数信息
//            List<SpuItemAttrGroup> attrGroups = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
//            skuItemVo.setGroupAttrs(attrGroups);
//        }, executor);

//        // 6.查询当前sku是否参与秒杀优惠
//        CompletableFuture<Void> secKillFuture = CompletableFuture.runAsync(() -> {
//            R skuSeckillInfo = seckillFeignService.getSkuSeckillInfo(skuId);
//            if (skuSeckillInfo.getCode() == 0) {
//                // 注意null的问题
//                SeckillSkuRedisTo data = skuSeckillInfo.getData(new TypeReference<SeckillSkuRedisTo>() {});
//                SeckillInfoVo seckillInfoVo = new SeckillInfoVo();
//                BeanUtils.copyProperties(data,seckillInfoVo);
//                skuItemVo.setSeckillInfoVo(seckillInfoVo);
//            }
//        }, executor);
        // 等待所有任务都完成再返回
//        CompletableFuture.allOf(imageFuture,saleAttrFuture,descFuture,baseAttrFuture,secKillFuture).get();
//        CompletableFuture.allOf(imageFuture,saleAttrFuture,descFuture,baseAttrFuture).get();
        return skuItemVo;
    }

}
