package com.example.numberpicker;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CircularArrayAdapter extends BaseAdapter {

    public static final int TOTAL_NUM = Integer.MAX_VALUE;
    public static final int HALF_MAX_VALUE = TOTAL_NUM / 2;
    public final int MIDDLE;
    private int minHeight = 0;
    private ArrayList<Integer> objects = new ArrayList<Integer>();
    private LayoutInflater mInflater;

    private class ViewTag {
        TextView mText;

        private ViewTag(TextView mText) {
            this.mText = mText;
        }
    }

    public CircularArrayAdapter(Context context, ArrayList<Integer> objects) {
        MIDDLE = HALF_MAX_VALUE - HALF_MAX_VALUE % objects.size();
        mInflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return TOTAL_NUM;
    }

    @Override
    public Integer getItem(int position) {
        return objects.get(position % objects.size());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertview, ViewGroup parent) {
        ViewTag tag;

        if (convertview == null) {
            convertview = mInflater.inflate(R.layout.item_number, null);
            tag = new ViewTag((TextView) convertview.findViewById(R.id.number));
            convertview.setTag(tag);
        } else
            tag = (ViewTag) convertview.getTag();

        tag.mText.setText("" + getItem(position));
        tag.mText.setMinimumHeight(minHeight);
        tag.mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, minHeight / 2);
        tag.mText.setTextColor(Color.WHITE);
        convertview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
        return convertview;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }
}
