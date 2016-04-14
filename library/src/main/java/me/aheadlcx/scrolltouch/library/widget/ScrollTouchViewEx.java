package me.aheadlcx.scrolltouch.library.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Description:
 * Creator: aheadlcx
 * Date:16/4/5 下午5:12
 */
public class ScrollTouchViewEx extends FrameLayout {
    private static final String TAG = "ScrollTouchViewEx";
    private static final String TAG_ACTION = "TAG_ACTION";
    private static final String TAG_ACTION_CANCEL = "TAG_ACTION_CANCEL";
    private static final String TAG_DURATION = "TAG_DURATION";
    private static final String TAG_MOVE = "TAG_MOVE";
    private static final String TAG_MOVE_DOWN = "TAG_MOVE_DOWN";
    private static final String TAG_MOVE_METHOD = "TAG_MOVE_METHOD";
    private static final String TAG_MOVE_HALF = "TAG_MOVE_HALF";
    private static final String TAG_MOVE_EDGE = "TAG_MOVE_EDGE";
    /**
     * 完全滑动出去屏幕的高度，正值。
     */
    public int exitOffSet;
    /**
     * 完全打开时，距离屏幕上方的高度，正值。
     */
    public int allOpen;
    /**
     * 打开一半时，距离屏幕上方的高度，正值。
     */
    public int halfOpen;
    public int duration = 900;
    MotionEvent mLastMotionEvent;
    int mPagingTouchSlop;
    int mPagingTouchSlopX;
    private boolean mHasSendCancelEvent = false;
    private Status curStatus;
    private Indicator mIndicator;
    private InnerViewCallBack mInnerViewCallBack;
    //子 View 消费了水平上滑动的事件
    private boolean childViewResumeHorMoveTouch = false;
    private Scroller scroller;
    private MyScrollViewEx mScrollViewEx;


    public ScrollTouchViewEx(Context context) {
        this(context, null);
    }

    public ScrollTouchViewEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollTouchViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mIndicator = new Indicator();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            scroller = new Scroller(getContext(), null, true);
        } else {
            scroller = new Scroller(getContext());
        }

        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        //触摸事件最少识别距离
        mPagingTouchSlopX = conf.getScaledTouchSlop();
        mPagingTouchSlop = conf.getScaledTouchSlop();
        mPagingTouchSlop = 10;
    }

    public void setScrollViewEx(MyScrollViewEx scrollViewEx) {
        mScrollViewEx = scrollViewEx;
    }

    public InnerViewCallBack getInnerViewCallBack() {
        return mInnerViewCallBack;
    }

    public void setInnerViewCallBack(InnerViewCallBack innerViewCallBack) {
        mInnerViewCallBack = innerViewCallBack;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i(TAG_ACTION, "dispatchTouchEvent: TAG_ACTION = " + ev.getAction());
        boolean resume = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!shouldInterTouchEvent(ev)) {
                    if (childViewResumeHorMoveTouch) {
                        Log.i(TAG_ACTION_CANCEL, "dispatchTouchEvent: !!!shouldInterTouchEvent ");
                        childViewResumeHorMoveTouch = dispatchTouchEventSuper(ev);
                        return childViewResumeHorMoveTouch;
                    }
                }
                if (childViewResumeHorMoveTouch) {
                    Log.i(TAG_ACTION_CANCEL, "dispatchTouchEvent: childViewResumeHorMoveTouch");
                    onRelease();
                    if (!mIndicator.hasMoveAfterPressDown()) {
                        Log.i(TAG_MOVE_METHOD, "dispatchTouchEvent: !!!! hasMoveAfterPressDown");
                        childViewResumeHorMoveTouch = dispatchTouchEventSuper(ev);
                    }
                    return childViewResumeHorMoveTouch;
                }

                if (!isScrolling() && !mIndicator.hasMoveAfterPressDown()) {
                    Log.i(TAG_ACTION_CANCEL, "dispatchTouchEvent: !!!!isScrolling");
                    childViewResumeHorMoveTouch = dispatchTouchEventSuper(ev);
                    onRelease();
                    return childViewResumeHorMoveTouch;
                }
                onRelease();
                sendOnceCancelEvent();

                return true;
            case MotionEvent.ACTION_MOVE:
                if (!shouldInterTouchEvent(ev)) {
                    return dispatchTouchEventSuper(ev);
                }
                Log.e(TAG_MOVE, "ACTION_MOVE: begin ");
                if (mInnerViewCallBack == null) {
                    throw new IllegalStateException(" should catch childView as InnerViewCallBack");
                }
                mLastMotionEvent = ev;
                //TIPS: 16/4/6 有可能子 View 消费了滑动，但是这里记录的滑动参数，就有误了
                mIndicator.onMove(ev.getX(), ev.getY());
                float offsetX = mIndicator.getOffsetX();
                float offsetY = mIndicator.getOffsetY();

                float offsetXAbs = Math.abs(offsetX);
                float offsetYAbs = Math.abs(offsetY);
                boolean engouhHorizontalDeltaX = false;
                Log.e(TAG_MOVE, "offsetX =  " + offsetX + "\n" + "offsetY = " + offsetY + "\n"
                        + "mPagingTouchSlop = " + mPagingTouchSlop);

                if (Math.abs(offsetX) < mPagingTouchSlop && Math.abs(offsetY) < mPagingTouchSlop) {
                    return true;
                }

                if (Math.abs(offsetX) > mPagingTouchSlopX) {
                    engouhHorizontalDeltaX = true;
                }

                boolean isVertical = false;
                if (Math.abs(offsetY) > Math.abs(offsetX)) {
                    isVertical = true;
                }
                boolean moveDown = offsetY > 0;
                boolean moveUp = !moveDown;

                boolean childViewMayNeedUpOrDownTouchEvent = false;

                // 让包裹的类，返回到底需不需要竖直方向事件了, 如果内部是 ScrollView 就用 getScrollY == 0 来做判断
                //如果是 RecycleView 就拿第一个可见的 Item View 是不是第一个。
                if (mInnerViewCallBack.mayNeedUpOrDownTouchEvent()) {
//                    return dispatchTouchEventSuper(ev);
                    childViewMayNeedUpOrDownTouchEvent = true;
                } else {

                }

                if (curStatus == Status.Exit) {
                    //关闭了就不进行任何处理了
                    Log.e(TAG_MOVE, "curStatus == " + Status.Exit);
                    return true;
                } else if (curStatus == Status.HalfOpened) {
                    Log.e(TAG_MOVE, "curStatus == " + Status.HalfOpened);
                    //打开了一半，竖向滑动自己处理，横向滑动给 子 View 处理。
                    //横向滑动
                    if (!isVertical) {
                        Log.e(TAG_MOVE, "isUpOrDown == " + isVertical);

                        childViewResumeHorMoveTouch = dispatchTouchEventSuper(ev);
                        Log.e(TAG_MOVE, "isUpOrDown == childViewResumeHorMoveTouch ==" +
                                childViewResumeHorMoveTouch);
                        return childViewResumeHorMoveTouch;
                        //竖向滑动，半打开状态，肯定交给自己来处理
                    } else {
                        Log.e(TAG_MOVE, "isUpOrDown == " + isVertical + "\n" + "move offsetY = " + offsetY);
                        move(offsetY);
                        childViewResumeHorMoveTouch = false;
                        return true;
                    }
                    //全打开状态
                } else if (curStatus == Status.AllOpened) {
                    Log.e(TAG_MOVE, "curStatus == " + Status.AllOpened);

                    if (!isVertical) {
                        if (!engouhHorizontalDeltaX) {//横向滑动必须足够大
                            return true;
                        }
                        childViewResumeHorMoveTouch = dispatchTouchEventSuper(ev);
                        return childViewResumeHorMoveTouch;
                        //竖向滑动，半打开状态，肯定交给自己来处理
                    }

                    //已经全打开了，网上滑动事件，都交给子 View 来处理。
                    if (moveUp) {
                        childViewResumeHorMoveTouch = dispatchTouchEventSuper(ev);
                        return childViewResumeHorMoveTouch;
                    }

                    //可以往下
                    if (moveDown) {
                        if (!childViewMayNeedUpOrDownTouchEvent) {
                            move(offsetY);
                            return true;
                        } else {
                            childViewResumeHorMoveTouch = dispatchTouchEventSuper(ev);
                            return childViewResumeHorMoveTouch;
                        }
                    }


                    // 滑动中的状态
                } else if (curStatus == Status.scrolling) {
                    Log.e(TAG_MOVE, "curStatus == " + Status.scrolling);
                    move(offsetY);
                    return true;
                }


                break;
            case MotionEvent.ACTION_DOWN:
                hasSendOnceCancelEvent = false;
                if (!shouldInterTouchEvent(ev)) {
                    return dispatchTouchEventSuper(ev);
                }
                if (!scroller.isFinished()) {
                    scroller.forceFinished(true);
                    scroller.abortAnimation();
                }
                mIndicator.onPressDown(ev.getX(), ev.getY());
                onDownChangeStatus();
                childViewResumeHorMoveTouch = false;
                mHasSendCancelEvent = false;
                dispatchTouchEventSuper(ev);
                return true;
        }


        return resume;
    }

    private boolean shouldInterTouchEvent(MotionEvent e) {
        float y = e.getY();
        int scrollY = getScrollY();
        int scrollYAbs = -scrollY;
        if (y >= scrollYAbs) {
            return true;
        }

        return false;
    }

    public boolean dispatchTouchEventSuper(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private void onRelease() {
        int scrollY = getScrollY();
        int scrollYAbs = -scrollY;

        // 处于 全打开 - 半打开中间
        if (scrollYAbs > allOpen && scrollYAbs < halfOpen) {
            int toTopLeft = scrollYAbs - allOpen;
            int toHaflLeft = halfOpen - scrollYAbs;
            if (toTopLeft > toHaflLeft) {
                scrollNow2HalfOpened();
            } else {
                scrollNowToAllOpened();
            }
            // 处于半打开中间 - 关闭状态中间
        } else if (scrollYAbs > halfOpen && scrollYAbs < exitOffSet) {
            int toHaflLeft = scrollYAbs - halfOpen;
            int toExitLeft = exitOffSet - scrollYAbs;
            if (toHaflLeft >= toExitLeft) {
                scrollNowToExit();
            } else {
                scrollNow2HalfOpened();
            }

        }
    }

    public boolean isScrolling() {
        if (curStatus == Status.scrolling) {
            return true;
        }
        return false;
    }

    //移动 View 的操作
    private void move(float offsetY) {
        Log.i(TAG, "move: begin");
        changeStatusToScrolling();
        int targetY = 0;
        targetY = (int) (getScrollY() - offsetY);
        Log.i(TAG, "move: scrollY before move = " + getScrollY());
        Log.i(TAG, "move: targetY= " + targetY);
        Log.i(TAG_MOVE_DOWN, "move: offsetY = " + offsetY + "\n" + "targetY = " + targetY + "\n"
        + "exitOffSet = " + exitOffSet);
        //往上最多全打开状态
        if (targetY > -allOpen) {
            Log.i(TAG_MOVE_EDGE, "move: TAG_MOVE_EDGE -- up == " + targetY);
            targetY = -allOpen;
        }

        // 往下最多关闭状态
        if (targetY < -exitOffSet) {
            Log.i(TAG_MOVE_EDGE, "move: TAG_MOVE_EDGE -- down == " + targetY);
            targetY = -exitOffSet;
        }
        if (targetY != getScrollY()) {
            Log.i(TAG_MOVE_METHOD, "move: targetY = " + targetY + "\n" +
                    "getScrollY" + getScrollY());
            mIndicator.setHasMoveAfterPressDown();
        }
        scrollTo(0, targetY);
        Log.i(TAG, "move: scrollY after move = " + getScrollY());
        if (!mHasSendCancelEvent) {
            sendCancelEvent();
            mHasSendCancelEvent = true;
        }
    }

    private void onDownChangeStatus() {
        int scrollY = getScrollY();
        int scrollYAbs = -scrollY;

        if (scrollYAbs == allOpen) {
            changeStatusToAllOpened();
        } else if (scrollYAbs == halfOpen) {
            changeStatusToHalfOpened();
        } else if (scrollYAbs == exitOffSet) {
            changeStatusToExit();
        } else {
            changeStatusToScrolling();
        }
    }

    /**
     * 从现在的状态，滚动到半打开
     */
    public void scrollNow2HalfOpened() {
        changeStatusToHalfOpened();
        int dy = -getScrollY() + (-halfOpen);
        int durationNeed = getDurationNeed(dy);
        Log.i(TAG_DURATION, "scrollNowToExit: scrollNow2HalfOpened = durationNeed == " + durationNeed);
        scrollToWithAnimation(dy, durationNeed);
        if (mScrollViewEx != null) {
            mScrollViewEx.setShouldHanldeTouch(false);
        }
    }

    /**
     * 从现在状态，滚动到全打开状态
     */
    public void scrollNowToAllOpened() {
        changeStatusToAllOpened();
        int dy = -getScrollY() + (-allOpen);
        int durationNeed = getDurationNeed(dy);
        Log.i(TAG_DURATION, "scrollNowToExit: scrollNowToAllOpened = durationNeed == " + durationNeed);
        scrollToWithAnimation(dy, durationNeed);
        if (mScrollViewEx != null) {
            mScrollViewEx.setShouldHanldeTouch(true);
        }

    }

    /**
     * 滚动到 关闭状态
     */
    public void scrollNowToExit() {
        changeStatusToExit();
        int targetScrollY = -exitOffSet;
        int nowScrollY = getScrollY();
        int deltaY = -nowScrollY + targetScrollY;
        int durationNeed = getDurationNeed(deltaY);
        Log.i(TAG_DURATION, "scrollNowToExit: scrollNowToExit = durationNeed == " + durationNeed);
        scrollToWithAnimation(deltaY, durationNeed);
        if (mScrollViewEx != null) {
            mScrollViewEx.setShouldHanldeTouch(true);
        }

    }

    private void changeStatusToScrolling() {
        curStatus = Status.scrolling;
        if (mScrollViewEx != null) {
            mScrollViewEx.setShouldHanldeTouch(true);
        }

    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    /**
     * @return whether has send
     */
    private boolean sendCancelEvent() {
        if (mLastMotionEvent == null) {
            return false;
        }
        MotionEvent last = mLastMotionEvent;
        MotionEvent cancelEvent = MotionEvent.obtain(last.getDownTime(), last.getEventTime() +
                        ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(),
                last.getY(), last.getMetaState());
        dispatchTouchEventSuper(cancelEvent);
        return true;
    }

    private boolean hasSendOnceCancelEvent = false;

    private boolean sendOnceCancelEvent() {
        if (!hasSendOnceCancelEvent) {
            sendCancelEvent();
            hasSendOnceCancelEvent = true;
        }
        return hasSendOnceCancelEvent;
    }

    public void changeStatusToAllOpened() {
        curStatus = Status.AllOpened;
        if (mScrollViewEx != null) {
            mScrollViewEx.setShouldHanldeTouch(true);
        }
    }

    public void changeStatusToHalfOpened() {
        curStatus = Status.HalfOpened;
    }

    public void changeStatusToExit() {
        curStatus = Status.Exit;
    }

    /**
     * 获取滑动剩下的距离，需要的时间
     *
     * @param deltaY
     * @return
     */
    private int getDurationNeed(int deltaY) {
        int deltaYAbs = Math.abs(deltaY);
        int originDelta = halfOpen - allOpen;
        return (int) (((float) deltaYAbs / (float) originDelta) * duration);
    }

    private void scrollToWithAnimation(int deltaY, int duration) {
        scroller.startScroll(0, getScrollY(), 0, (int) deltaY, duration);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (!scroller.isFinished() && scroller.computeScrollOffset()) {
            int currY = scroller.getCurrY();
            scrollTo(0, currY);
            invalidate();
        }
    }

    /**
     * 直接滚动到，关闭状态，再慢慢的滚动到半打开状态
     */
    public void showHaflStatus() {
        changeStatusToHalfOpened();
        int targetScrollY = -exitOffSet;
        int nowScrollY = getScrollY();
        int dy = -nowScrollY + targetScrollY;
        scrollTo(getScrollX(), dy);
        if (mScrollViewEx != null) {
            mScrollViewEx.setShouldHanldeTouch(false);
        }
        scrollNow2HalfOpened();

    }

    public void exitWithoutAnimation() {
        scrollTo(0, -exitOffSet);
    }

    /**
     * @return whether has send
     */
    private boolean sendDownEvent() {
        if (mLastMotionEvent == null) {
            return false;
        }
        MotionEvent last = mLastMotionEvent;
        MotionEvent cancelEvent = MotionEvent.obtain(last.getDownTime(), last.getEventTime() +
                        ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_DOWN, last.getX(),
                last.getY(), last.getMetaState());
        dispatchTouchEventSuper(cancelEvent);
        return true;
    }

    public int getHalfOpen() {
        return halfOpen;
    }

    public int getAllOpen() {
        return allOpen;
    }

    public void setAllOpen(int allOpen) {
        this.allOpen = allOpen;
    }

    public int getExitOffSet() {
        return exitOffSet;
    }

    public void setExitOffSet(int exitOffSet) {
        this.exitOffSet = exitOffSet;
    }

    public boolean isHalfOpen() {
        if (curStatus == Status.HalfOpened) {
            return true;
        }
        return false;
    }

    public void setHalfOpen(int halfOpen) {
        this.halfOpen = halfOpen;
    }

    /**
     * 描述 状态
     */
    public enum Status {
        Exit, AllOpened, HalfOpened, scrolling;
    }

    public interface InnerViewCallBack {
        boolean mayNeedUpOrDownTouchEvent();
    }
}
