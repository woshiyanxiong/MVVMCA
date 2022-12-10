package com.component.widget;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;

/**
 * Created by yan_x
 * PathMeasure 实现矩形进度条
 * @date 2021/10/26 14:48
 * @description
 */
public class PathMeasureView extends View {
    /**
     * 背景和进度线画笔
     */
    private Paint mPaint, bgPaint;

    private Path path;
    private Path mDst;
    private PathMeasure mPathMeasure;
    private float mLength, totalLength;
    private float stop, width, height;
    private float action;
    private float radius = 50;
    private int proColor=0;
    private int bgColor=0;

    public PathMeasureView(Context context) {
        super(context);
    }

    public PathMeasureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.parseColor("#50D2CB"));
        mPaint.setStrokeWidth(6);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.parseColor("#E1F3F3"));
        bgPaint.setStrokeWidth(5);
        path = new Path();
        mDst = new Path();

        Log.e("mLength", "" + mLength + "," + totalLength);
    }

    public void setCurProgress(float progress) {
        stop = mLength * progress + action;
        Log.e("stop", "" + stop);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDst.reset();
        canvas.drawRoundRect(new RectF(2.5f, 2.5f, width - 2.5f, height - 2.5f), radius, radius, bgPaint);
        canvas.save();
        mPathMeasure.getSegment(action, stop, mDst, true);
        canvas.drawPath(mDst, mPaint);
        if (stop >= mLength) {
            canvas.save();
            Log.e("ddddddd", "" + Math.abs(stop - mLength));
            mPathMeasure.getSegment(0, Math.abs(stop - mLength), mDst, true);
            canvas.drawPath(mDst, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取view宽的SpecSize和SpecMode
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);

        // 获取view高的SpecSize和SpecMode
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);


        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            // 当view的宽和高都设置为wrap_content时，调用setMeasuredDimension(measuredWidth,measureHeight)方法设置view的宽/高为400px
            setMeasuredDimension(100, 48);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            // 当view的宽设置为wrap_content时，设置View的宽为你想要设置的大小（这里我设置400px）,高就采用系统获取的heightSpecSize
            setMeasuredDimension(400, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            // 当view的高设置为wrap_content时，设置View的高为你想要设置的大小（这里我设置400px）,宽就采用系统获取的widthSpecSize
            setMeasuredDimension(widthSpecSize, 400);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        path.addRoundRect(new RectF(3, 3, width - 3, height - 3), radius, radius, Path.Direction.CCW);
        mPathMeasure = new PathMeasure(path, true);
        mLength = mPathMeasure.getLength();
        action = (height - radius - 3) + width / 2;
        totalLength = (float) (mLength + action);
        Log.e("onMeasure", "," + w + "," + h);
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setProColor(@ColorRes  int proColor) {
        this.proColor = proColor;
    }

    public void setBgColor(@ColorRes int bgColor) {
        this.bgColor = bgColor;
    }
}
