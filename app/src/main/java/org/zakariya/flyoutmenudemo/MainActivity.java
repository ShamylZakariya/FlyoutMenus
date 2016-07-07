package org.zakariya.flyoutmenudemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.widget.TextView;

import org.zakariya.flyoutmenu.FlyoutMenuView;
import org.zakariya.flyoutmenudemo.menus.PaletteFlyoutButtonRenderer;
import org.zakariya.flyoutmenudemo.menus.PaletteFlyoutMenuItem;
import org.zakariya.flyoutmenudemo.menus.ToolFlyoutButtonRenderer;
import org.zakariya.flyoutmenudemo.menus.ToolFlyoutMenuItem;

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
		float toolInset = getResources().getDimension(R.dimen.tool_button_inset);


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

		float inset = getResources().getDimension(R.dimen.palette_swatch_inset);
		final PaletteFlyoutButtonRenderer renderer = new PaletteFlyoutButtonRenderer(inset);
		paletteFlyoutMenu.setButtonRenderer(renderer);

		paletteFlyoutMenu.setSelectionListener(new FlyoutMenuView.SelectionListener() {
			@Override
			public void onItemSelected(FlyoutMenuView flyoutMenuView, FlyoutMenuView.MenuItem item) {
				paletteFlyoutMenuSelectionId = item.getId();
				int color = ((PaletteFlyoutMenuItem) item).getColor();
				renderer.setCurrentColor(color);
				setBrushColor(color);
			}

			@Override
			public void onDismissWithoutSelection(FlyoutMenuView flyoutMenuView) {
			}
		});
	}

}
