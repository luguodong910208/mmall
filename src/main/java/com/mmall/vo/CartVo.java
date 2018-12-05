package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

public class CartVo {
	private List<CartProductVo> cartProductVoList;
	private BigDecimal productTotalPrice;
	private Boolean allChecked;
	private String imageHost;
	public List<CartProductVo> getCartProductVoList() {
		return cartProductVoList;
	}
	public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
		this.cartProductVoList = cartProductVoList;
	}
	public BigDecimal getProductTotalPrice() {
		return productTotalPrice;
	}
	public void setProductTotalPrice(BigDecimal productTotalPrice) {
		this.productTotalPrice = productTotalPrice;
	}
	public Boolean getAllChecked() {
		return allChecked;
	}
	public void setAllChecked(Boolean allChecked) {
		this.allChecked = allChecked;
	}
	public String getImageHost() {
		return imageHost;
	}
	public void setImageHost(String imageHost) {
		this.imageHost = imageHost;
	}
	
}
