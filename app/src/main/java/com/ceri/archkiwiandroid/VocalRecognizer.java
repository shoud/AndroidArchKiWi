package com.ceri.archkiwiandroid;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class VocalRecognizer implements RecognitionListener {

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String COMMANDS = "commandes";

    private SpeechRecognizer recognizer;
    private SocketClient socketClient;

    public VocalRecognizer(final MainActivity activity, SocketClient socketClient) {
        this.socketClient = socketClient;

        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        Log.i("VocalRecPrep", "Preparing recognizer...");
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(activity);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null)
                    Log.e("VocalRecPrep", "Error while preparing recognizer: " + result);
                else
                    Log.i("VocalRecPrep", "Recognizer ready!");
            }
        }.execute();
    }

    public void listen() {
        recognizer.startListening(COMMANDS, 2000);
    }

    public void onDestroy() {
        recognizer.cancel();
        recognizer.shutdown();
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        Log.d("VocalRecPartRes", text);
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            Log.i("VocalRecRes", text);
            parse(text);
        }
    }

    private void parse(String text) {
        int angle = 0, power = 50, duration = 1000;

        if (text.contains("recule"))
            angle = 180;
        if (text.contains("fonce"))
            power = 80;

        if (text.contains("vers la gauche")) {
            if (angle == 0)
                angle = -60;
            else
                angle = -120;
        } else if (text.contains("vers la droite")) {
            if (angle == 0)
                angle = 60;
            else
                angle = 120;
        }

        if (text.contains("demi-tour")) {
            angle = -90;
            power = 60;
            duration = 1400;
            if (text.contains("rapidement"))
                duration = 1000;
            if (text.contains("lentement"))
                duration = 2700;
        }
        if (text.contains("rapidement")) {
            power += 20;
        } else if (text.contains("lentement")) {
            power -= 20;
        }

        if (text.contains("pendant deux secondes"))
            duration = 2000;
        else if (text.contains("pendant trois secondes"))
            duration = 3000;

        if (socketClient != null)
            socketClient.send("M;" + angle + ";" + power + ";", duration);
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        recognizer.stop();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "fr-ptm"))
                .setDictionary(new File(assetsDir, "fr.dict"))

                // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                //.setRawLogDir(assetsDir)

                // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-45f)

                // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        recognizer.addListener(this);

        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "commands.gram");
        recognizer.addGrammarSearch(COMMANDS, menuGrammar);
    }

    @Override
    public void onError(Exception error) {
        Log.e("VocalRecErr", error.toString());
    }

    @Override
    public void onTimeout() {
        recognizer.startListening(COMMANDS);
    }
}