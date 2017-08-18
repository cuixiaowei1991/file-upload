package com.cn.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.cn.servlet.common.BothCall;

/**
 * 文件(图片)上传
 * @author songzhili
 * 2016年11月8日上午9:36:13
 */
public class ImageUpload extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2777424555764832756L;
	/****/
	private final Logger logger = Logger.getLogger(ImageUpload.class);
	
	public void service(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException ,IOException {
       
		
		/****/
		String remoteAddress = request.getRemoteAddr();
		String redirect = request.getParameter("redirect");
		StringBuilder log = new StringBuilder("=========>调用方ip:");
		log.append(remoteAddress).append(",redirect:").append(redirect);
		logger.debug(log.toString());
		
		int maxnum = Integer.valueOf(this.getInitParameter("max_num"));
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
			String result = BothCall.fileOrImageCreate(localhost, savePath, fileList, maxnum);
			response.sendRedirect(redirect + "?"
					+ URLEncoder.encode(result, "UTF-8"));
		}catch (FileUploadException ex) {
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
