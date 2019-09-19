package external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;


public class TicketMasterClient {
	// construct the url to send to ticket master.
	private static final String HOST = "https://app.ticketmaster.com";
	private static final String PATH = "/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "event";
	private static final int DEFAULT_RADIUS = 50;
	private static final int DEFAULT_SIZE = 30;
	// ticket master上自己的consumer key
	private static final String API_KEY = "xW63pKCHMWppnRstirNTFA1EXDOPUZA7";
	
	public List<Item> search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		
		try {
			//防止 keyword中有空格等特殊符号，需要encode。
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String query = String.format("apikey=%s&latlong=%s,%s&keyword=%s&radius=%s&size=%s", API_KEY, lat, lon, keyword, DEFAULT_RADIUS, DEFAULT_SIZE);
		String url = HOST + PATH + "?" + query;
		
		StringBuilder response = new StringBuilder();
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET"); // set the http request method.
			connection.connect(); // send the http request
			System.out.println("Sending request to: " + url);
			
			int responseCode = connection.getResponseCode();
			System.out.println("Response code: " + responseCode);
			
			if (responseCode != 200) {
				return new ArrayList<>();
			}
			
			// get http response from ticket master.
			// Create a BufferedReader to help read text from a character-input stream.
			// Provide for the efficient reading of characters, arrays, and lines.
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			// reader.readLine() read in 8KB for each call.
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			JSONObject obj = new JSONObject(response.toString());
			// if there is _embedded
			if (!obj.isNull("_embedded")) {
				JSONObject embedded = obj.getJSONObject("_embedded");
				return getItemList(embedded.getJSONArray("events"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	// Convert JSONArray to a list of item objects.
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		for (int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			if (!event.isNull("dates")) {
				JSONObject dates = event.getJSONObject("dates");
				if (!dates.isNull("start")) {
					JSONObject start = dates.getJSONObject("start");
					if (!start.isNull("localDate")) builder.setLocalDate(start.getString("localDate"));
				}
				
			}
			builder.setAddress(getAddress(event));
			builder.setCategories(getCategories(event));
			builder.setImageUrl(getImageUrl(event));

			// item is one event from TicketMaster.
			Item item = builder.build();
			itemList.add(item);
		}
		return itemList;
	}

	// Check if there is address and city of the event.
	// If yes, get the address (at most 3 lines) and the city.
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				for (int i = 0; i < venues.length(); ++i) {
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder builder = new StringBuilder();
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if (!address.isNull("line1")) {
							builder.append(address.getString("line1"));
						}
						
						if (!address.isNull("line2")) {
							builder.append(",");
							builder.append(address.getString("line2"));
						}
						
						if (!address.isNull("line3")) {
							builder.append(",");
							builder.append(address.getString("line3"));
						}
					}
					
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						builder.append(",");
						builder.append(city.getString("name"));
					}
					
					String result = builder.toString();
					if (!result.isEmpty()) {
						return result;
					}
				}
			}
		}
		return "";	
	}
	
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray array = event.getJSONArray("images");
			for (int i = 0; i < array.length(); i++) {
				JSONObject image = array.getJSONObject(i);
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}

	private Set<String> getCategories(JSONObject event) throws JSONException {		
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); ++i) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}


	
//	/**
//	 * Main entry to test TicketMasterClient.
//	 */
//	public static void main(String[] args) {
//		TicketMasterClient client = new TicketMasterClient();
//		List<Item> events = client.search(34.052235,  -118.243683, null);
//
//		for (Item event : events) {
//			System.out.println(event.toJSONObject());
//		}
//	}
}
