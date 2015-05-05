package dl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetQuery {

	static String VIDEO_TAG = "href";

	static String TITLE_TAG = "title";

	static String VIDEO_PRE = DlUtils.baseUrl;

	/////////////////query param
	//	static String BASE_QUERY_URL = "https://u.f-q.me/results?search_sort=$SORT&search_query=$QUERY&page=$PAGE";
	static String BASE_QUERY_URL = "https://u.f-q.me/results?search_query=$QUERY&page=$PAGE";
	//	static String BASE_QUERY_URL = "https://u.f-q.me/results?lclk=long&filters=long&search_query=$QUERY&page=$PAGE"; //long

	//https://u.f-q.me/results?lclk=long&filters=long&search_query=mobility

	static String SEARCH_SORT = "video_view_count";

	static String SEARCH_QUERY = "mobility"; //keywords

	//	static String PAGE_COUNT = "page";

	/////////////////query param

	static List<String> CHECK_TAGS = Arrays.asList("/watch?", TITLE_TAG, "Duration");

	public static void main(String[] args) throws IOException {

		SEARCH_QUERY = SEARCH_QUERY.replace(" ", "+");

		Map<String, String> allMap = new HashMap<String, String>();

		for (int i = 1; i <= 5; i++) {
			String url = BASE_QUERY_URL.replace("$SORT", SEARCH_SORT)
					.replace("$QUERY", SEARCH_QUERY).replace("$PAGE", i + "");

			System.out.println("query : " + url);

			Map<String, String> map = new HashMap<String, String>();
			DlUtils.parseLines(url, false, map, CHECK_TAGS, VIDEO_PRE, VIDEO_TAG, TITLE_TAG);

			for (String key : map.keySet()) {
				if (!allMap.containsKey(key)) {
					allMap.put(key, map.get(key));
				}
				else {
					System.out.println(" ========== duplicate url " + key + " " + map.get(key));
				}
			}
		}

		allMap.put(DlUtils.title, SEARCH_QUERY.replace("+", "_"));
		DlUtils.mapToFile(allMap, "query");
	}

}
