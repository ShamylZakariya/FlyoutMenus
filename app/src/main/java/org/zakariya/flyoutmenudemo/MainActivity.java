package org.zakariya.flyoutmenudemo;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.widget.TextView;

import org.zakariya.flyoutmenu.FlyoutMenuView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

public class MainActivity extends AppCompatActivity {

	private static float BRUSH_SCALE = 16;

	@Bind(R.id.toolSelectorFlyoutMenu)
	FlyoutMenuView toolSelectorFlyoutMenu;

	@Bind(R.id.paletteFlyoutMenu)
	FlyoutMenuView paletteFlyoutMenu;

	@Bind(R.id.brushStateTextView)
	TextView brushStateTextView;

	@State
	int toolFlyoutMenuSelectionId = 0;

	@State
	int paletteFlyoutMenuSelectionId = 0;

	float brushSize;
	boolean brushIsEraser;
	int brushColor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		Icepick.restoreInstanceState(this, savedInstanceState);

		// build our menus
		configureToolFlyoutMenu();
		configurePaletteFlyoutMenu();

		toolSelectorFlyoutMenu.setSelectedMenuItemById(toolFlyoutMenuSelectionId);
		paletteFlyoutMenu.setSelectedMenuItemById(paletteFlyoutMenuSelectionId);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Icepick.saveInstanceState(this, outState);
		super.onSaveInstanceState(outState);
	}

	public float getBrushSize() {
		return brushSize;
	}

	public void setBrushSize(float brushSize) {
		this.brushSize = brushSize;
		updateBrushStateTextView();
	}

	public boolean isBrushEraser() {
		return brushIsEraser;
	}

	public void setBrushIsEraser(boolean brushIsEraser) {
		this.brushIsEraser = brushIsEraser;
		updateBrushStateTextView();
	}

	public int getBrushColor() {
		return brushColor;
	}

	public void setBrushColor(int brushColor) {
		this.brushColor = brushColor;
		updateBrushStateTextView();
	}

	@SuppressLint("SetTextI18n")
	void updateBrushStateTextView(){
		@SuppressLint("DefaultLocale")
		String brushSizeStr = String.format("%.2f", getBrushSize());

		if (isBrushEraser()) {
			brushStateTextView.setText("Eraser size: " + brushSizeStr);
		} else {
			String colorStr = String.format("#%06X", 0xFFFFFF & getBrushColor());
			brushStateTextView.setText("Brush size: " + brushSizeStr + " color: " + colorStr);
		}
	}

	void configureToolFlyoutMenu() {

		float alphaCheckerSize = getResources().getDimension(R.dimen.alpha_checker_size);
		int toolColor = ContextCompat.getColor(this, R.color.toolFillColor);
		int alphaCheckerColor = ContextCompat.getColor(this, R.color.alphaCheckerColor);
		float toolInset = getResources().getDimension(R.dimen.tool_inset);


		final int count = 6;
		List<ToolFlyoutMenuItem> items = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			float size = (float) (i + 1) / (float) count;
			items.add(new ToolFlyoutMenuItem(items.size(), size, false, alphaCheckerSize, alphaCheckerColor, toolColor));
		}

		for (int i = 0; i < count; i++) {
			float size = (float) (i + 1) / (float) count;
			items.add(new ToolFlyoutMenuItem(items.size(), size, true, alphaCheckerSize, alphaCheckerColor, toolColor));
		}

		toolSelectorFlyoutMenu.setLayout(new FlyoutMenuView.GridLayout(count, FlyoutMenuView.GridLayout.UNSPECIFIED));
		toolSelectorFlyoutMenu.setAdapter(new FlyoutMenuView.ArrayAdapter<>(items));

		final ToolFlyoutButtonRenderer toolButtonRenderer = new ToolFlyoutButtonRenderer(toolInset, 1, false, alphaCheckerSize, alphaCheckerColor, toolColor);
		toolSelectorFlyoutMenu.setButtonRenderer(toolButtonRenderer);

		toolSelectorFlyoutMenu.setSelectionListener(new FlyoutMenuView.SelectionListener() {
			@Override
			public void onItemSelected(FlyoutMenuView flyoutMenuView, FlyoutMenuView.MenuItem item) {
				toolFlyoutMenuSelectionId = item.getId();
				ToolFlyoutMenuItem toolMenuItem = (ToolFlyoutMenuItem) item;

				toolButtonRenderer.setSize(toolMenuItem.getSize());
				toolButtonRenderer.setIsEraser(toolMenuItem.isEraser());
				setBrushSize(toolMenuItem.getSize() * BRUSH_SCALE);
				setBrushIsEraser(toolMenuItem.isEraser());
			}

			@Override
			public void onDismissWithoutSelection(FlyoutMenuView flyoutMenuView) {
			}
		});
	}

	void configurePaletteFlyoutMenu() {

		int cols = 8;
		int rows = 8;
		float hsl[] = {0, 0, 0};
		float cornerRadius = 0;
		if (paletteFlyoutMenu.getItemMargin() > 0) {
			cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
		}

		paletteFlyoutMenu.setLayout(new FlyoutMenuView.GridLayout(cols, FlyoutMenuView.GridLayout.UNSPECIFIED));

		List<PaletteFlyoutMenuItem> items = new ArrayList<>();
		for (int r = 0; r < rows; r++) {
			float hue = 360f * ((float) r / (float) rows);
			hsl[0] = hue;
			for (int c = 0; c < cols; c++) {
				if (c == 0) {
					float lightness = (float) r / (float) (rows - 1);
					hsl[1] = 0;
					hsl[2] = lightness;
				} else {
					float lightness = (float) c / (float) cols;
					hsl[1] = 1;
					hsl[2] = lightness;
				}
				items.add(new PaletteFlyoutMenuItem(items.size(), ColorUtils.HSLToColor(hsl), cornerRadius));
			}
		}

		paletteFlyoutMenu.setAdapter(new FlyoutMenuView.ArrayAdapter<>(items));

		float insetDp = 8;
		float insetPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, insetDp, getResources().getDisplayMetrics());
		final PaletteFlyoutButtonRenderer renderer = new PaletteFlyoutButtonRenderer(insetPx);
		paletteFlyoutMenu.setButtonRenderer(renderer);

		paletteFlyoutMenu.setSelectionListener(new FlyoutMenuView.SelectionListener() {
			@Override
			public void onItemSelected(FlyoutMenuView flyoutMenuView, FlyoutMenuView.MenuItem item) {
				paletteFlyoutMenuSelectionId = item.getId();
				int color = ((PaletteFlyoutMenuItem) item).color;
				renderer.setCurrentColor(color);
				setBrushColor(color);
			}

			@Override
			public void onDismissWithoutSelection(FlyoutMenuView flyoutMenuView) {
			}
		});
	}

	private static final class PaletteFlyoutButtonRenderer implements FlyoutMenuView.ButtonRenderer {

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
		public int getCurrentColor() {
			return currentColor;
		}

		public void setCurrentColor(int currentColor) {
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

	private static final class ToolFlyoutButtonRenderer implements FlyoutMenuView.ButtonRenderer {

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

		@SuppressWarnings("unused")
		@ColorInt
		public int getFillColor() {
			return toolRenderer.getFillColor();
		}

		@SuppressWarnings("unused")
		public void setFillColor(@ColorInt int fillColor) {
			toolRenderer.setFillColor(fillColor);
		}

		public float getSize() {
			return toolRenderer.getSize();
		}

		public void setSize(float size) {
			toolRenderer.setSize(size);
		}

		@SuppressWarnings("unused")
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


	private static final class ToolFlyoutMenuItem extends FlyoutMenuView.MenuItem {

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

	private static final class PaletteFlyoutMenuItem extends FlyoutMenuView.MenuItem {

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
	}

	/**
	 * Convenience class for rendering the tool state in the ToolFlyoutMenuItem and ToolFlyoutButtonRenderer
	 */
	private static final class ToolRenderer {

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

}
