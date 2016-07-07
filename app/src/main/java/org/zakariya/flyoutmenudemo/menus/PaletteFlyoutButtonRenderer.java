package org.zakariya.flyoutmenudemo.menus;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;

import org.zakariya.flyoutmenu.FlyoutMenuView;

/**
 * Created by shamyl on 7/7/16.
 */
public class PaletteFlyoutButtonRenderer implements FlyoutMenuView.ButtonRenderer {

	Paint paint;
	RectF insetButtonBounds = new RectF();
	float inset;

	@ColorInt
	int currentColor;

	double currentColorLuminance;

	public PaletteFlyoutButtonRenderer(float inset) {
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
	public void onDraw(Canvas canvas, RectF buttonBounds, @ColorInt int buttonColor, float alpha) {
		paint.setAlpha((int) (alpha * 255f));
		paint.setColor(buttonColor);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawOval(buttonBounds, paint);

		insetButtonBounds.left = buttonBounds.left + inset;
		insetButtonBounds.top = buttonBounds.top + inset;
		insetButtonBounds.right = buttonBounds.right - inset;
		insetButtonBounds.bottom = buttonBounds.bottom - inset;
		
		paint.setColor(currentColor);
		canvas.drawOval(insetButtonBounds, paint);

		if (currentColorLuminance > 0.7) {
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(0x33000000);
			canvas.drawOval(insetButtonBounds, paint);
		}
	}
}
