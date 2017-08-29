package com.foomei.common.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foomei.common.mapper.JsonMapper;

public class HttpClientUtil {

	private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

	private static JsonMapper json;
	private static SSLContext sslcontext = null;
	private static SSLConnectionSocketFactory sslSocketFactory = null;
	private static PoolingHttpClientConnectionManager connectionManager = null;
//	private static RequestConfig requestConfig = null;

	static {
		json = JsonMapper.nonEmptyMapper();

		try {
			sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
			    @Override
			    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				    return true;
				}
			}).build();
			sslSocketFactory = new SSLConnectionSocketFactory(sslcontext, new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()    
			           .register("http", PlainConnectionSocketFactory.INSTANCE)    
			           .register("https", sslSocketFactory)    
			           .build();   
			connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			// 配置超时时间（连接服务端超时1分，请求数据返回超时2分）
//			requestConfig = RequestConfig.custom().setConnectTimeout(120000).setSocketTimeout(60000).setConnectionRequestTimeout(60000).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static HttpResponse get(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset, HttpClientContext context) throws Exception {
		url = buildQuery(url, bodyParams, charset);
		logger.info("api url:" + url);
		logger.info("api head params:" + json.toJson(headParams));
		if(context.getCookieStore() != null) {
			logger.info("api cookies:" + json.toJson(context.getCookieStore().getCookies()));
		}

		HttpGet httpGet = new HttpGet(url);
		HttpClient client = getPoolingHttpClient();
		for (Entry<String, String> entry : headParams.entrySet()) {
			httpGet.addHeader(entry.getKey(), entry.getValue());
		}
		
		HttpResponse response = client.execute(httpGet, context);
		
		return response;
	}
	
	public static HttpEntity getEntity(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset, HttpClientContext context) throws Exception {
		url = buildQuery(url, bodyParams, charset);
		logger.info("api url:" + url);
		logger.info("api head params:" + json.toJson(headParams));
		if(context.getCookieStore() != null) {
			logger.info("api cookies:" + json.toJson(context.getCookieStore().getCookies()));
		}

		HttpEntity entity = null;
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpClient client = getHttpClient();
			for (Entry<String, String> entry : headParams.entrySet()) {
				httpGet.addHeader(entry.getKey(), entry.getValue());
			}
			HttpResponse response = client.execute(httpGet, context);
			int status = response.getStatusLine().getStatusCode();
			entity = response.getEntity();
			if (status != 200) {
				throw new RuntimeException(String.format("api return error http code %d, detail: %n%s", status, EntityUtils.toString(entity, charset)));
			}
			return entity;
		} catch (Exception e) {
			String msg = String.format("Failed to call api '%s'", url);
			logger.error(msg, e);
			httpGet.abort();
			throw e;
		}
	}

	public static String getString(String url) throws Exception {
		return EntityUtils.toString(getEntity(url, new HashMap<String, String>(), new HashMap<String, String>(), "UTF-8", getContext()), "UTF-8");
	}
	
	public static String getString(String url, String charset) throws Exception {
		return EntityUtils.toString(getEntity(url, new HashMap<String, String>(), new HashMap<String, String>(), charset, getContext()), charset);
	}
	
	public static String getString(String url, Map<String, String> params) throws Exception {
		return EntityUtils.toString(getEntity(url, params, new HashMap<String, String>(), "UTF-8", getContext()), "UTF-8");
	}
	
	public static String getString(String url, Map<String, String> params, String charset) throws Exception {
		return EntityUtils.toString(getEntity(url, params, new HashMap<String, String>(), charset, getContext()), charset);
	}

	public static String getString(String url, Map<String, String> bodyParams, Map<String, String> headParams) throws Exception {
		return EntityUtils.toString(getEntity(url, bodyParams, headParams, "UTF-8", getContext()), "UTF-8");
	}
	
	public static String getString(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset) throws Exception {
		return EntityUtils.toString(getEntity(url, bodyParams, headParams, charset, getContext()), charset);
	}
	
	public static String getString(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset, HttpClientContext context) throws Exception {
		return EntityUtils.toString(getEntity(url, bodyParams, headParams, charset, context), charset);
	}
	
	public static HttpResponse put(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset, HttpClientContext context) throws Exception {
		logger.info("api url:" + url);
		logger.info("api head params:" + json.toJson(headParams));
		logger.info("api body params:" + json.toJson(bodyParams));
		if(context.getCookieStore() != null) {
			logger.info("api cookies:" + json.toJson(context.getCookieStore().getCookies()));
		}

		HttpPut httpPut = new HttpPut(url);
		HttpClient client = getPoolingHttpClient();
		for (Entry<String, String> entry : headParams.entrySet()) {
			httpPut.addHeader(entry.getKey(), entry.getValue());
		}
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Entry<String, String> entry : bodyParams.entrySet()) {
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		httpPut.setEntity(new UrlEncodedFormEntity(nvps, charset));

		HttpResponse response = client.execute(httpPut, context);

		return response;
	}

	public static HttpEntity putEntity(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset, HttpClientContext context) throws Exception {
		logger.info("api url:" + url);
		logger.info("api head params:" + json.toJson(headParams));
		logger.info("api body params:" + json.toJson(bodyParams));
		if(context.getCookieStore() != null) {
			logger.info("api cookies:" + json.toJson(context.getCookieStore().getCookies()));
		}

		HttpEntity entity = null;
		HttpPut httpPut = new HttpPut(url);
		try {
			HttpClient client = getHttpClient();
			for (Entry<String, String> entry : headParams.entrySet()) {
				httpPut.addHeader(entry.getKey(), entry.getValue());
			}
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (Entry<String, String> entry : bodyParams.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httpPut.setEntity(new UrlEncodedFormEntity(nvps, charset));

			HttpResponse response = client.execute(httpPut, context);
			int status = response.getStatusLine().getStatusCode();
			entity = response.getEntity();
			if (status != 200) {
				throw new RuntimeException(String.format("api return error http code %d, detail: %n%s", status, EntityUtils.toString(entity, charset)));
			}
			return entity;
		} catch (Exception e) {
			String msg = String.format("Failed to call api '%s'", url);
			logger.error(msg, e);
			httpPut.abort();
			throw e;
		}
	}

	public static String putString(String url) throws Exception {
		return EntityUtils.toString(putEntity(url, new HashMap<String, String>(), new HashMap<String, String>(), "UTF-8", getContext()), "UTF-8");
	}
	
	public static String putString(String url, String charset) throws Exception {
		return EntityUtils.toString(putEntity(url, new HashMap<String, String>(), new HashMap<String, String>(), charset, getContext()), charset);
	}
	
	public static String putString(String url, Map<String, String> params) throws Exception {
		return EntityUtils.toString(putEntity(url, params, new HashMap<String, String>(), "UTF-8", getContext()), "UTF-8");
	}
	
	public static String putString(String url, Map<String, String> params, String charset) throws Exception {
		return EntityUtils.toString(putEntity(url, params, new HashMap<String, String>(), charset, getContext()), charset);
	}

	public static String putString(String url, Map<String, String> bodyParams, Map<String, String> headParams) throws Exception {
		return EntityUtils.toString(putEntity(url, bodyParams, headParams, "UTF-8", getContext()), "UTF-8");
	}
	
	public static String putString(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset) throws Exception {
		return EntityUtils.toString(putEntity(url, bodyParams, headParams, charset, getContext()), charset);
	}
	
	public static String putString(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset, HttpClientContext context) throws Exception {
		return EntityUtils.toString(putEntity(url, bodyParams, headParams, charset, context), charset);
	}
	
	public static HttpResponse post(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset, HttpClientContext context) throws Exception {
		logger.info("api url:" + url);
		logger.info("api head params:" + json.toJson(headParams));
		logger.info("api body params:" + json.toJson(bodyParams));
		if(context.getCookieStore() != null) {
			logger.info("api cookies:" + json.toJson(context.getCookieStore().getCookies()));
		}

		HttpPost httpPost = new HttpPost(url);
		HttpClient client = getPoolingHttpClient();
		for (Entry<String, String> entry : headParams.entrySet()) {
			httpPost.addHeader(entry.getKey(), entry.getValue());
		}
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Entry<String, String> entry : bodyParams.entrySet()) {
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, charset));

		HttpResponse response = client.execute(httpPost, context);

		return response;	
	}

	public static HttpEntity postEntity(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset, HttpClientContext context) throws Exception {
		logger.info("api url:" + url);
		logger.info("api head params:" + json.toJson(headParams));
		logger.info("api body params:" + json.toJson(bodyParams));
		if(context.getCookieStore() != null) {
			logger.info("api cookies:" + json.toJson(context.getCookieStore().getCookies()));
		}

		HttpEntity entity = null;
		HttpPost httpPost = new HttpPost(url);
		try {
			HttpClient client = getHttpClient();
			for (Entry<String, String> entry : headParams.entrySet()) {
				httpPost.addHeader(entry.getKey(), entry.getValue());
			}
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (Entry<String, String> entry : bodyParams.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, charset));

			HttpResponse response = client.execute(httpPost, context);
			int status = response.getStatusLine().getStatusCode();
			entity = response.getEntity();
			if (status != 200) {
				throw new RuntimeException(String.format("api return error http code %d, detail: %n%s", status, EntityUtils.toString(entity, charset)));
			}
			return entity;
		} catch (Exception e) {
			String msg = String.format("Failed to call api '%s'", url);
			logger.error(msg, e);
			httpPost.abort();
			throw e;
		}
	}
	
	public static String postString(String url) throws Exception {
		return EntityUtils.toString(postEntity(url, new HashMap<String, String>(), new HashMap<String, String>(), "UTF-8", getContext()), "UTF-8");
	}
	
	public static String postString(String url, String charset) throws Exception {
		return EntityUtils.toString(postEntity(url, new HashMap<String, String>(), new HashMap<String, String>(), charset, getContext()), charset);
	}
	
	public static String postString(String url, Map<String, String> params) throws Exception {
		return EntityUtils.toString(postEntity(url, params, new HashMap<String, String>(), "UTF-8", getContext()), "UTF-8");
	}
	
	public static String postString(String url, Map<String, String> params, String charset) throws Exception {
		return EntityUtils.toString(postEntity(url, params, new HashMap<String, String>(), charset, getContext()), charset);
	}
	
	public static String postString(String url, Map<String, String> bodyParams, Map<String, String> headParams) throws Exception {
		return EntityUtils.toString(postEntity(url, bodyParams, headParams, "UTF-8", getContext()), "UTF-8");
	}
	
	public static String postString(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset) throws Exception {
		return EntityUtils.toString(postEntity(url, bodyParams, headParams, charset, getContext()), charset);
	}
	
	public static String postString(String url, Map<String, String> bodyParams, Map<String, String> headParams, String charset, HttpClientContext context) throws Exception {
		return EntityUtils.toString(postEntity(url, bodyParams, headParams, charset, context), charset);
	}

	/**
	 * 
	 * @param params 请求参数
	 * @return 构建query
	 */
	public static String buildQuery(String url, Map<String, String> params, String charset) {
		if (params == null || params.isEmpty()) {
			return url;
		}
		
		StringBuilder sb = new StringBuilder(url);
		if(StringUtils.contains(url, "?")) {
			sb.append("&");
		} else {
			sb.append("?");
		}
		
		boolean first = true;
		for (Entry<String, String> entry : params.entrySet()) {
			if (first) {
				first = false;
			} else {
				sb.append("&");
			}
			String key = entry.getKey();
			String value = entry.getValue();
			if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
				try {
					sb.append(key).append("=").append(URLEncoder.encode(value, charset));
				} catch (UnsupportedEncodingException e) {
				}
			}
		}
		return sb.toString();

	}

	public static CloseableHttpClient getPoolingHttpClient() {
		return HttpClientBuilder.create()
				.setConnectionManager(connectionManager)
				.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
				.setRedirectStrategy(new DefaultRedirectStrategy())
//				.setDefaultRequestConfig(requestConfig)
//				.setDefaultCookieStore(new BasicCookieStore())
				.build();
	}
	
	public static CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create()
				.setSSLSocketFactory(sslSocketFactory)
				.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
				.setRedirectStrategy(new DefaultRedirectStrategy())
//				.setDefaultRequestConfig(requestConfig)
//				.setDefaultCookieStore(new BasicCookieStore())
				.build();
	}

	public static HttpClientContext getContext() {
		return HttpClientContext.create();
	}

	public static List<Cookie> getCookies(HttpClientContext context) {
		return context.getCookieStore().getCookies();
	}

	public static String getCookie(HttpClientContext context, String name) {
		List<Cookie> cookies = context.getCookieStore().getCookies();
		for (Cookie cookie : cookies) {
			if (StringUtils.equals(name, cookie.getName())) {
				return cookie.getValue();
			}
		}

		return null;
	}

}
