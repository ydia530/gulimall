package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author DY
 * @email ydia530@aucklanduni.ac.nz
 * @date 2022-01-08 14:31:07
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
