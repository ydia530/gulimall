package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.VO.Catalog2Vo;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>Title: IndexController</p>
 * Description：
 * date：2020/6/9 14:01
 */
@Controller
public class IndexController {

	@Autowired
	CategoryService categoryService;

	@GetMapping({"/", "/index.html"})
	public String indexPage(Model model){
		List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
		model.addAttribute("categorys", categoryEntities);
		return "index";
	}

	@ResponseBody
	@GetMapping("/index/catalog.json")
	public Map<String, List<Catalog2Vo>> getCatelogJson(){
		Map<String, List<Catalog2Vo>> map = categoryService.getCatelogJson();
		return map;
	}

}
