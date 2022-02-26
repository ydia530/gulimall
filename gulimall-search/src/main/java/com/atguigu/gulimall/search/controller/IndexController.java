package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.SearchService;
import com.atguigu.gulimall.search.vo.SearchParam;

import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: IndexController</p>
 * Description：
 * date：2020/6/9 14:01
 */
@Controller
public class IndexController {

    @Autowired
    SearchService searchService;


    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request){

        // 获取路径原生的查询属性
        searchParam.set_queryString(request.getQueryString());
        // ES中检索到的结果 传递给页面
        SearchResult result = searchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }

}
