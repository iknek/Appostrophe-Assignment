package com.example.app_test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SnapLineDraw extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Line> snapLines = new ArrayList<>();

    public SnapLineDraw(Context ctx) {
        super(ctx);
        paint.setColor(Color.parseColor("#81D4FA"));
        paint.setStrokeWidth(7);
    }

    /**
     * Call to update line’s endpoints and show it
    */
    public void showLine(float x1, float y1, boolean vertical) {
        bringToFront();
        if (vertical) {
            snapLines.add(new Line(x1, 0, x1, getHeight()));
        } else {
            snapLines.add(new Line(0, y1, getWidth(), y1));
        }
        invalidate();
    }

    /**
     *  Hides line (e.g. when drag ends)
    */
    public void clearLines() {
        snapLines.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Line line : snapLines) {
            canvas.drawLine(line.startX, line.startY, line.endX, line.endY, paint);
        }
    }

    private static class Line {
        float startX, startY, endX, endY;
        Line(float x1, float y1, float x2, float y2) {
            this.startX = x1;
            this.startY = y1;
            this.endX = x2;
            this.endY = y2;
        }
    }

}



