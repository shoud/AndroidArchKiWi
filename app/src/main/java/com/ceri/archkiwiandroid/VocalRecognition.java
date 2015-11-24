package com.ceri.archkiwiandroid;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;


/**
 * Created by thomas on 24/11/2015.
 */
public class VocalRecognition implements RecognitionListener
{

    private SpeechRecognizer recognizer;

    @Override
    public void onBeginningOfSpeech()
    {

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis)
    {
        if (hypothesis == null)
            return;

    }


    @Override
    public void onEndOfSpeech()
    {

    }


    @Override
    public void onResult(Hypothesis hypothesis)
    {

    }

    @Override
    public void onError(Exception error)
    {

    }


    @Override
    public void onTimeout()
    {

    }


}
