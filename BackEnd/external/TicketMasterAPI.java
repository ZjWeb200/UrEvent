package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

// Backend server connecting to TicketMaster API
// Send HTTP request to TicketMaster API and get response.
public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "BAh5INLAfx9FM0jmRAEXLlhBXVGPkmY9";

	/**
	 * Helper methods
	 */
	// {
	// "name": "laioffer",
	// "id": "12345",
	// "url": "www.laioffer.com",
	// ...
	// "_embedded": {
	// "venues": [
	// {
	// "address": {
	// "line1": "101 First St,",
	// "line2": "Suite 101",
	// "line3": "...",
	// },
	// "city": {
	// "name": "San Francisco"
	// }
	// ...
	// },
	// ...
	// ]
	// }
	// ...
	// }

	// address 在 _embedded, venues下.最终地址要 具体地址+城市
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");

			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");

				// 此处只care第一个主要address。 第一个有可能为空，而第二个不为空，故用for loop找第一个非空的。
				for (int i = 0; i < venues.length(); i++) {
					JSONObject venue = venues.getJSONObject(i);

					StringBuilder sb = new StringBuilder();

					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");

						if (!address.isNull("line1")) {
							sb.append(address.getString("line1"));
						}
						if (!address.isNull("line2")) {
							sb.append(" ");
							sb.append(address.getString("line2"));
						}
						if (!address.isNull("line3")) {
							sb.append(" ");
							sb.append(address.getString("line3"));
						}
					}
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						if (!city.isNull("name")) {
							sb.append(", ");
							sb.append(city.getString("name"));
						}
					}

					if (!sb.toString().equals("")) {
						return sb.toString();
					}
				}
			}
		}
		return "";
	}

	// {"images": [{"url": "www.example.com/my_image.jpg"}, ...]}
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");

			// 也只关注第一个url (public url of the image)
			for (int i = 0; i < images.length(); i++) {
				JSONObject image = images.getJSONObject(i);

				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}

	// {"classifications" : [{"segment": {"name": "music"}}, ...]}
	private Set<String> getCategories(JSONObject event) throws JSONException {
		// 支持多个tag,可以通过多个categories搜索到该event。
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment"); // segment is a primary genre for an
																					// entity (Music, Sports, etc.)

					if (!segment.isNull("name")) {
						String name = segment.getString("name");
						categories.add(name);
					}
				}
			}
		}
		return categories;
	}

	// Convert JSONArray to a list of item objects.
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();

		for (int i = 0; i < events.length(); i++) {
			JSONObject event = events.getJSONObject(i);

			ItemBuilder builder = new ItemBuilder();
			// 以下5个都在TicketMaster Response structure _embedded events下的第一层
			if (!event.isNull("name")) { // 检测key是否存在
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			builder.setCategories(getCategories(event));
			builder.setAddress(getAddress(event));
			builder.setImageUrl(getImageUrl(event));

			itemList.add(builder.build());
		}
		return itemList;
	}

	// The function which actually sends HTTP request and get response with
	// TicketMaster API
	public List<Item> search(double lat, double lon, String keyword) {
		// if no specific keyword, 用 default即可
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}

		// Encode keyword, 可能不是ASCII
		try {
			keyword = java.net.URLEncoder.encode(keyword, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// convert lat and lon to geo hash
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);

		// query part of URL
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, keyword, 50);

		// Open a HTTP connection between your Java application and TicketMaster based
		// on url
		try {
			// 创建URL，response由HTTP来支持，故此处生成http的url connection.
			// 此connection连接java application和ticket master API.
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();

			// Set request method to GET. 这一句不写也可以，因为default method就是GET.
			connection.setRequestMethod("GET");

			// Send request to TicketMaster and get response, response code could be
			// returned directly
			// response body is saved in InputStream of connection.
			// 200 - ok; 404 - file not found
			int responseCode = connection.getResponseCode();

			/*
			 * 如果request失败，做其他处理 if (responseCode != 200) {
			 * 
			 * }
			 */

			// debug
			System.out.println("\nSending 'GET' request to URL: " + URL + "?" + query);
			System.out.println("Response code: " + responseCode);

			// Now read response body to get events data
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputline;
			StringBuilder response = new StringBuilder();
			// 一行一行read in data.
			while ((inputline = in.readLine()) != null) {
				response.append(inputline);
			}
			in.close(); // close buffered reader
			connection.disconnect();

			// 将读取的string转化为JSON
			JSONObject obj = new JSONObject(response.toString());
			// 判断response structure 里的 _embedded是否存在，数据都在它里面
			if (obj.isNull("_embedded")) {
				return new ArrayList<>();
			}
			// we want _embedded 里面的 events array
			JSONObject embedded = obj.getJSONObject("_embedded");
			JSONArray events = embedded.getJSONArray("events");
			return getItemList(events);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// 最后如果try没进入说明有异常，返回一个空的ArrayList即可
		return new ArrayList<>();
	}

	// Helper. 用于 debug
	private void queryAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null);
		try {
			for (Item event : events) {
				System.out.println(event.toJSONObject());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		tmApi.queryAPI(37.38, -122.08);
		// London, UK
		tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
	}

}
