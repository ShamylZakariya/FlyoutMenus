package org.zakariya.flyoutmenudemo.menus;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import org.zakariya.flyoutmenu.FlyoutMenuView;

public class PaletteFlyoutMenu {


    public static class ButtonRenderer extends FlyoutMenuView.ButtonRenderer {

        Paint paint;
        RectF insetButtonBounds = new RectF();
        float inset;

        @ColorInt
        int currentColor;

        double currentColorLuminance;

        public ButtonRenderer(float inset) {
            super();
            paint = new Paint();
            paint.setAntiAlias(true);
            this.inset = inset;
        }

        @SuppressWarnings("unused")
        public
        @ColorInt
        int getCurrentColor() {
            return currentColor;
        }

        public void setCurrentColor(@ColorInt int currentColor) {
            this.currentColor = currentColor;
            currentColorLuminance = ColorUtils.calculateLuminance(this.currentColor);
        }

        @Override
        public void onDrawButtonContent(Canvas canvas, RectF buttonBounds, @ColorInt int buttonColor, float alpha) {

            insetButtonBounds.left = buttonBounds.left + inset;
            insetButtonBounds.top = buttonBounds.top + inset;
            insetButtonBounds.right = buttonBounds.right - inset;
            insetButtonBounds.bottom = buttonBounds.bottom - inset;

            paint.setAlpha((int) (alpha * 255f));
            paint.setColor(currentColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawOval(insetButtonBounds, paint);

            if (currentColorLuminance > 0.7) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(0x33000000);
                canvas.drawOval(insetButtonBounds, paint);
            }
        }
    }

    public static class MenuItem extends FlyoutMenuView.MenuItem {

        @ColorInt
        int color;

        Paint paint;
        float cornerRadius;

        public MenuItem(int id, @ColorInt int color, float cornerRadius) {
            super(id);
            this.color = ColorUtils.setAlphaComponent(color, 255);
            this.cornerRadius = cornerRadius;
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
        }

        @Override
        public void onDraw(Canvas canvas, RectF bounds, float degreeSelected) {
            if (cornerRadius > 0) {
                canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint);
            } else {
                canvas.drawRect(bounds, paint);
            }
        }

        public int getColor() {
            return color;
        }

        public Paint getPaint() {
            return paint;
        }

        public float getCornerRadius() {
            return cornerRadius;
        }
    }
}
