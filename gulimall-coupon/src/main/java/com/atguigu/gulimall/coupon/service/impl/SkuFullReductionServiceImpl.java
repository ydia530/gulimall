package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.to.MemberPrice;
import com.atguigu.common.to.SkuReductionDto;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import com.atguigu.gulimall.coupon.service.MemberPriceService;
import com.atguigu.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.coupon.dao.SkuFullReductionDao;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.atguigu.gulimall.coupon.service.SkuFullReductionService;


/**
 * @author diaoyuan
 */
@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionDto skuReductionDto) {
        // 保存sku的优惠、满减、会员价格等信息  [跨库] sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionDto.getSkuId());//skuId
        skuLadderEntity.setFullCount(skuReductionDto.getFullCount());
        skuLadderEntity.setDiscount(skuReductionDto.getDiscount());
        // 是否参加其他优惠
        skuLadderEntity.setAddOther(skuReductionDto.getCountStatus());
        // 有的满减条件才保存

        if(skuReductionDto.getFullCount() > 0){
            skuLadderService.save(skuLadderEntity);
        }
//        skuLadderService.save(skuLadderEntity);

        // sms_sku_full_reduction // 满减
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionDto, skuFullReductionEntity);
        if((skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0")) == 1)){
            this.save(skuFullReductionEntity);
        }

        // sms_member_price  保存价格等属性
        List<MemberPrice> memberPrice = skuReductionDto.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(m -> {
            MemberPriceEntity priceEntity = new MemberPriceEntity();
            priceEntity.setSkuId(skuReductionDto.getSkuId());
            priceEntity.setMemberLevelId(m.getId());
            priceEntity.setMemberLevelName(m.getName());
            priceEntity.setMemberPrice(m.getPrice());
            priceEntity.setAddOther(1);

            return priceEntity;

        }).filter(item ->
                // 输入的商品价格必须要大于0才保存
                (item.getMemberPrice().compareTo(new BigDecimal("0")) > 0)
        ).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }
}
