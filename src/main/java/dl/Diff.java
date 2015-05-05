package dl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Diff {

	static String newFile = "Exercises_and_Instruction_767_list";
	static String oldFile = "Exercises_and_Instruction_751";
	static String difFile = "";

	public static void main(String[] args) throws IOException {

		Map<String, String> newMap = getMap(newFile);

		System.out.println(" newMap count " + newMap.size());

		Map<String, String> oldMap = getMap(oldFile);

		System.out.println(" oldMap count " + oldMap.size());

		for (String oldKey : oldMap.keySet()) {
			newMap.remove(oldKey);
		}

		System.out.println(" diff count " + newMap.size());

		for (String dif : newMap.keySet()) {
			System.out.println(dif + " " + newMap.get(dif));
		}
	}

	public static Map<String, String> getMap(String fileName)
			throws IOException {

		Path path = Paths.get("./dlurl", fileName);
		Reader reader = Files.newBufferedReader(path);
		BufferedReader br = new BufferedReader(reader);

		List<String> allLines = br.lines().collect(Collectors.toList());

		System.out.println(" ===== success count ==== " + allLines.size());

		Map<String, String> map = new HashMap<>();
		for (String line : allLines) {
			String[] pp = line.split(" ");
			if (map.containsKey(pp[0].trim())) {
				System.out.println("=================== " + pp[1].trim());
			} else {
				map.put(pp[0].trim(), pp[1].trim());
			}
		}
		return map;
	}
}
