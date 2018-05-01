package com.capstone.scanapp_final;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.digitalpersona.uareu.UareUSampleJava.R;
import com.digitalpersona.uareu.ReaderCollection;
//import android.support.v7.app.AppCompatActivity;


public class MainActivity extends Activity {

    boolean readerConnected = false;
    private ReaderCollection readers;
    private String m_deviceName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar Header = (Toolbar) findViewById(R.id.header);
//        setSupportActionBar(Header);
    }

    // click enroll button
    public void clickEnroll(View view) {
        Intent enroll = new Intent(this, EnrollActivity.class);
        Intent noConnection= new Intent(this, NoConnectionActivity.class);

//        // check for fingerprint reader
//        try
//        {
//            Context applContext = getApplicationContext();
//            Globals inst = Globals.getInstance();
//            readers = inst.getReaders(applContext);
//        } catch (UareUException e)
//        {
//            onBackPressed();
//        }
//
//        if (readers.size() == 0)
//            startActivity(noConnection);
//        else
        startActivity(enroll);
    }

    // click verify button
    public void clickVerify(View view) {
        Intent verify = new Intent(this, VerifyActivity.class);
        Intent noConnection= new Intent(this, NoConnectionActivity.class);

//        if (readerConnected) {}
        startActivity(verify);
    }

    public void clickCheck(View view) {
        Intent check = new Intent(this, CheckActivity.class);
        Intent noConnection= new Intent(this, NoConnectionActivity.class);

//        if (readerConnected) {}
        startActivity(check);
    }

    public void clickConnect(View view) {
        Intent connect = new Intent(this, ConnectActivity.class);
        Intent noConnection= new Intent(this, NoConnectionActivity.class);

//        if (readerConnected) {}
        startActivity(connect);
    }

    @Override
    public void onBackPressed()
    {
        Intent i = new Intent();
        setResult(Activity.RESULT_OK, i);
        finish();
    }
}
