package com.mmall.controller.backend;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {
	@Autowired
	private IUserService iUserService;
	@Autowired
	private IProductService iProductService;
	@Autowired
	private IFileService iFileService;
	/**
	 * 保存商品
	 * @param session
	 * @param product
	 * @return
	 */
	@RequestMapping("save.do")
	@ResponseBody
	public ServerResponse productSave(HttpSession session, Product product){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
		}
		if(iUserService.checkAdminRole(user).isSuccess()){
			//增加产品的业务逻辑
			return iProductService.saveOrUpdateProduct(product);
		} else {
			return ServerResponse.createByErrorMessage("无操作权限");
		}
	}
	/**
	 * 设置商品状态
	 * @param session
	 * @param productId
	 * @param status
	 * @return
	 */
	@RequestMapping("set_sale_status.do")
	@ResponseBody
	public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
		}
		if(iUserService.checkAdminRole(user).isSuccess()){
			//设置产品上下级状态
			return iProductService.setSaleStatus(productId, status);
		} else {
			return ServerResponse.createByErrorMessage("无操作权限");
		}
	}
	/**
	 * 获取商品详情
	 * @param session
	 * @param productId
	 * @return
	 */
	@RequestMapping("detail.do")
	@ResponseBody
	public ServerResponse getDetail(HttpSession session, Integer productId){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
		}
		if(iUserService.checkAdminRole(user).isSuccess()){
			//获取商品详情
			return iProductService.manageProductDetail(productId);
		} else {
			return ServerResponse.createByErrorMessage("无操作权限");
		}
	}
	/**
	 * 查询商品列表
	 * @param session
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping("list.do")
	@ResponseBody
	public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
		}
		if(iUserService.checkAdminRole(user).isSuccess()){
			//获取商品详情
			return iProductService.getProductList(pageNum, pageSize);
		} else {
			return ServerResponse.createByErrorMessage("无操作权限");
		}
	}
	/**
	 * 商品查询
	 * @param session
	 * @param productName
	 * @param productId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping("search.do")
	@ResponseBody
	public ServerResponse productSearch(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
		}
		if(iUserService.checkAdminRole(user).isSuccess()){
			//获取商品详情
			return iProductService.searchProduct(productName, productId, pageNum, pageSize);
		} else {
			return ServerResponse.createByErrorMessage("无操作权限");
		}
	}
	/**
	 * 文件上传
	 * @param file
	 * @param request
	 * @return
	 */
	@RequestMapping("upload.do")
	@ResponseBody
	public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
		}
		if(iUserService.checkAdminRole(user).isSuccess()){
			String path = request.getSession().getServletContext().getRealPath("upload");
			String targetFileName = iFileService.upload(file, path);
			String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
			
			Map fileMap = Maps.newHashMap();
			fileMap.put("uri", targetFileName);
			fileMap.put("url", url);
			return ServerResponse.createBySuccess(fileMap);
		} else {
			return ServerResponse.createByErrorMessage("无权限操作");
		}
	}
	
	@RequestMapping("richtext_img_upload.do")
	@ResponseBody
	public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		Map resultMap = Maps.newHashMap();
		if(user == null){
			resultMap.put("success", false);
			resultMap.put("msg", "请登录管理员账户");
			return resultMap;
		}
		//富文本对于返回值有自己的要求，我们使用的是simditor所以按照simditor的要求进行返回
//		{
//			"success":true/false,
//			"msg":"error message", # optional,
//			"file_path":[real file path]
//		}
		if(iUserService.checkAdminRole(user).isSuccess()){
			String path = request.getSession().getServletContext().getRealPath("upload");
			String targetFileName = iFileService.upload(file, path);
			if(StringUtils.isBlank(targetFileName)){
				resultMap.put("success", false);
				resultMap.put("msg", "上传失败");
				return resultMap;
			}
			String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
			resultMap.put("success", true);
			resultMap.put("msg", "上传成功");
			resultMap.put("file_path", url);
			response.addHeader("Access-Controller-Allow-Headers", "X-File-Name");
			return resultMap;
		} else {
			resultMap.put("success", false);
			resultMap.put("msg", "无权限操作");
			return resultMap;
		}
	}
}
