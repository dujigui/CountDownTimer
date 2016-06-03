package me.pheynix.countdowntimer;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by pheynix on 5/26/16.
 */
public class CountDownView extends View {
    public interface OnCountDownListener {
        void onTick(long currentTimeInMillis);

        void onFinish();

        void onSet(int minute);
    }

    int shortMarkLength = 20;
    int longMarkLength = 40;
    int shortMarkStrokeWidth = 4;
    int longMarkStrokeWidth = 8;
    int defaultHeight = 140;
    int defaultWidth = 500;
    int markGap = 40;
    Paint paint = new Paint();
    int selectedMarkColor = 0xFFE65329;
    int unselectedMarkColor = Color.BLACK;
    float selection = 0;
    int textSize = 40;
    int gapAbove = 10;
    int gapBelow = 10;
    int indicatorSize = 40;
    Rect textBoundary = new Rect();
    Path indicator = new Path();
    float previousX;
    float previousY;
    ValueAnimator roundingAnimation;
    boolean isIncreasing = true;
    float minSelection = 0;
    float maxSelection = 120;
    OnCountDownListener listener;
    CountDownTimer timer;


    public void setOnCountDowListener(OnCountDownListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (timer == null) {
            timer = new CountDownTimer((int) selection * 60 * 1000, 1000) {
                @Override public void onTick(long millisUntilFinished) {
                    selection -= 1 / 60.0;
                    if (listener != null) {
                        listener.onTick((long) (selection * 60 * 1000));
                    }
                    postInvalidate();
                }

                @Override public void onFinish() {
                    selection -= 1 / 60.0;
                    if (listener != null) {
                        listener.onFinish();
                    }
                    postInvalidate();
                    timer = null;
                }
            }.start();
        }
    }

    public void pause() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void resume(){
        start();
    }


    public void cancel() {
        selection = (int) selection;
        if (listener != null) {
            listener.onSet((int) selection);
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        postInvalidate();
    }


    public CountDownView(Context context) {
        super(context);
        initialize();
    }

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public CountDownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CountDownView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureDimension(defaultWidth, widthMeasureSpec), measureDimension(defaultHeight, heightMeasureSpec));
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int halfMarkCount = (int) ((width / 2.0) / markGap + 5);

        float startX = (float) (width / 2.0 - halfMarkCount * markGap);
        float startY = textSize + gapAbove;
        float markOffset = (float) (markGap * (selection - Math.floor(selection)));
        int firstNumber = (int) selection - halfMarkCount;

        for (int i = 0, markCount = halfMarkCount * 2; i < markCount; i++) {
            int currentTotalGap = i * markGap;
            int currentNumber = firstNumber + i;
            if (currentNumber < minSelection || currentNumber > maxSelection) {
                continue;
            }

            if (currentNumber % 10 == 0) {
                paint.setStrokeWidth(longMarkStrokeWidth);

                canvas.drawLine(
                        startX + currentTotalGap - markOffset,
                        startY,
                        startX + currentTotalGap - markOffset,
                        startY + longMarkLength,
                        paint);

                String text = String.valueOf(currentNumber);
                paint.getTextBounds(text, 0, text.length(), textBoundary);
                int textOffset = (textBoundary.right - textBoundary.left) / 2;
                canvas.drawText(
                        text,
                        startX + currentTotalGap - markOffset - textOffset,
                        textSize,
                        paint);
            } else {
                paint.setStrokeWidth(shortMarkStrokeWidth);

                int markDifference = (longMarkLength - shortMarkLength) / 2;
                canvas.drawLine(
                        startX + currentTotalGap - markOffset,
                        startY + markDifference,
                        startX + currentTotalGap - markOffset,
                        startY + shortMarkLength + markDifference,
                        paint);
            }
        }

        indicator.moveTo(width / 2, textSize + gapAbove + longMarkLength + gapBelow);
        indicator.lineTo(width / 2 - indicatorSize / 2, textSize + gapAbove + longMarkLength + gapBelow + indicatorSize);
        indicator.lineTo(width / 2 + indicatorSize / 2, textSize + gapAbove + longMarkLength + gapBelow + indicatorSize);
        indicator.lineTo(width / 2, textSize + gapAbove + longMarkLength + gapBelow);
        indicator.close();
        paint.setColor(selectedMarkColor);
        canvas.drawPath(indicator, paint);
        paint.setColor(unselectedMarkColor);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                previousX = event.getX();
                previousY = event.getY();
                interruptAnimation();
                return true;
            case MotionEvent.ACTION_MOVE:
                float distanceX = event.getX() - previousX;
                isIncreasing = distanceX < 0;
                selection -= distanceX / markGap;
                if (selection < minSelection) {
                    selection = minSelection;
                } else if (selection > maxSelection) {
                    selection = maxSelection;
                }
                if (listener != null) {
                    listener.onSet((int) selection);
                }
                previousX = event.getX();
                previousY = event.getY();
                postInvalidate();
                return true;
            case MotionEvent.ACTION_UP:
                float toValue = (float) (isIncreasing ? Math.ceil(selection) : Math.floor(selection));

                roundingAnimation = ValueAnimator.ofFloat(selection, toValue);
                roundingAnimation.setInterpolator(new DecelerateInterpolator());
                roundingAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        selection = (float) animation.getAnimatedValue();
                        postInvalidate();
                    }
                });
                roundingAnimation.setDuration(250);
                roundingAnimation.start();

                if (listener != null) {
                    listener.onSet((int) toValue);
                }
                return true;
            default:
                return false;
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override protected void onDetachedFromWindow() {
        interruptAnimation();
        super.onDetachedFromWindow();
    }

    private int measureDimension(int defaultValue, int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        if (mode == MeasureSpec.EXACTLY) {
            return size;
        } else if (mode == MeasureSpec.AT_MOST) {
            return Math.min(defaultValue, size);
        } else {
            return defaultValue;
        }
    }

    private void initialize() {
        paint.setColor(unselectedMarkColor);
        paint.setTextSize(textSize);
        defaultHeight = textSize + gapAbove + longMarkLength + gapBelow + indicatorSize;
    }

    private void initialize(Context context, AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CountDownView, 0, 0);
        shortMarkLength = (int) typedArray.getDimension(R.styleable.CountDownView_shortMarkLength, shortMarkLength);
        longMarkLength = (int) typedArray.getDimension(R.styleable.CountDownView_longMarkLength, longMarkLength);
        shortMarkStrokeWidth = (int) typedArray.getDimension(R.styleable.CountDownView_shortMarkStrokeWidth, shortMarkStrokeWidth);
        longMarkStrokeWidth = (int) typedArray.getDimension(R.styleable.CountDownView_longMarkStrokeWidth, longMarkStrokeWidth);
        markGap = (int) typedArray.getDimension(R.styleable.CountDownView_markGap, markGap);
        selectedMarkColor = typedArray.getColor(R.styleable.CountDownView_selectedMarkColor, selectedMarkColor);
        unselectedMarkColor = typedArray.getColor(R.styleable.CountDownView_unselectedMarkColor, unselectedMarkColor);
        selection = typedArray.getFloat(R.styleable.CountDownView_selection, selection);
        textSize = (int) typedArray.getDimension(R.styleable.CountDownView_textSize, textSize);
        gapAbove = (int) typedArray.getDimension(R.styleable.CountDownView_gapAbove, gapAbove);
        gapBelow = (int) typedArray.getDimension(R.styleable.CountDownView_gapBelow, gapBelow);
        indicatorSize = (int) typedArray.getDimension(R.styleable.CountDownView_indicatorSize, indicatorSize);
        minSelection = (int) typedArray.getFloat(R.styleable.CountDownView_minSelection, minSelection);
        maxSelection = (int) typedArray.getFloat(R.styleable.CountDownView_maxSelection, maxSelection);
        typedArray.recycle();

        initialize();
    }

    private void interruptAnimation() {
        if (roundingAnimation != null) {
            roundingAnimation.cancel();
            roundingAnimation = null;
        }
    }
}
