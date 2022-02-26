package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * @author Yuan Diao
 * @date 2022/2/21
 */
public interface SearchService {

    /**
     * @param searchParam 检索条件
     * @return 检索结果
     */
    SearchResult search(SearchParam searchParam);
}
