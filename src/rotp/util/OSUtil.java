package rotp.util;

public class OSUtil {
	public enum OS {
		WINDOWS, LINUX, MAC, SOLARIS, UNKNOWN
	}; // Operating systems.

	private static OS os = null;
	public static String getFullOSName()	{ return System.getProperty("os.name"); }
	public static String getJavaVersion()	{ return System.getProperty("java.version"); }

	public static OS getOS() {
		if (os == null) {
			String operSys = getFullOSName().toLowerCase();
			if (operSys.contains("mac") || operSys.contains("darwin"))
				os = OS.MAC;
			else if (operSys.contains("win"))
				os = OS.WINDOWS;
			else if (operSys.contains("nix") || operSys.contains("nux") || operSys.contains("aix"))
				os = OS.LINUX;
			else if (operSys.contains("sunos"))
				os = OS.SOLARIS;
			else
				os = OS.UNKNOWN;
		}
		return os;
	}

	public static boolean isWindows()	{ return getOS() == OS.WINDOWS; }
	public static boolean isMac()		{ return getOS() == OS.MAC; }
	public static boolean isLinux()		{ return getOS() == OS.LINUX; }
	public static boolean isSolaris()	{ return getOS() == OS.SOLARIS; }
	public static boolean isUnknown()	{ return getOS() == OS.UNKNOWN; }
}
