package me.aheadlcx.scrolltouch.library.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.ScrollView;

/**
 * Description:
 * Creator: aheadlcx
 * Date:16/4/5 下午10:04
 */
public class MyScrollViewEx extends ScrollView implements ScrollTouchViewEx.InnerViewCallBack {

    private static final String TAG = "MyScrollViewEx";

    private boolean needUpOrDownTouchEvent = false;

    private boolean shouldHanldeTouch = true;

    public MyScrollViewEx(Context context) {
        this(context, null);
    }

    public MyScrollViewEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyScrollViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (getScrollY() == 0) {
            needUpOrDownTouchEvent = false;
        } else {
            needUpOrDownTouchEvent = true;
        }

    }

    @Override
    public boolean mayNeedUpOrDownTouchEvent() {
        return needUpOrDownTouchEvent;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();

        while (true) {
            if (parent != null && parent instanceof ScrollTouchViewEx) {
                ((ScrollTouchViewEx) parent).setInnerViewCallBack(this);
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = false;
        if (!shouldHanldeTouch) {
            result = false;
        } else {
            result = super.onTouchEvent(ev);
        }
        Log.i(TAG, "onTouchEvent: result = " + result);
        return result;
    }


    public void setShouldHanldeTouch(boolean shouldHanldeTouch) {
        this.shouldHanldeTouch = shouldHanldeTouch;
    }
}
