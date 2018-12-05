package com.mmall.common;

import java.util.Set;

import com.google.common.collect.Sets;

public class Const {
	public static final String CURRENT_USER = "currentUser";
	
	public interface Role{
		int ROLE_CUSTOMER = 0;//普通用户
		int ROLE_ADMIN = 1;//管理员
	}
	
	public interface ProductListOrderBy{
		Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc");
	}
	
	public interface Cart{
		int CHECKED = 1;//即购物车选中状态
		int UN_CHECKED = 0;//即购物车未选中状态
		
		String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
		String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
	}
	
	public static final String EMAIL = "email";
	public static final String USERNAME = "username";
	
	public enum ProductStatusEnum{
		ON_SALE(1,"在线");
		
		private String value;
		private int code;
		public String getValue() {
			return value;
		}
		public int getCode() {
			return code;
		}
		ProductStatusEnum(int code, String value) {
			this.value = value;
			this.code = code;
		}
		
	}
}
