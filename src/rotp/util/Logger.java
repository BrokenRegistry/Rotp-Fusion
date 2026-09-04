package rotp.util;

import rotp.ui.RotPUI;

import java.io.PrintWriter;
import java.util.function.Consumer;

public class Logger {
    public static Consumer<String> logListener = null;

    public static Consumer<String> registerLogListener(Consumer<String> listener) {
        Consumer<String> oldListener = Logger.logListener;
        Logger.logListener = listener;
        return oldListener;
    }

    public static void logToFile(String line) {
        if (RotPUI.useDebugFile) {
            PrintWriter debugFile = RotPUI.debugFile();
            if (debugFile != null) {
                debugFile.println(line);
                debugFile.flush();
            }
        }
    }

}
