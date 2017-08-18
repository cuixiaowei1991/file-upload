package com.cn.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.cn.servlet.common.BothCall;
/**
 * 客户端传过来一张图片
 * 服务器端生成大,中,小三张图片
 * @author songzhili
 * 2016年11月9日下午3:31:34
 */
public class ImageBigMidMin extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2107060174636518635L;
   
	/****/
	private final Logger logger = Logger.getLogger(ImageBigMidMin.class);
	
	public void service(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException ,IOException{
		
		String remoteAddress = request.getRemoteAddr();
		String redirect = request.getParameter("redirect");
		StringBuilder log = new StringBuilder("=========>调用方ip:");
		log.append(remoteAddress).append(",redirect:").append(redirect);
		logger.debug(log.toString());
		/****/
		int maxnum = Integer.parseInt(this.getInitParameter("max_num"));
		int bigWidth = Integer.parseInt(this.getInitParameter("bigWidth"));
		int bigHeight = Integer.parseInt(this.getInitParameter("bigHeight"));
		/****/
		int midWidth = Integer.parseInt(this.getInitParameter("midWidth"));
		int midHeight = Integer.parseInt(this.getInitParameter("midHeight"));
		/****/
		int minWidth = Integer.parseInt(this.getInitParameter("minWidth"));
		int minHeight = Integer.parseInt(this.getInitParameter("minHeight"));
		/****/
		String localhost = this.getInitParameter("localhost");
		String realPath = this.getServletConfig().getServletContext().getRealPath("");
		StringBuilder savePath = new StringBuilder();
		savePath.append(realPath).append("/").append("uploads");
		try {
			/****/
			DiskFileItemFactory fac = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(fac);
			/**设置编码**/
			upload.setHeaderEncoding("UTF-8");
			List<FileItem> fileList = upload.parseRequest(request);
			/**1.创建大 图片**/
			Map<String, String> bigResult = BothCall.newImageForWidHei(localhost, savePath, 
					fileList, bigWidth, bigHeight, maxnum);
			/**2.创建中 图片**/
			savePath.setLength(0);
			savePath.append(realPath).append("uploads");
			Map<String, String> midResult = BothCall.newImageForWidHei(localhost, savePath, 
					fileList, midWidth, midHeight, maxnum);
			/**3.创建小 图片**/
			savePath.setLength(0);
			savePath.append(realPath).append("uploads");
			Map<String, String> minResult = BothCall.newImageForWidHei(localhost, savePath, 
					fileList, minWidth, minHeight, maxnum);
			
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode json = mapper.createObjectNode();
			ArrayNode images = mapper.createArrayNode();
			/****/
			ObjectNode bigImage = mapper.createObjectNode();
			bigImage.put("bigName", bigResult.get("name"));
			bigImage.put("bigSize", bigResult.get("size"));
			bigImage.put("bigType", bigResult.get("type"));
			bigImage.put("bigUrl", bigResult.get("url"));
			images.add(bigImage);
			/****/
			ObjectNode midImage = mapper.createObjectNode();
			midImage.put("midName", midResult.get("name"));
			midImage.put("midSize", midResult.get("size"));
			midImage.put("midType", midResult.get("type"));
			midImage.put("midUrl", midResult.get("url"));
			images.add(midImage);
			/****/
			ObjectNode minImage = mapper.createObjectNode();
			minImage.put("minName", minResult.get("name"));
			minImage.put("minSize", minResult.get("size"));
			minImage.put("minType", minResult.get("type"));
			minImage.put("minUrl", minResult.get("url"));
			images.add(minImage);
			json.put("files", images);
			response.sendRedirect(redirect + "?"
					+ URLEncoder.encode(json.toString(), "UTF-8"));
		}catch(FileUploadException fue){
			logger.debug("=========>解析请求异常,redirect:"+redirect);
			response.getWriter().print("n");
			return;
		}catch (Exception e) {
			logger.debug("========>写图片异常,redirect:"+redirect);
			response.getWriter().print("n");
			return;
		} 
	}
}
















