package com.example.numberpicker;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = "CustomNumberPicker";

    private Button getIndex;
    private TextView result;
    private NumberPicker numpicker2, numpicker1, numpicker0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Start");

        result = (TextView) findViewById(R.id.result);
        getIndex = (Button) findViewById(R.id.getIndex);
        numpicker2 = (NumberPicker) findViewById(R.id.powerOf2);
        numpicker1 = (NumberPicker) findViewById(R.id.powerOf1);
        numpicker0 = (NumberPicker) findViewById(R.id.powerOf0);
        result.setText("Go to index 000");
        result.setTextSize(40);

        getIndex.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.setText("Go to index " + numpicker2.getCurrentIndex() + numpicker1.getCurrentIndex()
                        + numpicker0.getCurrentIndex());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
