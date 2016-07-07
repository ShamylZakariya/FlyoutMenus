package org.zakariya.flyoutmenudemo.menus.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.ColorInt;

/**
 * Convenience class for rendering the tool state in the ToolFlyoutMenuItem and ToolFlyoutButtonRenderer
 */
@SuppressWarnings("unused")
public class ToolRenderer {

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

	public ToolRenderer(float size, boolean isEraser, float alphaCheckerSize, @ColorInt int alphaCheckerColor, @ColorInt int fillColor) {
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
