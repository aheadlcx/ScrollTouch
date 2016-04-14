package me.aheadlcx.scrolltouch.library.widget;

import android.graphics.PointF;

/**
 * Description:
 * Creator: aheadlcx
 * Date:16/4/5 下午5:56
 */
public class Indicator {

    private boolean mIsUnderTouch = false;
    private int mPressedPos = 0;
    private int mCurrentPos = 0;
    private float mOffsetX;
    private float mOffsetY;
    private boolean hasMoveAfterPressDown;
    //阻尼系数
    private float mResistance = 1.0f;
    private PointF mPtLastMove = new PointF();

    public void onPressDown(float x, float y){
        hasMoveAfterPressDown = false;
        mIsUnderTouch = true;
        mPressedPos = mCurrentPos;
        mPtLastMove.set(x, y);
    }

    public void onMove(float x, float y){
        float offsetX = x - mPtLastMove.x;
        float offsetY = y - mPtLastMove.y;
        processOnMove(offsetX, offsetY);
        mPtLastMove.set(x, y);
    }

    private void processOnMove(float offsetX, float offsetY){
        setOffXY(offsetX, offsetY/mResistance);
    }

    private void setOffXY(float offsetX, float offsetY){
        mOffsetX = offsetX;
        mOffsetY = offsetY;
    }

    public float getOffsetY() {
        return mOffsetY;
    }

    public float getOffsetX() {

        return mOffsetX;
    }

    public void setHasMoveAfterPressDown() {
        this.hasMoveAfterPressDown = true;
    }

    public boolean hasMoveAfterPressDown(){

        return hasMoveAfterPressDown;
    }
}
