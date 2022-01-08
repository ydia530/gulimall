package com.atguigu.gulimall.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 *
 *
 * @author DY
 * @email ydia530@aucklanduni.ac.nz
 * @date 2022-01-08 14:31:07
 */
@Data
@TableName("undo_log")
public class UndoLogEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@TableId
	private Long id;
	/**
	 *
	 */
	private Long branchId;
	/**
	 *
	 */
	private String xid;
	/**
	 *
	 */
	private String context;
	/**
	 *
	 */
	private byte[] rollbackInfo;
	/**
	 *
	 */
	private Integer logStatus;
	/**
	 *
	 */
	private Date logCreated;
	/**
	 *
	 */
	private Date logModified;
	/**
	 *
	 */
	private String ext;

}
