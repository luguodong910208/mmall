package com.mmall.service.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
@Service("iCartService")
public class CartServiceImpl implements ICartService {
	@Autowired
	private CartMapper cartMapper;
	
	@Autowired
	private ProductMapper productMapper;
	/**
	 * 添加商品到购物车
	 * @param userId
	 * @param count
	 * @param productId
	 * @return
	 */
	public ServerResponse<CartVo> add(Integer userId, Integer count, Integer productId){
		if(productId == null || count == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		
		Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
		if(cart == null){
			//这个产品在购物车中不存在，需要新增一个这个产品的记录
			Cart cartItem = new Cart();
			cartItem.setQuantity(count);
			cartItem.setChecked(Const.Cart.CHECKED);
			cartItem.setProductId(productId);
			cartItem.setUserId(userId);
			cartMapper.insert(cartItem);
		} else {
			//这个产品已经在购物车中存在
			//如果产品已经存在，数量相加
			count = cart.getQuantity() + count;
			cart.setQuantity(count);
			cartMapper.updateByPrimaryKeySelective(cart);
		}
		return this.list(userId);
	}
	/**
	 * 更新产品数量
	 * @param userId
	 * @param count
	 * @param productId
	 * @return
	 */
	public ServerResponse<CartVo> update(Integer userId, Integer count, Integer productId){
		if(productId == null || count == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		
		Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
		if(cart != null){
			cart.setQuantity(count);
		}
		cartMapper.updateByPrimaryKeySelective(cart);
		return this.list(userId);
	}
	/**
	 * 从购物车中删除商品
	 * @param userId
	 * @param productIds
	 * @return
	 */
	public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds){
		List<String> productList = Splitter.on(",").splitToList(productIds);
		if(CollectionUtils.isEmpty(productList)){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		cartMapper.deleteByUserIdProductIds(userId, productList);
		return this.list(userId);
	}
	/**
	 * 查询购物车列表
	 * @param userId
	 * @return
	 */
	public ServerResponse<CartVo> list(Integer userId){
		CartVo cartVo = this.getCartVoLimit(userId);
		return ServerResponse.createBySuccess(cartVo);
	}
	/**
	 * 选择或者反选
	 */
	public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked){
		cartMapper.checkedOrUnCheckedProduct(userId, productId, checked);
		return this.list(userId);
	}
	/**
	 * 获取用户购物车内的总的产品数量
	 * @param userId
	 * @return
	 */
	public ServerResponse<Integer> getCartProductCount(Integer userId){
		if(userId == null){
			return ServerResponse.createBySuccess(0);
		}
		return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
	}
	/**
	 * 购物车功能核心代码
	 * @param userId
	 * @return
	 */
	private CartVo getCartVoLimit(Integer userId){
		CartVo cartVo = new CartVo();
		List<Cart> cartList = cartMapper.selectCartByUserId(userId);
		List<CartProductVo> cartProductVoList = Lists.newArrayList();
		
		BigDecimal cartTotalPrice = new BigDecimal("0");
		
		if(CollectionUtils.isNotEmpty(cartList)){
			for(Cart cartItem : cartList){
				CartProductVo cartProductVo = new CartProductVo();
				cartProductVo.setId(cartItem.getId());
				cartProductVo.setUserId(userId);
				cartProductVo.setProductId(cartItem.getProductId());
				
				Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
				if(product != null){
					cartProductVo.setProductMainImage(product.getMainImage());
					cartProductVo.setProductName(product.getName());
					cartProductVo.setProductSubtitle(product.getSubtitle());
					cartProductVo.setProductStatus(product.getStatus());
					cartProductVo.setProductPrice(product.getPrice());
					cartProductVo.setProductStock(product.getStock());
					
					//判断库存
					int buyLimitCount = 0;
					if(product.getStock() >= cartItem.getQuantity()){
						buyLimitCount = cartItem.getQuantity();
						cartProductVo.setLimitQuatity(Const.Cart.LIMIT_NUM_SUCCESS);
					} else {
						buyLimitCount = product.getStock();
						cartProductVo.setLimitQuatity(Const.Cart.LIMIT_NUM_FAIL);
						Cart cartForQuatity = new Cart();
						cartForQuatity.setId(cartItem.getId());
						cartForQuatity.setQuantity(buyLimitCount);
						cartMapper.updateByPrimaryKeySelective(cartForQuatity);
					}
					cartProductVo.setQuatity(buyLimitCount);
					
					//计算总价
					cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuatity()));
					cartProductVo.setProductChecked(cartItem.getChecked());
				}
				
				if(cartItem.getChecked() == Const.Cart.CHECKED){
					//如果已经被勾选，增加到整个购物车总价中
					cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
				}
				cartProductVoList.add(cartProductVo);
			}
		}
		cartVo.setProductTotalPrice(cartTotalPrice);
		cartVo.setCartProductVoList(cartProductVoList);
		cartVo.setAllChecked(this.getAllCheckedStatus(userId));
		cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
		return cartVo;
	}
	/**
	 * 获取全选状态
	 * @param userId
	 * @return
	 */
	private boolean getAllCheckedStatus(Integer userId){
		if(userId == null){
			return false;
		}
		return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
	}
}
