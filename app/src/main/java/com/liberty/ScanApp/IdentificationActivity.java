/* 
 * File: 		CaptureFingerprintActivity.java
 * Created:		2013/05/03
 * 
 * copyright (c) 2013 DigitalPersona Inc.
 */

package com.liberty.ScanApp;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.Priority;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class IdentificationActivity extends Activity
{
    private Button m_back;
    private Button getDataButton;
    private String m_deviceName = "";

    private Reader m_reader = null;
    private int m_DPI = 0;
    private Bitmap m_bitmap = null;
    private String myMessage;
    private ImageView m_imgView;
    private TextView m_selectedDevice;
    private TextView m_title;
    private boolean m_reset = false;
    private TextView m_text_conclusion;
    private String m_text_conclusionString;
    private Reader.CaptureResult cap_result = null;
    private Fmd m_fmd = null;
    private String m_uid;

    Engine engine;

    String[] uids;
    Fmd[] fmds;
    TextView dataView;
    private ProgressDialog pd;
    String urlStr =  "https://refugees.cscapstone.us/mobileAPI/returnAllFMDs";
    Engine.Candidate[] candidates;

    private void initializeActivity()
    {
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        m_title = (TextView) findViewById(R.id.title);
        m_title.setText("Identify");
        m_selectedDevice = (TextView) findViewById(R.id.selected_device);
        m_deviceName = getIntent().getExtras().getString("device_name");

        m_selectedDevice.setText("Device: " + m_deviceName);

        m_imgView = (ImageView) findViewById(R.id.bitmap_image);
        m_bitmap = Globals.GetLastBitmap();
        if (m_bitmap == null) m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
        m_imgView.setImageBitmap(m_bitmap);

        m_text_conclusion = (TextView) findViewById(R.id.text_conclusion);
        m_back = (Button) findViewById(R.id.back);
        engine = UareUGlobal.GetEngine();

        m_back.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                onBackPressed ();
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        initializeActivity();

        dataView = (TextView) findViewById(R.id.data);
        new JsonTask().execute(urlStr);
        getDataButton = (Button) findViewById(R.id.getdata);

        // hide views
        getDataButton.setVisibility(View.GONE);
        dataView.setVisibility(View.GONE);

        // initiliaze dp sdk
        try
        {
            Context applContext = getApplicationContext();
            m_reader = Globals.getInstance().getReader(m_deviceName, applContext);
            m_reader.Open(Priority.EXCLUSIVE);
            m_DPI = Globals.GetFirstDPI(m_reader);
        }
        catch (Exception e)
        {
            Log.w("UareUSampleJava", "error during init of reader");
            m_deviceName = "";
            onBackPressed();
            return;
        }

        // loop capture on a separate thread to avoid freezing the UI
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    m_reset = false;
                    while (!m_reset)
                    {
                        cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);
                        // an error occurred
                        if (cap_result == null || cap_result.image == null) continue;
                        // save bitmap image locally
                        m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());

                        // identification
                        m_fmd = engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
                        m_uid = "";

                        try {
                            candidates = engine.Identify(m_fmd, 0, fmds, 214748, 1);
                        }catch(UareUException e){}

                        m_uid = uids[candidates[0].fmd_index];


                        // send to LineApp
                        String lineAppURI = ("lineapp://searchFMD/" + uids[candidates[0].fmd_index]);
                        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(lineAppURI));
                        startActivity(browse);
                        onBackPressed();

                        m_text_conclusionString = Globals.QualityToString(cap_result);
                        runOnUiThread(new Runnable()
                        {
                            @Override public void run()
                            {
                                UpdateGUI();
                            }
                        });
                    }
                }
                catch (Exception e)
                {
                    if(!m_reset)
                    {
                        Log.w("UareUSampleJava", "error during capture: " + e.toString());
                        m_deviceName = "";
                        onBackPressed();
                    }
                }
            }
        }).start();
    }

    public void UpdateGUI()
    {
        m_imgView.setImageBitmap(m_bitmap);
        m_imgView.invalidate();
        m_text_conclusion.setText(m_text_conclusionString);
        m_title.setText(myMessage);
    }

    @Override
    public void onBackPressed()
    {
        try
        {
            m_reset = true;
            try {m_reader.CancelCapture(); } catch (Exception e) {}
            m_reader.Close();

        }
        catch (Exception e)
        {
            Log.w("UareUSampleJava", "error during reader shutdown");
        }

        Intent i = new Intent();
        i.putExtra("device_name", m_deviceName);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    // called when orientation has changed to manually destroy and recreate activity
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_identification);
        initializeActivity();
    }



    // Network Request Processing
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(IdentificationActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()) {
                pd.dismiss();
            }

            //dataView.setText(result);
            //parseResult(result);
            dataView.setText(parseResult(result));
            //dataView.setText("TEST");
            //dataView.setText("");


        }

        String parseResult(String result){
            String[] inputs = result.split("(\"\\},)?\\{\"_id\"\\:\"");
            int pop = inputs.length;
            String[] temp;
            String[] lines = new String[pop];
            uids = new String[pop-1];
            fmds = new Fmd[pop-1];
            String output =  "";
            byte[] tmpFmd;

            // for every recipient, isolate uid and fmd
            for(int i = 1; i < inputs.length; i++) {
                temp = inputs[i].split("(\",\"fmd\"\\:\")|(\"\\}\\]\\})");
                output = output + "UID : " + temp[0].length() + "\nFMD : " + temp[1].length() + "\n";
                uids[i - 1] = temp[0];
                tmpFmd = Base64.decode(temp[1], Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
                try {
                    fmds[i - 1] = UareUGlobal.GetImporter().ImportFmd(tmpFmd, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
                } catch(UareUException e){
                    output = "Failure";
                }
            }
            return output;
        }
    }

}
