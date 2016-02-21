package com.subbu.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by subrahmanyam on 05-01-2016.
 */
public class MyView extends View {
    private long mAngle;

    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p = new Paint();
        p.setColor(Color.BLUE);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);
        int x1 = getWidth() / 2;
        int y1 = getHeight() / 2;
        canvas.drawCircle(x1, y1, getWidth() / 2, p);
        int x2 = (int) (x1 + x1 * Math.cos(mAngle));
        int y2 = (int) (y1 - (y1 * Math.sin(mAngle)));
        canvas.drawLine(x1, y1, x2, y2, p);
    }

    public void setAngle(long aLong) {
        mAngle = aLong;
        invalidate();
    }
}
