import java.io.File;

public class CheckSpaceTest {
	public static void main(String[] args) {
		File file = new File(".");
		long free = file.getFreeSpace();
		long all = file.getTotalSpace();
		long useable = file.getUsableSpace();
		System.out.println(free);
		System.out.println(all);
		System.out.println(useable);
		System.out.println((all - free) * 100 / all + "%");
	}
}
