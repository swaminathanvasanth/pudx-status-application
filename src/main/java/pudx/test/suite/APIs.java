package pudx.test.suite;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class APIs {

	static CloseableHttpClient client;
	static HttpPost httpPost;
	static String json;
	static JSONObject jo;
	static StringEntity entity;
	static CloseableHttpResponse response;
	static JSONArray items_id;
	static JSONObject aqm_status, flood_status, iitm_status, streetlight_status, wifi_status, iitm_forecast_status, safetipin_status, changebhai_status, tomtom_status, itms_status;
	static HashMap<String, String> aqm, flood, iitm, streetlight, wifi, iitm_forecast, safetipin, changebhai, tomtom, itms, item_status;
	static String resourceServer,resourceServerGroups;
	private static String[] resourceServerGroupName;
	static JSONParser parser;
	static Object obj;
	static JSONObject jsonObject;
	static String state;
	static HashSet<String> items = new HashSet<String>();
	private static final Logger logger = Logger.getLogger(APIs.class.getName());

	public static void main(String[] a) {

		Properties config_prop = new Properties();
		InputStream config_input = null;

		try {

			config_input = new FileInputStream("config.properties");
			config_prop.load(config_input);
			resourceServer = config_prop.getProperty("resourceServer");
			resourceServerGroups = config_prop.getProperty("resourceServerGroups");
			resourceServerGroupName = resourceServerGroups.split(";");

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (config_input != null) {
				try {
					config_input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		read_catalogue();

		try {
			startTests();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void read_catalogue() {
		items_id = new JSONArray();
		try {
			parser = new JSONParser();
			JSONArray itemsArray = (JSONArray) parser.parse(new FileReader("items.json"));
			for (int i = 0; i < itemsArray.size(); i++) {
				JSONObject jo = (JSONObject) itemsArray.get(i);
				String __id = (String) jo.get("id");
				items_id.add(__id);
			}

			logger.info("Updated items list. Totally loaded " + items_id.size() + " items");

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void startTests() throws ClientProtocolException, IOException {

		long start = System.currentTimeMillis();
		
		logger.info("Starting Tests for " + items_id.size() + " items.");
		String id;
		item_status = new HashMap<String, String>();
		aqm = new HashMap<String, String>();
		flood = new HashMap<String, String>();
		iitm = new HashMap<String, String>();
		iitm_forecast = new HashMap<String, String>();
		safetipin = new HashMap<String, String>();
		changebhai = new HashMap<String, String>();
		tomtom = new HashMap<String, String>();
		wifi = new HashMap<String, String>();
		streetlight = new HashMap<String, String>();
		itms = new HashMap<String, String>();
		
		SSLContextBuilder builder = new SSLContextBuilder();
		SSLConnectionSocketFactory sslsf = null;
		try {
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			sslsf = new SSLConnectionSocketFactory(
			        builder.build());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		client = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		httpPost = new HttpPost(resourceServer);

		for (int i = 0; i < items_id.size(); i++) {
			id = items_id.get(i).toString().replaceAll("\\\\", "");
			logger.info("Testing : " + id);
			jo = new JSONObject();
			jo.put("id", id); // id = id.replaceAll("\\\\", "");
			jo.put("options", "status");

			entity = new StringEntity(jo.toString());
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			response = client.execute(httpPost);

			int status = response.getStatusLine().getStatusCode();

			// Getting the response body.
			String responseBody = EntityUtils.toString(response.getEntity());

			if (status == 200) {
				System.out.println(responseBody);
				
				if(responseBody.length()>2)
				{
				JSONParser parser = new JSONParser();
				JSONArray itemsArray = null;
				try {
					itemsArray = (JSONArray) parser.parse(responseBody);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				JSONObject jo = (JSONObject) itemsArray.get(0);
						 	
			 	state = (String) jo.get("status");
				
				System.out.println("SUCCESS");
				} else {
					state = "in-active";
					System.out.println(responseBody);
					System.out.println("BAD Request");
				}
			} else {
				state = "down";
				System.out.println(responseBody);
				System.out.println("BAD Request");
			}

			if(id.split("/")[3].equalsIgnoreCase(resourceServerGroupName[0])) {
				aqm.put(id, state);
			} else if(id.split("/")[3].equalsIgnoreCase(resourceServerGroupName[1])) {
				flood.put(id, state);
			} else if(id.split("/")[3].equalsIgnoreCase(resourceServerGroupName[2])) {
				iitm.put(id, state);
			} else if(id.split("/")[3].equalsIgnoreCase(resourceServerGroupName[3])) {
				streetlight.put(id, state);
			} else if(id.split("/")[3].equalsIgnoreCase(resourceServerGroupName[4])) {
				wifi.put(id, state);
			} else if(id.split("/")[3].equalsIgnoreCase(resourceServerGroupName[5])) {
				iitm_forecast.put(id, state);
			} else if(id.split("/")[3].equalsIgnoreCase(resourceServerGroupName[6])) {
				tomtom.put(id, state);
			} else if(id.split("/")[3].equalsIgnoreCase(resourceServerGroupName[7])) {
				safetipin.put(id, state);
			} else if(id.split("/")[3].equalsIgnoreCase(resourceServerGroupName[7])) {
				itms.put(id, state);
			} else if(id.split("/")[3].equalsIgnoreCase(resourceServerGroupName[7])) {
				changebhai.put(id, state);
			}
		}

		client.close();

		System.out.println("Completed Tests");
		
		aqm_status = new JSONObject();
		aqm_status.put(resourceServerGroupName[0], new JSONObject(aqm));
		
		flood_status = new JSONObject();
		flood_status.put(resourceServerGroupName[1], new JSONObject(flood));
		
		iitm_status = new JSONObject();
		iitm_status.put(resourceServerGroupName[2], new JSONObject(iitm));
		
		iitm_forecast_status = new JSONObject();
		iitm_forecast_status.put(resourceServerGroupName[5], new JSONObject(iitm_forecast));
		
		streetlight_status = new JSONObject();
		streetlight_status.put(resourceServerGroupName[3], new JSONObject(streetlight));
		
		wifi_status = new JSONObject();
		wifi_status.put(resourceServerGroupName[4], new JSONObject(wifi));
		
		tomtom_status = new JSONObject();
		tomtom_status.put(resourceServerGroupName[6], new JSONObject(tomtom));
		
		safetipin_status = new JSONObject();
		safetipin_status.put(resourceServerGroupName[7], new JSONObject(safetipin));
		
		itms_status = new JSONObject();
		itms_status.put(resourceServerGroupName[8], new JSONObject(itms));
		
		changebhai_status = new JSONObject();
		changebhai_status.put(resourceServerGroupName[9], new JSONObject(changebhai));
		
		
		JSONArray array_of_status = new JSONArray();
		
		array_of_status.add(aqm_status);
		array_of_status.add(flood_status);
		array_of_status.add(iitm_status);
		array_of_status.add(iitm_forecast_status);
		array_of_status.add(streetlight_status);
		array_of_status.add(wifi_status);
		array_of_status.add(tomtom_status);
		array_of_status.add(safetipin_status);
		array_of_status.add(itms_status);
		array_of_status.add(changebhai_status);

		System.out.println("-------------- JOB Completed -----------");
		System.out.println(array_of_status.toJSONString().replaceAll("\\\\", ""));
		long end = System.currentTimeMillis();

		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");

	}

}
