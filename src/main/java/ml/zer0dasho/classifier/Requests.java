package ml.zer0dasho.classifier;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

public class Requests {

	public static HttpResponse get(String url) throws ClientProtocolException, IOException {
		HttpClient client = HttpClients.createDefault();
		
		HttpGet get = new HttpGet(url);
		get.addHeader("User-Agent", "classrooms-finder");
		
		return client.execute(get);
	}
	
	public static String readAllBytes(InputStream is) throws IOException {
		String result = new String();
		
		int bit = -1;
		
		while((bit = is.read()) != -1)
			result += (char) bit;
		
		return result;
	}
	
	public static interface Throws<T> {
		public T run() throws Exception;	
	}
	
	public static <T> T Try(Throws<T> th) {
		try {
			return th.run();
		}catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}