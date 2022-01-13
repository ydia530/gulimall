package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 品牌
 *
 * @author DY
 * @email ydia530@aucklanduni.ac.nz
 * @date 2022-01-08 13:42:58
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	private Long brandId;

	/**
	 * 品牌名
	 */
	@NotEmpty(message = "品牌吗不能为空")
	private String name;

	/**
	 * 品牌logo地址
	 */
	@NotEmpty
	@URL
	private String logo;

	/**
	 * 介绍
	 */
	private String descript;

	/**
	 * 检索首字母
	 */
	@NotEmpty
//	@Pattern(regexp = "/^[A-Za-z]^$/", message = "首字母必须是一个字母")
	private String firstLetter;

	/**
	 * 排序
	 */
	@NotNull
	@Min(value = 0, message = "排序必须大于0")
	private Integer sort;

	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	private Integer showStatus;

}
