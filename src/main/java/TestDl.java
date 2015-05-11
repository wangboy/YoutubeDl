import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * 多线程断点续传 基于 (HTTP)
 * 
 * @author Bin Windows NT 6.1
 */
public class TestDl {

	public static int NET_LIMIT = 300 * 1024;

	public static File curFile = null;

	public static ExecutorService exe = Executors.newFixedThreadPool(2);

	static int threadCount = 4;

	static String doneDir = "done";

	static String urlDir = "url";

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		if (args != null && args.length > 0) {
			int limit = -1;
			try {
				limit = Integer.parseInt(args[0]);
			}
			catch (Exception e) {}
			if (limit > 0) {
				Log.log("============== set limit to " + limit + " ==============");
				NET_LIMIT = limit;
			}
		}
		while (true) {
			Thread.sleep(5000);

			dlFromDir(urlDir);
		}

		// exe.shutdown();
	}

	public static void dlFromDir(String dir) {
		File dirFile = new File(dir);
		File[] files = dirFile.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				curFile = file;
				try {
					Log.log("============== start down load from " + file.getName()
							+ " ==============");
					dlFromFile(file);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	// TODO check diff

	public static void dlFromFile(File file) {

		final List<String[]> urls = new ArrayList<String[]>();

		try {
			urls.addAll(readMap(file));
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}

		Log.log("============== all file count " + urls.size() + " in " + file.getName()
				+ " ==============");

		if (urls.isEmpty()) {
			Log.log("============== " + file.getName() + " is empty ==============");
			return;
		}

		final AtomicLong limit = new AtomicLong();
		final List<ControlFileFetch> works = foo(urls, limit, threadCount);
		final List<ControlFileFetch> finishWorks = new ArrayList<ControlFileFetch>();

		while (true) {
			boolean allFinish = true;
			// reset limit
			limit.set(NET_LIMIT / 2);
			synchronized (limit) {
				limit.notifyAll();
			}

			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}

			// check all finish
			finishWorks.clear();
			for (ControlFileFetch work : works) {
				if (!work.finish) {
					allFinish = false;
				}
				else {
					finishWorks.add(work);
				}
			}

			// move finish files
			for (ControlFileFetch work : finishWorks) {
				works.remove(work);
				urls.remove(work.pair);
				// move mp4
				moveFinishFile(work.pair[1]);
			}
			// update url file
			if (!finishWorks.isEmpty()) {
				updateUrlsFile(urls);
			}

			if (allFinish) {
				break;
			}
		}

		file.delete();
		// exe.shutdown();
		Log.log("============== all finish from " + file.getName() + "==============");

	}

	public static void moveFinishFile(String fileName) {
		try {
			Log.log("============== mv finish file " + fileName + " ==============");
			// done dir
			String curFileName = curFile.getName();
			File dir = new File("." + File.separator + "done" + File.separator + curFileName);
			dir.mkdirs();

			// mv
			String path = "." + File.separator + fileName;
			File file = new File(path);
			if (file.exists()) {
				file.renameTo(new File(dir.getPath() + File.separator + fileName));
			}

			File infoFile = new File("." + File.separator + fileName + ".info");
			infoFile.delete();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean validFileName(String fileName) {
		if (fileName.contains("_Deleted_Video_.mp4") || fileName.contains("_Private_Video_.mp4")) {
			return false;
		}
		return true;
	}

	public static List<String[]> readMap(File file) throws IOException {
		List<String[]> ret = new ArrayList<String[]>();
		try {
			// 创建数据输出流
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String line = null;
			while ((line = br.readLine()) != null && !line.equals("")) {
				String[] urls = line.split(" ");

				String url = urls[0];
				String fileName = urls[1];
				if (!validFileName(fileName)) {
					Log.log("===<<<<<<<<<<<<<<<<== exclude invalid file " + url + " = " + fileName);
					continue;
				}
				ret.add(urls);
			}
			br.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static void updateUrlsFile(List<String[]> urls) {
		try {
			Log.log("============== update urls file  ==============");
			String curFileName = curFile.getPath();
			// to new file
			String newFile = curFileName + "_new";
			File file = new File(newFile);
			file.delete();
			file.createNewFile();

			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (String[] pair : urls) {
				bw.write(pair[0] + " " + pair[1]);
				bw.newLine();
			}
			bw.close();

			// rename to old
			curFile.delete();
			file.renameTo(new File(curFileName));
		}
		catch (Exception e) {
			Log.log("保存下载信息出错:" + e.getMessage());
			e.printStackTrace();
		}

	}

	public static List<ControlFileFetch> foo(List<String[]> urls, final AtomicLong limit,
												int threadCount) {
		List<ControlFileFetch> ret = new ArrayList<ControlFileFetch>();
		for (String[] pair : urls) {
			String url = pair[0];
			final String fileName = pair[1];

			try {
				TranBean bean = new TranBean(url, ".", fileName, threadCount, limit);
				ControlFileFetch fileFetch = new ControlFileFetch(bean);
				fileFetch.pair = pair;
				exe.submit(fileFetch);
				ret.add(fileFetch);
				// fileFetch.start();
			}
			catch (Exception e) {
				Log.log(" start work fail " + url + " " + fileName);
				// Log.log("多线程下载文件出错:" + e.getMessage());
				e.printStackTrace();
				// System.exit(1);
			}
		}
		return ret;
	}

	public static String httpsGet(String su) {

		String https_url = su;
		URL url;
		try {

			url = new URL(https_url);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			// dumpl all cert info
			return print_https_cert(con);

			// dump all the content
			// List<String> lines = getContent(con);
			// return lines;
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String print_https_cert(HttpsURLConnection con) {

		if (con != null) {
			try {
				Log.log(" Https Response Code : " + con.getResponseCode());
				Map<String, List<String>> map = con.getHeaderFields();
				// for (String key : map.keySet()) {
				// Log.log(key + " = " + map.get(key));
				// }

				// Log.log(map.get("Location").get(0));
				if (map.containsKey("Location")) {
					return map.get("Location").get(0);
				}
			}
			catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;

	}

}

// 扩展多线程类,负责文件的抓取,控制内部线程
class ControlFileFetch implements Runnable {
	TranBean tranBean = null; // 扩展信息bean

	long[] startPosition; // 开始位置

	long[] endPosition; // 结束位置

	FileFetch[] childThread; // 子线程对象

	long fileLength; // 文件长度

	boolean isFitstGet = true; // 是否第一次去文件

	boolean isStopGet = false; // 停止标志

	File fileName; // 文件下载的临时信息

	DataOutputStream output; // 输出到文件的输出流

	AtomicLong limit;

	String httpUrl;

	public boolean finish = false;

	public String[] pair = null;

	public ControlFileFetch(TranBean tranBean) {
		this.tranBean = tranBean;
		fileName = new File(tranBean.getFileDir() + File.separator + tranBean.getFileName()
				+ ".info"); // 创建文件
		// Log.log(tranBean.getFileDir() + File.separator +
		// tranBean.getFileName()
		// + ".info");
		if (fileName.exists()) {
			isFitstGet = false;
			readInfo();
		}
		else {
			startPosition = new long[tranBean.getCount()];
			endPosition = new long[tranBean.getCount()];
		}
		this.limit = tranBean.getLimit();
	}

	public void run() {
		try {
			Log.log(" start download " + fileName);
			// to http
			String httpUrl = null;
			int retry = 0;
			while (httpUrl == null && retry <= 20) {
				try {
					httpUrl = TestDl.httpsGet(tranBean.getWebAddr());
					if (httpUrl == null) {
						Log.log(fileName + " === retry =====" + retry++);
						try {
							Thread.sleep(1000);
						}
						catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (httpUrl != null) {
				this.httpUrl = httpUrl;
			}
			else {
				Log.log("=======  get http url fail , " + tranBean.getWebAddr() + " "
						+ tranBean.getFileName());
				return;
			}

			if (isFitstGet) { // 第一次读取文件

				for (int j = 0; j < 10; j++) {
					fileLength = getFieldSize(); // 调用方法获取文件长度
					if (fileLength > 0) {
						break;
					}
					System.out.println(fileName + " === getFieldSize =====" + j);
				}

				if (fileLength == -1) {
					System.err.println(fileName + " 文件长度为止");
				}
				else if (fileLength == -2) {
					System.err.println(fileName + "不能访问文件");
				}
				else {
					// 循环划分 每个线程要下载的文件的开始位置
					for (int i = 0; i < startPosition.length; i++) {
						startPosition[i] = (long) (i * (fileLength / startPosition.length));
					}
					// 循环划分每个线程要下载的文件的结束位置
					for (int i = 0; i < endPosition.length - 1; i++) {
						endPosition[i] = startPosition[i + 1];
					}
					// 设置最后一个 线程的下载 结束位置 文件的的长度
					endPosition[endPosition.length - 1] = fileLength;
				}
			}
			else {
				fileLength = endPosition[endPosition.length - 1];
			}
			// 创建 子线程数量的数组
			childThread = new FileFetch[startPosition.length];

			Log.log(fileName + "文件的长度:" + fileLength);

			long start = System.currentTimeMillis();

			// TestDl.perSec.set(TestDl.limit);

			for (int i = 0; i < startPosition.length; i++) {
				childThread[i] = new FileFetch(this.httpUrl, tranBean.getFileDir() + File.separator
						+ tranBean.getFileName(), startPosition[i], endPosition[i], i, this.limit);
				Log.log(fileName + "线程" + (i + 1) + ",的开始位置=" + startPosition[i] + ",结束位置="
						+ endPosition[i]);
				childThread[i].start();
			}

			boolean breakWhile = false;
			int print = 1;
			while (!isStopGet) {
				// synchronized (TestDl.lock) {
				// TestDl.lock.notifyAll();
				// }

				savePosition();
				Log.sleep(500);//

				// /print percent
				if (print++ % 10 == 0) { // 5sec
					// print = 1;
					long partLength = fileLength / startPosition.length;
					for (int i = 0; i < startPosition.length; i++) {
						long sp = childThread[i].startPosition;
						long ep = childThread[i].endPosition;
						long remain = (ep - sp) <= 0 ? 0 : ep - sp;
						long dlCount = partLength - remain;

						long percent = (dlCount * 100) / partLength;
						Log.log(fileName + "线程" + (i + 1) + " " + percent + " % ");
					}
				}

				// check finish
				breakWhile = true;
				for (int i = 0; i < startPosition.length; i++) { // 循环实现下载文件
					if (!childThread[i].downLoadOver) {
						breakWhile = false;
						break;
					}
				}
				if (breakWhile)
					break;

				// TestDl.perSec.set(TestDl.limit);
			}
			// System.err.println("文件下载结束!");
			savePosition();

			finish = true;

			long end = System.currentTimeMillis();
			long useTime = end - start;

			long speed = 0;
			if (useTime != 0) {
				speed = (long) ((fileLength / 1024) / (useTime / 1000d));
			}

			// TODO check file size again

			Log.log("============== DownLoad file " + fileName + " finish ");
			Log.log(" use time " + useTime / 1000 + " s ," + " file length "
					+ ((fileLength / 1024) / 1024) + ", speed " + speed + " k/s ");
		}
		catch (Exception e) {
			Log.log(fileName + "下载文件出错:" + e.getMessage());
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	// 保存下载信息(文件指针信息)
	private void savePosition() {
		try {
			output = new DataOutputStream(new FileOutputStream(fileName));
			output.writeInt(startPosition.length); // 几个线程
			for (int i = 0; i < startPosition.length; i++) {
				output.writeLong(childThread[i].startPosition);
				output.writeLong(childThread[i].endPosition);
			}
			output.close();
		}
		catch (Exception e) {
			Log.log(fileName + "保存下载信息出错:" + e.getMessage());
			e.printStackTrace();
		}

	}

	// 获得文件的长度
	public long getFieldSize() {
		long fileLength = -1;
		try {
			URL url = new URL(this.httpUrl); // 根据网址传入网址创建URL对象
			// 打开连接对象
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			// 设置描述发出HTTP请求的终端信息
			httpConnection.setRequestProperty("User-Agent", "NetFox");
			int responseCode = httpConnection.getResponseCode();
			// Log.log(" getFieldSize responseCode = " + responseCode);
			// 表示不能访问文件
			if (responseCode >= 400) {
				errorCode(responseCode);
				return -2;
			}
			// for (String key : httpConnection.getHeaderFields().keySet()) {
			// Log.log(key + " = " +
			// httpConnection.getHeaderFields().get(key));
			// }
			String head;
			for (int i = 1;; i++) {
				head = httpConnection.getHeaderFieldKey(i); // 获取文件头部信息
				if (head != null) {
					if (head.equals("Content-Length")) { // 根据头部信息获取文件长度
						fileLength = Long.parseLong(httpConnection.getHeaderField(head));
						break;
					}
				}
				else {
					break;
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			Log.log(fileName + "获取文件长度出错:" + e.getMessage());
		}
		// Log.log("文件长度" + fileLength);
		return fileLength;

	}

	private void errorCode(int errorCode) {
		Log.log(fileName + "错误代码:" + errorCode);
	}

	// 读取文件指针位置
	private void readInfo() {
		try {
			// 创建数据输出流
			DataInputStream input = new DataInputStream(new FileInputStream(fileName));
			int count = input.readInt(); // 读取分成的线程下载个数
			startPosition = new long[count]; // 设置开始线程
			endPosition = new long[count]; // 设置结束线程
			for (int i = 0; i < startPosition.length; i++) {
				startPosition[i] = input.readLong(); // 读取每个线程的开始位置
				endPosition[i] = input.readLong(); // 读取每个线程的结束位置
			}
			input.close();
		}
		catch (Exception e) {
			Log.log(fileName + "读取文件指针位置出错:" + e.getMessage());
			e.printStackTrace();
		}
	}

}

// 扩展线程类,实现部分文件的抓取
class FileFetch extends Thread {
	String webAddr; // 网址

	long startPosition; // 开始位置

	long endPosition; // 结束位置

	int threadID; // 线程编号

	boolean downLoadOver = false; // 下载结束

	boolean isStopGet = false; // 是否停止请求

	FileAccess fileAccessI = null; // 存储文件的类

	String fileName;

	AtomicLong limit;

	public FileFetch(String surl, String sname, long startPosition, long endPosition, int threadID,
						AtomicLong limit) throws IOException {
		this.webAddr = surl;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.threadID = threadID;
		this.fileAccessI = new FileAccess(sname, startPosition);
		this.limit = limit;
		this.fileName = sname;
	}

	// 实现线程的方法
	public void run() {
		while (startPosition < endPosition && !isStopGet) {
			try {
				URL url = new URL(webAddr); // 根据网络资源创建URL对象
				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(); // 创建 打开的连接对象
				// 设置描述发出的HTTP请求的终端的信息
				httpConnection.setRequestProperty("User-Agent", "NetFox");
				String sproperty = "bytes=" + startPosition + "-";
				httpConnection.setRequestProperty("RANGE", sproperty);
				Log.log(sproperty);

				// 获取 网络资源的输入流
				InputStream input = httpConnection.getInputStream();

				byte[] b = new byte[1024];
				int nRead;
				// 循环将文件下载制定目录
				while ((nRead = input.read(b, 0, 1024)) > 0 && startPosition < endPosition
						&& !isStopGet) {
					startPosition += fileAccessI.write(b, 0, nRead); // 调用方法将内容写入文件

					long remain = this.limit.addAndGet(-nRead);
					if (remain < 0) {
						// Log.log(fileName + " 线程\t" + (threadID + 1) +
						// "\t wait !");
						synchronized (this.limit) {
							this.limit.wait();
						}
					}
				}

				if (startPosition >= endPosition) {
					downLoadOver = true; // 这里判断没问题么？
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(500);
				}
				catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		Log.log(fileName + " 线程\t" + (threadID + 1) + "\t结束....");

		if (startPosition >= endPosition) {
			downLoadOver = true;
		}
	}

	// 打印回应的头的信息
	public void logResponseHead(HttpURLConnection con) {
		for (int i = 1;; i++) {
			String header = con.getHeaderFieldKey(i); // 循环答应回应的头信息
			if (header != null) {
				Log.log(header + ":" + con.getHeaderField(header));
			}
			else
				break;
		}
	}

	public void splitterStop() {
		isStopGet = true;
	}

}

// 存储文件的类
class FileAccess implements Serializable {
	RandomAccessFile saveFile; // 要保存的文件

	long position;

	public FileAccess() throws IOException {
		this("", 0);
	}

	public FileAccess(String sname, long position) throws IOException {
		this.saveFile = new RandomAccessFile(sname, "rw"); // 创建随机读取对象, 以 读/写的方式
		this.position = position;
		saveFile.seek(position); // 设置指针位置
	}

	int count = 0;

	long fileSize = 0;

	// 将字符数据 写入文件
	public synchronized int write(byte[] b, int start, int length) {
		int n = -1;
		try {
			saveFile.write(b, start, length);
			n = length;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// if (count++ % 1000 == 0) {
		// try {
		// Log.log(this.saveFile.length() + "  " + this.fileSize);
		// Log.log(" to file " + length + " bytes ");
		// this.fileSize = this.saveFile.length();
		// }
		// catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

		return n;
	}
}

// 传输保存信息的类
class TranBean {
	private String webAddr; // 下载地址

	private String fileDir; // 下载到指定的目录

	private String fileName; // 下载后文件的新名字

	private int count; // 文件分几个线程下载, 默认为 3个

	private AtomicLong limit;

	// public TranBean() { // 默认的构造方法
	// this("", "", "", 3, null);
	// } // 带参数的构造方法

	public TranBean(String webAddr, String fileDir, String fileName, int count, AtomicLong limit) {
		this.webAddr = webAddr;
		this.fileDir = fileDir;
		this.fileName = fileName;
		this.count = count;
		this.limit = limit;
	}

	public String getWebAddr() {
		return webAddr;
	}

	public void setWebAddr(String webAddr) {
		this.webAddr = webAddr;
	}

	public String getFileDir() {
		return fileDir;
	}

	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public AtomicLong getLimit() {
		return limit;
	}

	public void setLimit(AtomicLong limit) {
		this.limit = limit;
	}

}

// 线程运行信息显示的日志类
class Log {
	static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Log() {}

	public static void sleep(int nsecond) {
		try {
			Thread.sleep(nsecond);
		}
		catch (Exception e) {
			Log.log("线程沉睡");
		}
	}

	public static void log(String message) { // 显示日志信息
		System.err.println("[" + formatter.format(new Date()) + "]:" + message);
	}

	// public static void log(int message) { // 显示日志信息
	// System.err.println(message);

	// }
}