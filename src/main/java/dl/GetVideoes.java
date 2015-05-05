package dl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetVideoes {

	// static String url = "https://u.f-q.me/user/womensworkoutchannel/videos";
	// //womensworkoutchannel

	// static String url = "https://u.f-q.me/user/1DAYUMAY/videos"; //1DAYUMAY
	// static String url = "https://u.f-q.me/user/playboy/videos"; //playboy
//	static String url = "https://u.f-q.me/user/bartkwan/videos"; // bartkwan
//	static String url = "https://u.f-q.me/user/myfitgirls/videos"; // myfitgirls
	
//	static String url = "https://u.f-q.me/user/SexyBikiniWorkout/videos"; // SexyBikiniWorkout
//	static String url = "https://u.f-q.me/user/womensworkoutchannel/videos"; // womensworkoutchannel
//	static String url = "https://u.f-q.me/user/BikiniModelFitness/videos"; // BikiniModelFitness
	
	static String url = "https://u.f-q.me/user/CrossFitHQ/videos"; // CrossFitHQ
	//
	// static String url = "https://u.f-q.me/user/BikiniModelFitness/videos";
	// //BikiniModelFitness

	static String VIDEO_TAG = "href";

	static String TITLE_TAG = "title";

	static String VIDEO_PRE = DlUtils.baseUrl;

	static List<String> CHECK_TAGS = Arrays.asList("/watch?", TITLE_TAG);

	public static void main(String[] args) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		DlUtils.parseLines(url, false, map, CHECK_TAGS, VIDEO_PRE, VIDEO_TAG,
				TITLE_TAG);
		DlUtils.mapToFile(map, "vedio");
	}

}
