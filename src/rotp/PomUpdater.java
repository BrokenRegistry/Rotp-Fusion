package rotp;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PomUpdater {
	private static final String VER_B = "<version>";
	private static final String VER_E = "</version>";
	static void updatePom()	{
		String folder = Rotp.jarPath() + "/..";
		String name = "pom.xml";
		File pomFile = new File(folder, name);
		if (pomFile.exists() && pomFile.isFile()) {
			String pattern = "yyyy-MM-dd";
			DateFormat df = new SimpleDateFormat(pattern);
			Date today = Calendar.getInstance().getTime();
			String todayAsString = df.format(today);
			String newVersion = "Fusion-" + todayAsString;
			updatePom (pomFile, newVersion);
		}
		else {
			System.err.println("Pom file not found");
		}
	}
	static boolean updatePom (File pomFile, String newVersion)	{
		boolean sameVersion = false;
		boolean tobeUpdated = false;
		String currentVersion = "";
		ArrayList<String> content = new ArrayList<>();

//		// Load and modify if needed
//		try ( BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream(pomFile), "UTF-8"));) {
//			String input;
//			while (!sameVersion && (input = in.readLine()) != null) {
//				if (input.contains(VER_B)) {
//					int start = input.indexOf(VER_B) + VER_B.length();
//					int end = input.indexOf(VER_E);
//					currentVersion = input.substring(start, end);
//					sameVersion = newVersion.equals(currentVersion);
//					if (sameVersion)
//						return false;
//					String output = input.replace(currentVersion, newVersion);
//					content.add(output);
//					tobeUpdated = true;
//				}
//				else
//					content.add(input);
//			}
//		}
//		catch (FileNotFoundException e) {
//			System.err.println(pomFile.toString() + " not found.");
//		}
//		catch (IOException e) {
//			System.err.println("PomFile update load -- IOException: "+ e.toString());
//		}
//
//		if (!tobeUpdated) {
//			System.err.println("No version line found in the PomFile");
//			return false;
//		}
//
//		// Write if modified
//		try (FileOutputStream fout = new FileOutputStream(pomFile);
//			PrintWriter out = new PrintWriter(new OutputStreamWriter(fout, "UTF-8")); ) {
//				for (String line : content)
//					out.println(line);
//			}
//			catch (IOException e) {
//				System.err.println("PomFile update save -- IOException: "+ e.toString());
//				return false;
//			}
		return true;
	}
}
