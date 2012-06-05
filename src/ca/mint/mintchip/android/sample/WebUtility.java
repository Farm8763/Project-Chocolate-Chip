package ca.mint.mintchip.android.sample;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class WebUtility {

	private static final String HEADER_LOCATION = "Location";


	public static String downloadContent(String url) throws ClientProtocolException, IOException {
		
		HttpClient client = new DefaultHttpClient();   
		
		HttpGet httpGet = new HttpGet(url);
		
		HttpResponse response = client.execute(httpGet);
		
		return EntityUtils.toString(response.getEntity());
	}
	
	
	public static String postContent(String url, String content) throws ClientProtocolException, IOException {    
		
		HttpClient client = new DefaultHttpClient();
		HttpClientParams.setRedirecting(client.getParams(), false); 
		
		HttpPost httpPost = new HttpPost(url);  
		
		StringEntity entity = new StringEntity(content);
		
		httpPost.setEntity(entity);        
		
		HttpResponse response = client.execute(httpPost);
		
		String newUrl = null;
		
	    // Handle the redirect
		switch (response.getStatusLine().getStatusCode()) {
			
			case HttpStatus.SC_TEMPORARY_REDIRECT:
			case HttpStatus.SC_MOVED_PERMANENTLY:
			case HttpStatus.SC_MOVED_TEMPORARILY:
			    
				Header locationHeader = response.getLastHeader(HEADER_LOCATION); 
			    if (locationHeader != null) { 
			    	newUrl = locationHeader.getValue(); 		    
			    }
			    break;
		}
		
		return newUrl;
	}	
}
