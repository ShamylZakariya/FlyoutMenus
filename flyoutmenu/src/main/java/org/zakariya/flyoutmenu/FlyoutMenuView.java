package org.zakariya.flyoutmenu;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@SuppressWarnings("unused")
public class FlyoutMenuView extends View implements ValueAnimator.AnimatorUpdateListener {

	public interface SelectionListener {
		void onItemSelected(FlyoutMenuView flyoutMenuView, MenuItem item);

		void onDismissWithoutSelection(FlyoutMenuView flyoutMenuView);
	}

	@SuppressWarnings("unused")
	public static class Size {
		int width;
		int height;

		public Size() {
		}

		public Size(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}

			if (o instanceof Size) {
				Size other = (Size) o;
				return other.width == width && other.height == height;
			}

			return false;
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 31 * result + width;
			result = 31 * result + height;
			return result;
		}
	}

	public static abstract class ButtonRenderer {

		Paint paint;

		public ButtonRenderer() {
			paint = new Paint();
			paint.setAntiAlias(true);
		}

		public void onDrawButtonBase(Canvas canvas, RectF buttonBounds, @ColorInt int buttonColor, float alpha) {
			paint.setAlpha((int) (alpha * 255f));
			paint.setColor(buttonColor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawOval(buttonBounds, paint);
		}

		abstract public void onDrawButtonContent(Canvas canvas, RectF buttonBounds, @ColorInt int buttonColor, float alpha);
	}

	/**
	 * Base interface for items to be added to the FlyoutMenuView
	 */
	public static class MenuItem {

		int id;

		public MenuItem(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		/**
		 * Draw the contents of the MenuItem
		 *
		 * @param canvas         the canvas to draw with
		 * @param bounds         the bounds of this item in its coordinate space, where the origin (top,left) is 0,0
		 * @param degreeSelected the degree to which this item is selected, will be 0 or 1, and in between during selection animations
		 */
		public void onDraw(Canvas canvas, RectF bounds, float degreeSelected) {
		}
	}

	/**
	 * Base class for layout managers which position MenuItem instances in the FlyoutMenu.
	 * Layout is responsible for determining the minimum size menu that can show all items, and
	 * is responsible for positioning items individually.
	 */
	public interface Layout {
		/**
		 * @param itemCount the number of items in the menu
		 * @return the minimum size the FlyoutMenu must be to display all items. How they're packed is up to the Layout
		 */
		Size getMinimumSizeForItems(int itemCount, int itemWidthPx, int itemHeightPx, int itemMarginPx);

		/**
		 * @param positionInList the individual MenuItem's index in the adapter
		 * @param itemWidthPx    width of item to layout
		 * @param itemHeightPx   height of the item to layout
		 * @param itemMarginPx   the margin around the item
		 * @return the Rect describing the position of this item, in pixels, where the origin (0,0) is the top left of the flyout menu
		 */
		Rect getLayoutRectForItem(int positionInList, int itemWidthPx, int itemHeightPx, int itemMarginPx);
	}

	/**
	 * Basic Layout implementation that places items in a grid.
	 */
	@SuppressWarnings("unused")
	public static class GridLayout implements Layout {

		public static final int UNSPECIFIED = 0;

		int cols, rows;

		/**
		 * Creates a GirdLayout with a specified number of columns or rows. You must specify one concrete value, and
		 * one unspecified using GridLayout.UNSPECIFIED. E.g. calling GridLayout(4, GridLayout.UNSPECIFIED) would produce
		 * a layout with 4 columns. If your adapter described 12 items, the layout would have three rows.
		 *
		 * @param cols number of columns to use, or GridLayout.UNSPECIFIED
		 * @param rows number of rows to use, or GridLayout.UNSPECIFIED
		 */
		public GridLayout(int cols, int rows) {
			this.cols = cols;
			this.rows = rows;

			if (this.cols > 0 && this.rows > 0) {
				throw new IllegalArgumentException("one of cols or rows attribute must be 0, both cannot be set");
			}
		}

		@Override
		public Size getMinimumSizeForItems(int itemCount, int itemWidthPx, int itemHeightPx, int itemMarginPx) {
			Size size = new Size();
			if (cols > 0) {
				// fixed number of columns
				int requiredRows = (int) Math.ceil((float) itemCount / (float) cols);
				size.width = cols * itemWidthPx + ((cols + 1) * itemMarginPx);
				size.height = requiredRows * itemHeightPx + ((requiredRows + 1) * itemMarginPx);
			} else if (rows > 0) {
				int requiredCols = (int) Math.ceil((float) itemCount / (float) rows);
				size.width = requiredCols * itemWidthPx + ((requiredCols + 1) * itemMarginPx);
				size.height = rows * itemHeightPx + ((rows + 1) * itemMarginPx);
			} else {
				throw new IllegalArgumentException("one of cols or rows attribute must be 0, both cannot be set");
			}

			return size;
		}

		@Override
		public Rect getLayoutRectForItem(int positionInList, int itemWidthPx, int itemHeightPx, int itemMarginPx) {
			int row;
			int col;

			if (cols > 0) {
				row = (int) Math.floor((float) positionInList / cols);
				col = positionInList - row * cols;
			} else if (rows > 0) {
				col = (int) Math.floor((float) positionInList / rows);
				row = positionInList - col * rows;
			} else {
				throw new IllegalArgumentException("one of cols or rows attribute must be 0, both cannot be set");
			}

			Rect rect = new Rect();
			rect.left = col * itemWidthPx + (col + 1) * itemMarginPx;
			rect.top = row * itemHeightPx + (row + 1) * itemMarginPx;
			rect.right = rect.left + itemWidthPx;
			rect.bottom = rect.top + itemHeightPx;

			return rect;
		}
	}

	/**
	 * Base class for a FlyoutMenu's data source - the thing providing the MenuItems
	 */
	public interface Adapter {
		/**
		 * @return the number of MenuItems in this Adapter
		 */
		int getCount();

		/**
		 * @param position the index of the item to vend
		 * @return the MenuItem at position
		 */
		MenuItem getItem(int position);
	}

	/**
	 * Convenience Adapter implementation wrapping an Array.
	 *
	 * @param <T>
	 */
	@SuppressWarnings("unused")
	public static class ArrayAdapter<T> implements Adapter {

		private List<T> items;

		public ArrayAdapter(List<T> items) {
			this.items = items;
		}

		public ArrayAdapter(T[] items) {
			this.items = Arrays.asList(items);
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public MenuItem getItem(int position) {
			return (MenuItem) items.get(position);
		}
	}


	private static class MenuItemLayout {
		MenuItem item;
		int itemAdapterPosition;
		RectF frame;
		RectF bounds;
	}

	@SuppressWarnings("unused")
	private static final String TAG = FlyoutMenuView.class.getSimpleName();

	private static final int ANIMATION_DURATION_MILLIS = 225; // 225 normal

	private static final int DEFAULT_BUTTON_ELEVATION_DP = 4;
	private static final int DEFAULT_MENU_ELEVATION_DP = 8;

	private static final int MENU_CORNER_RADIUS_DP = 4;
	private static final int DEFAULT_ITEM_SIZE_DP = 48;
	private static final int DEFAULT_ITEM_MARGIN_DP = 8;

	private static final int DEFAULT_BUTTON_SIZE_DP = 56;

	private static final float DEFAULT_HORIZONTAL_MENU_ANCHOR = 1f;
	private static final boolean DEFAULT_HORIZONTAL_MENU_ANCHOR_OUTSIDE = false;
	private static final float DEFAULT_VERTICAL_MENU_ANCHOR = 0.5f;
	private static final boolean DEFAULT_VERTICAL_MENU_ANCHOR_OUTSIDE = false;
	private static final float DEFAULT_MENU_MARGIN_DP = 16;

	private static final int SHADOW_COLOR = 0xFF000000;
	private static final int SHADOW_ALPHA = 32;


	Paint paint;

	@ColorInt
	int buttonBackgroundColor = 0xFFFFFFFF;

	@ColorInt
	int menuBackgroundColor = 0xFFFFFFFF;

	@ColorInt
	int selectedItemBackgroundColor = 0x0; // defaults to transparent

	@ColorInt
	int shieldColor = 0x77303030;

	boolean shieldVisible;

	float buttonElevation;
	float menuElevation;

	int buttonSize;
	PointF buttonCenter;
	RectF buttonFillOval = new RectF();
	int buttonRadius;

	Drawable buttonDrawable;
	Bitmap buttonShadowBitmap;

	float horizontalMenuAnchor = 0.5f;
	float verticalMenuAnchor = 0.5f;
	boolean horizontalMenuAnchorOutside = false;
	boolean verticalMenuAnchorOutside = false;
	float menuMargin = 0;

	int itemWidth;
	int itemHeight;
	int itemMargin;
	Adapter adapter;
	Layout layout;
	MenuItem selectedMenuItem;
	MenuItem previouslySelectedMenuItem;
	RectF selectedMenuItemBounds = new RectF();

	SelectionListener selectionListener;
	ButtonRenderer buttonRenderer;

	ValueAnimator menuAnimator;
	ValueAnimator selectionAnimator;
	float menuOpenTransition; // 0 is closed, 1 is menuOpen
	float selectionTransition;

	boolean menuOverlayViewAttached;
	MenuOverlayView menuOverlayView;

	boolean dialogMode = false;
	boolean wasOpenedAsDialog = false;

	public FlyoutMenuView(Context context) {
		super(context);
		init(null, 0);
	}

	public FlyoutMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public FlyoutMenuView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {

		// workaround for lack of clip path in API < 18
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			setLayerType(LAYER_TYPE_SOFTWARE, null);
		}

		paint = new Paint();
		paint.setAntiAlias(true);

		// TODO see how to get 'windowBackground' from android.R.style.ThemeOverlay_Material_Dialog
		// https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/res/res/values/themes_material.xml
		// ThemeOverlay.Material.Dialog -> windowBackground -> @drawable/dialog_background_material
		// https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/res/res/drawable/dialog_background_material.xml
		// ?attr/colorBackground
		// https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/res/res/values/themes_material.xml#47
		// But where's the ALPHA coming from? colorBackground is opaque...


		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FlyoutMenuView, defStyle, 0);

		setButtonSize(a.getDimensionPixelSize(R.styleable.FlyoutMenuView_fmButtonSize, (int)dp2px(DEFAULT_BUTTON_SIZE_DP)));
		setDialogMode(a.getBoolean(R.styleable.FlyoutMenuView_fmDialogMode, false));
		setButtonBackgroundColor(a.getColor(R.styleable.FlyoutMenuView_fmButtonBackgroundColor, buttonBackgroundColor));
		setMenuBackgroundColor(a.getColor(R.styleable.FlyoutMenuView_fmMenuBackgroundColor, menuBackgroundColor));
		setSelectedItemBackgroundColor(a.getColor(R.styleable.FlyoutMenuView_fmSelectedItemBackgroundColor, selectedItemBackgroundColor));
		setShieldVisible(a.getBoolean(R.styleable.FlyoutMenuView_fmShieldVisible, false));
		setShieldColor(a.getColor(R.styleable.FlyoutMenuView_fmShieldColor, shieldColor));
		setItemWidth(a.getDimensionPixelSize(R.styleable.FlyoutMenuView_fmItemWidth, (int) dp2px(DEFAULT_ITEM_SIZE_DP)));
		setItemHeight(a.getDimensionPixelSize(R.styleable.FlyoutMenuView_fmItemHeight, (int) dp2px(DEFAULT_ITEM_SIZE_DP)));
		setItemMargin(a.getDimensionPixelSize(R.styleable.FlyoutMenuView_fmItemMargin, (int) dp2px(DEFAULT_ITEM_MARGIN_DP)));

		setMenuMargin(a.getDimensionPixelSize(R.styleable.FlyoutMenuView_fmMenuMargin, (int) dp2px(DEFAULT_MENU_MARGIN_DP)));
		setHorizontalMenuAnchor(a.getFloat(R.styleable.FlyoutMenuView_fmHorizontalMenuAnchor, DEFAULT_HORIZONTAL_MENU_ANCHOR));
		setHorizontalMenuAnchorOutside(a.getBoolean(R.styleable.FlyoutMenuView_fmHorizontalMenuAnchorOutside, DEFAULT_HORIZONTAL_MENU_ANCHOR_OUTSIDE));
		setVerticalMenuAnchor(a.getFloat(R.styleable.FlyoutMenuView_fmVerticalMenuAnchor, DEFAULT_VERTICAL_MENU_ANCHOR));
		setVerticalMenuAnchorOutside(a.getBoolean(R.styleable.FlyoutMenuView_fmVerticalMenuAnchorOutside, DEFAULT_VERTICAL_MENU_ANCHOR_OUTSIDE));

		// if a menuAnchor is defined, it will override explicit menu anchors read above
		parseMenuAnchorSpec(a.getString(R.styleable.FlyoutMenuView_fmMenuAnchor));

		setButtonDrawable(a.getDrawable(R.styleable.FlyoutMenuView_fmButtonSrc));
		setButtonElevation(a.getDimensionPixelSize(R.styleable.FlyoutMenuView_fmButtonElevation, (int) dp2px(DEFAULT_BUTTON_ELEVATION_DP)));
		setMenuElevation(a.getDimensionPixelSize(R.styleable.FlyoutMenuView_fmMenuElevation, (int) dp2px(DEFAULT_MENU_ELEVATION_DP)));

		a.recycle();
	}

	@SuppressWarnings("unused")
	public Adapter getAdapter() {
		return adapter;
	}

	@SuppressWarnings("unused")
	public void setAdapter(Adapter adapter) {
		this.adapter = adapter;

		if (menuOverlayView != null) {
			menuOverlayView.invalidateMenuItemLayout();
		}
	}

	public Layout getLayout() {
		return layout;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;

		if (menuOverlayView != null) {
			menuOverlayView.invalidateMenuItemLayout();
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (getParent() != null) {
			ViewGroup v = (ViewGroup) getParent();
			v.setClipChildren(false);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int desiredWidth = getPaddingLeft() + getPaddingRight() + getButtonSize();
		int desiredHeight = getPaddingTop() + getPaddingBottom() + getButtonSize();
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			width = Math.min(desiredWidth, widthSize);
		} else {
			width = desiredWidth;
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			height = Math.min(desiredHeight, heightSize);
		} else {
			height = desiredHeight;
		}

		// apply measurement
		setMeasuredDimension(width, height);
	}


	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		updateLayoutInfo();
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		final float pinion = 0.25f;
		if (menuOpenTransition < pinion) {
			drawButton(canvas, 1 - menuOpenTransition / pinion);
		}
	}

	void drawButton(Canvas canvas, float alpha) {

		// scale button down as it fades out
		if (alpha < 1) {
			Matrix m = new Matrix();
			m.preTranslate(-buttonCenter.x, -buttonCenter.y);
			m.postScale(alpha, alpha);
			m.postTranslate(buttonCenter.x, buttonCenter.y);
			canvas.concat(m);
		}

		if (buttonElevation > 0) {
			if (buttonShadowBitmap == null) {
				buttonShadowBitmap = createButtonShadowBitmap();
			}

			paint.setAlpha((int) ((alpha * alpha * alpha) * 255));
			float buttonShadowOffset = buttonElevation / 2;
			canvas.drawBitmap(buttonShadowBitmap, buttonCenter.x - buttonShadowBitmap.getWidth() / 2, buttonCenter.y - buttonShadowBitmap.getHeight() / 2 + buttonShadowOffset, paint);
		}

		int scaledAlpha = (int) (alpha * 255);
		paint.setAlpha(scaledAlpha);

		if (buttonRenderer == null) {
			paint.setColor(buttonBackgroundColor);
			canvas.drawOval(buttonFillOval, paint);
		}

		if (buttonDrawable != null) {
			buttonDrawable.setAlpha(scaledAlpha);

			// scale the radius to fit drawable inside circle
			float innerRadius = buttonRadius / 1.41421356237f;
			buttonDrawable.setBounds(
					(int) (buttonCenter.x - innerRadius),
					(int) (buttonCenter.y - innerRadius),
					(int) (buttonCenter.x + innerRadius),
					(int) (buttonCenter.y + innerRadius));

			buttonDrawable.draw(canvas);
		} else if (buttonRenderer != null) {
			buttonRenderer.onDrawButtonBase(canvas, buttonFillOval, buttonBackgroundColor, alpha);
			buttonRenderer.onDrawButtonContent(canvas, buttonFillOval, buttonBackgroundColor, alpha);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				animateMenuOpenChange(true, false);
				wasOpenedAsDialog = false;
				return true;

			case MotionEvent.ACTION_UP:

				// if we're configured to behave in dialog mode, mark that we were opened in dialog
				// mode IFF user release touch on top of the button. this will signal to MenuOverlayView
				// to handle ACTION_DOWN & ACTION_UP events
				if (isInDialogMode() && event.getX() >= 0 && event.getX() <= getWidth() && event.getY() >= 0 && event.getY() <= getHeight()) {
					wasOpenedAsDialog = true;
					return true;
				}

				MenuItem item = null;
				if (menuOverlayView != null) {
					item = menuOverlayView.findMenuItemAtFlyoutMenuViewRelativePosition(event.getX(), event.getY());
				}

				dismissMenuWithMenuItem(item);
				return true;
		}

		return super.onTouchEvent(event);
	}

	/**
	 * Activities or Fragments may want to dismiss a FlyoutMenu on back button press. If the menu is open,
	 * this will dismiss it and return true. If the menu was not open, this will perform no action and will return false.
	 * @return dismisses the menu, returning true if the menu was open, false if not.
	 */
	public boolean dismiss(){
		if (menuOverlayView != null) {
			animateMenuOpenChange(false, false);
			return true;
		}

		return false;
	}

	void dismissMenuWithMenuItem(@Nullable MenuItem item) {
		if (item != null) {
			setSelectedMenuItem(item);
		} else if (selectionListener != null) {
			selectionListener.onDismissWithoutSelection(this);
		}

		// close menu - delay iff an item was selected
		animateMenuOpenChange(false, item != null);
	}

	void animateMenuOpenChange(final boolean open, boolean delay) {

		if (menuAnimator != null) {
			menuAnimator.cancel();
		}

		menuAnimator = ValueAnimator.ofFloat(menuOpenTransition, open ? 1 : 0);
		menuAnimator.setDuration(ANIMATION_DURATION_MILLIS);
		menuAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		menuAnimator.addUpdateListener(this);
		menuAnimator.setStartDelay(delay ? ANIMATION_DURATION_MILLIS : 0);

		menuAnimator.addListener(new Animator.AnimatorListener() {

			boolean canceled = false;

			@Override
			public void onAnimationStart(Animator animation) {
				if (open) {
					attachMenuOverlayView();
				}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (!open && !canceled) {
					detachMenuOverlayView();
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				canceled = true;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});

		menuAnimator.start();
	}


	/**
	 * @return the drawable rendered by the button
	 */
	@SuppressWarnings("unused")
	public Drawable getButtonDrawable() {
		return buttonDrawable;
	}

	@SuppressWarnings("unused")
	public ButtonRenderer getButtonRenderer() {
		return buttonRenderer;
	}

	@SuppressWarnings("unused")
	public void setButtonRenderer(ButtonRenderer buttonRenderer) {
		this.buttonRenderer = buttonRenderer;
		this.buttonDrawable = null;
	}

	/**
	 * Set the drawable drawn by the button
	 *
	 * @param buttonDrawable the thing to draw in the button
	 */
	public void setButtonDrawable(Drawable buttonDrawable) {
		this.buttonDrawable = buttonDrawable;
		this.buttonRenderer = null;
		invalidate();
	}

	/**
	 * Set a bitmap to use as image drawn by button
	 *
	 * @param bitmap the bitmap to draw in the button
	 */
	@SuppressWarnings("unused")
	public void setButtonImage(Bitmap bitmap) {
		setButtonDrawable(new BitmapDrawable(getResources(), bitmap));
	}

	/**
	 * @return the width in pixels of items in the menu
	 */
	@SuppressWarnings("unused")
	public int getItemWidth() {
		return itemWidth;
	}

	/**
	 * Set the width in pixels of items in the menu
	 *
	 * @param itemWidth the width in pixels of items in the menu
	 */
	public void setItemWidth(int itemWidth) {
		this.itemWidth = itemWidth;
		if (menuOverlayView != null) {
			menuOverlayView.invalidateMenuItemLayout();
		}
	}

	/**
	 * @return the height in pixels of items in the menu
	 */
	@SuppressWarnings("unused")
	public int getItemHeight() {
		return itemHeight;
	}

	/**
	 * Set the height in pixels of items in the menu
	 *
	 * @param itemHeight the height in pixels of items in the menu
	 */
	public void setItemHeight(int itemHeight) {
		this.itemHeight = itemHeight;
		if (menuOverlayView != null) {
			menuOverlayView.invalidateMenuItemLayout();
		}
	}

	/**
	 * @return the margin in pixels around items in the menu
	 */
	@SuppressWarnings("unused")
	public int getItemMargin() {
		return itemMargin;
	}

	/**
	 * Set the margin in pixels around items in the menu
	 *
	 * @param itemMargin the margin in pixels around items in the menu
	 */
	public void setItemMargin(int itemMargin) {
		this.itemMargin = itemMargin;
		if (menuOverlayView != null) {
			menuOverlayView.invalidateMenuItemLayout();
		}
	}

	/**
	 * @return the argb color of the background fill of the menu
	 */
	@SuppressWarnings("unused")
	@ColorInt
	public int getMenuBackgroundColor() {
		return menuBackgroundColor;
	}

	/**
	 * Set the background argb color of the menu
	 *
	 * @param menuBackgroundColor the argb color of the background of the menu
	 */
	public void setMenuBackgroundColor(@ColorInt int menuBackgroundColor) {
		this.menuBackgroundColor = ColorUtils.setAlphaComponent(menuBackgroundColor, 255);
		if (menuOverlayView != null) {
			menuOverlayView.invalidate();
		}
	}

	@SuppressWarnings("unused")
	@ColorInt
	public int getShieldColor() {
		return shieldColor;
	}

	public void setShieldColor(@ColorInt int shieldColor) {
		this.shieldColor = shieldColor;
		if (menuOverlayView != null) {
			menuOverlayView.invalidate();
		}
	}

	public boolean isShieldVisible() {
		return shieldVisible;
	}

	public void setShieldVisible(boolean shieldVisible) {
		this.shieldVisible = shieldVisible;
		if (menuOverlayView != null) {
			menuOverlayView.invalidate();
		}
	}

	/**
	 * @return Get the background fill color for the button
	 */
	@SuppressWarnings("unused")
	@ColorInt
	public int getButtonBackgroundColor() {
		return buttonBackgroundColor;
	}

	/**
	 * Set the background fill color for the button
	 *
	 * @param buttonBackgroundColor the argb color for the background of the button
	 */
	public void setButtonBackgroundColor(int buttonBackgroundColor) {
		this.buttonBackgroundColor = ColorUtils.setAlphaComponent(buttonBackgroundColor, 255);
		invalidate();
	}

	/**
	 * @return the color used to highlight the selected menu item
	 */
	@SuppressWarnings("unused")
	@ColorInt
	public int getSelectedItemBackgroundColor() {
		return selectedItemBackgroundColor;
	}

	/**
	 * @param selectedItemBackgroundColor the argb color used to highlight selection
	 */
	public void setSelectedItemBackgroundColor(int selectedItemBackgroundColor) {
		this.selectedItemBackgroundColor = selectedItemBackgroundColor;
		if (menuOverlayView != null) {
			menuOverlayView.invalidate();
		}
	}


	/**
	 * @return the horizontal anchor point of the menu.
	 */
	@SuppressWarnings("unused")
	public float getHorizontalMenuAnchor() {
		return horizontalMenuAnchor;
	}

	/**
	 * Set the horizontal anchorpoint of the menu. A value of 0 anchors the menu
	 * to the left of the button, and a value of 1 anchors to the right. A value of 0.5
	 * centers the menu horizontally.
	 *
	 * @param horizontalMenuAnchor the anchor value, from 0 to 1
	 */
	public void setHorizontalMenuAnchor(float horizontalMenuAnchor) {
		this.horizontalMenuAnchor = Math.min(Math.max(horizontalMenuAnchor, 0), 1);

		if (menuOverlayView != null) {
			menuOverlayView.invalidateMenuFill();
		}
	}

	/**
	 * @return the vertical anchor point of the menu.
	 */
	@SuppressWarnings("unused")
	public float getVerticalMenuAnchor() {
		return verticalMenuAnchor;
	}

	/**
	 * Set the vertical anchorpoint of the menu. A value of 0 anchors the menu
	 * to the top of the button, and a value of 1 anchors to the bottom. A value of 0.5
	 * centers the menu vertically.
	 *
	 * @param verticalMenuAnchor the anchor value, from 0 to 1
	 */
	public void setVerticalMenuAnchor(float verticalMenuAnchor) {
		this.verticalMenuAnchor = Math.min(Math.max(verticalMenuAnchor, 0), 1);

		if (menuOverlayView != null) {
			menuOverlayView.invalidateMenuFill();
		}
	}

	/**
	 * @return if true, the horizontal anchor point attaches to the left edge of the button when horizontal anchor is 0, and to the right edge when horizontal anchor is 1
	 */
	@SuppressWarnings("unused")
	public boolean isHorizontalMenuAnchorOutside() {
		return horizontalMenuAnchorOutside;
	}

	/**
	 * @return get the SelectionListener instance
	 */
	@SuppressWarnings("unused")
	public SelectionListener getSelectionListener() {
		return selectionListener;
	}

	/**
	 * Set a listener to be notified when items in the menu are selected
	 *
	 * @param selectionListener listener to be notified on item selection
	 */
	@SuppressWarnings("unused")
	public void setSelectionListener(SelectionListener selectionListener) {
		this.selectionListener = selectionListener;
	}

	/**
	 * @param horizontalMenuAnchorOutside if true, a horizontal anchor of 0 will hang the menu's right edge off the left edge of the button, and a value of 1 will hang the menu's left edge off the right edge of the button. If false, and the horizontal anchor is 0, the menu's left edge will hang off the button's right edge, and if the horizontal anchor is 1, the menu's right edge will anchor to the button's left
	 */
	public void setHorizontalMenuAnchorOutside(boolean horizontalMenuAnchorOutside) {
		this.horizontalMenuAnchorOutside = horizontalMenuAnchorOutside;

		if (menuOverlayView != null) {
			menuOverlayView.invalidateMenuFill();
		}
	}

	/**
	 * @return if true, the vertical anchor point attaches to the top edge of the button when vertical anchor is 0, and to the bottom edge when vertical anchor is 1
	 */
	@SuppressWarnings("unused")
	public boolean isVerticalMenuAnchorOutside() {
		return verticalMenuAnchorOutside;
	}

	/**
	 * @param verticalMenuAnchorOutside if true, a vertical anchor of 0 will hang the menu's bottom edge off the top edge of the button, and a value of 1 will hang the menu's top edge off the bottom edge of the button. If false, and the vertical anchor is 0, the menu's bottom edge will hang off the button's bottom edge, and if the vertical anchor is 1, the menu's top edge will anchor to the button's top edge
	 */
	public void setVerticalMenuAnchorOutside(boolean verticalMenuAnchorOutside) {
		this.verticalMenuAnchorOutside = verticalMenuAnchorOutside;

		if (menuOverlayView != null) {
			menuOverlayView.invalidateMenuFill();
		}
	}

	/**
	 * Get the margin outside the menu. This margin will be taken into account when the layout of the menu tries to
	 * prevent the menu from going off screen or overlaying the navigation bar
	 *
	 * @return the margin in pixels outside the menu
	 */
	@SuppressWarnings("unused")
	public float getMenuMargin() {
		return menuMargin;
	}

	/**
	 * Set the margin outside the menu. This margin will be taken into account when the layout of the menu tries to
	 * prevent the menu from going off screen or overlaying the navigation bar
	 *
	 * @param menuMargin the margin in pixels outside the menu
	 */
	public void setMenuMargin(float menuMargin) {
		this.menuMargin = menuMargin;
		if (menuOverlayView != null) {
			menuOverlayView.invalidateMenuFill();
		}
	}

	@SuppressWarnings("unused")
	public float getButtonElevation() {
		return buttonElevation;
	}

	/**
	 * Set the button element's elevation in pixels
	 *
	 * @param buttonElevation the button element's elevation in pixels. Set to zero to disable elevation shadow.
	 */
	public void setButtonElevation(float buttonElevation) {
		this.buttonElevation = buttonElevation;
		buttonShadowBitmap = null; // invalidate
	}

	@SuppressWarnings("unused")
	public float getMenuElevation() {
		return menuElevation;
	}

	/**
	 * Set the menu element's elevation in pixels
	 *
	 * @param menuElevation the menu element's elevation in pixels. Set to zero to disable elevation shadow.
	 */
	public void setMenuElevation(float menuElevation) {
		this.menuElevation = menuElevation;
		if (this.menuOverlayView != null) {
			this.menuOverlayView.menuShadowBitmap = null;
		}
	}

	/**
	 * @return get the currently selected menu item, or null if none is selected
	 */
	@SuppressWarnings("unused")
	@Nullable
	public MenuItem getSelectedMenuItem() {
		return selectedMenuItem;
	}

	/**
	 * Set the current menu item selection. Triggers notification of SelectionListener, if one assigned
	 *
	 * @param selectedMenuItem MenuItem to make the current selection
	 */
	public void setSelectedMenuItem(@Nullable MenuItem selectedMenuItem) {
		if (selectionListener != null && selectedMenuItem != null) {
			selectionListener.onItemSelected(this, selectedMenuItem);
		}

		if (selectedMenuItem != this.selectedMenuItem) {
			this.previouslySelectedMenuItem = this.selectedMenuItem;
			this.selectedMenuItem = selectedMenuItem;

			selectionAnimator = ValueAnimator.ofFloat(0, 1);
			selectionAnimator.setDuration(ANIMATION_DURATION_MILLIS);
			selectionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
			selectionAnimator.addUpdateListener(this);
			selectionAnimator.start();

			invalidate();
		}
	}

	/**
	 * Set the current selection to the menu item at a given adapter position
	 *
	 * @param adapterPosition adapter position of menu item to make the current selection
	 */
	@SuppressWarnings("unused")
	public void setSelectedMenuItemByAdapterPosition(int adapterPosition) {
		setSelectedMenuItem(adapter.getItem(adapterPosition));
	}

	/**
	 * Set the current selection to the menu item with a given ID
	 *
	 * @param menuItemId id of the menu item to make the current selection
	 */
	@SuppressWarnings("unused")
	public void setSelectedMenuItemById(int menuItemId) {
		for (int i = 0, n = adapter.getCount(); i < n; i++) {
			MenuItem item = adapter.getItem(i);
			if (item.getId() == menuItemId) {
				setSelectedMenuItem(item);
				return;
			}
		}
	}

	@SuppressWarnings("unused")
	public boolean isInDialogMode() {
		return dialogMode;
	}

	/**
	 * FlyoutMenuView by default behaves as follows: press to open -> drag to desired item -> release to select. In dialog
	 * mode, pressing opens the menu, releasing does nothing. A subsequent tap on an item selects it and closes the menu.
	 *
	 * @param dialogMode if true, the FlyoutMenuView will operate in dialog mode.
	 */
	public void setDialogMode(boolean dialogMode) {
		this.dialogMode = dialogMode;
	}

	/**
	 * @return the size in pixels of the trigger button
	 */
	public int getButtonSize() {
		return buttonSize;
	}

	/**
	 * @param buttonSize the size in pixels of the trigger button
	 */
	public void setButtonSize(int buttonSize) {
		this.buttonSize = buttonSize;
		requestLayout();
	}

	void updateLayoutInfo() {
		float innerWidth = getWidth() - (getPaddingLeft() + getPaddingRight());
		float innerHeight = getHeight() - (getPaddingTop() + getPaddingBottom());
		buttonCenter = new PointF(getPaddingLeft() + (innerWidth / 2), getPaddingTop() + (innerHeight / 2));
		buttonRadius = (int) Math.min(innerWidth / 2, innerHeight / 2);
		buttonFillOval = new RectF(
				buttonCenter.x - buttonRadius,
				buttonCenter.y - buttonRadius,
				buttonCenter.x + buttonRadius,
				buttonCenter.y + buttonRadius);

		if (menuOverlayView != null) {
			menuOverlayView.invalidateMenuFill();
		}
	}

	Bitmap createButtonShadowBitmap() {
		int shadowRadius = (int) buttonElevation * 2;
		int bitmapRadius = buttonRadius + (shadowRadius / 2);
		int bitmapSize = bitmapRadius * 2;
		Bitmap shadowBitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
		shadowBitmap.eraseColor(0x0);

		int colors[] = {
				ColorUtils.setAlphaComponent(SHADOW_COLOR, SHADOW_ALPHA),
				ColorUtils.setAlphaComponent(SHADOW_COLOR, 0)
		};

		float stops[] = {
				(float) (buttonRadius - (shadowRadius / 2)) / (float) bitmapRadius,
				1f
		};

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setShader(new RadialGradient(bitmapRadius, bitmapRadius, bitmapRadius, colors, stops, Shader.TileMode.CLAMP));

		Canvas canvas = new Canvas(shadowBitmap);
		canvas.drawRect(0, 0, bitmapSize, bitmapSize, paint);

		return shadowBitmap;
	}

	float dp2px(float dp) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		if (animation == menuAnimator) {
			menuOpenTransition = (float) animation.getAnimatedValue();
		} else if (animation == selectionAnimator) {
			selectionTransition = (float) animation.getAnimatedValue();
		}
		invalidate();
		if (menuOverlayView != null) {
			menuOverlayView.invalidate();
		}
	}

	void attachMenuOverlayView() {
		if (menuOverlayView == null) {
			menuOverlayView = new MenuOverlayView(getContext(), this);
		}

		if (!menuOverlayViewAttached) {
			ViewGroup rootView = (ViewGroup) getRootView();
			rootView.addView(menuOverlayView);

			menuOverlayView.setLeft(0);
			menuOverlayView.setTop(0);
			menuOverlayView.setRight(rootView.getWidth());
			menuOverlayView.setBottom(rootView.getHeight());
			menuOverlayViewAttached = true;
		}
	}

	void detachMenuOverlayView() {
		if (menuOverlayView != null && menuOverlayViewAttached) {
			ViewGroup rootView = (ViewGroup) getRootView();
			rootView.removeView(menuOverlayView);
			menuOverlayViewAttached = false;
		}
	}

	void parseMenuAnchorSpec(String spec) {

//			one token:
//			top | (right,end) | bottom | (left,start) | center
//
//			two tokens:
//			[ [m] ] center
//			[[m]b]  (inner_start,inner_left,inner_top)
//			[m][b]  (outer_start,outer_left,outer_top)
//			[b[m]]  (inner_end,inner_right,inner_bottom)
//			[b][m]  (outer_end,outer_right,outer_bottom)

		if (!TextUtils.isEmpty(spec)) {
			spec = spec.toLowerCase().trim();
			String[] tokens = spec.split("\\|", 2);

			if (tokens.length == 1) {

				//if there's only one token, then use the convenience anchor matches
				switch (tokens[0]) {
					case "top":
						setHorizontalMenuAnchor(0.5f);
						setHorizontalMenuAnchorOutside(false);
						setVerticalMenuAnchor(0);
						setVerticalMenuAnchorOutside(true);
						break;

					case "right":
					case "end":
						setHorizontalMenuAnchor(1);
						setHorizontalMenuAnchorOutside(true);
						setVerticalMenuAnchor(0.5f);
						setVerticalMenuAnchorOutside(false);
						break;

					case "bottom":
						setHorizontalMenuAnchor(0.5f);
						setHorizontalMenuAnchorOutside(false);
						setVerticalMenuAnchor(1);
						setVerticalMenuAnchorOutside(true);
						break;

					case "left":
					case "start":
						setHorizontalMenuAnchor(0);
						setHorizontalMenuAnchorOutside(true);
						setVerticalMenuAnchor(0.5f);
						setVerticalMenuAnchorOutside(false);
						break;

					case "center":
						setHorizontalMenuAnchor(0.5f);
						setHorizontalMenuAnchorOutside(false);
						setVerticalMenuAnchor(0.5f);
						setVerticalMenuAnchorOutside(false);
						break;

					default:
						throw new IllegalArgumentException("Unrecognized FlyoutMenuView menuAnchor token \"" + tokens[0] + "\"");
				}

			} else if (tokens.length == 2) {
				// two tokens - parse the horizontal spec, then vertical
				parseMenuAnchorAttachmentSpec(tokens[0], true);
				parseMenuAnchorAttachmentSpec(tokens[1], false);
			}
		}
	}

	void parseMenuAnchorAttachmentSpec(String attachmentSpec, boolean horizontal) {

		float anchor;
		boolean outside;

		switch (attachmentSpec) {
			case "center":
				anchor = 0.5f;
				outside = false;
				break;

			case "inner_start":
			case "inner_left":
			case "inner_top":
				anchor = 0;
				outside = false;
				break;

			case "outer_start":
			case "outer_left":
			case "outer_top":
			case "start":
			case "left":
			case "top":
				anchor = 0;
				outside = true;
				break;

			case "outer_end":
			case "outer_right":
			case "outer_bottom":
			case "end":
			case "right":
			case "bottom":
				anchor = 1;
				outside = true;
				break;

			case "inner_end":
			case "inner_right":
			case "inner_bottom":
				anchor = 1;
				outside = false;
				break;

			default:
				throw new IllegalArgumentException("Unrecognized FlyoutMenuView menuAnchor token \"" + attachmentSpec + "\"");
		}

		if (horizontal) {
			setHorizontalMenuAnchor(anchor);
			setHorizontalMenuAnchorOutside(outside);
		} else {
			setVerticalMenuAnchor(anchor);
			setVerticalMenuAnchorOutside(outside);
		}
	}

	@SuppressLint("ViewConstructor")
	static class MenuOverlayView extends View {

		Paint paint;
		FlyoutMenuView flyoutMenuView;
		Rect flyoutMenuViewRect = new Rect();
		PointF buttonCenter = new PointF();

		Bitmap menuShadowBitmap;
		int menuShadowRadius;
		int menuShadowInset;
		Rect menuShadowSrcRect = new Rect();
		Rect menuShadowDstRect = new Rect();

		Path menuOpenShapePath;
		Path menuFillOvalPath = new Path();
		float menuBackgroundCornerRadius;
		RectF menuOpenRect;
		RectF menuFillOval = new RectF();
		int menuOpenRadius;

		ArrayList<MenuItemLayout> itemLayouts = new ArrayList<>();

		public MenuOverlayView(Context context, FlyoutMenuView flyoutMenuView) {
			super(context);

			// workaround for lack of clip path in API < 18
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
				setLayerType(LAYER_TYPE_SOFTWARE, null);
			}

			paint = new Paint();
			paint.setAntiAlias(true);
			this.flyoutMenuView = flyoutMenuView;

			menuBackgroundCornerRadius = flyoutMenuView.dp2px(MENU_CORNER_RADIUS_DP);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			invalidateMenuFill();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (flyoutMenuView.wasOpenedAsDialog) {
						return true;
					}
					break;

				case MotionEvent.ACTION_UP:
					if (flyoutMenuView.wasOpenedAsDialog) {
						flyoutMenuView.dismissMenuWithMenuItem(findMenuItemAtLocalPosition(event.getX(), event.getY()));
						return true;
					}
					break;
			}

			return super.onTouchEvent(event);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			// get rect for parent
			flyoutMenuView.getGlobalVisibleRect(flyoutMenuViewRect);
			buttonCenter.x = flyoutMenuViewRect.exactCenterX();
			buttonCenter.y = flyoutMenuViewRect.exactCenterY();

			// this is cheap to call every frame since it only does work if it was invalidated previously
			computeMenuFill();
			layoutMenuItems();

			int blankerAlpha = Color.alpha(flyoutMenuView.getShieldColor());
			if (flyoutMenuView.isShieldVisible() && blankerAlpha > 0) {
				blankerAlpha = (int) (255f * ((blankerAlpha / 255f) * flyoutMenuView.menuOpenTransition));
				paint.setColor(flyoutMenuView.getShieldColor());
				paint.setAlpha(blankerAlpha);
				canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
			}

			// we draw the menu during a rescaled slice of the menuOpenTransition [pinion->1]
			final float pinion = 0.15f;
			if (flyoutMenuView.menuOpenTransition >= pinion) {
				drawMenu(canvas, (flyoutMenuView.menuOpenTransition - pinion) / (1f - pinion));
			}
		}

		void drawMenu(Canvas canvas, float alpha) {

			paint.setAlpha(0xFF);
			paint.setStyle(Paint.Style.FILL);

			float pinion = 0.5f;

			if (flyoutMenuView.menuElevation > 0 && alpha > pinion) {
				if (menuShadowBitmap == null) {
					menuShadowBitmap = createMenuShadowBitmap();
				}
				float shadowAlpha = (alpha - pinion) / (1f - pinion);
				int menuShadowOffset = (int) (flyoutMenuView.menuElevation / 2);
				drawMenuShadow(canvas, paint, menuOpenRect, menuShadowBitmap, menuShadowRadius, menuShadowInset, 0, menuShadowOffset, shadowAlpha * shadowAlpha);
			}

			// set clip to the menu shape
			canvas.save();
			canvas.clipPath(menuOpenShapePath);

			// add oval clip for reveal animation
			float radius = (float) flyoutMenuView.buttonRadius + (alpha * (float) (menuOpenRadius - flyoutMenuView.buttonRadius));
			menuFillOval.left = buttonCenter.x - radius;
			menuFillOval.top = buttonCenter.y - radius;
			menuFillOval.right = buttonCenter.x + radius;
			menuFillOval.bottom = buttonCenter.y + radius;

			menuFillOvalPath.reset();
			menuFillOvalPath.addOval(menuFillOval, Path.Direction.CW);
			canvas.clipPath(menuFillOvalPath);

			// fill menu background
			paint.setAlpha(255);
			paint.setColor(flyoutMenuView.menuBackgroundColor);
			canvas.drawRect(menuOpenRect, paint);

			float selectedItemBackgroundColorAlpha = (float) Color.alpha(flyoutMenuView.selectedItemBackgroundColor) / 255f;

			// draw menu items - note: clip path is still active
			for (MenuItemLayout menuItemLayout : itemLayouts) {
				canvas.save();
				canvas.translate(menuOpenRect.left + menuItemLayout.frame.left, menuOpenRect.top + menuItemLayout.frame.top);

				if (menuItemLayout.item == flyoutMenuView.previouslySelectedMenuItem) {
					int itemSelectionAlpha = (int) (selectedItemBackgroundColorAlpha * (1f - flyoutMenuView.selectionTransition) * 255);
					paint.setColor(ColorUtils.setAlphaComponent(flyoutMenuView.selectedItemBackgroundColor, itemSelectionAlpha));

					flyoutMenuView.selectedMenuItemBounds.left = menuItemLayout.bounds.left - flyoutMenuView.itemMargin / 2;
					flyoutMenuView.selectedMenuItemBounds.top = menuItemLayout.bounds.top - flyoutMenuView.itemMargin / 2;
					flyoutMenuView.selectedMenuItemBounds.right = menuItemLayout.bounds.right + flyoutMenuView.itemMargin / 2;
					flyoutMenuView.selectedMenuItemBounds.bottom = menuItemLayout.bounds.bottom + flyoutMenuView.itemMargin / 2;

					menuItemLayout.item.onDraw(canvas, menuItemLayout.bounds, 1f - flyoutMenuView.selectionTransition);
				} else if (menuItemLayout.item == flyoutMenuView.selectedMenuItem) {
					int itemSelectionAlpha = (int) (selectedItemBackgroundColorAlpha * flyoutMenuView.selectionTransition * 255);
					paint.setColor(ColorUtils.setAlphaComponent(flyoutMenuView.selectedItemBackgroundColor, itemSelectionAlpha));

					flyoutMenuView.selectedMenuItemBounds.left = menuItemLayout.bounds.left - flyoutMenuView.itemMargin / 2;
					flyoutMenuView.selectedMenuItemBounds.top = menuItemLayout.bounds.top - flyoutMenuView.itemMargin / 2;
					flyoutMenuView.selectedMenuItemBounds.right = menuItemLayout.bounds.right + flyoutMenuView.itemMargin / 2;
					flyoutMenuView.selectedMenuItemBounds.bottom = menuItemLayout.bounds.bottom + flyoutMenuView.itemMargin / 2;

					canvas.drawRoundRect(flyoutMenuView.selectedMenuItemBounds, menuBackgroundCornerRadius, menuBackgroundCornerRadius, paint);

					menuItemLayout.item.onDraw(canvas, menuItemLayout.bounds, flyoutMenuView.selectionTransition);
				} else {
					menuItemLayout.item.onDraw(canvas, menuItemLayout.bounds, 0);
				}

				canvas.restore();
			}

			canvas.restore();
		}


		void drawMenuShadow(Canvas canvas, Paint paint, RectF rect, Bitmap shadowBitmap, int shadowRadius, int inset, int xOffset, int yOffset, float alpha) {
			canvas.save();
			paint.setAlpha((int) (alpha * 255));

			int left = (int) rect.left - shadowRadius + inset + xOffset;
			int top = (int) rect.top - shadowRadius + inset + yOffset;
			int right = (int) rect.right + shadowRadius - inset + xOffset;
			int bottom = (int) rect.bottom + shadowRadius - inset + yOffset;

			// top left corner
			menuShadowSrcRect.left = 0;
			menuShadowSrcRect.top = 0;
			menuShadowSrcRect.right = shadowRadius;
			menuShadowSrcRect.bottom = shadowRadius;
			menuShadowDstRect.left = left;
			menuShadowDstRect.top = top;
			menuShadowDstRect.right = left + shadowRadius;
			menuShadowDstRect.bottom = top + shadowRadius;
			canvas.drawBitmap(shadowBitmap, menuShadowSrcRect, menuShadowDstRect, paint);

			// top right corner
			menuShadowSrcRect.left = shadowBitmap.getWidth() - shadowRadius;
			menuShadowSrcRect.top = 0;
			menuShadowSrcRect.right = shadowBitmap.getWidth();
			menuShadowSrcRect.bottom = shadowRadius;
			menuShadowDstRect.left = right - shadowRadius;
			menuShadowDstRect.top = top;
			menuShadowDstRect.right = right;
			menuShadowDstRect.bottom = top + shadowRadius;
			canvas.drawBitmap(shadowBitmap, menuShadowSrcRect, menuShadowDstRect, paint);

			// bottom right corner
			menuShadowSrcRect.left = shadowBitmap.getWidth() - shadowRadius;
			menuShadowSrcRect.top = shadowBitmap.getHeight() - shadowRadius;
			menuShadowSrcRect.right = shadowBitmap.getWidth();
			menuShadowSrcRect.bottom = shadowBitmap.getHeight();
			menuShadowDstRect.left = right - shadowRadius;
			menuShadowDstRect.top = bottom - shadowRadius;
			menuShadowDstRect.right = right;
			menuShadowDstRect.bottom = bottom;
			canvas.drawBitmap(shadowBitmap, menuShadowSrcRect, menuShadowDstRect, paint);

			// bottom left corner
			menuShadowSrcRect.left = 0;
			menuShadowSrcRect.top = shadowBitmap.getHeight() - shadowRadius;
			menuShadowSrcRect.right = shadowRadius;
			menuShadowSrcRect.bottom = shadowBitmap.getHeight();
			menuShadowDstRect.left = left;
			menuShadowDstRect.top = bottom - shadowRadius;
			menuShadowDstRect.right = left + shadowRadius;
			menuShadowDstRect.bottom = bottom;
			canvas.drawBitmap(shadowBitmap, menuShadowSrcRect, menuShadowDstRect, paint);

			// draw the top edge
			menuShadowSrcRect.left = shadowRadius;
			menuShadowSrcRect.top = 0;
			menuShadowSrcRect.right = shadowBitmap.getWidth() - shadowRadius;
			menuShadowSrcRect.bottom = shadowRadius;
			menuShadowDstRect.left = left + shadowRadius;
			menuShadowDstRect.top = top;
			menuShadowDstRect.right = right - shadowRadius;
			menuShadowDstRect.bottom = top + shadowRadius;
			canvas.drawBitmap(shadowBitmap, menuShadowSrcRect, menuShadowDstRect, paint);

			// draw the right edge
			menuShadowSrcRect.left = shadowBitmap.getWidth() - shadowRadius;
			menuShadowSrcRect.top = shadowRadius;
			menuShadowSrcRect.right = shadowBitmap.getWidth();
			menuShadowSrcRect.bottom = shadowBitmap.getHeight() - shadowRadius;
			menuShadowDstRect.left = right - shadowRadius;
			menuShadowDstRect.top = top + shadowRadius;
			menuShadowDstRect.right = right;
			menuShadowDstRect.bottom = bottom - shadowRadius;
			canvas.drawBitmap(shadowBitmap, menuShadowSrcRect, menuShadowDstRect, paint);

			// draw the bottom edge
			menuShadowSrcRect.left = shadowRadius;
			menuShadowSrcRect.top = shadowBitmap.getHeight() - shadowRadius;
			menuShadowSrcRect.right = shadowBitmap.getWidth() - shadowRadius;
			menuShadowSrcRect.bottom = shadowBitmap.getHeight();
			menuShadowDstRect.left = left + shadowRadius;
			menuShadowDstRect.top = bottom - shadowRadius;
			menuShadowDstRect.right = right - shadowRadius;
			menuShadowDstRect.bottom = bottom;
			canvas.drawBitmap(shadowBitmap, menuShadowSrcRect, menuShadowDstRect, paint);

			// draw center
			menuShadowSrcRect.left = shadowRadius;
			menuShadowSrcRect.top = shadowRadius;
			menuShadowSrcRect.right = shadowBitmap.getWidth() - shadowRadius;
			menuShadowSrcRect.bottom = shadowBitmap.getHeight() - shadowRadius;
			menuShadowDstRect.left = left + shadowRadius;
			menuShadowDstRect.top = top + shadowRadius;
			menuShadowDstRect.right = right - shadowRadius;
			menuShadowDstRect.bottom = bottom - shadowRadius;
			canvas.drawBitmap(shadowBitmap, menuShadowSrcRect, menuShadowDstRect, paint);

			// draw the left edge
			menuShadowSrcRect.left = 0;
			menuShadowSrcRect.top = shadowRadius;
			menuShadowSrcRect.right = shadowRadius;
			menuShadowSrcRect.bottom = shadowBitmap.getHeight() - shadowRadius;
			menuShadowDstRect.left = left;
			menuShadowDstRect.top = top + shadowRadius;
			menuShadowDstRect.right = left + shadowRadius;
			menuShadowDstRect.bottom = bottom - shadowRadius;
			canvas.drawBitmap(shadowBitmap, menuShadowSrcRect, menuShadowDstRect, paint);


			// restore drawing state
			canvas.restore();
			paint.setAlpha(255);
		}

		Bitmap createMenuShadowBitmap() {
			menuShadowRadius = (int) flyoutMenuView.menuElevation * 2;
			menuShadowInset = menuShadowRadius / 2;
			int size = 2 * menuShadowRadius + 1;
			Bitmap shadowBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
			shadowBitmap.eraseColor(0x0); // clear

			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setShader(new RadialGradient(
					menuShadowRadius + 1,
					menuShadowRadius + 1,
					menuShadowRadius,
					ColorUtils.setAlphaComponent(SHADOW_COLOR, SHADOW_ALPHA),
					ColorUtils.setAlphaComponent(SHADOW_COLOR, 0),
					Shader.TileMode.CLAMP));

			Canvas canvas = new Canvas(shadowBitmap);
			canvas.drawRect(0, 0, size, size, paint);

			return shadowBitmap;
		}

		void invalidateMenuFill() {
			menuOpenRect = null;
			menuOpenShapePath = null;
			menuOpenRadius = 0;
		}

		boolean needsComputeMenuFill() {
			return menuOpenRadius <= 0;
		}

		void computeMenuFill() {
			if (flyoutMenuView.adapter != null && flyoutMenuView.layout != null && needsComputeMenuFill()) {
				Size menuSize = flyoutMenuView.layout.getMinimumSizeForItems(flyoutMenuView.adapter.getCount(), flyoutMenuView.itemWidth, flyoutMenuView.itemHeight, flyoutMenuView.itemMargin);

				float buttonRadius = flyoutMenuView.buttonRadius;
				float buttonLeft = buttonCenter.x - buttonRadius;
				float buttonRight = buttonCenter.x + buttonRadius;
				float buttonTop = buttonCenter.y - buttonRadius;
				float buttonBottom = buttonCenter.y + buttonRadius;

				float leftMin;
				float leftMax;
				float topMin;
				float topMax;

				if (flyoutMenuView.horizontalMenuAnchorOutside) {
					leftMin = buttonLeft - menuSize.width - flyoutMenuView.menuMargin;
					leftMax = buttonRight + flyoutMenuView.menuMargin;
				} else {
					leftMin = buttonLeft;
					leftMax = buttonRight - menuSize.width;
				}

				if (flyoutMenuView.verticalMenuAnchorOutside) {
					topMin = buttonTop - menuSize.height - flyoutMenuView.menuMargin;
					topMax = buttonBottom + flyoutMenuView.menuMargin;
				} else {
					topMin = buttonTop;
					topMax = buttonBottom - menuSize.height;
				}

				int menuLeft = (int) (leftMin + flyoutMenuView.horizontalMenuAnchor * (leftMax - leftMin));
				int menuTop = (int) (topMin + flyoutMenuView.verticalMenuAnchor * (topMax - topMin));

				// the frame of the menu, when open
				menuOpenRect = new RectF(menuLeft, menuTop, menuLeft + menuSize.width, menuTop + menuSize.height);
				sanitizeMenuPosition(menuOpenRect);

				// the round rect we'll use as clipping mask for the animated fill
				menuOpenShapePath = new Path();
				menuOpenShapePath.addRoundRect(menuOpenRect, menuBackgroundCornerRadius, menuBackgroundCornerRadius, Path.Direction.CW);

				// compute the circular radius to fill the menuOpenShapePath
				float a = distanceToButtonCenter(menuOpenRect.left, menuOpenRect.top);
				float b = distanceToButtonCenter(menuOpenRect.right, menuOpenRect.top);
				float c = distanceToButtonCenter(menuOpenRect.right, menuOpenRect.bottom);
				float d = distanceToButtonCenter(menuOpenRect.left, menuOpenRect.bottom);
				menuOpenRadius = (int) Math.max(a, Math.max(b, Math.max(c, d)));
			}
		}

		/**
		 * Attempt to position menuRect such that it does not fall off any screen edge or overlap the navigation bar
		 *
		 * @param menuRect the menuRect to fit to
		 */
		void sanitizeMenuPosition(RectF menuRect) {
			Point appUsableScreenSize = getAppUsableScreenSize();
			float margin = flyoutMenuView.menuMargin;
			float right = appUsableScreenSize.x - margin;
			float bottom = appUsableScreenSize.y - margin;

			if (menuRect.left < margin) {
				float dx = margin - menuRect.left;
				menuRect.offset(dx, 0);
			}

			if (menuRect.top < margin) {
				float dy = margin - menuRect.top;
				menuRect.offset(0, dy);
			}

			if (menuRect.right > right) {
				float dx = right - menuRect.right;
				menuRect.offset(dx, 0);
			}

			if (menuRect.bottom > bottom) {
				float dy = bottom - menuRect.bottom;
				menuRect.offset(0, dy);
			}
		}

		float distanceToButtonCenter(float x, float y) {
			return (float) Math.sqrt(
					(x - buttonCenter.x) * (x - buttonCenter.x) +
							(y - buttonCenter.y) * (y - buttonCenter.y));
		}


		void invalidateMenuItemLayout() {
			itemLayouts.clear();

			// a new layout affects positioning of the menu as well
			invalidateMenuFill();
		}

		boolean needsLayoutMenuItems() {
			return itemLayouts.isEmpty();
		}

		void layoutMenuItems() {
			if (flyoutMenuView.adapter != null && flyoutMenuView.layout != null && needsLayoutMenuItems()) {

				for (int i = 0, n = flyoutMenuView.adapter.getCount(); i < n; i++) {
					MenuItemLayout itemLayout = new MenuItemLayout();
					itemLayout.item = flyoutMenuView.adapter.getItem(i);
					itemLayout.itemAdapterPosition = i;
					itemLayout.frame = new RectF(flyoutMenuView.layout.getLayoutRectForItem(i, flyoutMenuView.itemWidth, flyoutMenuView.itemHeight, flyoutMenuView.itemMargin));
					itemLayout.bounds = new RectF(0, 0, flyoutMenuView.itemWidth, flyoutMenuView.itemHeight);

					itemLayouts.add(itemLayout);
				}

				invalidateMenuFill();
			}
		}

		/**
		 * Find the MenuItem under a given x,y location in the coordinate system of the parent FlyoutMenuView
		 *
		 * @param x the x coordinate location (in the coordinate system of the parent FlyoutMenuView)
		 * @param y the y coordinate location (in the coordinate system of the parent FlyoutMenuView)
		 * @return the MenuItem under that location, or null if none
		 */
		@Nullable
		MenuItem findMenuItemAtFlyoutMenuViewRelativePosition(float x, float y) {
			return findMenuItemAtLocalPosition(flyoutMenuViewRect.left + x, flyoutMenuViewRect.top + y);
		}

		@Nullable
		MenuItem findMenuItemAtLocalPosition(float x, float y) {

			// convert the point from the MenuOverlayView's coordinate system to the menu's
			float menuX = x - menuOpenRect.left;
			float menuY = y - menuOpenRect.top;

			for (MenuItemLayout menuItemLayout : itemLayouts) {
				if (menuX >= menuItemLayout.frame.left && menuX <= menuItemLayout.frame.right &&
						menuY >= menuItemLayout.frame.top && menuY <= menuItemLayout.frame.bottom) {
					return menuItemLayout.item;
				}
			}

			return null;
		}

		/**
		 * Get the usable screen size (everything but the navigation bar)
		 * http://stackoverflow.com/questions/20264268/how-to-get-height-and-width-of-navigation-bar-programmatically/29938139#29938139
		 * @return the size of the usable area of the screen
		 */
		Point getAppUsableScreenSize() {
			WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			Display display = windowManager.getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			return size;
		}
	}
}
