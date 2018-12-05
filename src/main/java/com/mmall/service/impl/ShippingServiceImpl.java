package com.mmall.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {
	@Autowired
	private ShippingMapper shippingMapper;
	/**
	 * 新增收货地址
	 * @param userId
	 * @param shipping
	 * @return
	 */
	public ServerResponse add(Integer userId, Shipping shipping){
		shipping.setUserId(userId);
		//由于本身生成的为int格式，为了能够让前端直接获取shippingid，需要在sql语句中增加useGeneratedKeys="true" keyProperty="id"内容标签
		int rowCount = shippingMapper.insert(shipping);
		if(rowCount > 0){
			Map result = Maps.newHashMap();
			result.put("shippingId", shipping.getId());
			return ServerResponse.createBySuccess("新建地址成功", result);
		}
		return ServerResponse.createByErrorMessage("新建地址失败");
	}
	/**
	 * 删除收货地址
	 * @param userId
	 * @param shippingId
	 * @return
	 */
	public ServerResponse<String> del(Integer userId, Integer shippingId){
		int rowCount = shippingMapper.deleteByShippingIdUserId(userId, shippingId);
		if(rowCount > 0){
			return ServerResponse.createBySuccess("删除地址成功");
		}
		return ServerResponse.createByErrorMessage("删除地址失败");
	}
	/**
	 * 更新收货地址
	 * @param userId
	 * @param shipping
	 * @return
	 */
	public ServerResponse update(Integer userId, Shipping shipping){
		shipping.setUserId(userId);
		//由于本身生成的为int格式，为了能够让前端直接获取shippingid，需要在sql语句中增加useGeneratedKeys="true" keyProperty="id"内容标签
		int rowCount = shippingMapper.updateByShipping(shipping);
		if(rowCount > 0){
			return ServerResponse.createBySuccess("更新地址成功");
		}
		return ServerResponse.createByErrorMessage("更新地址失败");
	}
	/**
	 * 查询收货地址
	 * @param userId
	 * @param shippingId
	 * @return
	 */
	public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
		Shipping shipping = shippingMapper.selectByShippingIdUserId(userId, shippingId);
		if(shipping == null){
			return ServerResponse.createByErrorMessage("无法查询到该地址");
		}
		return ServerResponse.createBySuccess("查询地址成功", shipping);
	}
	/**
	 * 查询收货地址列表
	 * @param userId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize){
		PageHelper.startPage(pageNum, pageSize);
		List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
		PageInfo pageInfo = new PageInfo(shippingList);
		return ServerResponse.createBySuccess(pageInfo);
	}
}
