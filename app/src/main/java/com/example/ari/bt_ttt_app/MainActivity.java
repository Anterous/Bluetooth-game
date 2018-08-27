package com.example.ari.bt_ttt_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    Button clearText;
    EditText name;
    Button play;

    public static String MyName = "";
    public static String OpponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play = findViewById(R.id.play);
        play.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        name = findViewById(R.id.myName);
                        MyName = name.getText().toString();

                        if (MyName.trim().equals("")) {
                            name.setError("Enter Name");
                        } else {
                            Intent intent;
                            intent = new Intent(MainActivity.this, BT_TTT_names.class);
                            startActivity(intent);
                        }
                    }
                }
        );
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }
}

