package com.ceri.archkiwiandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 02/12/2015.
 */
public class VocalRecognizer
{
    private MainActivity mainActivity;
    private SpeechRecognizer asr = null;
    private Intent intent = null;

    public VocalRecognizer(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        asr = SpeechRecognizer.createSpeechRecognizer(mainActivity);
        asr.setRecognitionListener(new ResSpeech());
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    }

    /**
     * Permet de lancer un enregistrement vocale
     */
    public void enregistrement() {
        //Lance un enregistrement
        mainActivity.startActivityForResult(intent, 0);
    }

    /**
     * Permet de stopper un enregistrement vocale
     */
    public void stopperEnregistrement() {
        //Permet d'arrêter l'enregistrement
        asr.stopListening();
    }

    private class ResSpeech implements RecognitionListener
    {

        @Override
        public void onReadyForSpeech(Bundle params){}

        @Override
        public void onBeginningOfSpeech(){}

        @Override
        public void onRmsChanged(float rmsdB){}

        @Override
        public void onBufferReceived(byte[] buffer){}

        @Override
        public void onEndOfSpeech(){}

        @Override
        /**
         * Méthode peremettant de signigier une érreure dans les logs
         * si il y a eu un problème avec google speech
         */
        public void onError(int erreur)
        {
            //Le message a ecrire dans le log
            String message;
            switch (erreur)
            {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "Audio enregistrement erreur.";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "Erreur client.";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "Permissions insuffisantes";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "Erreur de réseau.";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "Temps réseau dépassé.";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "Pas de résultat.";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "Le service est surchargé.";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "Erreur du serveur";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "Pas de son enregistré";
                    break;
                default:
                    message = "Erreur inconnu";
            }
            Log.e("SpeechRecognizer", message);
        }

        @Override
        /**
         * La méthode qui reçoit le résulat de speech
         */
        public void onResults(Bundle results) {
            //la liste des résulats possible, en 0 le plus possible vers le moins possible
            List<String> tmp = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            //On garde la possibilité la plus sûr
            String phrase = tmp.get(0);
            //Faire le traitement
            Log.e("Phrase trouvée ", phrase);

        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    }
}
