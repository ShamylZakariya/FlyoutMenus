package org.zakariya.flyoutmenudemo.menus;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;

import org.zakariya.flyoutmenu.FlyoutMenuView;
import org.zakariya.flyoutmenudemo.menus.util.ToolRenderer;

/**
 * Created by shamyl on 7/7/16.
 */
@SuppressWarnings("unused")
public class ToolFlyoutButtonRenderer implements FlyoutMenuView.ButtonRenderer {

	ToolRenderer toolRenderer;
	Paint paint;
	RectF insetButtonBounds = new RectF();
	float inset;

	public ToolFlyoutButtonRenderer(float inset, float size, boolean isEraser, float alphaCheckerSize, @ColorInt int alphaCheckerColor, @ColorInt int fillColor) {
		this.inset = inset;
		toolRenderer = new ToolRenderer(size, isEraser, alphaCheckerSize, alphaCheckerColor, fillColor);
		paint = new Paint();
		paint.setAntiAlias(true);
	}

	@ColorInt
	public int getFillColor() {
		return toolRenderer.getFillColor();
	}

	public void setFillColor(@ColorInt int fillColor) {
		toolRenderer.setFillColor(fillColor);
	}

	public float getSize() {
		return toolRenderer.getSize();
	}

	public void setSize(float size) {
		toolRenderer.setSize(size);
	}

	public boolean isEraser() {
		return toolRenderer.isEraser();
	}

	public void setIsEraser(boolean isEraser) {
		toolRenderer.setIsEraser(isEraser);
	}

	@Override
	public void onDraw(Canvas canvas, RectF buttonBounds, @ColorInt int buttonColor, float alpha) {
		paint.setAlpha((int) (alpha * 255f));
		paint.setColor(buttonColor);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawOval(buttonBounds, paint);

		insetButtonBounds.left = buttonBounds.left + inset;
		insetButtonBounds.top = buttonBounds.top + inset;
		insetButtonBounds.right = buttonBounds.right - inset;
		insetButtonBounds.bottom = buttonBounds.bottom - inset;
		toolRenderer.draw(canvas, insetButtonBounds);
	}
}
