package org.zakariya.flyoutmenudemo.menus;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

import org.zakariya.flyoutmenu.FlyoutMenuView;

public class IconFlyoutMenu {

    static class IconDrawer {

        Drawable icon;
        float inset;
        Path clipPath;
        RectF previousBounds;
        float alpha;

        IconDrawer(Drawable icon, float inset) {
            this.icon = icon;
            this.inset = inset;
            this.previousBounds = new RectF();
            setAlpha(1);
        }

        Drawable getIcon() {
            return icon;
        }

        void setIcon(Drawable icon) {
            this.icon = icon;
        }

        float getAlpha() {
            return alpha;
        }

        void setAlpha(float alpha) {
            this.alpha = Math.min(Math.max(alpha, 0), 1);
        }

        public float getInset() {
            return inset;
        }

        public void setInset(float inset) {
            this.inset = inset;
        }

        void draw(Canvas canvas, RectF bounds) {
            bounds = new RectF(bounds);
            bounds.inset(inset, inset);
            canvas.save();

            if (clipPath == null || !bounds.equals(previousBounds)) {
                previousBounds = bounds;
                clipPath = new Path();
                clipPath.addCircle(bounds.centerX(), bounds.centerY(), Math.min(bounds.width(), bounds.height()) / 2, Path.Direction.CW);
            }

            canvas.clipPath(clipPath);

            float availableWidth = bounds.width();
            float availableHeight = bounds.height();

            float width = icon.getIntrinsicWidth();
            if (width < 0) width = availableWidth;

            float height = icon.getIntrinsicHeight();
            if (height < 0) height = bounds.height();

            if (width > availableWidth) {
                float scale = availableWidth / width;
                width *= scale;
                height *= scale;
            }

            if (height < availableHeight) {
                float scale = availableHeight / height;
                width *= scale;
                height *= scale;
            }

            icon.setBounds(
                    (int) (bounds.centerX() - width / 2),
                    (int) (bounds.centerY() - height / 2),
                    (int) (bounds.centerX() + width / 2),
                    (int) (bounds.centerY() + height / 2)
            );

            icon.setAlpha((int) (this.alpha * 255f));
            icon.draw(canvas);

            canvas.restore();
        }
    }

    public static class MenuItem extends FlyoutMenuView.MenuItem {

        IconDrawer iconDrawer;

        public MenuItem(int id, Drawable icon, float inset) {
            super(id);
            iconDrawer = new IconDrawer(icon, inset);
            iconDrawer.setAlpha(1);
        }

        public Drawable getIcon() {
            return iconDrawer.getIcon();
        }

        @Override
        public void onDraw(Canvas canvas, RectF bounds, float degreeSelected) {
            iconDrawer.draw(canvas, bounds);
        }
    }

    public static class ButtonRenderer extends FlyoutMenuView.ButtonRenderer {

        IconDrawer iconDrawer;

        public ButtonRenderer(Drawable drawable, float inset) {
            super();
            iconDrawer = new IconDrawer(drawable, inset);
        }

        public void setIcon(Drawable icon) {
            iconDrawer.setIcon(icon);
        }

        public Drawable getIcon() {
            return iconDrawer.getIcon();
        }

        @Override
        public void onDrawButtonContent(Canvas canvas, RectF buttonBounds, @ColorInt int buttonColor, float alpha) {
            iconDrawer.setAlpha(alpha);
            iconDrawer.draw(canvas, buttonBounds);
        }
    }
}
