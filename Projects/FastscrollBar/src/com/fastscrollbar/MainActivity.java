package com.fastscrollbar;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

    private FastScrollBar fastscroll;
    private ListView listview;
    private ArrayList<String> testList = new ArrayList<String>();
    private ArrayAdapter<String> testAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getView();
        setArrayList();
        testAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, testList);
        listview.setAdapter(testAdapter);
        fastscroll.setListView(listview);
        
        ActionBar bar = getActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void getView() {
        fastscroll = (FastScrollBar) findViewById(R.id.fastscroll);
        listview = (ListView) findViewById(R.id.listview);
    }

    private void setArrayList() {
        for (int i = 1; i < 1000; i++)
            testList.add("" + i);
    }

}
