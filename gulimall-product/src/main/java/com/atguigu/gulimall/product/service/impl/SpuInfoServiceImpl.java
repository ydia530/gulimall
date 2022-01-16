package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.VO.BaseAttrs;
import com.atguigu.gulimall.product.VO.Skus;
import com.atguigu.gulimall.product.VO.SpuSaveVo;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


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


        //5. 保存当前spu对应的sku信息
        //5.1 sku的基本信息 ： pms_sku_info
        List<Skus> skus = spuInfo.getSkus();
        if (!CollectionUtils.isEmpty(skus)){
            skus.forEach(item -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
            });
        }



        //5.2 sku图片信息 pms_sku_images
        //5.3 sku销售属性信息 pms_sku_sale_attr_value
    }


    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        baseMapper.insert(spuInfoEntity);
    }

}
