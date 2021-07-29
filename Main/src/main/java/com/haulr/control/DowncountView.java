package com.haulr.control;

/**
 * @description     Downcount View
 *
 * @author          Adrian
 */
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.haulr.R;

public class DowncountView extends View {

    /**
     * Paint for drawing left and passed time.
     */
    private static Paint mPaintTime;

    /**
     * RectF for draw circle progress.
     */
    private RectF rectF;
    private RectF rectTransparent;

    /**
     * Paint for circle progress left
     */
    private static Paint mPaintProgressEmpty;

    /**
     * Paint for circle progress loaded
     */
    private static Paint mPaintProgressLoaded;

    private static Paint mTransparentPaint;

    /**
     * Image Height and Width values.
     */
    private int mHeight;
    private int mWidth;

    /**
     * Center values for cover image.
     */
    private float mCenterX;
    private float mCenterY;

    /**
     * Cover image is rotating. That is why we hold that value.
     */
    private int mRotateDegrees;

    /**
     * Handler for posting runnable object
     */
    private Handler mHandlerRotate;

    /**
     * Runnable for turning image (default velocity is 10)
     */
    private final Runnable mRunnableRotate = new Runnable() {
        @Override
        public void run() {
            if (isRotating) {

                if (currentProgress > maxProgress) {
                    currentProgress = maxProgress;
                    setProgress(currentProgress);
                }

                updateCoverRotate();
                mHandlerRotate.postDelayed(mRunnableRotate, ROTATE_DELAY);
            }
        }
    };

    /**
     * Handler for posting runnable object
     */
    private Handler mHandlerProgress;

    /**
     * Runnable for turning image (default velocity is 10)
     */
    private Runnable mRunnableProgress = new Runnable() {
        @Override
        public void run() {
            if (isRotating) {
                currentProgress += 0.1;
                mHandlerProgress.postDelayed(mRunnableProgress, PROGRESS_SECOND_MS);
            }
        }
    };

    /**
     * isRotating
     */
    private boolean isRotating;

    /**
     * Handler will post runnable object every @ROTATE_DELAY seconds.
     */
    private static int ROTATE_DELAY = 10;

    /**
     * 1 sn = 1000 ms
     */
    private static int PROGRESS_SECOND_MS = 100;

    /**
     * mRotateDegrees count increase 1 by 1 default.
     * I used that parameter as velocity.
     */
    private static float VELOCITY = 2f;

    /**
     * Default color code for cover
     */
    private int mCoverColor = Color.TRANSPARENT;

    /**
     * Color code for progress left.
     */
    private int mProgressEmptyColor = 0xFF00212E;

    /**
     * Color code for progress loaded.
     */
    private int mProgressLoadedColor = 0xFF00815E;

    /**
     * Time text size
     */
    private int mTextSize = 40;

    /**
     * Default text color
     */
    private int mTextColor = 0xFFFFFFFF;

    /**
     * Current progress value
     */
    private float currentProgress = 0;

    /**
     * Max progress value
     */
    private int maxProgress = 60;

    /**
     * Auto progress value start progressing when
     * cover image start rotating.
     */
    private boolean isAutoProgress = true;

    /**
     * Progressview and time will be visible/invisible depends on this
     */
    private boolean mProgressVisibility = true;

    /**
     * Constructor
     */
    public DowncountView(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * Constructor
     */
    public DowncountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Constructor
     */
    public DowncountView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Constructor
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DowncountView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * Initializes resource values, create objects which we need them later.
     * Object creation must not called onDraw() method, otherwise it won't be
     * smooth.
     */
    private void init(Context context, AttributeSet attrs) {

        setWillNotDraw(false);

        mRotateDegrees = 0;

        //Handler and Runnable object for turn cover image by updating rotation degrees
        mHandlerRotate = new Handler();

        //Handler and Runnable object for progressing.
        mHandlerProgress = new Handler();

        //Progress paint object creation
        mPaintProgressEmpty = new Paint();
        mPaintProgressEmpty.setAntiAlias(true);
        mPaintProgressEmpty.setColor(mProgressEmptyColor);
        mPaintProgressEmpty.setStyle(Paint.Style.STROKE);
        mPaintProgressEmpty.setStrokeWidth(10.0f);

        mPaintProgressLoaded = new Paint();
        mPaintProgressLoaded.setAntiAlias(true);
        mPaintProgressLoaded.setColor(mProgressLoadedColor);
        mPaintProgressLoaded.setStyle(Paint.Style.STROKE);
        mPaintProgressLoaded.setStrokeWidth(20.0f);

        mTransparentPaint = new Paint();
        mTransparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mTransparentPaint.setAntiAlias(true);

        mPaintTime = new Paint();
        mPaintTime.setColor(mTextColor);
        mPaintTime.setAntiAlias(true);
        mPaintTime.setTextSize(mTextSize);

        //rectF and rect initializes
        rectF = new RectF();
        rectTransparent = new RectF();
    }

    Bitmap bitmap;
    Canvas temp;

    /**
     * Calculate mWidth, mHeight, mCenterX, mCenterY values and
     * scale resource bitmap. Create shader. This is not called multiple times.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        if (w != oldw || h != oldh) {
            int minSide = Math.min(w, h);
            mWidth = minSide;
            mHeight = minSide;

            mCenterX = mWidth / 2f;
            mCenterY = mHeight / 2f;

            //set RectF left, top, right, bottom coordiantes
            rectF.set(20, 20, mWidth - 20, mHeight - 20);
            rectTransparent.set(40, 40, mWidth-40, mHeight-40);

            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            temp = new Canvas(bitmap);
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * This is where magic happens as you know.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Transparent Region
        temp.drawColor(0xff303030);
        //temp.drawCircle(mCenterX, mCenterY, mCenterX, mTransparentPaint);
        temp.drawArc(rectTransparent, 0, 360, false, mTransparentPaint);
        canvas.drawBitmap(bitmap, 0, 0, null);

        if (mProgressVisibility) {
            //Draw empty progress
            canvas.drawArc(rectF, 270, -1 * calculatePastProgressDegree(), false, mPaintProgressEmpty);

            // Draw full progress
            canvas.drawArc(rectF, 0, 360, false, mPaintProgressLoaded);
        }
    }

    /**
     * Start turning image
     */
    public void start() {

        isRotating = true;
        mHandlerRotate.removeCallbacksAndMessages(null);
        mHandlerRotate.postDelayed(mRunnableRotate, ROTATE_DELAY);
        if (isAutoProgress) {
            mHandlerProgress.removeCallbacksAndMessages(null);
            mHandlerProgress.postDelayed(mRunnableProgress, PROGRESS_SECOND_MS);
        }
        postInvalidate();
    }

    /**
     * Update rotate degree of cover and invalide onDraw();
     */
    public void updateCoverRotate() {
        mRotateDegrees += VELOCITY;
        mRotateDegrees = mRotateDegrees % 360;
        postInvalidate();
    }

    /**
     * Checks is rotating
     */
    public boolean isRotating() {
        return isRotating;
    }

    /**
     * Set velocity.When updateCoverRotate() method called,
     * increase degree by velocity value.
     */
    public void setVelocity(int velocity) {
        if (velocity > 0) VELOCITY = velocity;
    }

    /**
     * sets progress empty color
     */
    public void setProgressEmptyColor(int color) {
        mProgressEmptyColor = color;
        mPaintProgressEmpty.setColor(mProgressEmptyColor);
        postInvalidate();
    }

    /**
     * sets progress loaded color
     */
    public void setProgressLoadedColor(int color) {
        mProgressLoadedColor = color;
        mPaintProgressLoaded.setColor(mProgressLoadedColor);
        postInvalidate();
    }

    /**
     * Sets total seconds of music
     */
    public void setMax(int maxProgress) {
        this.maxProgress = maxProgress;
        postInvalidate();
    }

    /**
     * Sets current seconds of music
     */
    public void setProgress(float currentProgress) {
        if (0 <= currentProgress && currentProgress <= maxProgress) {
            this.currentProgress = currentProgress;
            postInvalidate();
        }
    }

    /**
     * Get current progress seconds
     */
    public float getProgress() {
        return currentProgress;
    }

    /**
     * Calculate left seconds
     */
    private float calculateLeftSeconds() {
        return maxProgress - currentProgress;
    }

    /**
     * Return passed seconds
     */
    private float calculatePassedSeconds() {
        return currentProgress;
    }

    /**
     * Convert seconds to time
     */
    private String secondsToTime(int seconds) {
        String time = "";

        String minutesText = String.valueOf(seconds / 60);
        if (minutesText.length() == 1) minutesText = "0" + minutesText;

        String secondsText = String.valueOf(seconds % 60);
        if (secondsText.length() == 1) secondsText = "0" + secondsText;

        time = minutesText + ":" + secondsText;

        return time;
    }

    /**
     * Calculate passed progress degree
     */
    private float calculatePastProgressDegree() {
        return (360 * currentProgress) / maxProgress;
    }

    /**
     * If you do not want to automatic progress, you can disable it
     * and implement your own handler by using setProgress method repeatedly.
     */
    public void setAutoProgress(boolean isAutoProgress) {
        this.isAutoProgress = isAutoProgress;
    }

    /**
     * Sets time text color
     */
    public void setTimeColor(int color) {
        mTextColor = color;
        mPaintTime.setColor(mTextColor);
        postInvalidate();
    }

    public void setProgressVisibility(boolean mProgressVisibility) {
        this.mProgressVisibility = mProgressVisibility;
        postInvalidate();
    }

}
