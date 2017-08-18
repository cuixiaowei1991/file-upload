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

import com.cn.common.StringUtils;
import com.cn.servlet.common.BothCall;
/**
 * 用户自定义图片的宽高
 * @author songzhili
 * 2016年11月9日下午3:28:31
 */
public class ImageUserDefined extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1012478275374038562L;
   
	
	/****/
	private final Logger logger = Logger.getLogger(ImageUserDefined.class);
	
	public void service(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException ,IOException {
		
		String remoteAddress = request.getRemoteAddr();
		String redirect = request.getParameter("redirect");
		StringBuilder log = new StringBuilder("=========>调用方ip:");
		log.append(remoteAddress).append(",redirect:").append(redirect);
		logger.debug(log.toString());
		/****/
		int maxnum = Integer.parseInt(this.getInitParameter("max_num"));
		String sourceWidth = request.getParameter("width");
		if(StringUtils.isEmpty(sourceWidth)){
			sourceWidth = "1000";
		}
		String sourceHeight = request.getParameter("height");
		if(StringUtils.isEmpty(sourceHeight)){
			sourceHeight = "1000";
		}
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
			int height = Integer.parseInt(sourceHeight);
			int width = Integer.parseInt(sourceWidth);
			Map<String, String> result = BothCall.newImageForWidHei(localhost, savePath, 
					fileList, width, height, maxnum);
			String json = BothCall.transferData(result);
			response.sendRedirect(redirect + "?"
					+ URLEncoder.encode(json, "UTF-8"));
		}catch(FileUploadException fue){
			logger.debug("=========>解析请求异常,redirect:"+redirect);
			response.getWriter().print("n");
			return;
		}catch(IOException ioe){
			logger.debug("=========>图片操作异常,redirect:"+redirect);
			response.getWriter().print("n");
			return;
		}catch (Exception e) {
			logger.debug("========>写图片异常,redirect:"+redirect);
			response.getWriter().print("n");
			return;
		}
	}
}
















