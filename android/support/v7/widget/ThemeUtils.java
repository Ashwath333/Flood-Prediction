package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.util.TypedValue;

class ThemeUtils {
    static final int[] ACTIVATED_STATE_SET;
    static final int[] CHECKED_STATE_SET;
    static final int[] DISABLED_STATE_SET;
    static final int[] EMPTY_STATE_SET;
    static final int[] FOCUSED_STATE_SET;
    static final int[] NOT_PRESSED_OR_FOCUSED_STATE_SET;
    static final int[] PRESSED_STATE_SET;
    static final int[] SELECTED_STATE_SET;
    private static final int[] TEMP_ARRAY;
    private static final ThreadLocal<TypedValue> TL_TYPED_VALUE;

    ThemeUtils() {
    }

    static {
        TL_TYPED_VALUE = new ThreadLocal();
        DISABLED_STATE_SET = new int[]{-16842910};
        FOCUSED_STATE_SET = new int[]{16842908};
        ACTIVATED_STATE_SET = new int[]{16843518};
        PRESSED_STATE_SET = new int[]{16842919};
        CHECKED_STATE_SET = new int[]{16842912};
        SELECTED_STATE_SET = new int[]{16842913};
        NOT_PRESSED_OR_FOCUSED_STATE_SET = new int[]{-16842919, -16842908};
        EMPTY_STATE_SET = new int[0];
        TEMP_ARRAY = new int[1];
    }

    public static ColorStateList createDisabledStateList(int textColor, int disabledTextColor) {
        states = new int[2][];
        int[] colors = new int[]{DISABLED_STATE_SET, disabledTextColor};
        int i = 0 + 1;
        states[i] = EMPTY_STATE_SET;
        colors[i] = textColor;
        i++;
        return new ColorStateList(states, colors);
    }

    public static int getThemeAttrColor(Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, null, TEMP_ARRAY);
        try {
            int color = a.getColor(0, 0);
            return color;
        } finally {
            a.recycle();
        }
    }

    public static ColorStateList getThemeAttrColorStateList(Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, null, TEMP_ARRAY);
        try {
            ColorStateList colorStateList = a.getColorStateList(0);
            return colorStateList;
        } finally {
            a.recycle();
        }
    }

    public static int getDisabledThemeAttrColor(Context context, int attr) {
        ColorStateList csl = getThemeAttrColorStateList(context, attr);
        if (csl != null && csl.isStateful()) {
            return csl.getColorForState(DISABLED_STATE_SET, csl.getDefaultColor());
        }
        TypedValue tv = getTypedValue();
        context.getTheme().resolveAttribute(16842803, tv, true);
        return getThemeAttrColor(context, attr, tv.getFloat());
    }

    private static TypedValue getTypedValue() {
        TypedValue typedValue = (TypedValue) TL_TYPED_VALUE.get();
        if (typedValue != null) {
            return typedValue;
        }
        typedValue = new TypedValue();
        TL_TYPED_VALUE.set(typedValue);
        return typedValue;
    }

    static int getThemeAttrColor(Context context, int attr, float alpha) {
        int color = getThemeAttrColor(context, attr);
        return ColorUtils.setAlphaComponent(color, Math.round(((float) Color.alpha(color)) * alpha));
    }
}
