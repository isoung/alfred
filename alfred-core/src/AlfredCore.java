import javax.sound.sampled.*;
import java.util.*;
import javax.xml.transform.Source;
import java.io.*;
import java.lang.annotation.Target;
import org.apache.commons.codec.binary.Base64;

import com.google.cloud.speech.spi.v1.SpeechClient;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;


/**
 * Created by isaiah on 5/1/17.
 */
public class AlfredCore {
    private final AudioFormat AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);
    int CHUNK_SIZE = 1024;

    private AlfredCommands commands;
    private TargetDataLine mic;

    public AlfredCore() throws IOException, LineUnavailableException {
        commands = new AlfredCommands();
        mic = AudioSystem.getTargetDataLine(AUDIO_FORMAT);
    }

    public void start() throws IOException, Exception {
        mic.open();
        mic.start();

        boolean saveData = false;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        long currentTime = 0;

        while (true) {
            byte[] data = read();
            int level = calculateRMSLevel(data);

            if (level >= 18) {
                saveData = true;
                currentTime = new Date().getTime();
            }

            if (saveData) {
                output.write(data, 0, CHUNK_SIZE);

                if ((new Date().getTime() - currentTime) > 1500) {
                    AlfredNotifications.displayMessage("Thinking...");
                    saveData = false;

                    SpeechClient speech = SpeechClient.create();
                    ByteString audioBytes = ByteString.copyFrom(output.toByteArray());

                    RecognitionConfig config = RecognitionConfig.newBuilder()
                            .setEncoding(AudioEncoding.LINEAR16)
                            .setSampleRateHertz(44100)
                            .setLanguageCode("en-US")
                            .build();
                    RecognitionAudio audio = RecognitionAudio.newBuilder()
                            .setContent(audioBytes)
                            .build();

                    RecognizeResponse response = speech.recognize(config, audio);
                    List<SpeechRecognitionResult> results = response.getResultsList();

                    if (results.size() > 0) {
                        String transcript = results.get(0).getAlternativesList().get(0).getTranscript();

                        if (transcript.toUpperCase().indexOf("HEY ALFRED") > -1)
                            commands.evaluate(transcript);
                    }

                    speech.close();
                    output.reset();
                }
            }
        }
    }

    private byte[] read() {
        byte[] data = new byte[mic.getBufferSize() / 5];
        mic.read(data, 0, CHUNK_SIZE);

        return data;
    }

    public static void main(String[] args) throws LineUnavailableException, FileNotFoundException, IOException, Exception {
        System.out.println("Starting Alfred");
        AlfredCore core = new AlfredCore();
        core.start();

//        AlfredCommands commands = new AlfredCommands();
//        commands.evaluate("open Alfred from the desktop");
    }

    // http://www.indiana.edu/~emusic/acoustics/amplitude.htm
    private int calculateRMSLevel(byte[] audioData)
    {
        long lSum = 0;
        for(int i=0; i < audioData.length; i++)
            lSum = lSum + audioData[i];

        double dAvg = lSum / audioData.length;

        double sumMeanSquare = 0d;

        for(int j=0; j < audioData.length; j++)
            sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);
        double averageMeanSquare = sumMeanSquare / audioData.length;
        return (int)(Math.pow(averageMeanSquare,0.5d) + 0.5);
    }
}
