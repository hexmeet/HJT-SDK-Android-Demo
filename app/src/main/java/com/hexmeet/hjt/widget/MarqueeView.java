package com.hexmeet.hjt.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.hexmeet.hjt.R;

import org.apache.log4j.Logger;

public class MarqueeView extends View {
    private Logger LOG = Logger.getLogger(this.getClass());
    public static final int DEFAULT_SPEED = 5;
    public static final  int SCROLL_SPEED_STATIC = 10;
    public static final  int SCROLL_SPEED_SLOW = 11;
    public static final  int SCROLL_SPEED_MIDDLE = 12;
    public static final  int SCROLL_SPEED_FAST = 13;

    private int mTextColor = Color.WHITE;
    private int backgroundColor = Color.BLACK;

    private float mTextSize = 50;
    private boolean mIsRepeat;//是否重复滚动
    private int mStartPoint;// 开始滚动的位置  0是从最左面开始    1是从最末尾开始
    private int mDirection;//滚动方向 0 向左滚动   1向右滚动
    private int mSpeed;//滚动速度
    private int repeatLimit = -1;
    private int repeatCount = 0;

    private int textWidth = 0;
    private int textHeight = 0;//Font baseline
    private int textBottom = 0;//Font baseline
    private int currentX = 0;// 当前x的位置
    private int sepX = 5;//每一步滚动的距离

    private String  text;
    private TextPaint textPaint;
    private boolean start = false;
    private MarquessRepeatedListener listener;


    public interface MarquessRepeatedListener {
        void onRepeatEnd();
    }

    public MarqueeView(Context context) {
        super(context);
        init(null);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MarqueeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    void init(AttributeSet attrs) {
        if (attrs != null) {
            readAttrs(attrs);
        } else {
            mIsRepeat = true;
            text = "";
            mStartPoint = 1;
            mDirection = 0;
            mSpeed = DEFAULT_SPEED;
        }
    }

    void readAttrs(AttributeSet attrs) {
        int[] attrsArray = new int[]{
                android.R.attr.text,
                android.R.attr.background,
                R.attr.textcolor,
                R.attr.fontSize,
                R.attr.isRepeat,
                R.attr.speed,
                R.attr.startPoint,
                R.attr.direction
        };

        TypedArray ta = getContext().obtainStyledAttributes(attrs, attrsArray);
        text = ta.getString(0);
        if(text == null) {
            text = "";
        }
        backgroundColor = ta.getColor(1, Color.BLACK); // 3 is the index of the array of the textColor attribute
        mTextColor = ta.getColor(2, Color.WHITE); // 3 is the index of the array of the textColor attribute
        mTextSize = ta.getDimension(3, 30); // 2 is the index in the array of the textSize attribute
        mIsRepeat = ta.getBoolean(4, true);
        mStartPoint = ta.getInt(5, 1);
        mDirection = ta.getInt(6, 0);
        mSpeed = ta.getInt(7, DEFAULT_SPEED);
        ta.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (text != null && textPaint != null && getVisibility() == View.VISIBLE) {
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();

            int contentWidth = getWidth() - paddingLeft - paddingRight;
            int contentHeight = getHeight() - paddingTop - paddingBottom;

            if (mSpeed > 0) {
                if (mDirection == 0) {//向左滚动
                    if (currentX <= -textWidth) {
//                        Log.i("RoneyTest", "---onScroll count-["+repeatCount+"]--");
                        repeatCount ++;
                        if (!mIsRepeat || repeatCount == repeatLimit) {
                            onScrollOver();
                        }
                        currentX = contentWidth;
                    } else {
                        currentX -= sepX;
                    }
                } else {//  向右滚动
                    if (currentX >= contentWidth) {
                        if (!mIsRepeat || repeatCount == repeatLimit) {
                            onScrollOver();
                        }
                        currentX = -textWidth;
                    } else {
                        currentX += sepX;
                    }
                }
            }

            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();

            if (canvas != null) {
                canvas.drawColor(backgroundColor, PorterDuff.Mode.SRC);
                int currentY = (contentHeight + textHeight)/2 - textBottom;
                if(mSpeed < 0 && textWidth > width){
                    canvas.drawText(text, 0, currentY, textPaint);
                }else if (mSpeed < 0) {//(contentWidth - textWidth)/2
                    canvas.drawText(text, (contentWidth - textWidth)/2, currentY, textPaint);
                } else {
                    canvas.drawText(text, currentX, currentY, textPaint);
                }
            }

            if (mSpeed > 0) {
                invalidateAfter(mSpeed);
            }
        }
    }

    void invalidateAfter(long delay) {
        removeCallbacks(invalidateRunnable);
        if(start) {
            postDelayed(invalidateRunnable, delay);
        }
    }

    Runnable invalidateRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    private void onScrollOver() {
        stop();
//        Log.i("RoneyTest", "---onScrollOver---");
        if(listener != null) {
            listener.onRepeatEnd();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            width = widthSize;
        } else {
            width = this.getWidth();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paint.density = getResources().getDisplayMetrics().density;
            paint.setTextSize(mTextSize);

            height = (int) ((double) (Math.abs(paint.ascent()) + Math.abs(paint.descent())) * 1.8);
        }

        setMeasuredDimension(width, height);
    }

    void renewPaint() {
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.density = getResources().getDisplayMetrics().density;
        textPaint.setTextSize(mTextSize);
        textPaint.setColor(mTextColor);
        textPaint.setStrokeWidth(0.5f);
        textPaint.setFakeBoldText(true);

        textWidth = (int) textPaint.measureText(text);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textBottom = (int) fontMetrics.bottom;
        textHeight = (int) (textBottom - fontMetrics.top);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        if (mStartPoint == 0) {
            currentX = 0;
        } else {
            currentX = width - getPaddingLeft() - getPaddingRight();
        }
    }

    public void setText(String text, int bgColor, int textColor, int textSize, int repeatLimit, int speedMode) {
        LOG.info("Update Message Overlay: {"+text+"}, repeat: ["+repeatLimit+"], speedMode: ["+speedMode+"],textSize: ["+textSize+"],bgColor: ["+bgColor+"]");

        if (text == null) {
            text = "";
        }
        this.text = text;
        start = true;

        mTextColor = textColor;
        backgroundColor = bgColor;
        this.repeatLimit = repeatLimit;
        repeatCount = 0;

        if(speedMode == SCROLL_SPEED_STATIC) {
            mSpeed = -1;
        } else if(speedMode == SCROLL_SPEED_SLOW) {
            mSpeed = 5;
            sepX = 3;
        } else if(speedMode == SCROLL_SPEED_MIDDLE) {
            mSpeed = 5;
            sepX = 10;
        } else if(speedMode == SCROLL_SPEED_FAST) {
            mSpeed = 5;
            sepX = 25;
        }

        renewPaint();
        requestLayout();
        invalidateAfter(mSpeed * 2);
    }

    public void stop() {
        start = false;
        removeCallbacks(invalidateRunnable);
    }

    public boolean isRunning(){
        return start;
    }

    public void setMarquessRepeatedListener(MarquessRepeatedListener listener) {
        this.listener = listener;
    }
}
