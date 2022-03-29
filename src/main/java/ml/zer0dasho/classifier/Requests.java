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
		HttpClient client = HttpClients.createMinimal();
		HttpGet get = new HttpGet(url);
		return client.execute(get);
	}
	
	public static String readAllBytes(InputStream is) throws IOException {
		return new String(is.readAllBytes());
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