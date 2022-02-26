package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuReductionDto;
import com.atguigu.common.to.SpuBoundsDto;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.VO.*;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSupInfo(SpuSaveVo spuInfo) {
        //1.保存基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfo, spuInfoEntity);
        this.saveBaseSpuInfo(spuInfoEntity);

        //2. 保存描述图片 pms_spu_info_desc
        List<String> description = spuInfo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", description));
        spuInfoDescService.saveSpuInfoDescription(spuInfoDescEntity);

        //3. 保存图片集  pms_spu_image
        List<String> images = spuInfo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        //4. 保存规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuInfo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());
            productAttrValueEntity.setAttrName(attrService.getById(attr.getAttrId()).getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);

        Bounds bounds = spuInfo.getBounds();
        SpuBoundsDto spuBoundsDto = new SpuBoundsDto();
        BeanUtils.copyProperties(bounds, spuBoundsDto);
        spuBoundsDto.setId(spuInfoEntity.getId());
        R r1 = couponFeignService.saveSpuBounds(spuBoundsDto);
        if (r1.getCode() != 0){
            log.error("保存失败");
        }
        //5. 保存当前spu对应的sku信息
        List<Skus> skus = spuInfo.getSkus();
        if (!CollectionUtils.isEmpty(skus)){
            skus.forEach(item -> {
                //5.1 sku的基本信息 ： pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatelogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        skuInfoEntity.setSkuDefaultImg(image.getImgUrl());
                        break;
                    }
                }
                skuInfoService.save(skuInfoEntity);

                //5.2）、sku的图片信息；pms_sku_image
                List<SkuImagesEntity> skuImagesEntities = item.getImages().stream().map(image -> {
                    SkuImagesEntity imagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(image, imagesEntity);
                    imagesEntity.setSkuId(skuInfoEntity.getSkuId());
                    return imagesEntity;
                }).filter(image->{
                    return !StringUtils.isEmpty(image.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                //5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(at -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(at, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);


                SkuReductionDto skuReductionDto = new SkuReductionDto();
                BeanUtils.copyProperties(item, skuReductionDto);
                skuReductionDto.setSkuId(skuInfoEntity.getSkuId());
                if(skuReductionDto.getFullCount() > 0 || skuReductionDto.getFullPrice().compareTo(new BigDecimal(0)) == 1){
                    R r = couponFeignService.saveSkuReduction(skuReductionDto);
                    if (r.getCode() != 0){
                        log.error("保存失败");
                    }
                }
            });
        }


    }


    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        baseMapper.insert(spuInfoEntity);
    }

    /**
     * spu管理模糊查询
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        // 根据 spu管理带来的条件进行叠加模糊查询
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> w.eq("id", key).or().like("spu_name", key));
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catelog_id", catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 1 组装数据 查出当前spuId对应的所有sku信息
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        // 查询这些sku是否有库存
        List<Long> skuids = skus.stream().map(sku -> sku.getSkuId()).collect(Collectors.toList());
        // 2 封装每个sku的信息

        // 3.查询当前sku所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        // 得到基本属性id
        List<Long> attrIds = baseAttrs.stream().map(attr -> attr.getAttrId()).collect(Collectors.toList());
        // 过滤出可被检索的基本属性id，即search_type = 1
        Set<Long> ids = new HashSet<>(attrService.selectSearchAttrIds(attrIds));
        // 可被检索的属性封装到SkuEsModel.Attrs中
        List<SkuEsModel.Attrs> attrs = baseAttrs.stream()
                .filter(item -> ids.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attr = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attr);
                    return attr;
                }).collect(Collectors.toList());
        // 每件skuId是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            // 远程调用库存系统 查询该sku是否有库存
            List<SkuHasStockVo> hasStock = wareFeignService.getSkuHasStock(skuids);
            // 构造器受保护 所以写成内部类对象
            stockMap = hasStock.stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
            log.debug("服务调用成功" + hasStock);
        } catch (Exception e) {
            log.error("库存服务调用失败: 原因{}", e);
        }

        Map<Long, Boolean> finalStockMap = stockMap;//防止lambda中改变
        // 开始封装es
        List<SkuEsModel> skuEsModels = skus.stream().map(sku -> {
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            // 4 设置库存，只查是否有库存，不查有多少
            if (finalStockMap == null) {
                esModel.setHasStock(true);
            } else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            // TODO 1.热度评分  刚上架是0
            esModel.setHotScore(0L);
            // 设置品牌信息
            BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());

            // 查询分类信息
            CategoryEntity categoryEntity = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(categoryEntity.getName());

            // 保存商品的属性，  查询当前sku的所有可以被用来检索的规格属性，同一spu都一样，在外面查一遍即可
            esModel.setAttrs(attrs);
            return esModel;
        }).collect(Collectors.toList());

        // 5.发给ES进行保存  gulimall-search
        R r = searchFeignService.productStatusUp(skuEsModels);
        if (r.getCode() == 0) {
            // 远程调用成功
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // 远程调用失败 TODO 接口幂等性 重试机制
            /**
             * Feign 的调用流程  Feign有自动重试机制
             * 1. 发送请求执行
             * 2.
             */
        }
    }


}
