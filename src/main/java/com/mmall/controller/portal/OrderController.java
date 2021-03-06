package com.mmall.controller.portal;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;

@Controller
@RequestMapping("/order/")
public class OrderController {
	
	@Autowired
	private IOrderService iOrderService;
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	/**
	 * 支付功能
	 * @param session
	 * @param orderNo
	 * @param request
	 * @return
	 */
	@RequestMapping("pay.do")
	@ResponseBody
	public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		String path = request.getSession().getServletContext().getRealPath("upload");
		return iOrderService.pay(orderNo, user.getId(), path);
	}
	/**
	 * 支付宝回调
	 * @param request
	 * @return
	 */
	@RequestMapping("alipay_call_back.do")
	@ResponseBody
	public Object alipayCallBack(HttpServletRequest request){
		Map<String, String> params = Maps.newHashMap();
		
		Map requestParams = request.getParameterMap();
		for(Iterator iter = requestParams.keySet().iterator();iter.hasNext();){
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for(int i = 0; i < values.length; i++){
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}
		logger.info("支付宝回调，sign:{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_sign"), params.toString());
		
		//非常重要，验证回调的正确性，是不是支付宝发的，并且还要避免重复通知
		params.remove("sign_type");
		try {
			boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
		} catch (AlipayApiException e) {
			logger.error("支付宝回调异常", e);
		}
		
		//TODO 验证各种数据
		
		ServerResponse serverResponse = iOrderService.alipayCallback(params);
		if(serverResponse.isSuccess()){
			return Const.AlipayCallBack.RESPONSE_SUCCESS;
		}
		return Const.AlipayCallBack.RESPONSE_FAILED;
	}
	/**
	 * 查看订单付款状态
	 * @param session
	 * @param orderNo
	 * @return
	 */
	@RequestMapping("query_order_pay_status.do")
	@ResponseBody
	public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
		if(serverResponse.isSuccess()){
			return ServerResponse.createBySuccess(true);
		}
		return ServerResponse.createBySuccess(false);
	}
	/**
	 * 创建订单
	 * @param session
	 * @param shippingId
	 * @return
	 */
	@RequestMapping("create.do")
	@ResponseBody
	public ServerResponse create(HttpSession session, Integer shippingId){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		return iOrderService.createOrder(user.getId(), shippingId);
	}
	/**
	 * 取消订单
	 * @param session
	 * @param orderNo
	 * @return
	 */
	@RequestMapping("cancel.do")
	@ResponseBody
	public ServerResponse cancel(HttpSession session, Long orderNo){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		return iOrderService.cancel(user.getId(), orderNo);
	}
	/**
	 * 获取购物车中商品信息
	 * @param session
	 * @return
	 */
	@RequestMapping("get_order_cart_product.do")
	@ResponseBody
	public ServerResponse getOrderCartProduct(HttpSession session){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		return iOrderService.getOrderCartProduct(user.getId());
	}
	/**
	 * 获取订单详情
	 * @param session
	 * @param orderNo
	 * @return
	 */
	@RequestMapping("detail.do")
	@ResponseBody
	public ServerResponse detail(HttpSession session, Long orderNo){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		return iOrderService.getOrderDetail(user.getId(), orderNo);
	}
	/**
	 * 获取订单列表
	 * @param session
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping("list.do")
	@ResponseBody
	public ServerResponse list(HttpSession session, @RequestParam(value="pageNum", defaultValue="1") int pageNum, @RequestParam(value="pageSize", defaultValue="10") int pageSize){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		return iOrderService.getOrderList(user.getId(), pageNum, pageSize);
	}
}
