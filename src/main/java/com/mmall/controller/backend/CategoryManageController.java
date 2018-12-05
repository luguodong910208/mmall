//package com.mmall.controller.backend;
//
//import javax.servlet.http.HttpSession;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import com.mmall.common.Const;
//import com.mmall.common.ResponseCode;
//import com.mmall.common.ServerResponse;
//import com.mmall.pojo.User;
//
//@Controller
//@RequestMapping("/manage/category")
//public class CategoryManageController {
//	
//	public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId" ,defaultValue = "0") int parentId){
//		User user = (User)session.getAttribute(Const.CURRENT_USER);
//		if(user == null){
//			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
//			
//		}
//	}
//}
