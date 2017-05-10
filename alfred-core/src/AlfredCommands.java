import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by isaiah on 5/4/17.
 */
public class AlfredCommands {
    private Desktop desktop = Desktop.getDesktop();

    public AlfredCommands() throws IOException {

    }

    public void evaluate(String evaluationString) throws IOException, URISyntaxException {
        ArrayList<String> splitString = new ArrayList<String>(Arrays.asList(evaluationString.split(" ")));

        if (evaluationString.toUpperCase().indexOf("SAY HELLO") > -1) {
            AlfredNotifications.displayMessage("Hello!");
        }
        else if(isQuestion(evaluationString)) {
            googleSearch(splitString);
        }
        else if(evaluationString.toUpperCase().indexOf("FROM THE DESKTOP") > -1) {
            int indexOpen = splitString.indexOf("open");
            int indexFrom = splitString.indexOf("from");
            String fileName = "";

            for (int i = indexOpen + 1; i < indexFrom; i++) {
                if (i == indexFrom - 1) {
                    fileName += splitString.get(i);
                }
                else {
                    fileName += splitString.get(i) + " ";
                }
            }

            openFileFromDesktop(fileName);
        }
        else if (splitString.indexOf("open") > -1) {
            int indexOpen = splitString.indexOf("open");
            String app = "";

            for (int i = indexOpen + 1; i < splitString.size(); i++) {
                if (!splitString.get(i).equalsIgnoreCase("the"))
                    app += " " + splitString.get(i);
            }

            openApplication(app);
        }
        else if (splitString.indexOf("thank") > -1) {
            AlfredNotifications.displayMessage("No problem!");
        }
        else {
            AlfredNotifications.displayMessage("Sorry, I could not understand you. Please say it again.");
        }
    }

    private void openApplication(String appName) throws IOException {
        ArrayList<File> files = new ArrayList<File>(Arrays.asList(new File("/Applications").listFiles()));
        String appPath = "";
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(appName.trim() + ".app") ||
                    file.getName().equalsIgnoreCase(appName.trim() + "s.app")) {
                appPath = "/Applications/" + file.getName();
                break;
            }
        }

        if (appPath != "") {
            desktop.open(new File(appPath));
        }
        else {
            AlfredNotifications.displayMessage("No app called " + appName + " found");
        }
    }

    private void openFileFromDesktop(String fileName) throws IOException {
        String userHome = System.getProperty("user.home");
        ArrayList<File> files = new ArrayList<File>(Arrays.asList(new File(userHome + "/Desktop").listFiles()));
        String filePath = "";

        for (File file : files) {
            if (file.getName().toUpperCase().contains(fileName.trim().toUpperCase())) {
                filePath = userHome + "/Desktop/" + file.getName();
                break;
            }
        }

        if (filePath != "") {
            desktop.open(new File(filePath));
        }
        else {
            AlfredNotifications.displayMessage("No file called " + fileName + " found");
        }
    }

    private void googleSearch(ArrayList<String> splitString) throws IOException, URISyntaxException {
        Desktop.getDesktop().browse(new URI("https://www.google.com/#q=" + createQueryString(splitString)));
    }

    private String createQueryString(ArrayList<String> splitString) {
        String queryString = "";

        for(int i = 2; i < splitString.size(); i++) {
            queryString += (i + 1) != splitString.size() ? splitString.get(i) + "+" : splitString.get(i);
        }
        return queryString;
    }

    private boolean isQuestion(String question) {
        String upperCaseQuestion = question.toUpperCase();

        if (upperCaseQuestion.indexOf("WHAT") > -1) {
            return true;
        }
        else if (upperCaseQuestion.indexOf("WHERE") > -1) {
            return true;
        }
        else if (upperCaseQuestion.indexOf("WHEN") > -1) {
            return true;
        }
        else if (upperCaseQuestion.indexOf("WHY") > -1) {
            return true;
        }
        else if (upperCaseQuestion.indexOf("HOW") > -1) {
            return true;
        }
        else if (upperCaseQuestion.indexOf("WHO") > -1) {
            return true;
        }
        else {
            return false;
        }
    }
}
