package com.firm.order.utils;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public class FileHelper {

	
	/**
	 * 上传
	 * @param filePath 路径
	 * @param bannerImage 上传的文件数据
	 * @return fileName 文件名
	 * @throws Exception
	 */
	public static String upload(String filePath,MultipartFile bannerImage) throws Exception {
		
		// 如果文件不为空，写入上传路径
		String fileName = bannerImage.getOriginalFilename();
		if (fileName != null && !fileName.equals("")) {
			String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
			String filePrefix = fileName.substring(0, fileName.lastIndexOf("."));
			// 清除掉所有特殊字符
			String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
			filePrefix = Pattern.compile(regEx).matcher(filePrefix).replaceAll("").trim();
			fileName = UUID.randomUUID().toString().replaceAll("-", "") + "_" + filePrefix  + fileSuffix;
			File file = new File(filePath, fileName);
			// 判断路径是否存在，如果不存在就创建一个
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			// 将上传文件保存到一个目标文件当中
			bannerImage.transferTo(new File(filePath + File.separator + fileName));
			return fileName;
		} else {
			throw new Exception("fileName为空！");
		}
	}

	/**
	 * 浏览器下载
	 * @param filePath 路径
	 * @param fileName 文件名
	 * @param isDelete 是否删除源文件
	 * @return
	 * @throws Exception
	 */
	public static ResponseEntity<byte[]> download(String filePath, String fileName,boolean isDelete) throws Exception {
		// 下载文件路径
		File file = new File(filePath + File.separator + fileName);
		HttpHeaders headers = new HttpHeaders();
		// 下载显示的文件名，解决中文名称乱码问题
		//String downloadFielName = new String(fileName.getBytes("UTF-8"),"iso-8859-1");
		//String downloadFielName = new String(fileName.getBytes("UTF-8"));
		String downloadFielName = URLEncoder.encode(fileName,"utf-8");
		// 通知浏览器以attachment（下载方式）打开图片
		//headers.setContentDispositionFormData("attachment", downloadFielName);
		headers.set("Content-Disposition", "attachment;filename="+downloadFielName);
		headers.add("Access-Control-Expose-Headers", "Content-Disposition");
		// application/octet-stream ： 二进制流数据（最常见的文件下载）。
		MediaType mediaType = new MediaType(MediaType.APPLICATION_OCTET_STREAM,Charset.forName("utf-8"));
		headers.setContentType(mediaType);
		byte[] reByte = FileUtils.readFileToByteArray(file);
		if(isDelete){
			file.delete();
		}
		return new ResponseEntity<byte[]>(reByte, headers, HttpStatus.CREATED);
	}

}
