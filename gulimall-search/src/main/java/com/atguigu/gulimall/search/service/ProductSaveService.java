package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author Yuan Diao
 * @date 2022/2/2
 */


public interface ProductSaveService {
    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
