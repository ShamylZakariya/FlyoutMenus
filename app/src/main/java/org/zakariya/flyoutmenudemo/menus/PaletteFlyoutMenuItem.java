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
public class PaletteFlyoutMenuItem extends FlyoutMenuView.MenuItem {

	@ColorInt
	int color;

	Paint paint;
	float cornerRadius;

	public PaletteFlyoutMenuItem(int id, @ColorInt int color, float cornerRadius) {
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
