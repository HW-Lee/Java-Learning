package com.fastscrollbar;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FastScrollBar extends LinearLayout {

    private ArrayList<TextView> tvArray = new ArrayList<TextView>();
    private ArrayList<String> indexTable = new ArrayList<String>();
    private ArrayList<Integer> scrollRoute = new ArrayList<Integer>();
    private ListView lv;
    private Context context;
    private int lineHeight;
    private int lastscroll = 0;
    private boolean isEnabled = true;
    private boolean isScrollAnimEnabled = true;

    private final int ORIGINAL_TEXTCOLOR = Color.WHITE;
    private final int ORIGINAL_BACKGROUND = Color.TRANSPARENT;
    private final int TOUCH_TEXTCOLOR = Color.BLACK;
    private final int TOUCH_BACKGROUND = Color.WHITE;
    private final int FOCUS_TEXTCOLOR = Color.WHITE;
    private final int FOCUS_BACKGROUND = Color.GRAY;
    private final int SCROLL_DISTANCE = 30;
    private final int ANIM_DELAY_UNIT = 4;

    private class Direction {
        public static final int POSITIVE = 1;
        public static final int NEGATIVE = 0;
    }

    public FastScrollBar(Context context) {
        super(context);
        this.context = context;
        setIndexTable();
    }

    public FastScrollBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setIndexTable();
    }

    public FastScrollBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        setIndexTable();
    }

    private void setIndexTable() {
        indexTable.add("001");
        for (int i = 1; i < 20; i++) {
            if (i == 1)
                indexTable.add("050");
            else
                indexTable.add("" + 50 * i);
        }
        produceFastscrollBar();
    }

    private void produceFastscrollBar() {
        for (int i = 0; i <= indexTable.size() + 1; i++) {
            TextView tv = new TextView(context);
            LinearLayout.LayoutParams lparams;
            lparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f);
            int ind = i - 1;
            if (ind == -1 || ind == indexTable.size())
                tv.setText("");
            else {
                tv.setText(indexTable.get(ind));
                tv.setId(ind);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(ORIGINAL_TEXTCOLOR);
                tv.setBackgroundColor(ORIGINAL_BACKGROUND);
                tv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent e) {
                        fastscrolling(v);
                        return false;
                    }
                });
            }
            tv.setLayoutParams(lparams);
            if (ind != -1 && ind != indexTable.size())
                tvArray.add(tv);
            this.addView(tv);
            this.invalidate();
        }
        lineHeight = ((TextView) findViewById(1)).getHeight();
    }

    private void fastscrolling(View v) {
        if (isEnabled() == false)
            return;
        TextView tv = (TextView) findViewById(v.getId());
        if (lineHeight == 0)
            lineHeight = tv.getHeight();
    }

    private void fastscrolling(int ind) {
        if (isEnabled() == false)
            return;
        if (lv == null)
            return;
        if (lastscroll < ind) {
            lastscroll = ind;
            ScrollToPosition(ind - 1, Direction.POSITIVE);
        } else if (lastscroll > ind) {
            lastscroll = ind;
            ScrollToPosition(ind - 1, Direction.NEGATIVE);
        } else
            try {
                lv.setSelection(ind - 1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    public void setListView(ListView lv) {
        if (lv == null)
            return;
        this.lv = lv;
        lv.setSmoothScrollbarEnabled(true);
        lv.setFastScrollEnabled(false);
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setScrollAnimEnabled(boolean enabled) {
        isScrollAnimEnabled = enabled;
    }

    public boolean isScrollAnimEnabled() {
        return isScrollAnimEnabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int ind = 0;
        switch (e.getAction()) {

        case (MotionEvent.ACTION_DOWN):
            if (lineHeight != 0) {
                ind = (int) e.getY() / lineHeight - 1;
                if (ind < 0 || ind > indexTable.size() - 1)
                    return true;
                ind = ensureInd(ind);
                TextView tv = tvArray.get(ind);
                ind = Integer.parseInt(tv.getText().toString());
                fastscrollUsing(tv);
                fastscrolling(ind);
            }
            return true;
        case (MotionEvent.ACTION_MOVE):
            if (lineHeight != 0) {
                ind = (int) e.getY() / lineHeight - 1;
                if (ind < 0 || ind > indexTable.size() - 1)
                    return true;
                ind = ensureInd(ind);
                TextView tv = tvArray.get(ind);
                ind = Integer.parseInt(((TextView) findViewById(ind)).getText().toString());
                fastscrollUsing(tv);
                fastscrolling(ind);
            }
            return true;
        default:
            backToOriginal();
            return false;

        }
    }

    private int ensureInd(int ind) {
        int start = 0;
        int end = indexTable.size() - 1;
        if (ind > end)
            return end;
        else if (ind < start)
            return start;
        else
            return ind;
    }

    private void setAllTextColor(int colors) {
        for (int i = 0; i < indexTable.size(); i++)
            tvArray.get(i).setTextColor(colors);
    }

    private void setAllTextBackColor(int colors) {
        for (int i = 0; i < indexTable.size(); i++)
            tvArray.get(i).setBackgroundColor(colors);
    }

    private void backToOriginal() {
        for (int i = 0; i < indexTable.size(); i++) {
            tvArray.get(i).setBackgroundColor(ORIGINAL_BACKGROUND);
            tvArray.get(i).setTextColor(ORIGINAL_TEXTCOLOR);
        }
    }

    private void fastscrollUsing(TextView tv) {
        setAllTextColor(TOUCH_TEXTCOLOR);
        setAllTextBackColor(TOUCH_BACKGROUND);
        tv.setBackgroundColor(FOCUS_BACKGROUND);
        tv.setTextColor(FOCUS_TEXTCOLOR);
    }

    private synchronized void ScrollToPosition(int pos, int direction) {
        scrollRoute.clear();
        if (direction != Direction.POSITIVE && direction != Direction.NEGATIVE)
            return;
        if (direction == Direction.POSITIVE) {
            for (int i = pos - SCROLL_DISTANCE; i <= pos; i++) {
                scrollRoute.add(i);
            }
        } else {
            for (int i = pos + SCROLL_DISTANCE; i >= pos; i--) {
                scrollRoute.add(i);
            }
        }
        if (isScrollAnimEnabled())
            for (int i = 0; i < scrollRoute.size(); i++)
                go(scrollRoute.get(i), i);
        else
            try {
                lv.setSelection(pos);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    private void go(final int pos, int ind) {
        Handler scroller = new Handler();
        scroller.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    lv.setSelection(pos);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, ANIM_DELAY_UNIT * ind);
    }
}
