package dl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetList {

	//	static String url = "https://u.f-q.me/browse_ajax?action_continuation=1&continuation=4qmFsgIeEhRWTFBMQ0JFOEE3MDFCRkMzMkQxRRoGQ0dVJTNE";

	//	static String url = "https://u.f-q.me/playlist?list=PLB67wXqPqtMcNVA-UndNUy7bahGJzLQbT"; //MobilityWOD_Episodes_199
	//	static String url = "https://u.f-q.me/playlist?list=PLdWvFCOAvyr0G-RZOvzlX1EroaIWA90bD"; //Classics_The_CrossFit_Games_176
	//	static String url = "https://u.f-q.me/playlist?list=PL5A46ABC019107E6E"; //Barbell Shrugged 173

	//	static String url = "https://u.f-q.me/playlist?list=PLdWvFCOAvyr0BNll5u6WbeR7RBerKczdB"; //2015_CrossFit_Games_Update_38
	//	static String url = "https://u.f-q.me/playlist?list=PLCBE8A701BFC32D1E"; //Exercises_and_Instruction_751

	//	static String url = "https://u.f-q.me/playlist?list=PLI9OxtiOLZQjSaW0Ep_s6-F2Khun3K4DE"; //The Daily BS  //Barbell Shrugged
	//	static String url = "https://u.f-q.me/playlist?list=PLI9OxtiOLZQhrtiXcWZKujR3wpTEnqaGe"; //Technique WOD  //Barbell Shrugged
	static String url = "https://u.f-q.me/playlist?list=PL0Og3AfvotoJW57OyOyp9bw8-SAT_kl25";

	static String VIDEO_TAG = "data-video-id";

	static String TITLE_TAG = "data-title";

	static String VIDEO_PRE = "https://u.f-q.me/watch?v=";

	static List<String> CHECK_TAGS = Arrays.asList(VIDEO_TAG, TITLE_TAG);

	public static void main(String[] args) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		DlUtils.parseLines(url, false, map, CHECK_TAGS, VIDEO_PRE, VIDEO_TAG, TITLE_TAG);
		DlUtils.mapToFile(map, "list");
	}

}
