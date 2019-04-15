package org.zakariya.flyoutmenudemo;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.zakariya.flyoutmenu.FlyoutMenuView;
import org.zakariya.flyoutmenudemo.menus.EmojiFlyoutMenu;
import org.zakariya.flyoutmenudemo.menus.IconFlyoutMenu;
import org.zakariya.flyoutmenudemo.menus.PaletteFlyoutMenu;
import org.zakariya.flyoutmenudemo.menus.ToolFlyoutMenu;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

public class MainActivity extends AppCompatActivity {

    private static float BRUSH_SCALE = 16;

    @BindView(R.id.toolSelectorFlyoutMenu)
    FlyoutMenuView toolSelectorFlyoutMenu;

    @BindView(R.id.paletteFlyoutMenu)
    FlyoutMenuView paletteFlyoutMenu;

    @BindView(R.id.smileyFlyoutMenu)
    FlyoutMenuView smileyFlyoutMenu;

    @BindView(R.id.iconFlyoutMenu)
    FlyoutMenuView iconFlyoutMenu;

    @BindView(R.id.brushStateTextView)
    TextView brushStateTextView;


    @State
    int toolFlyoutMenuSelectionId = 0;

    @State
    int paletteFlyoutMenuSelectionId = 0;

    @State
    int smileyFlyoutMenuSelectionId = 0;

    @State
    int iconFlyoutMenuSelectionId = 0;


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
        configureSmileyFlyoutMenu();
        configureIconFlyoutMenu();

        toolSelectorFlyoutMenu.setSelectedMenuItemById(toolFlyoutMenuSelectionId);
        paletteFlyoutMenu.setSelectedMenuItemById(paletteFlyoutMenuSelectionId);
        smileyFlyoutMenu.setSelectedMenuItemById(smileyFlyoutMenuSelectionId);
        iconFlyoutMenu.setSelectedMenuItemById(iconFlyoutMenuSelectionId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Icepick.saveInstanceState(this, outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        // if any menus are open, dismiss them and consume event.
        boolean handled = false;
        if (toolSelectorFlyoutMenu.dismiss() ||
                paletteFlyoutMenu.dismiss() ||
                smileyFlyoutMenu.dismiss() ||
                iconFlyoutMenu.dismiss()
        ) {
            handled = true;
        }

        if (!handled) {
            super.onBackPressed();
        }
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
    void updateBrushStateTextView() {
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
        List<ToolFlyoutMenu.MenuItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float size = (float) (i + 1) / (float) count;
            items.add(new ToolFlyoutMenu.MenuItem(items.size(), size, false, alphaCheckerSize, alphaCheckerColor, toolColor));
        }

        for (int i = 0; i < count; i++) {
            float size = (float) (i + 1) / (float) count;
            items.add(new ToolFlyoutMenu.MenuItem(items.size(), size, true, alphaCheckerSize, alphaCheckerColor, toolColor));
        }

        toolSelectorFlyoutMenu.setLayout(new FlyoutMenuView.GridLayout(count, FlyoutMenuView.GridLayout.UNSPECIFIED));
        toolSelectorFlyoutMenu.setAdapter(new FlyoutMenuView.ArrayAdapter<>(items));

        final ToolFlyoutMenu.ButtonRenderer toolButtonRenderer = new ToolFlyoutMenu.ButtonRenderer(toolInset, 1, false, alphaCheckerSize, alphaCheckerColor, toolColor);
        toolSelectorFlyoutMenu.setButtonRenderer(toolButtonRenderer);

        toolSelectorFlyoutMenu.setSelectionListener(new FlyoutMenuView.SelectionListener() {
            @Override
            public void onItemSelected(FlyoutMenuView flyoutMenuView, FlyoutMenuView.MenuItem item) {
                toolFlyoutMenuSelectionId = item.getId();
                ToolFlyoutMenu.MenuItem toolMenuItem = (ToolFlyoutMenu.MenuItem) item;

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
        float[] hsl = {0, 0, 0};
        float cornerRadius = 0;
        if (paletteFlyoutMenu.getItemMargin() > 0) {
            cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
        }

        paletteFlyoutMenu.setLayout(new FlyoutMenuView.GridLayout(cols, FlyoutMenuView.GridLayout.UNSPECIFIED));

        List<PaletteFlyoutMenu.MenuItem> items = new ArrayList<>();
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
                items.add(new PaletteFlyoutMenu.MenuItem(items.size(), ColorUtils.HSLToColor(hsl), cornerRadius));
            }
        }

        paletteFlyoutMenu.setAdapter(new FlyoutMenuView.ArrayAdapter<>(items));

        float inset = getResources().getDimension(R.dimen.palette_swatch_inset);
        final PaletteFlyoutMenu.ButtonRenderer renderer = new PaletteFlyoutMenu.ButtonRenderer(inset);
        paletteFlyoutMenu.setButtonRenderer(renderer);

        paletteFlyoutMenu.setSelectionListener(new FlyoutMenuView.SelectionListener() {
            @Override
            public void onItemSelected(FlyoutMenuView flyoutMenuView, FlyoutMenuView.MenuItem item) {
                paletteFlyoutMenuSelectionId = item.getId();
                @ColorInt int color = ((PaletteFlyoutMenu.MenuItem) item).getColor();
                renderer.setCurrentColor(color);
                setBrushColor(color);
            }

            @Override
            public void onDismissWithoutSelection(FlyoutMenuView flyoutMenuView) {
            }
        });
    }

    void configureIconFlyoutMenu() {
        float inset = getResources().getDimension(R.dimen.icon_inset);
        List<IconFlyoutMenu.MenuItem> menuItems = new ArrayList<>();
        int[] drawables = {
                R.drawable.ic_android_black_24dp,
                R.drawable.ic_ac_unit_black_24dp,
                R.drawable.ic_airport_shuttle_black_24dp,
                R.drawable.cat
        };

        for (int drawableId : drawables) {
            Drawable drawable = getResources().getDrawable(drawableId);
            menuItems.add(new IconFlyoutMenu.MenuItem(menuItems.size(), drawable, inset));
        }

        iconFlyoutMenu.setLayout(new FlyoutMenuView.GridLayout(2, FlyoutMenuView.GridLayout.UNSPECIFIED));
        iconFlyoutMenu.setAdapter(new FlyoutMenuView.ArrayAdapter<>(menuItems));

        final IconFlyoutMenu.ButtonRenderer renderer = new IconFlyoutMenu.ButtonRenderer(
                getResources().getDrawable(R.drawable.ic_android_black_24dp),
                inset);
        iconFlyoutMenu.setButtonRenderer(renderer);

        iconFlyoutMenu.setSelectionListener(new FlyoutMenuView.SelectionListener() {
            @Override
            public void onItemSelected(FlyoutMenuView flyoutMenuView, FlyoutMenuView.MenuItem item) {
                iconFlyoutMenuSelectionId = item.getId();
                renderer.setIcon(((IconFlyoutMenu.MenuItem) item).getIcon());
            }

            @Override
            public void onDismissWithoutSelection(FlyoutMenuView flyoutMenuView) {
            }
        });
    }

    void configureSmileyFlyoutMenu() {

        int[] emojiCodes = {
                0x1F60D, //smiling face heart shaped eyes
                0x1F605, // smiling face with open mouth and cold sweat
                0x1F60A, // smiling face
                0x1F613, // face with cold sweat
                0x1F61E, // disappointed face
                0x1F620, // angry face
                0x1F62D, // loudly crying face
                0x1F4A9, // pile of poo
        };

        int[] charCodes = {
                0x0041,
                0x0042,
                0x0043,
                0x0044,
                0x0045,
                0x0046,
                0x0047,
                0x0048,
        };

        final boolean canUseEmoji = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;

        @ColorInt int color = ContextCompat.getColor(this, R.color.smileyMenuCharColor);
        float fontSizeInMenu = getResources().getDimension(R.dimen.smiley_menu_item_size) * 0.5f;
        float fontSizeInButton = getResources().getDimension(R.dimen.flyout_menu_button_size) * 0.5f;

        List<EmojiFlyoutMenu.MenuItem> menuItems = new ArrayList<>();

        for (int i = 0; i < emojiCodes.length; i++) {
            int code = canUseEmoji ? emojiCodes[i] : charCodes[i];
            menuItems.add(new EmojiFlyoutMenu.MenuItem(menuItems.size(), code, fontSizeInMenu, color));
        }

        smileyFlyoutMenu.setLayout(new FlyoutMenuView.GridLayout(2, FlyoutMenuView.GridLayout.UNSPECIFIED));
        smileyFlyoutMenu.setAdapter(new FlyoutMenuView.ArrayAdapter<>(menuItems));

        final EmojiFlyoutMenu.ButtonRenderer renderer = new EmojiFlyoutMenu.ButtonRenderer(emojiCodes[0], fontSizeInButton, color);
        smileyFlyoutMenu.setButtonRenderer(renderer);

        smileyFlyoutMenu.setSelectionListener(new FlyoutMenuView.SelectionListener() {
            @Override
            public void onItemSelected(FlyoutMenuView flyoutMenuView, FlyoutMenuView.MenuItem item) {
                smileyFlyoutMenuSelectionId = item.getId();
                renderer.setEmojiCode(((EmojiFlyoutMenu.MenuItem) item).getEmojiCode());
            }

            @Override
            public void onDismissWithoutSelection(FlyoutMenuView flyoutMenuView) {
            }
        });

    }

}
