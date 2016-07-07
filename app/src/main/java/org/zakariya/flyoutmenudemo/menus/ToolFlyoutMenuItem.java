package org.zakariya.flyoutmenudemo.menus;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.ColorInt;

import org.zakariya.flyoutmenu.FlyoutMenuView;
import org.zakariya.flyoutmenudemo.menus.util.ToolRenderer;

/**
 * Created by shamyl on 7/7/16.
 */
public class ToolFlyoutMenuItem extends FlyoutMenuView.MenuItem {

	ToolRenderer toolRenderer;

	public ToolFlyoutMenuItem(int id, float size, boolean isEraser, float alphaCheckerSize, @ColorInt int alphaCheckerColor, @ColorInt int fillColor) {
		super(id);
		toolRenderer = new ToolRenderer(size, isEraser, alphaCheckerSize, alphaCheckerColor, fillColor);
	}

	public float getSize() {
		return toolRenderer.getSize();
	}

	public boolean isEraser() {
		return toolRenderer.isEraser();
	}

	@Override
	public void onDraw(Canvas canvas, RectF bounds, float degreeSelected) {
		toolRenderer.draw(canvas, bounds);
	}
}
