import sun.security.util.SecurityConstants;
import sun.tools.jar.CommandLine;

import java.io.IOException;

/**
 * Created by isaiah on 5/6/17.
 */
public class AlfredNotifications {

    public static void displayMessage(String msg) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String[] args = { "osascript", "-e", "display notification \"" + msg +  "\" with title \"Alfred\"" };
        runtime.exec(args);
    }
}
