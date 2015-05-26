package dl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetListMulti {

	//	static String url = "https://u.f-q.me/browse_ajax?action_continuation=1&continuation=4qmFsgIeEhRWTFBMQ0JFOEE3MDFCRkMzMkQxRRoGQ0dVJTNE";

	//	static String url = "https://u.f-q.me/playlist?list=PLB67wXqPqtMcNVA-UndNUy7bahGJzLQbT"; //MobilityWOD_Episodes_199
	//	static String url = "https://u.f-q.me/playlist?list=PLdWvFCOAvyr0G-RZOvzlX1EroaIWA90bD"; //Classics_The_CrossFit_Games_176
	//	static String url = "https://u.f-q.me/playlist?list=PL5A46ABC019107E6E"; //Barbell Shrugged 173

	//	static String url = "https://u.f-q.me/playlist?list=PLdWvFCOAvyr0BNll5u6WbeR7RBerKczdB"; //2015_CrossFit_Games_Update_38
	//	static String url = "https://u.f-q.me/playlist?list=PLCBE8A701BFC32D1E"; //Exercises_and_Instruction_751

	//	static String url = "https://u.f-q.me/playlist?list=PLI9OxtiOLZQjSaW0Ep_s6-F2Khun3K4DE"; //The Daily BS  //Barbell Shrugged
	//	static String url = "https://u.f-q.me/playlist?list=PLI9OxtiOLZQhrtiXcWZKujR3wpTEnqaGe"; //Technique WOD  //Barbell Shrugged
	static String[] urls = new String[] {
			"https://u.f-q.me/playlist?list=PLgpYj0ECdkRrc1-gT4z7H9dxJpScRISel",//
			"https://u.f-q.me/playlist?list=PL272BD4839046157C",//
			"https://u.f-q.me/playlist?list=PLy8PuCtkXKnz3gutUfLiHybjP4WOzgAv9",//
			"https://u.f-q.me/playlist?list=PL0mP15ZH9m1rDSwsC7wZ-eRnHpcsCulPj",//
			"https://u.f-q.me/playlist?list=PL5E2AD723EAB6F172",//
			"https://u.f-q.me/playlist?list=PL0Og3AfvotoJW57OyOyp9bw8-SAT_kl25",//
			"https://u.f-q.me/playlist?list=PL865XuXfDCuihzdtZnlXBYzwrzbT3ng_Y",//
			"https://u.f-q.me/playlist?list=PLgKoFnrm-_CcL5apj0ds24beAR35DTT3B",//
			
			"https://u.f-q.me/playlist?list=PLp2lpdqjTahJfFgjfHrgDEkehZ1ITucBB",//
			"https://u.f-q.me/playlist?list=PL8FD9BC0AFBDC6EDB",//
			"https://u.f-q.me/playlist?list=PLHGaixoCtNhdvHulc51aZl5qvmj4-M4hC",//
			"https://u.f-q.me/playlist?list=PLNvi2HFQfE346mL6qva2BtkACLfHgr9Pg",//
			"https://u.f-q.me/playlist?list=PLTJcXJjCg8RsBoIt6K-Xi4BEWUkHKIkXO",//
			"https://u.f-q.me/playlist?list=PLJxAFePjZCkJUXLtKCHrLyjucbG7Gsqv0",//
			"https://u.f-q.me/playlist?list=PLUwBNE8tClniWibdrSSZAAHMJW0kWWFFa",//
			"https://u.f-q.me/playlist?list=PLuHcnCySZng6ZfuKoFXWJYAqyrt3OuX0P",//
			
			"https://u.f-q.me/playlist?list=PL40613DE1B635EE4F",//
			"https://u.f-q.me/playlist?list=PLOBrWlgp_aoL55OziqQcpXnOlWiWC5oTY",//
			"https://u.f-q.me/playlist?list=PLxoBGtrWfDEILKl5swuHxjiPNU5j0p9xE",//
	};

	static String VIDEO_TAG = "data-video-id";

	static String TITLE_TAG = "data-title";

	static String VIDEO_PRE = "https://u.f-q.me/watch?v=";

	static List<String> CHECK_TAGS = Arrays.asList(VIDEO_TAG, TITLE_TAG);

	public static void main(String[] args) throws IOException {

		Map<String, String> baseMap = new HashMap<String, String>();
		for (String url : urls) {
			Map<String, String> map = new HashMap<String, String>();
			DlUtils.parseLines(url, false, map, CHECK_TAGS, VIDEO_PRE, VIDEO_TAG, TITLE_TAG);
			baseMap.putAll(map);
		}

		baseMap.put(DlUtils.title, "scoliosis_exercises_for_correction");

		DlUtils.mapToFile(baseMap, "list");
	}

}
