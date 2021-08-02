/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.gemvary.testuart;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;

public class ConsoleActivity extends SerialPortActivity {

    private static final String TAG = "ConsoleActivity";

    EditText mReception;

    Button mBtn_send;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);

        final EditText Emission = (EditText) findViewById(R.id.EditTextEmission);

        mReception = (EditText) findViewById(R.id.EditTextReception);
        mBtn_send = (Button) findViewById(R.id.BTN_send);

        mBtn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*String sendstr = Emission.getText().toString().trim();

                try {
                    mOutputStream.write(sendstr.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onClick: error: "+ e.getMessage() );
                }*/

                try {
                    if (mOutputStream != null) {
                        final byte[] outBuffer = new byte[]{(byte) 0xaa, (byte) 0xaa, (byte) 0x21, (byte) 0x01, (byte) 0xff};
                        if (mOutputStream == null) return;
                        mOutputStream.write(outBuffer);
                        Log.e(TAG, "setOnClickListener mOutputStream Sent "+ getStringBuilder(outBuffer).length() +" "+ getStringBuilder(outBuffer).toString());
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

            }
        });

        moveTaskToBack(true);

        /*Emission.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int i;
                CharSequence t = v.getText();
                char[] text = new char[t.length()];
                for (i = 0; i < t.length(); i++) {
                    text[i] = t.charAt(i);
                }
                try {
                    mOutputStream.write(new String(text).getBytes());
                    mOutputStream.write('\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });*/
    }

    private void playAudioAssuranceTone(String strAudioFileName){
        try{
            boolIsMessagePlayed = true;
            Log.e(TAG, "playAudioAssuranceTone: strAudioFileName "+ strAudioFileName );
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource("/sdcard/"+strAudioFileName);//! set valid path
            mediaPlayer.prepare();
            mediaPlayer.start();

        }
        catch(IOException e){
            Log.e(TAG, "playAudioAssuranceTone: "+ e.getMessage());
        }
    }

    private boolean boolIsMessagePlayed = false;
    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            public void run() {
                StringBuilder sb = getStringBuilder(buffer);
                Log.e(TAG,"onDataReceived Testuart app "+ " " +sb.toString());

                /*if (mReception != null) {
                    mReception.append(new String(buffer, 0, size));
                }*/

                if (boolIsMessagePlayed == true) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            boolIsMessagePlayed = false;
                        }
                    }, 5000);
                    return;
                }
                String[] strarrKeycodes = sb.toString().split(" ");
                // ignore incoming data
                if (strarrKeycodes[1].contentEquals("0x30") && strarrKeycodes[2].contentEquals("0x33")){
                    Log.e(TAG, "onDataReceived: Panel Token");
                    playAudioAssuranceTone(getResources().getString(R.string.dooropenpleaseenter));

                }
                else if (strarrKeycodes[1].contentEquals("0x33") && strarrKeycodes[2].contentEquals("0x31")){
                    Log.e(TAG, "onDataReceived: Raytel Token");
                    playAudioAssuranceTone(getResources().getString(R.string.dooropenpleaseenter));

                }


            }
        });
    }

    private StringBuilder getStringBuilder(byte[] buffer) {
        byte[] buffera = Arrays.copyOf(buffer, 5);
        StringBuilder sb = new StringBuilder();

        for (byte bytei: buffera ) {
            String s = String.format("%02X", bytei); // this is a hex string now
            sb.append("0x"+s+" ");
        }
        return sb;
    }

}
