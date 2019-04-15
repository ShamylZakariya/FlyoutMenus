package org.zakariya.flyoutmenudemo.menus;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import androidx.annotation.ColorInt;

import org.zakariya.flyoutmenu.FlyoutMenuView;

public class ToolFlyoutMenu {

    public static class MenuItem extends FlyoutMenuView.MenuItem {

        Renderer renderer;

        public MenuItem(int id, float size, boolean isEraser, float alphaCheckerSize, @ColorInt int alphaCheckerColor, @ColorInt int fillColor) {
            super(id);
            renderer = new Renderer(size, isEraser, alphaCheckerSize, alphaCheckerColor, fillColor);
        }

        public float getSize() {
            return renderer.getSize();
        }

        public boolean isEraser() {
            return renderer.isEraser();
        }

        @Override
        public void onDraw(Canvas canvas, RectF bounds, float degreeSelected) {
            renderer.draw(canvas, bounds);
        }
    }


    @SuppressWarnings("unused")
    public static class ButtonRenderer extends FlyoutMenuView.ButtonRenderer {

        Renderer renderer;
        Paint paint;
        RectF insetButtonBounds = new RectF();
        float inset;

        public ButtonRenderer(float inset, float size, boolean isEraser, float alphaCheckerSize, @ColorInt int alphaCheckerColor, @ColorInt int fillColor) {
            this.inset = inset;
            renderer = new Renderer(size, isEraser, alphaCheckerSize, alphaCheckerColor, fillColor);
            paint = new Paint();
            paint.setAntiAlias(true);
        }

        @ColorInt
        public int getFillColor() {
            return renderer.getFillColor();
        }

        public void setFillColor(@ColorInt int fillColor) {
            renderer.setFillColor(fillColor);
        }

        public float getSize() {
            return renderer.getSize();
        }

        public void setSize(float size) {
            renderer.setSize(size);
        }

        public boolean isEraser() {
            return renderer.isEraser();
        }

        public void setIsEraser(boolean isEraser) {
            renderer.setIsEraser(isEraser);
        }

        @Override
        public void onDrawButtonContent(Canvas canvas, RectF buttonBounds, @ColorInt int buttonColor, float alpha) {
            insetButtonBounds.left = buttonBounds.left + inset;
            insetButtonBounds.top = buttonBounds.top + inset;
            insetButtonBounds.right = buttonBounds.right - inset;
            insetButtonBounds.bottom = buttonBounds.bottom - inset;

            paint.setAlpha((int) (alpha * 255f));
            renderer.draw(canvas, insetButtonBounds);
        }
    }

    /**
     * Convenience class for rendering the tool state in the MenuItem and ButtonRenderer
     */
    @SuppressWarnings("unused")
    static class Renderer {

        float size;
        float radius;
        float alphaCheckerSize;
        boolean isEraser;
        Path clipPath;
        Paint paint;
        RectF previousBounds;
        @ColorInt
        int fillColor;

        @ColorInt
        int alphaCheckerColor;

        public Renderer(float size, boolean isEraser, float alphaCheckerSize, @ColorInt int alphaCheckerColor, @ColorInt int fillColor) {
            this.alphaCheckerSize = alphaCheckerSize;
            this.alphaCheckerColor = alphaCheckerColor;
            paint = new Paint();
            paint.setAntiAlias(true);

            setSize(size);
            setIsEraser(isEraser);
            setFillColor(fillColor);
        }

        public float getSize() {
            return size;
        }

        public void setSize(float size) {
            this.size = Math.min(Math.max(size, 0), 1);
            clipPath = null;
        }

        public void setIsEraser(boolean isEraser) {
            this.isEraser = isEraser;
        }

        public boolean isEraser() {
            return isEraser;
        }

        public int getFillColor() {
            return fillColor;
        }

        public void setFillColor(int fillColor) {
            this.fillColor = fillColor;
        }

        public void draw(Canvas canvas, RectF bounds) {
            float maxRadius = Math.min(bounds.width(), bounds.height()) / 2;
            radius = size * maxRadius;

            if (isEraser) {
                buildEraserFillClipPath(bounds);

                canvas.save();
                canvas.clipPath(clipPath);

                int left = (int) bounds.left;
                int top = (int) bounds.top;
                int right = (int) bounds.right;
                int bottom = (int) bounds.bottom;

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFFFFFFF);
                canvas.drawRect(left, top, right, bottom, paint);

                paint.setColor(alphaCheckerColor);
                for (int y = top, j = 0; y < bottom; y += (int) alphaCheckerSize, j++) {
                    for (int x = left, k = 0; x < right; x += alphaCheckerSize, k++) {
                        if ((j + k) % 2 == 0) {
                            canvas.drawRect(x, y, x + alphaCheckerSize, y + alphaCheckerSize, paint);
                        }
                    }
                }

                canvas.restore();

                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius, paint);

            } else {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(fillColor);
                canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius, paint);
            }
        }

        void buildEraserFillClipPath(RectF bounds) {
            if (previousBounds == null || clipPath == null || !bounds.equals(previousBounds)) {
                previousBounds = new RectF(bounds);

                clipPath = new Path();
                clipPath.addCircle(bounds.centerX(), bounds.centerY(), radius, Path.Direction.CW);
            }
        }

    }
}
