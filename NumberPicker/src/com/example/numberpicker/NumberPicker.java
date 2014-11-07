package com.example.numberpicker;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ListView;

public class NumberPicker extends ListView {

    private static final String TAG = "CustomNumberPicker";
    private final int SCROLL_PERIOD = 100;
    private final int MIN_HEIGHT = 3;

    private int height = -1;
    private int width = -1;
    private int currentIndex = 0;
    private int ind = 0;

    private ArrayList<Integer> digitalBar = new ArrayList<Integer>();
    private CircularArrayAdapter digitalAdapter;

    public NumberPicker(Context context) {
        super(context);
        setup();
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setup() {
        for (int i = 0; i < 10; i++)
            digitalBar.add(i);
        digitalAdapter = new CircularArrayAdapter(getContext(), digitalBar);
        setAdapter(digitalAdapter);
        getConst();
        setDividerHeight(0);
    }

    private void getConst() {
        final ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (height != -1 && width != -1)
                    return;
                height = getHeight();
                width = getWidth();
                ind = digitalAdapter.MIDDLE + currentIndex - 1;
                setSelection(ind);
                digitalAdapter.setMinHeight(height / MIN_HEIGHT);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        try {
            switch (e.getAction()) {
            case (MotionEvent.ACTION_DOWN):
                return super.onTouchEvent(e);
            case (MotionEvent.ACTION_UP):
                ind = pointToPosition(0, height / 2 / MIN_HEIGHT);
                if (ind == -1) {
                    Log.d(TAG, "INVALID_ROW_ID");
                    return super.onTouchEvent(e);
                }
                fixIndex(ind);
                return super.onTouchEvent(e);
            case (MotionEvent.ACTION_MOVE):
                return super.onTouchEvent(e);
            default:
                ind = pointToPosition(0, height / 2 / MIN_HEIGHT);
                if (ind == -1) {
                    Log.d(TAG, "INVALID_ROW_ID");
                    return super.onTouchEvent(e);
                }
                fixIndex(ind);
                return super.onTouchEvent(e);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    private void fixIndex(final int ind) {
        Handler handler = new Handler();
        smoothScrollToPosition(ind);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentIndex = (digitalAdapter.getItem(ind) + 1) % digitalBar.size();
                setSelection(digitalAdapter.MIDDLE + currentIndex - 1);
                smoothScrollBy(0, 0);
                Log.d(TAG, "currentIndex = " + currentIndex);
            }
        }, SCROLL_PERIOD);
    }
}
