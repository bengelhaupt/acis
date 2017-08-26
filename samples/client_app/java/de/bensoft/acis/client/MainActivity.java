package de.bensoft.acis.client;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;

    private String token = "";
    private boolean isconnected = false;

    private int refreshrate = 2000;

    private Handler updater = new Handler();
    private SpeechRecognizer sr;

    private String endpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });

        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    tts.setLanguage(Locale.GERMAN);
                }
            }
        });

        if(checkEndpoint())
            init();
    }

    private boolean checkEndpoint(){
        endpoint = PreferenceManager.getDefaultSharedPreferences(this).getString("endpoint", "");
        if(endpoint.equals("")) {
            Toast.makeText(this, "ERROR: NO ENDPOINT SPECIFIED IN SETTINGS", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }

    private void init() {
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isconnected) {
                    tts.stop();
                    if(sr != null){
                        if(((FloatingActionButton) findViewById(R.id.fab)).getCompatElevation() == 50) {
                            sr.cancel();
                            sr.destroy();
                            ((FloatingActionButton) findViewById(R.id.fab)).setCompatElevation(0);
                        }
                    }
                    startSpeechRecognizer(null);
                }
                else
                    refreshToken();
            }
        });

        findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Custom Request");

                final EditText input = new EditText(MainActivity.this);

                input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                input.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("endpoint", "") + "/");
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String query = input.getText() + "&key=" + token;
                        getInternetData(query, new CallbackInterface() {
                            @Override
                            public void process(String s) {
                                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                alert.setTitle(query);
                                alert.setMessage(s);

                                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                });
                                alert.show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    private void refreshToken(){
        checkEndpoint();
        String username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");
        String password = PreferenceManager.getDefaultSharedPreferences(this).getString("password", "");

        if(!username.equals("") && !password.equals("")) {
            ((TextView) findViewById(R.id.status)).setText("FETCHING TOKEN");
            try {
                getInternetData(PreferenceManager.getDefaultSharedPreferences(this).getString("endpoint", "") +  "/auth?user=" + username + "&pass=" + generateMD5(password), new CallbackInterface() {
                    @Override
                    public void process(String s) {
                        if (!s.startsWith("ERROR")) {
                            token = s;
                            isconnected = true;
                            refreshrate = 300000;
                        } else {
                            if(!isconnected) {
                                refreshrate = 2000;
                                isconnected = false;
                            }
                        }
                        ((TextView) findViewById(R.id.status)).setText(s);
                    }
                });
            } catch (Exception ignored) {
                refreshrate = 2000;
                isconnected = false;
                refreshToken();
            }
        }
        else{
            ((TextView) findViewById(R.id.status)).setText("ERROR: INCOMPLETE AUTHENTICATION DATA");
        }

        updater.removeCallbacksAndMessages(null);
        updater.postDelayed(new Runnable(){
            public void run(){
                refreshToken();
            }
        }, refreshrate);
    }

    @Override
    public void onPause(){
        super.onPause();
        tts.stop();
        updater.removeCallbacksAndMessages(null);
        isconnected = false;
        if(sr != null)
            sr.destroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshToken();
    }

    private void startSpeechRecognizer(final String id) {
        sr = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        if (SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
            sr.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    ((FloatingActionButton) findViewById(R.id.fab)).setCompatElevation(50);
                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                }

                @Override
                public void onError(int error) {
                    if (error == 7 || error == 6 || error == 8) {
                        startSpeechRecognizer(id);
                    }
                    ((FloatingActionButton) findViewById(R.id.fab)).setCompatElevation(0);
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> l = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    float[] conf = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

                    ((FloatingActionButton) findViewById(R.id.fab)).setCompatElevation(0);

                    if(id == null)
                        makeRequest(l.get(0));
                    else
                        makeRespondRequest(id, l.get(0));

                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
            Intent ri = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            ri.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            ri.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "de");
            ri.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            ri.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true);
            ri.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            ri.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            sr.startListening(ri);
        }
    }

    class Response {
        public String Type;
        public String Name;
        public String Code;
        public String Message;
        public String Score;
        public String NewWritten;
        public String NewSpoken;
        public String TotalWritten;
        public String TotalSpoken;
        public String RequestId;
        public String RequestText;
    }

    private Response parseResponse(String s){
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(s)));
            doc.getDocumentElement().normalize();

            Response r = new Response();

            r.Type = doc.getElementsByTagName("type").item(0).getTextContent();
            if(!r.Type.equals("NO_RESULTS")) {
                r.Name = doc.getElementsByTagName("name").item(0).getTextContent();
                if(r.Type.equals("RESULT")) {
                    r.Code = doc.getElementsByTagName("code").item(0).getTextContent();
                    r.Message = doc.getElementsByTagName("message").item(0).getTextContent();
                }
                r.Score = doc.getElementsByTagName("score").item(0).getTextContent();
                r.NewWritten = doc.getElementsByTagName("written").item(0).getFirstChild().getTextContent();
                r.NewSpoken = doc.getElementsByTagName("spoken").item(0).getTextContent();
                r.TotalWritten = doc.getElementsByTagName("written").item(1).getTextContent();
                r.TotalSpoken = doc.getElementsByTagName("spoken").item(1).getTextContent();

                if(r.Type.equals("REQUEST_INPUT")) {
                    r.RequestId = doc.getElementsByTagName("id").item(0).getTextContent();
                    r.RequestText = doc.getElementsByTagName("text").item(0).getTextContent();
                }
            }
            return r;
        }
        catch(Exception e){
            return null;
        }
    }

    private void makeRequest(String query){
        if(isconnected) {
            final String oldtext = token;
            ((TextView) findViewById(R.id.status)).setText("MAKING REQUEST");
            final boolean serveroutput = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("serveroutput", false) || (isInHomeWiFi() && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("serveroutputifwifi", true));
            getInternetData(endpoint + "/request?key=" + token + "&mode=request&serveroutput=" + String.valueOf(serveroutput) + "&q=" + query, new CallbackInterface() {
                @Override
                public void process(String s) {
                    try {
                        if (!s.startsWith("ERROR")) {
                            Response r = parseResponse(s);
                            if(r.Type.equals("NO_RESULTS")) {
                                speechOutput("Keine passende Aktion gefunden.");
                                return;
                            }

                            speechOutput(r.NewSpoken);

                            if(r.Type.equals("REQUEST_INPUT")){
                                final String id = r.RequestId;
                                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("inputrequests", true)){
                                    while(tts.isSpeaking()){}
                                    startSpeechRecognizer(id);
                                }else {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                    alertDialog.setTitle(r.Name);
                                    alertDialog.setMessage(r.RequestText);

                                    final EditText input = new EditText(MainActivity.this);
                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT);
                                    input.setLayoutParams(lp);
                                    alertDialog.setCancelable(true);
                                    alertDialog.setView(input);

                                    alertDialog.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String text = input.getText().toString();
                                                    makeRespondRequest(id, text);
                                                }
                                            });

                                    alertDialog.setNegativeButton("ABORT",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });
                                    alertDialog.show();
                                }
                            }

                            ((TextView) findViewById(R.id.status)).setText(oldtext);
                        } else {
                            ((TextView) findViewById(R.id.status)).setText(s);
                            refreshrate = 2000;
                            isconnected = false;
                            refreshToken();
                        }
                    } catch (Exception e) {
                        ((TextView) findViewById(R.id.status)).setText(oldtext);
                    }
                }
            });
        }
        else{
            ((TextView) findViewById(R.id.status)).setText("NOT CONNECTED");
            refreshToken();
        }
    }

    private void makeRespondRequest(String id, String content){
        if(isconnected) {
            final String oldtext = token;
            ((TextView) findViewById(R.id.status)).setText("MAKING REQUEST");
            final boolean serveroutput = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("serveroutput", false) || (isInHomeWiFi() && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("serveroutputifwifi", true));
            getInternetData(endpoint + "/request?key=" + token + "&mode=respond&id=" + id + "&content=" + content, new CallbackInterface() {
                @Override
                public void process(String s) {
                    try {
                        if (!s.startsWith("ERROR")) {
                            Response r = parseResponse(s);

                            speechOutput(r.NewSpoken);

                            if(r.Type.equals("REQUEST_INPUT")){
                                final String id = r.RequestId;
                                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("inputrequests", true)){
                                    while(tts.isSpeaking()){}
                                    startSpeechRecognizer(id);
                                }else {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                    alertDialog.setTitle(r.Name);
                                    alertDialog.setMessage(r.RequestText);

                                    final EditText input = new EditText(MainActivity.this);
                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT);
                                    input.setLayoutParams(lp);
                                    alertDialog.setCancelable(true);
                                    alertDialog.setView(input);

                                    alertDialog.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String text = input.getText().toString();
                                                    makeRespondRequest(id, text);
                                                }
                                            });

                                    alertDialog.setNegativeButton("ABORT",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });
                                    alertDialog.show();
                                }
                            }

                            ((TextView) findViewById(R.id.status)).setText(oldtext);
                        } else {
                            ((TextView) findViewById(R.id.status)).setText(s);
                            refreshrate = 2000;
                            isconnected = false;
                            refreshToken();
                        }
                    } catch (Exception e) {
                        ((TextView) findViewById(R.id.status)).setText(oldtext);
                    }
                }
            });
        }
        else{
            ((TextView) findViewById(R.id.status)).setText("NOT CONNECTED");
            refreshToken();
        }
    }


    private void speechOutput(String line){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(line, TextToSpeech.QUEUE_ADD, null, "");
        }
        else
            tts.speak(line, TextToSpeech.QUEUE_ADD, null);
    }

    private boolean isInHomeWiFi(){
        try {
            final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
                return connectionInfo.getSSID().equals("\"" + PreferenceManager.getDefaultSharedPreferences(this).getString("homewifiname", "") + "\"");
            }
        }
        catch(Exception ignored){}
        return false;
    }

    private String generateMD5(String s) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(s));
            return String.format("%032x", new BigInteger(1, md5.digest()));
        } catch (NoSuchAlgorithmException ignored) {
        }
        return null;
    }

    public static void getInternetData(String url, CallbackInterface callbackinterface) {
        new DownloadData(callbackinterface).execute(url);
    }

    public static class DownloadData extends AsyncTask<String, Void, String> {

        private final CallbackInterface callbackinterface;

        public DownloadData(CallbackInterface ci) {
            this.callbackinterface = ci;
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                String link = args[0];
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder str = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null)
                {
                    str.append(line);
                }
                in.close();
                return str.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "ERROR: " + e.getClass().getName();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            callbackinterface.process(result);
        }
    }

    private interface CallbackInterface{
        void process(String s);
    }
}
