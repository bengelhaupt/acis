/*
  @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 */
package de.bensoft.acis.androidthings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import static android.speech.SpeechRecognizer.RESULTS_RECOGNITION;

/**
 * Hotword recognition class
 */
public class HotwordRecognition {

    private SpeechRecognizer mRecognizer = null;
    private HotwordListener mListener;
    private String[] mHotwords;
    private boolean mInnerContain = false;

    private Intent mHotwordIntent;
    private boolean mEnabled = false;

    public HotwordRecognition(Context context, String[] hotwords, boolean innerContain, Locale locale, SpeechRecognizer recognizer, HotwordListener listener) throws Exception {
        mHotwords = hotwords;
        mInnerContain = innerContain;
        mRecognizer = recognizer;
        mListener = listener;

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            mHotwordIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mHotwordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mHotwordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toLanguageTag());
            mHotwordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag());
            mHotwordIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
            mHotwordIntent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, false);
            mHotwordIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            mHotwordIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            mHotwordIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);
            setRecognizer();
        } else {
            throw new Exception("Recognition not available");
        }
    }

    public void startRecognition() {
        if (mEnabled) {
            mRecognizer.cancel();
            setRecognizer();
            mRecognizer.startListening(mHotwordIntent);
            Log.d("ACIS/HotwordRecognition", "Hotword recognition started");
        }
    }

    private RecognitionListener mHotwordRecognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int i) {
            Log.d("ACIS/HotwordRecognition", "Hotword error " + i);
            if (i != 5 && i != 3 && mEnabled) {
                mRecognizer.stopListening();
                mRecognizer.cancel();
                startRecognition();
            }
        }

        @Override
        public void onResults(Bundle bundle) {
            if (mEnabled) {
                ArrayList<String> results = bundle.getStringArrayList(RESULTS_RECOGNITION);
                if (results != null) {
                    Log.d("ACIS/HotwordRecognition", "Result, recognized words: " + getAllResults(results));
                    for (String mHotword : mHotwords) {
                        for (int i = 0; i < results.size(); i++) {
                            if ((" " + results.get(i).toLowerCase() + " ").contains(" " + mHotword.toLowerCase() + " ") || (mInnerContain && results.get(i).toLowerCase().contains(mHotword.toLowerCase()))) {
                                Log.d("ACIS/HotwordRecognition", "Hotword recognized");
                                mRecognizer.stopListening();
                                mRecognizer.cancel();
                                mListener.onHotword(HotwordRecognition.this);
                                return;
                            }
                        }
                    }
                }
                mRecognizer.cancel();
                startRecognition();
            }
        }

        private String getAllResults(ArrayList<String> results) {
            String res = "";
            for (String s : results) {
                res += s + ", ";
            }
            return res.substring(0, res.length() - 2);
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            if (mEnabled) {
                ArrayList<String> results = bundle.getStringArrayList(RESULTS_RECOGNITION);
                if (results != null) {
                    Log.d("ACIS/HotwordRecognition", "Result, recognized words: " + getAllResults(results));
                    for (String mHotword : mHotwords) {
                        for (int i = 0; i < results.size(); i++) {
                            if ((" " + results.get(i).toLowerCase() + " ").contains(" " + mHotword.toLowerCase() + " ")) {
                                Log.d("ACIS/HotwordRecognition", "Hotword recognized");
                                mRecognizer.stopListening();
                                mRecognizer.cancel();
                                mListener.onHotword(HotwordRecognition.this);
                                return;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };

    public void setRecognizer() {
        mRecognizer.setRecognitionListener(mHotwordRecognitionListener);
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public interface HotwordListener {
        void onHotword(HotwordRecognition hotwordImpl);
    }
}
