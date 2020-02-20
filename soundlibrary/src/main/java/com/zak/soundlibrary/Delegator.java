package com.zak.soundlibrary;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.sac.speech.GoogleVoiceTypingDisabledException;
import com.sac.speech.Speech;
import com.sac.speech.SpeechDelegate;
import com.sac.speech.SpeechRecognitionNotAvailable;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The type Delegator.
 */
public class Delegator {
    private static String language;
    private final Activity context;
    private Callback callback;
    private TestService service;
    private Speech speech;
    private static boolean firstRun = true;
    private static HashMap<String, String> codeBase;
    private String[] results;
    private Set<String> powerSetValues;


    /**
     * Instantiates a new Delegator.
     *
     * @param applicationContext the application context
     * @param lang               the lang : arabic , english
     * @param callback           the callback
     */
    public Delegator(Activity applicationContext, String lang, Callback callback) {
        this.context = applicationContext;
        language = lang;
        this.callback = callback;
        if (firstRun) {
            codeBase = new HashMap<>();
            getDataFromJson();
        }
    }

    /**
     * Start listen.
     */
    /*public void addNewAction(String key, String value) {
        codeBase.put(key, value);
    }
*/
    public void startListen() {
        service = new TestService(context, language, callback);
        service.startListen();
    }

    private String loadJson() {
        String json = null;
        try {
            InputStream is = context.getAssets().open("data2.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void getPowerSet(String[] set) {
        powerSetValues = new HashSet<>();
        long pow_set_size = (long) Math.pow(2, set.length + 1);
        int counter, j;
        String temp = "";

        for (counter = 0; counter < pow_set_size; counter++) {
            for (j = 0; j < set.length; j++) {
                if ((counter & (1 << j)) > 0)
                    temp += set[j] + " ";
                if (temp != "") {
                    String newTemp = temp.substring(0, temp.length() - 1);
                    powerSetValues.add(newTemp);
                }
            }
            temp = "";
        }
    }

   /* public void addActionToJson(String key, String value) {
        try {
            JSONObject obj = new JSONObject(loadJson());
            JSONArray codes = obj.getJSONArray("codes");
            JSONObject newAction = new JSONObject();
            newAction.put("id", key);
            newAction.put("code", value);
            // codes.put(newAction);
            //obj.put("codes", codes);
            codeBase = new HashMap<String, String>();
            //OutputStream is = context.getAssets().open("data2.json");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonFactory jfactory = new JsonFactory();
            JsonParser jParser = jfactory.createParser(loadJson());

            JsonGenerator jGenerator = jfactory
                    .createGenerator(stream, JsonEncoding.UTF8);
            getDataFromJson();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/

    private void getDataFromJson() {
        try {
            JSONObject obj = new JSONObject(loadJson());
            JSONArray codes = obj.getJSONArray("codes");
            for (int i = 0; i < codes.length(); i++) {
                JSONObject o = (JSONObject) codes.get(i);

                codeBase.put(o.getString("id"), o.getString("code"));
                firstRun = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class TestService implements SpeechDelegate, Speech.stopDueToDelay {

        private String language;
        private final Activity context;
        private AlertDialog alertDialog;
        private Callback callback;


        private TestService(Activity applicationContext, String lang, Callback callback) {
            this.context = applicationContext;
            language = lang;
            this.callback = callback;
        }

        /*
         * start listen service
         * show dialog
         * start listening
         * takes permission
         * */
        private void startListen() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Listening").setCancelable(false);
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            speech = Speech.init(context);

            speech.setListener(this);
            Locale locale = new Locale(language);
            speech.setLocale(locale);
            if (speech.isListening()) {
                speech.stopListening();
                muteBeepSoundOfRecorder();
            } else {
                System.setProperty("rx.unsafe-disable", "True");
                RxPermissions.getInstance(context).request(Manifest.permission.RECORD_AUDIO).subscribe(granted -> {
                    if (granted) {
                        try {
                            speech.stopTextToSpeech();
                            speech.startListening(null, this);

                        } catch (SpeechRecognitionNotAvailable exc) {
                            //showSpeechNotSupportedDialog();
                            exc.printStackTrace();

                        } catch (GoogleVoiceTypingDisabledException exc) {
                            //showEnableGoogleVoiceTyping();
                            exc.printStackTrace();
                        }
                    } else {
                        Toast.makeText(context, "permission required", Toast.LENGTH_LONG).show();
                    }
                });
                muteBeepSoundOfRecorder();
            }
        }

        /*
         * stop service when no sound received
         * */
        @Override
        public void onSpecifiedCommandPronounced(String event) {
            if (event.equals("1")) {
                stopServices();
            }

        }

        @Override
        public void onStartOfSpeech() {

        }


        @Override
        public void onSpeechRmsChanged(float value) {

        }

        /*
         * on speech partial results
         * print to log segmented message
         * */
        @Override
        public void onSpeechPartialResults(List<String> results) {
            for (String partial : results) {
                Log.d("Result", partial + "");
            }
        }

        /*
         * on speech
         * return result by calling callback interface when done
         * */
        @Override
        public void onSpeechResult(String result) {
            results = result.split(" ");
            getPowerSet(results);
            Iterator<String> it = powerSetValues.iterator();

            if (!TextUtils.isEmpty(result)) {
                {
                    stopServices();

                    while (it.hasNext()) {
                        String res = it.next();
                        if (codeBase.containsKey(res)) {
                            callback.onDone(codeBase.get(res));
                            break;
                        } else {
                            callback.onDone("000");
                        }
                    }
                }
            }
        }

        /**
         * Stop services
         * dismiss popup dialog
         * stop listening
         * shutdown speech service
         */
        public void stopServices() {
            alertDialog.dismiss();
            speech.stopTextToSpeech();
            speech.stopListening();
            speech.shutdown();
        }

        /*
         * mute redundant sounds
         * */
        private void muteBeepSoundOfRecorder() {

            AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if (manager != null) {
                manager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
                manager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
                manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                manager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
                manager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
            }
        }


    }

}