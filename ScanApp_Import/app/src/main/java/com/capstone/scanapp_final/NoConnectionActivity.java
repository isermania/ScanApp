package com.capstone.scanapp_final;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.digitalpersona.uareu.UareUSampleJava.R;
//import android.support.v7.app.AppCompatActivity;

public class NoConnectionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);
    }

    public void retryMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
