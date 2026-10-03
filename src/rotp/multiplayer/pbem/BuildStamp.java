package rotp.multiplayer.pbem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import rotp.Rotp;

/** Identifies the build that wrote a turn file; every player must run the same one. */
public final class BuildStamp {
    public static final String ZIP_ENTRY = "PlayByEmail.properties";
    public static final String KEY = "build";
    private static String current;

    private BuildStamp() { }

    public static synchronized String current() {
        if (current == null) {
            String date = "unknown";
            try (InputStream in = BuildStamp.class.getResourceAsStream("/build.properties")) {
                if (in != null) {
                    Properties properties = new Properties();
                    properties.load(in);
                    date = properties.getProperty("build.date", date);
                }
            } catch (IOException ignored) { }
            current = Rotp.releaseId + " " + date;
        }
        return current;
    }

    /** Thrown before deserializing a turn file from a different build. */
    public static final class MismatchException extends IOException {
        private static final long serialVersionUID = 1L;
        public final String fileBuild;
        public final String localBuild;
        public MismatchException(String fileBuild, String localBuild) {
            super("Turn file build " + fileBuild + " does not match " + localBuild);
            this.fileBuild = fileBuild;
            this.localBuild = localBuild;
        }
    }

    public static void requireMatch(String fileBuild, String localBuild) throws MismatchException {
        if (fileBuild != null && !fileBuild.equals(localBuild))
            throw new MismatchException(fileBuild, localBuild);
    }

    /** Checks a save before deserializing it; files without a stamp are not play-by-email turns. */
    public static void requireMatch(File save) throws MismatchException {
        requireMatch(read(save), current());
    }

    static String read(File save) {
        try (ZipFile zip = new ZipFile(save)) {
            ZipEntry entry = zip.getEntry(ZIP_ENTRY);
            if (entry == null) return null;
            Properties properties = new Properties();
            try (InputStream in = zip.getInputStream(entry)) { properties.load(in); }
            return properties.getProperty(KEY);
        } catch (IOException notZip) { return null; }
    }

    public static void write(ZipOutputStream out) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(KEY, current());
        out.putNextEntry(new ZipEntry(ZIP_ENTRY));
        properties.store(out, null);
        out.closeEntry();
    }
}
