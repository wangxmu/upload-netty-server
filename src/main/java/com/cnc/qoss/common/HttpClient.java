package com.cnc.qoss.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;
/**
 * 用于测试的http客户端
 * @author chensl
 *
 */
public class HttpClient {

	private static String paseResponse(HttpResponse response) {  
		HttpEntity entity = response.getEntity();  

		String charset = EntityUtils.getContentCharSet(entity);  

		String body = null;  
		try {  
			body = EntityUtils.toString(entity); 
			System.out.println(body);
		} catch (ParseException e) {  
			e.printStackTrace();  
		} catch (IOException e) {  
			e.printStackTrace();  
		}  

		return body;  
	}  

	private static HttpResponse sendRequest(DefaultHttpClient httpclient,  
			HttpUriRequest httpost) {  
		HttpResponse response = null;  

		try {  
			response = httpclient.execute(httpost);  
		} catch (ClientProtocolException e) {  
			e.printStackTrace();  
		} catch (IOException e) {  
			e.printStackTrace();  
		}  
		return response;  
	}  
	private static String invoke(DefaultHttpClient httpclient,  
			HttpUriRequest httpost) {  

		HttpResponse response = sendRequest(httpclient, httpost);  
		String body = paseResponse(response);  
		return body;  
	}
	
	public static String post(String url, String params) throws UnsupportedEncodingException {  
		DefaultHttpClient httpclient = new DefaultHttpClient();  
		String body = null;  
		System.out.println("create httppost:" + url);  
		HttpPost httpost = new HttpPost(url);
		
		StringEntity entity = new StringEntity(params.toString(), "utf-8");
		entity.setContentEncoding("UTF-8");
		entity.setContentType("application/json");
		httpost.setEntity(entity);

		body = invoke(httpclient, httpost);  
		httpclient.getConnectionManager().shutdown();  
		return body;  
	}


	/**
	 * 上传文件
	 * @throws  ParseException
	 * @throws  IOException
	 */   
	public static void postFile() throws ParseException, IOException{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try{
			// 要上传的文件的路径
			String filePath =new String("F:/log_processer/extract/123.gz");
			// 把一个普通参数和文件上传给下面这个地址 是一个servlet
			HttpPost httpPost =new HttpPost(
					"http://qossdc2.wscdns.com/upload");
			// 把文件转换成流对象FileBody
			File file =new File(filePath);
			FileBody bin =new FileBody(file); 
			StringBody uploadFileName = new StringBody(
					"aaa", ContentType.create(
							"text/plain", Consts.UTF_8));
			//以浏览器兼容模式运行，防止文件名乱码。 
			HttpEntity reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					//.addPart("uploadFile", bin)//uploadFile对应服务端类的同名属性<File类型>
					.addPart("uploadFileName", uploadFileName)//uploadFileName对应服务端类的同名属性<String类型>
					.setCharset(CharsetUtils.get("UTF-8")).build();

			httpPost.setEntity(reqEntity);

			System.out.println("发起请求的页面地址 "+ httpPost.getRequestLine());
			// 发起请求 并返回请求的响应
			CloseableHttpResponse response = httpClient.execute(httpPost);
			try{
				System.out.println("----------------------------------------");
				// 打印响应状态
				System.out.println(response.getStatusLine());
				// 获取响应对象
				HttpEntity resEntity = response.getEntity();
				if(resEntity !=null) {
					// 打印响应长度
					System.out.println("Response content length: "
							+ resEntity.getContentLength());
					// 打印响应内容
					System.out.println(EntityUtils.toString(resEntity,
							Charset.forName("UTF-8")));
				}
				// 销毁
				EntityUtils.consume(resEntity);
			}finally{
				response.close();
			}
		}finally{
			httpClient.close();
		}
	}

	public static void main(String[] args) throws ParseException, IOException {
		postFile();
	}
}
