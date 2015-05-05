package dl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetQuery {

	static String url = "https://u.f-q.me/results?search_sort=video_view_count&search_query=shoulder+mobility&page=2";
	// https://u.f-q.me/results?search_query=shoulder+mobility&search_sort=video_avg_rating
	// https://u.f-q.me/results?search_query=shoulder+mobility&search_sort=video_view_count

	static String VIDEO_TAG = "href";

	static String TITLE_TAG = "title";

	static String VIDEO_PRE = DlUtils.baseUrl;

	static List<String> CHECK_TAGS = Arrays.asList("/watch?", TITLE_TAG,
			"Duration");

	public static void main(String[] args) throws IOException {
		
		//for() page 1 ~ 10
		
		Map<String, String> map = new HashMap<String, String>();
		DlUtils.parseLines(url, false, map, CHECK_TAGS, VIDEO_PRE, VIDEO_TAG,
				TITLE_TAG);
		map.put(DlUtils.title, "shoulder_mobility");
		DlUtils.mapToFile(map, "query");
	}

}
