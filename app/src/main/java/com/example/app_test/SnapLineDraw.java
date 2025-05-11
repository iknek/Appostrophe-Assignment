package com.example.app_test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class SnapLineDraw extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float startX, startY, endX, endY;
    private boolean shouldDraw = false;

    public SnapLineDraw(Context ctx) {
        super(ctx);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
    }

    /** Call this to update the line’s endpoints and show it **/
    public void showLine(float x1, float y1, boolean vertical) {
        bringToFront();
        if (vertical) {
            startX = x1;
            endX = x1;
            startY = 0;
            endY = getHeight();
        } else {
            startY = y1;
            endY = y1;
            startX = 0;
            endX = getWidth();
        }
        shouldDraw = true;
        invalidate();
    }

    /** Hide the line (e.g. when drag ends) **/
    public void hideLine() {
        shouldDraw = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shouldDraw) {
            canvas.drawLine(startX, startY, endX, endY, paint);
        }
    }
}

