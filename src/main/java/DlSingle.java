import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class DlSingle {

	public static ExecutorService exe = Executors.newFixedThreadPool(10);

	// static String urlFile = "TestUrls";

	static int threadCount = 10;

	public static String url = "https://u.f-q.me/watch?v=gyqTn47RsEM";
	public static String file = "mm.mp4";

	public static void main(String[] args) throws InterruptedException,
			IOException {

		final List<String[]> urls = new ArrayList<String[]>();
		urls.add(new String[] { url, file });

		final AtomicLong limit = new AtomicLong();
		final List<ControlFileFetch> works = foo(urls, limit, threadCount);
		final List<ControlFileFetch> finishWorks = new ArrayList<ControlFileFetch>();

		new Thread() {
			public void run() {

				while (true) {
					boolean allFinish = true;
					// reset limit
					limit.set(TestDl.NET_LIMIT);
					synchronized (limit) {
						limit.notifyAll();
					}

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// check all finish
					finishWorks.clear();
					for (ControlFileFetch work : works) {
						if (!work.finish) {
							allFinish = false;
						} else {
							finishWorks.add(work);
						}
					}

					// move finish files
					for (ControlFileFetch work : finishWorks) {
						works.remove(work);
						urls.remove(work.pair);
						// move mp4
						TestDl.moveFinishFile(work.pair[1]);
					}
					// update url file
					if (!finishWorks.isEmpty()) {
						TestDl.updateUrlsFile(urls);
					}

					if (allFinish) {
						break;
					}
				}
				TestDl.exe.shutdown();
				Log.log("============== all finish ==============");
			}
		}.start();
		Log.log("============== all started  ==============");
	}

	public static List<ControlFileFetch> foo(List<String[]> urls,
			final AtomicLong limit, int threadCount) {
		List<ControlFileFetch> ret = new ArrayList<ControlFileFetch>();
		for (String[] pair : urls) {
			String url = pair[0];
			final String fileName = pair[1];

			try {
				TranBean bean = new TranBean(url, ".", fileName, threadCount,
						limit);
				ControlFileFetch fileFetch = new ControlFileFetch(bean);
				fileFetch.pair = pair;
				exe.submit(fileFetch);
				ret.add(fileFetch);
				// fileFetch.start();
			} catch (Exception e) {
				Log.log(" start work fail " + url + " " + fileName);
				// Log.log("多线程下载文件出错:" + e.getMessage());
				e.printStackTrace();
				// System.exit(1);
			}
		}
		return ret;
	}

}
