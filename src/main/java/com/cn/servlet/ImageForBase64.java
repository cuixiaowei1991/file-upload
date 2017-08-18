package com.cn.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.cn.common.JSONReader;
import com.cn.servlet.common.BothCall;

/**
 * 处理客户端传递过来的base64数据转为图片
 * @author songzhili
 * 2016年11月9日上午9:49:05
 */
public class ImageForBase64 extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9101801969812961606L;
	/****/
	private final Logger logger = Logger.getLogger(ImageForBase64.class);
	
	@SuppressWarnings("unchecked")
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
		savePath.append(realPath).append("/").append("uploads");
		InputStream in = request.getInputStream();
		try {
			String suffix = null;
			String content = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			StringWriter writer = new StringWriter();
			char[] chars = new char[256];
			int count = 0;
			while ((count = reader.read(chars)) > 0) {
				writer.write(chars, 0, count);
			}
			JSONReader jsonReader = new JSONReader();
			Object obj = jsonReader.read(writer.toString());
			if(obj instanceof Map<?, ?>){
				Map<String, Object> map = (Map<String, Object>)obj;
				suffix = map.get("suffix").toString();
				content = map.get("content").toString();
			}
			String result = BothCall
					.imageCreateForBase64(localhost, savePath, maxnum, suffix, content);
			PrintWriter out = response.getWriter();
			out.write(result);
			out.close();
		}catch(IOException ioe){
			logger.debug("");
			response.getWriter().print("n");
			return;
		}finally {
			if (in != null) {
				in.close();
			}
		}
	}
}