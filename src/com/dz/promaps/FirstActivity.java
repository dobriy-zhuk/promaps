package com.dz.promaps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by dz on 25.04.14.
 */
public class FirstActivity extends Activity {

    TextView score_text;
    Button button_start;
    Button about;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_xml);

        init();


        SharedPreferences preferences = getSharedPreferences("mySettings", Context.MODE_PRIVATE);


        if(preferences.contains("count_score"))
        {
           int count_score = preferences.getInt("count_score", 0);
            score_text.setText(count_score + " руб.");
        }
        else score_text.setText("0 руб.");

    }

    private void init(){

        score_text = (TextView) findViewById(R.id.TextEconomy);

        button_start = (Button) findViewById(R.id.button_start);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("StartActivity", "START");
                Intent intent = new Intent(FirstActivity.this, PlayActivity.class);
                startActivity(intent);
            }
        });

        about = (Button) findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("StartActivity","ABOUT");
                Intent intent = new Intent(FirstActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

    }

}
