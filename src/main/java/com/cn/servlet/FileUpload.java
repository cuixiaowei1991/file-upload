package com.cn.servlet;

import java.io.IOException;
import java.io.PrintWriter;
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
 * 文件上传
 * @author songzhili
 * 2016年11月9日下午2:08:16
 */
public class FileUpload extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2571386452393684126L;
    
	/****/
	private final Logger logger = Logger.getLogger(FileUpload.class);
	
	public void service(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException ,IOException{
		
		String remoteAddress = request.getRemoteAddr();
		String redirect = request.getParameter("redirect");
		StringBuilder log = new StringBuilder("=========>调用方ip:");
		log.append(remoteAddress).append(",redirect:").append(redirect);
		logger.debug(log.toString());
		/****/
		int maxnum = Integer.valueOf(this.getInitParameter("max_num"));
		String localhost = this.getInitParameter("localhost");
		String realPath = this.getServletConfig().getServletContext().getRealPath("");
		StringBuilder savePath = new StringBuilder();
		savePath.append(realPath).append("/").append("files");
		try {
			/****/
			DiskFileItemFactory fac = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(fac);
			/**设置编码**/
			upload.setHeaderEncoding("UTF-8");
			List<FileItem> fileList = upload.parseRequest(request);
			String result = BothCall.fileOrImageCreate(localhost, savePath, fileList, maxnum);
			PrintWriter out = response.getWriter();
			out.print(result);
			out.close();
		}catch (FileUploadException ex) {
			logger.debug("=========>解析请求异常");
			response.getWriter().print("n");
			return;
		}catch (Exception e) {
			logger.debug("========>写文件异常");
			response.getWriter().print("n");
			return;
		}
	}
}














