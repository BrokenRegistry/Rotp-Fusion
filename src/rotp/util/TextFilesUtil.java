package rotp.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TextFilesUtil {
	public static void replaceFirstLineStartingWith(Path filePath, String start, String newLine) {
		try {
			// Read all lines into a list
			List<String> lines = Files.readAllLines(filePath);

			// Replace the target line
			for (int i = 0; i < lines.size(); i++) {
				if (lines.get(i).startsWith(start)) {
					lines.set(i, newLine);
					break;
				}
			}

			// Write updated lines back to the file
			Files.write(filePath, lines);

			System.out.println("Line replaced successfully."); // TODO BR: Comment
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
