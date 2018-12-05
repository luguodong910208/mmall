package com.mmall.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
@Service("iFileService")
public class FileServiceImpl implements IFileService {
	private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
	
	public String upload(MultipartFile file, String path){
		String fileName = file.getOriginalFilename();
		//扩展名
		//abc.jpg
		//如果是fileName.substring(fileName.lastIndexOf("."))，得到的结果是.jpg
		String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);//获取的是jpg
		String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
		logger.info("开始上传文件，上传文件的文件名：{},上传文件的路径:{},新文件名{}", fileName, path, uploadFileName);
		
		File fileDir = new File(path);
		if(!fileDir.exists()){
			fileDir.setWritable(true);//允许写权限
			fileDir.mkdirs();//将所有的文件路径全部创建
		}
		File targetFile = new File(path, uploadFileName);
		
		try {
			file.transferTo(targetFile);
			//文件已经成功上传了
			
			//将targetFile上传到我们的ftp服务器上
			FTPUtil.uploadFile(Lists.newArrayList(targetFile));
			//上传完之后，删除upload下面的文件
			targetFile.delete();
		} catch (IOException e) {
			logger.error("上传文件异常", e);
			return null;
		}
		//A:abc.jpg
		//B:abc.jpg
		return targetFile.getName();
	}
}
