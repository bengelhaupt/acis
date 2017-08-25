/*
  @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 */
package de.bensoft.acis.androidthings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import dalvik.system.DexClassLoader;
import de.bensoft.acis.core.ACIS;
import de.bensoft.acis.core.Action;
import de.bensoft.acis.core.ActionMalformedException;
import de.bensoft.acis.core.ActionPackage;
import de.bensoft.acis.core.ActionResult;
import de.bensoft.acis.core.Parameter;
import de.bensoft.acis.core.WeightSet;
import de.bensoft.acis.core.environment.Environment;
import de.bensoft.acis.core.environment.SystemEnvironment;
import de.bensoft.acis.core.environment.SystemProperties;
import de.bensoft.acis.core.environment.UserInfo;
import de.bensoft.acis.core.environment.VisualOutput;
import de.bensoft.acis.core.language.Sentence;
import de.bensoft.acis.languages.BensoftGermanWiktionary;
import de.bensoft.acis.server.Server;
import de.bensoft.acis.server.ServerContext;
import de.bensoft.acis.server.User;
import de.bensoft.acis.server.contexts.SampleActionListHandler;
import de.bensoft.acis.server.contexts.SampleActionPackageInstallerHandler;
import de.bensoft.acis.server.contexts.SampleFileViewHandler;
import de.bensoft.acis.server.contexts.SampleRequestHandler;
import de.bensoft.acis.utils.Logging.Logger;
import de.bensoft.acis.utils.Logging.LoggingConfig;

import static android.speech.SpeechRecognizer.RESULTS_RECOGNITION;

public class MainActivity extends Activity {

    private static ACIS mACISSystem;

    private TextToSpeech mTTS;
    private TextView mConsole;

    public static SystemEnvironment ENVIRONMENT;
    public static File mWorkingDirectory;

    public static Logger mServerLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConsole = ((TextView) findViewById(R.id.console));

        mWorkingDirectory = getFilesDir();

        //initialize the Language and set its Logger
        BensoftGermanWiktionary language = new BensoftGermanWiktionary();
        try {
            LoggingConfig languageLoggingConfig = new LoggingConfig(language.getLogger().getLoggingConfig().getFile());
            languageLoggingConfig.setEnabled(false);
            Logger languageLogger = new Logger(languageLoggingConfig);
            language.setLogger(languageLogger);
        } catch (IOException ignored) {
        }

        //initialize text to speech
        mTTS = new TextToSpeech(MainActivity.this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                mTTS.setLanguage(Locale.GERMAN);
            }
        });

        //creates the Environment
        ENVIRONMENT = new SystemEnvironment() {

            @Override
            public SystemProperties getSystemProperties() {
                return new SystemProperties("ben.beta-androidthings-040817-master", 20170804, "04.08.2017", new Date("08/04/2017"),
                        "Ben-Noah Engelhaupt (bensoft.de)", "Production and personal use ACIS system.",
                        System.currentTimeMillis());
            }

            @Override
            public UserInfo getUserInfo() {
                return new UserInfo("Ben-Noah", "Engelhaupt", new String[]{"Ben"}, 18, "code@bensoft.de",
                        "Bornstraße 21, 99817 Eisenach, Deutschland");
            }

            @Override
            public boolean canSpeak() {
                return true;
            }

            @Override
            public void addOutput(String s) {
                addWrittenOutput(s);
                addSpokenOutput(s);
            }

            @Override
            public void addWrittenOutput(String s) {
                runOnUiThread(() -> {
                    mConsole.append(s + "\r\n");
                    ((ScrollView) mConsole.getParent().getParent()).scrollBy(0, 1000);
                });
            }

            @Override
            public void addSpokenOutput(String s) {
                runOnUiThread(() -> mTTS.speak(s, TextToSpeech.QUEUE_ADD, null, ""));
            }

            @Override
            public boolean canRequestInput() {
                return false;
            }

            @Override
            public String requestInput(String s) throws UnsupportedOperationException {
                throw new UnsupportedOperationException("Requesting input is not supported.");
            }

            @Override
            public boolean hasVisualOutput() {
                return false;
            }

            @Override
            public VisualOutput getVisualOutput() throws UnsupportedOperationException {
                throw new UnsupportedOperationException("There is no visual output available.");
            }
        };

        //initializes the ACIS system and sets the data directory
        mACISSystem = null;
        try {
            mACISSystem = new ACIS(language, ENVIRONMENT, mWorkingDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }

        mACISSystem.getWordCache().getLogger().getLoggingConfig().setEnabled(false);

        //starting initializations in separate thread
        new Thread(() -> {
            //loading of the ACIS language library
            ENVIRONMENT.addWrittenOutput("Starting initialization of language interface...");
            try {
                List<ActionPackage> actionPackages = loadActions(mACISSystem, getApplicationContext());
                if (actionPackages.size() != 0) {
                    for (ActionPackage ap : actionPackages) {
                        try {
                            mACISSystem.getActionManager().add(ap.getActions(mACISSystem.getLanguage()));

                        } catch (ActionMalformedException ie) {
                            mACISSystem.getLogger().e("JAR_ACTION_LOADER", "There was an error while creating Action '" + ie.getActionName() + "': " + ie.toString());
                        }
                    }
                } else {
                    mACISSystem.getLogger().e("JAR_ACTION_LOADER", "Error loading the packages");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ENVIRONMENT.addWrittenOutput("Initialization of language interface completed");


            //set the server Logger
            try {
                File mServerLogFile = new File(mWorkingDirectory.getAbsolutePath() + "/server.log");
                mServerLogger = new Logger(new LoggingConfig(mServerLogFile));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //starting Server
            try {
                ENVIRONMENT.addWrittenOutput("Starting server...");
                int port = Integer.valueOf(mACISSystem.getSystemPreferences().get("serverport", "4964")); //the port the server will run at
                Server s = new Server(port);
                s.setLogger(mServerLogger);

                //register some ServerContexts
                s.registerContext(new ServerContext("/request", mACISSystem, new SampleRequestHandler(), true));
                s.registerContext(new ServerContext("/actions", mACISSystem, new SampleActionListHandler(), true));
                s.registerContext(new ServerContext("/file", mACISSystem, new SampleFileViewHandler(), true));
                s.registerContext(new ServerContext("/install", mACISSystem, new SampleActionPackageInstallerHandler(() -> {
                    try {
                        List<ActionPackage> actionPackages = loadActions(mACISSystem, getApplicationContext());
                        if (actionPackages.size() != 0) {
                            for (ActionPackage ap : actionPackages) {
                                try {
                                    mACISSystem.getActionManager().add(ap.getActions(mACISSystem.getLanguage()));
                                } catch (ActionMalformedException ie) {
                                    mACISSystem.getLogger().e("JAR_ACTION_LOADER", "There was an error while creating Action '" + ie.getActionName() + "': " + ie.toString());
                                }
                            }
                        } else {
                            mACISSystem.getLogger().e("JAR_ACTION_LOADER", "Error loading the packages");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }), true));

                //add some users
                s.addUser(new User("Ben", "olaunce=acis"));
                s.addUser(new User("Tester", "tester", new String[]{"/actions", "/install", "/file"}));

                try {
                    s.start();
                    ENVIRONMENT.addWrittenOutput("Server started successfully. (" + InetAddress.getLocalHost().toString() + ":" + port + ")");
                } catch (Exception e) {
                    ENVIRONMENT.addWrittenOutput("Server could not be started: " + e.toString());
                }
            } catch (Exception e) {
                ENVIRONMENT.addWrittenOutput("Server could not be started... " + e.toString());
            }

            ENVIRONMENT.addWrittenOutput("Initialization completed...");

            ENVIRONMENT.addOutput("Willkommen");

            runOnUiThread(this::initSpeech);

        }).start();

    }


    static boolean actionRan = false;

    //Hotword and speech recognition
    private void initSpeech() {
        SpeechRecognizer mRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext(), ComponentName.unflattenFromString("com.google.android.voicesearch/.GoogleRecognitionService"));
        //SpeechRecognizer mRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        Intent speechRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, mACISSystem.getLanguage().getLanguage().toLanguageTag());
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mACISSystem.getLanguage().getLanguage().toLanguageTag());
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, false);
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);

        try {
            HotwordRecognition hotwordRecognition = new HotwordRecognition(getApplicationContext(), new String[]{"asus", "acis", "aces", "asis"}, true, Locale.US, mRecognizer, hotwordImplementation -> {
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200);
                hotwordImplementation.setEnabled(false);

                mRecognizer.setRecognitionListener(new RecognitionListener() {
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

                    boolean newAttempt = false;

                    @Override
                    public void onError(int i) {
                        Log.d("ACIS", "REC error " + i);
                        if (i != 5 && i != 3) {
                            if (!newAttempt) {
                                Log.d("ACIS/SpeechRecognition", "First recognition attempt failed, trying again");
                                mRecognizer.cancel();
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                ENVIRONMENT.addSpokenOutput("Ja?");
                                mRecognizer.startListening(speechRecognitionIntent);
                            } else {
                                Log.d("ACIS/SpeechRecognition", "Second recognition attempt failed, aborting");
                                ENVIRONMENT.addSpokenOutput("Dann eben nicht");
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                hotwordImplementation.setEnabled(true);
                                hotwordImplementation.startRecognition();
                            }
                            newAttempt = true;
                        } else {
                            mRecognizer.startListening(speechRecognitionIntent);
                        }
                    }

                    @Override
                    public void onResults(Bundle bundle) {
                        ArrayList<String> results = bundle.getStringArrayList(RESULTS_RECOGNITION);
                        if (!results.get(0).equals("")) {
                            try {
                                toneGen1.startTone(ToneGenerator.TONE_DTMF_0, 50);
                                Thread.sleep(100);
                                toneGen1.startTone(ToneGenerator.TONE_DTMF_0, 50);
                                Thread.sleep(50);
                            } catch (InterruptedException ignored) {
                            }
                            Log.d("ACIS/SpeechRecognition", "Recognition result: " + results.get(0));
                            mACISSystem.executeNewThread(results.get(0), 0.3f, new WeightSet(), new ACIS.OnExecutionListener() {
                                @Override
                                public ActionResult onActionRun(Action action, Environment environment, Sentence sentence, Parameter[] parameter) {
                                    actionRan = true;
                                    return super.onActionRun(action, environment, sentence, parameter);
                                }
                            });
                        }
                        mRecognizer.cancel();

                        try {
                            while (!actionRan) {
                                Thread.sleep(500);
                            }
                            Thread.sleep(1000);
                            while (mTTS.isSpeaking()) {
                                Thread.sleep(500);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        actionRan = false;
                        hotwordImplementation.setEnabled(true);
                        hotwordImplementation.startRecognition();
                    }

                    @Override
                    public void onPartialResults(Bundle bundle) {

                    }

                    @Override
                    public void onEvent(int i, Bundle bundle) {

                    }
                });
                Log.d("ACIS/SpeechRecognition", "Speech recognition started");
                mRecognizer.startListening(speechRecognitionIntent); //speechRecognition
            });

            hotwordRecognition.setEnabled(true);
            hotwordRecognition.startRecognition(); //hotword
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //loads ActionPackages from.jar files
    public static List<ActionPackage> loadActions(ACIS system, Context con) throws Exception {
        List<ActionPackage> actionLoaders = new ArrayList<>();
        for (File f : system.getPackageFilesDirectory().listFiles()) {
            if (f.getName().endsWith(".acp") || f.getName().endsWith(".jar")) {
                String filePath = f.getAbsolutePath();
                JarFile jarFile = new JarFile(filePath);
                Enumeration<JarEntry> en = jarFile.entries();
                while (en.hasMoreElements()) {
                    JarEntry je = en.nextElement();
                    if (je.isDirectory() || !je.getName().endsWith(".class")) {
                        continue;
                    }
                    String className = je.getName().substring(0, je.getName().length() - 6);
                    className = className.replace('/', '.');
                    DexClassLoader classLoader = new DexClassLoader(filePath, con.getCodeCacheDir().getAbsolutePath(), null, con.getClassLoader());
                    try {
                        Class<?> c = classLoader.loadClass(className);
                        for (Class<?> interf : c.getInterfaces()) {
                            if (interf.getName().equals("de.bensoft.acis.core.ActionPackage")) {
                                try {
                                    ActionPackage ap = (ActionPackage) c.newInstance();
                                    actionLoaders.add(ap);
                                } catch (InstantiationException i) {
                                    system.getLogger().e("JAR_ACTIONPACKAGE_LOADER", "There was an error while instantiating Class '" + c.getName() + "': " + i.toString());
                                }
                            }
                        }
                    } catch (ClassNotFoundException cnfe) {
                        system.getLogger().e("JAR_ACTIONPACKAGE_LOADER", "There was an error while loading Class '" + className + "': " + cnfe.toString());
                    }
                }
                jarFile.close();
            }
        }
        return actionLoaders;
    }
}