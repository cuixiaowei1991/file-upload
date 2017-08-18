package com.cn.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.cn.common.DESUtils;
import com.cn.common.JdbcClient;
import com.cn.common.MessageAndCode;
import com.cn.common.StringUtils;
/**
 * 接口秘钥校验
 * @author songzhili
 * 2016年11月15日下午3:28:06
 */
public class SecurityCheckFilter implements Filter {
    
	/**数据库中的数据缓存到该map中**/
	private Map<String, Map<String, String>> dataMap = new HashMap<String, Map<String,String>>();
	/****/
	private String secretKey;
	/****/
	private String dataFile;
	/****/
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	/****/
	private final Lock readLock = readWriteLock.readLock();
	/****/
	private final Lock writeLock = readWriteLock.writeLock();
	
	public void init(FilterConfig filterConfig) throws ServletException {
		this.secretKey = filterConfig.getInitParameter("secretKey");
		if(StringUtils.isEmpty(this.secretKey)){
			this.secretKey = "111111112222222233333333";
		}
		this.dataFile = filterConfig.getInitParameter("dataFile");
		if(StringUtils.isEmpty(this.dataFile)){
			this.dataFile = "database.properties";
		}
		transferData();
	}
    
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;
		/****/
		String indicate = request.getHeader("indicate");
		String certificateId = request.getHeader("certificateId");
		/**从请求头中获取秘钥**/
		String remoteSecret = request.getHeader("secret");
		if(StringUtils.isEmpty(indicate)
				&& StringUtils.isEmpty(certificateId)
				&& StringUtils.isEmpty(remoteSecret)){
			String errorMsg = returnErrorMessage("请求非法!!!!", false);
			response.setContentType("text/json;charset=UTF-8");
			PrintWriter writer = response.getWriter();
			writer.print(errorMsg);
			writer.close();
		}else{
			if(!StringUtils.isEmpty(indicate)){//修改-接口安全校验-白名单
				chain.doFilter(request, response);
				transferData();
			}else{//调用其他服务器的接口
				String errorMessage = null;
				/**获取appId开始校验**/
		        if(!StringUtils.isEmpty(certificateId)){//移动端
		        	if(this.dataMap.containsKey(certificateId)){
		        		String result = checkUpSecret(remoteSecret, certificateId);
		        		if(result == null){
		        			chain.doFilter(request, response);
		        		}else{
		        			errorMessage = result;
		        		}
		        	}else{
		        		errorMessage = returnErrorMessage("请求certificateId非法!!!!!",false);
		        	}
		        }else{//pc端
		        	/**获取域名开始校验**/
		    		String remoteAddress = request.getRemoteAddr();
		        	if(!StringUtils.isEmpty(remoteAddress)){
		        		if(this.dataMap.containsKey(remoteAddress)){
		            		String result = checkUpSecret(remoteSecret, remoteAddress);
		            		if(result == null){
		            			chain.doFilter(request, response);
		            		}else{
		            			errorMessage = result;
		            		}
		            	}else{
		            		errorMessage = returnErrorMessage("请求域名非法!!!!!",false);
		            	}
		        	}else{
		        		errorMessage = returnErrorMessage("无法获取请求域名地址",true);
		        	}
		        }
		        /****/
				if(errorMessage != null){
					response.setContentType("text/json;charset=UTF-8");
					PrintWriter writer = response.getWriter();
					writer.print(errorMessage);
					writer.close();
				}
			}
		}
	}
	
	public void destroy() {
		
	}
	/**
	 * 校验
	 * @param remoteSecret
	 * @param domainOrAppId
	 * @return
	 */
	private String checkUpSecret(String remoteSecret,String domainOrAppId){
		
		String result = null;
		readLock.lock();
		try {
			if(!StringUtils.isEmpty(remoteSecret)){
				Map<String, String> data = this.dataMap.get(domainOrAppId);
				String status = data.get("status");
				if(!StringUtils.isEmpty(status) && status.equals("1")){//启用
					String enable = data.get("enable");
					if(!StringUtils.isEmpty(enable) && enable.equals("1")){//放行
						result = null;
					}else{
						/****/
						String encodeSecret = data.get("secretString");
						remoteSecret = DESUtils.encrypt(domainOrAppId+remoteSecret, this.secretKey);
						if(!remoteSecret.equals(encodeSecret)){
							result =  returnErrorMessage("请求秘钥非法!!!!!",false);
						}
					}
				}else{//禁用
					result = returnErrorMessage("管理员禁止了你的请求",true);
				}
			}else{
				result =  returnErrorMessage("无法获取请求头中的秘钥",true);
			}
		}finally{
			readLock.unlock();
		}
		return result;
	}
	/**
	 * 查询数据库 更新数据
	 */
	private void transferData(){
		
		JdbcClient jdbcClient = new JdbcClient(this.dataFile);
		writeLock.lock();
		try {
			/****/
			List<Map<String, String>> list = jdbcClient.queryAuth();
			if(!list.isEmpty()){
				dataMap.clear();
			    for(int t=0;t<list.size();t++){
			    	Map<String, String> map = list.get(t);
			    	dataMap.put(map.get("domainOrAppId"), map);
			    }
			}
		}finally{
			/**关闭数据库链接**/
			jdbcClient.close();
			writeLock.unlock();
		}
	}
    /**
     * 
     * @param errorMessage
     * @return
     */
	private String returnErrorMessage(String errorMessage,boolean append){
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put(MessageAndCode.RSP_CODE, MessageAndCode.NULL_OBJECT);
		/****/
		StringBuilder together = new StringBuilder();
		if(append){
			together.append(errorMessage).append(MessageAndCode.NULL_OBJECT_MESSAGE);
		}else{
			together.append(errorMessage);
		}
    	node.put(MessageAndCode.RSP_DESC, together.toString());
		return node.toString();
	}
}