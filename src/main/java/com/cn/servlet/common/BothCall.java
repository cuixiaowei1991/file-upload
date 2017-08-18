package com.cn.servlet.common;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.cn.common.StringUtils;
/**
 * servlet调用的公共类
 * @author songzhili
 * 2016年11月10日上午9:13:41
 */
public class BothCall implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7576032740857482998L;
   
	/**
	 * 1.生成指定宽高的图片
	 * @param localhost
	 * @param savePath
	 * @param fileList
	 * @param width
	 * @param height
	 * @param maxnum
	 * @return
	 * @throws IOException
	 */
	public static synchronized Map<String, String> newImageForWidHei(String localhost
			,StringBuilder savePath,List<FileItem> fileList,int width,
			int height,int maxnum) throws IOException{
		
		String fileName = createFile(savePath, maxnum);
		/****/
		Map<String, String> result = null;
		for(FileItem item : fileList){
			String name = null;
			String extName = null;
			String newName = null;
			if (!item.isFormField()) {
				name = item.getName();
				if(StringUtils.isEmpty(name)) {
					continue;
				}
				long size = item.getSize();
				if (name.lastIndexOf(".") >= 0) {
					extName = name.substring(name.lastIndexOf(".")+1);
				}
				/**生成文件名称**/
				newName = UUID.randomUUID().toString();
				savePath.append("/").append(newName)
				.append(".").append(extName);
				/**开始操作图片**/
				InputStream inStream = item.getInputStream();
				BufferedImage bufferedImage = ImageIO.read(inStream);
				BufferedImage bufferedImageNew = new BufferedImage(width, 
						height, BufferedImage.TYPE_INT_BGR);  
			    Graphics graphics = bufferedImageNew.createGraphics();  
			    graphics.drawImage(bufferedImage, 0, 0, width, height, null);
			    FileOutputStream outputStream = new FileOutputStream(savePath.toString()); 
			    ImageIO.write(bufferedImageNew, extName, outputStream); 
			    bufferedImage.flush();
			    bufferedImageNew.flush();
			    outputStream.flush();  
			    outputStream.close();
			    inStream.close();
			    /****/
			    result = createMapResult(localhost, fileName, newName, extName, size);
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param localhost
	 * @param savePath
	 * @param fileList
	 * @param maxnum
	 * @return
	 * @throws Exception 
	 */
	public static synchronized String fileOrImageCreate(String localhost
			,StringBuilder savePath,List<FileItem> fileList,int maxnum) throws Exception{
		
		String fileName = createFile(savePath, maxnum);
		Map<String, String> result = null;
		for(FileItem item : fileList){
			String name = null;
			String extName = null;
			String newName = null;
			if (!item.isFormField()) {
				name = item.getName();
				if (StringUtils.isEmpty(name)) {
					continue;
				}
				long size = item.getSize();
				if (name.lastIndexOf(".") >= 0) {
					extName = name.substring(name.lastIndexOf(".")+1);
				}
				/**生成文件名称**/
				newName = UUID.randomUUID().toString();
				savePath.append("/").append(newName)
				.append(".").append(extName);
				File saveFile = new File(savePath.toString());
				/**文件(图片)写到文件夹中**/
				item.write(saveFile);
				/****/
				result = createMapResult(localhost, fileName, newName, extName, size);
			}
		}
		return transferData(result);
	}
	/**
	 * 
	 * @param localhost
	 * @param savePath
	 * @param maxnum
	 * @param suffix
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public static synchronized String imageCreateForBase64(String localhost
			,StringBuilder savePath,int maxnum,String suffix,String content) throws IOException{
		
		String fileName = createFile(savePath, maxnum);
		String imageName = UUID.randomUUID().toString();
		byte[] bytes = Base64.decodeBase64(content);
		for (int t = 0; t < bytes.length; t++) {
			if (bytes[t] < 0) {
				bytes[t] += 256;
			}
		}
		/**图片写到指定的目录**/
		savePath.append("/").append(imageName)
		.append(".").append(suffix);
		FileUtils.writeByteArrayToFile(new File(savePath.toString()), bytes, false);
		/****/
		String json = transferData(createMapResult(localhost, fileName, imageName, suffix, bytes.length));
		return json;
	}
	/**
	 * 
	 * @param localhost
	 * @param fileName
	 * @param newName
	 * @param extName
	 * @param size
	 * @return
	 */
	private static  Map<String, String> createMapResult(String localhost,String fileName,
			String newName,String extName,long size){
		
		Map<String, String> result = new HashMap<String, String>(8,0.75f);
		/****/
		StringBuilder suffix = new StringBuilder();
		suffix.append(fileName).append("/");
		suffix.append(newName).append(".").append(extName);
		/****/
		result.put("name", suffix.toString());
		result.put("size", Long.toString(size));
		suffix.setLength(0);
		suffix.append("image/").append(extName);
		result.put("type", suffix.toString());
		/*****/
		suffix.setLength(0);
		suffix.append(localhost);
		suffix.append(fileName).append("/");
		suffix.append(newName).append(".").append(extName);
		result.put("url", suffix.toString());
		/*****/
		return result;
	}
	/**
	 * 
	 * @param savePath
	 * @param maxnum
	 * @return
	 */
	private static synchronized String createFile(StringBuilder savePath,int maxnum){
		
		/**1.如果file-upload项目下 uploads目录不存在,创建该目录**/
		File firstFile = new File(savePath.toString());
		if (!firstFile.exists()) {
			firstFile.mkdirs();
		}
		/**2.以当前的日期为文件夹的名称**/
		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		String fileName = format.format(new Date());
		savePath.append("/").append(fileName);
		File secondFile = new File(savePath.toString());
		if (!secondFile.exists()) {
			secondFile.mkdirs();
		}
		/**3.判断当前文件夹中的文件数量是否达到上限**/
		if(secondFile.list().length >= maxnum){
			fileName = fileName+"add";
			savePath.append("add");
			File thirdFile = new File(savePath.toString());
			if(!thirdFile.exists()){
				thirdFile.mkdirs();
			}
		}
		return fileName;
	}
	
	public static String transferData(Map<String, String> result){
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode json = mapper.createObjectNode();
		ArrayNode images = mapper.createArrayNode();
		ObjectNode image = mapper.createObjectNode();
		image.put("name", result.get("name"));
		image.put("size", result.get("size"));
		image.put("type", result.get("type"));
		image.put("url", result.get("url"));
		images.add(image);
		json.put("files", images);
		return json.toString();
	}
}
