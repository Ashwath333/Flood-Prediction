package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.LruCache;
import android.support.v7.appcompat.C0114R;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.util.Xml;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class AppCompatDrawableManager {
    private static final int[] COLORFILTER_COLOR_BACKGROUND_MULTIPLY;
    private static final int[] COLORFILTER_COLOR_CONTROL_ACTIVATED;
    private static final int[] COLORFILTER_TINT_COLOR_CONTROL_NORMAL;
    private static final ColorFilterLruCache COLOR_FILTER_CACHE;
    private static final boolean DEBUG = false;
    private static final Mode DEFAULT_MODE;
    private static AppCompatDrawableManager INSTANCE = null;
    private static final String PLATFORM_VD_CLAZZ = "android.graphics.drawable.VectorDrawable";
    private static final String SKIP_DRAWABLE_TAG = "appcompat_skip_skip";
    private static final String TAG = "AppCompatDrawableManager";
    private static final int[] TINT_CHECKABLE_BUTTON_LIST;
    private static final int[] TINT_COLOR_CONTROL_NORMAL;
    private static final int[] TINT_COLOR_CONTROL_STATE_LIST;
    private ArrayMap<String, InflateDelegate> mDelegates;
    private final Object mDrawableCacheLock;
    private final WeakHashMap<Context, LongSparseArray<WeakReference<ConstantState>>> mDrawableCaches;
    private boolean mHasCheckedVectorDrawableSetup;
    private SparseArray<String> mKnownDrawableIdTags;
    private WeakHashMap<Context, SparseArray<ColorStateList>> mTintLists;
    private TypedValue mTypedValue;

    private interface InflateDelegate {
        Drawable createFromXmlInner(@NonNull Context context, @NonNull XmlPullParser xmlPullParser, @NonNull AttributeSet attributeSet, @Nullable Theme theme);
    }

    private static class AvdcInflateDelegate implements InflateDelegate {
        AvdcInflateDelegate() {
        }

        public Drawable createFromXmlInner(@NonNull Context context, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Theme theme) {
            try {
                return AnimatedVectorDrawableCompat.createFromXmlInner(context, context.getResources(), parser, attrs, theme);
            } catch (Exception e) {
                Log.e("AvdcInflateDelegate", "Exception while inflating <animated-vector>", e);
                return null;
            }
        }
    }

    private static class ColorFilterLruCache extends LruCache<Integer, PorterDuffColorFilter> {
        public ColorFilterLruCache(int maxSize) {
            super(maxSize);
        }

        PorterDuffColorFilter get(int color, Mode mode) {
            return (PorterDuffColorFilter) get(Integer.valueOf(generateCacheKey(color, mode)));
        }

        PorterDuffColorFilter put(int color, Mode mode, PorterDuffColorFilter filter) {
            return (PorterDuffColorFilter) put(Integer.valueOf(generateCacheKey(color, mode)), filter);
        }

        private static int generateCacheKey(int color, Mode mode) {
            return ((color + 31) * 31) + mode.hashCode();
        }
    }

    private static class VdcInflateDelegate implements InflateDelegate {
        VdcInflateDelegate() {
        }

        public Drawable createFromXmlInner(@NonNull Context context, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Theme theme) {
            try {
                return VectorDrawableCompat.createFromXmlInner(context.getResources(), parser, attrs, theme);
            } catch (Exception e) {
                Log.e("VdcInflateDelegate", "Exception while inflating <vector>", e);
                return null;
            }
        }
    }

    public AppCompatDrawableManager() {
        this.mDrawableCacheLock = new Object();
        this.mDrawableCaches = new WeakHashMap(0);
    }

    static {
        DEFAULT_MODE = Mode.SRC_IN;
        COLOR_FILTER_CACHE = new ColorFilterLruCache(6);
        COLORFILTER_TINT_COLOR_CONTROL_NORMAL = new int[]{C0114R.drawable.abc_textfield_search_default_mtrl_alpha, C0114R.drawable.abc_textfield_default_mtrl_alpha, C0114R.drawable.abc_ab_share_pack_mtrl_alpha};
        TINT_COLOR_CONTROL_NORMAL = new int[]{C0114R.drawable.abc_ic_commit_search_api_mtrl_alpha, C0114R.drawable.abc_seekbar_tick_mark_material, C0114R.drawable.abc_ic_menu_share_mtrl_alpha, C0114R.drawable.abc_ic_menu_copy_mtrl_am_alpha, C0114R.drawable.abc_ic_menu_cut_mtrl_alpha, C0114R.drawable.abc_ic_menu_selectall_mtrl_alpha, C0114R.drawable.abc_ic_menu_paste_mtrl_am_alpha};
        COLORFILTER_COLOR_CONTROL_ACTIVATED = new int[]{C0114R.drawable.abc_textfield_activated_mtrl_alpha, C0114R.drawable.abc_textfield_search_activated_mtrl_alpha, C0114R.drawable.abc_cab_background_top_mtrl_alpha, C0114R.drawable.abc_text_cursor_material, C0114R.drawable.abc_text_select_handle_left_mtrl_dark, C0114R.drawable.abc_text_select_handle_middle_mtrl_dark, C0114R.drawable.abc_text_select_handle_right_mtrl_dark, C0114R.drawable.abc_text_select_handle_left_mtrl_light, C0114R.drawable.abc_text_select_handle_middle_mtrl_light, C0114R.drawable.abc_text_select_handle_right_mtrl_light};
        COLORFILTER_COLOR_BACKGROUND_MULTIPLY = new int[]{C0114R.drawable.abc_popup_background_mtrl_mult, C0114R.drawable.abc_cab_background_internal_bg, C0114R.drawable.abc_menu_hardkey_panel_mtrl_mult};
        TINT_COLOR_CONTROL_STATE_LIST = new int[]{C0114R.drawable.abc_tab_indicator_material, C0114R.drawable.abc_textfield_search_material};
        TINT_CHECKABLE_BUTTON_LIST = new int[]{C0114R.drawable.abc_btn_check_material, C0114R.drawable.abc_btn_radio_material};
    }

    public static AppCompatDrawableManager get() {
        if (INSTANCE == null) {
            INSTANCE = new AppCompatDrawableManager();
            installDefaultInflateDelegates(INSTANCE);
        }
        return INSTANCE;
    }

    private static void installDefaultInflateDelegates(@NonNull AppCompatDrawableManager manager) {
        int sdk = VERSION.SDK_INT;
        if (sdk < 23) {
            manager.addDelegate("vector", new VdcInflateDelegate());
            if (sdk >= 11) {
                manager.addDelegate("animated-vector", new AvdcInflateDelegate());
            }
        }
    }

    public Drawable getDrawable(@NonNull Context context, @DrawableRes int resId) {
        return getDrawable(context, resId, DEBUG);
    }

    Drawable getDrawable(@NonNull Context context, @DrawableRes int resId, boolean failIfNotKnown) {
        checkVectorDrawableSetup(context);
        Drawable drawable = loadDrawableFromDelegates(context, resId);
        if (drawable == null) {
            drawable = createDrawableIfNeeded(context, resId);
        }
        if (drawable == null) {
            drawable = ContextCompat.getDrawable(context, resId);
        }
        if (drawable != null) {
            drawable = tintDrawable(context, resId, failIfNotKnown, drawable);
        }
        if (drawable != null) {
            DrawableUtils.fixDrawable(drawable);
        }
        return drawable;
    }

    public void onConfigurationChanged(@NonNull Context context) {
        synchronized (this.mDrawableCacheLock) {
            LongSparseArray<WeakReference<ConstantState>> cache = (LongSparseArray) this.mDrawableCaches.get(context);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    private static long createCacheKey(TypedValue tv) {
        return (((long) tv.assetCookie) << 32) | ((long) tv.data);
    }

    private Drawable createDrawableIfNeeded(@NonNull Context context, @DrawableRes int resId) {
        if (this.mTypedValue == null) {
            this.mTypedValue = new TypedValue();
        }
        TypedValue tv = this.mTypedValue;
        context.getResources().getValue(resId, tv, true);
        long key = createCacheKey(tv);
        Drawable dr = getCachedDrawable(context, key);
        if (dr != null) {
            return dr;
        }
        if (resId == C0114R.drawable.abc_cab_background_top_material) {
            dr = new LayerDrawable(new Drawable[]{getDrawable(context, C0114R.drawable.abc_cab_background_internal_bg), getDrawable(context, C0114R.drawable.abc_cab_background_top_mtrl_alpha)});
        }
        if (dr != null) {
            dr.setChangingConfigurations(tv.changingConfigurations);
            addDrawableToCache(context, key, dr);
        }
        return dr;
    }

    private Drawable tintDrawable(@NonNull Context context, @DrawableRes int resId, boolean failIfNotKnown, @NonNull Drawable drawable) {
        ColorStateList tintList = getTintList(context, resId);
        if (tintList != null) {
            if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
                drawable = drawable.mutate();
            }
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(drawable, tintList);
            Mode tintMode = getTintMode(resId);
            if (tintMode == null) {
                return drawable;
            }
            DrawableCompat.setTintMode(drawable, tintMode);
            return drawable;
        } else if (resId == C0114R.drawable.abc_seekbar_track_material) {
            ld = (LayerDrawable) drawable;
            setPorterDuffColorFilter(ld.findDrawableByLayerId(16908288), ThemeUtils.getThemeAttrColor(context, C0114R.attr.colorControlNormal), DEFAULT_MODE);
            setPorterDuffColorFilter(ld.findDrawableByLayerId(16908303), ThemeUtils.getThemeAttrColor(context, C0114R.attr.colorControlNormal), DEFAULT_MODE);
            setPorterDuffColorFilter(ld.findDrawableByLayerId(16908301), ThemeUtils.getThemeAttrColor(context, C0114R.attr.colorControlActivated), DEFAULT_MODE);
            return drawable;
        } else if (resId == C0114R.drawable.abc_ratingbar_material || resId == C0114R.drawable.abc_ratingbar_indicator_material || resId == C0114R.drawable.abc_ratingbar_small_material) {
            ld = (LayerDrawable) drawable;
            setPorterDuffColorFilter(ld.findDrawableByLayerId(16908288), ThemeUtils.getDisabledThemeAttrColor(context, C0114R.attr.colorControlNormal), DEFAULT_MODE);
            setPorterDuffColorFilter(ld.findDrawableByLayerId(16908303), ThemeUtils.getThemeAttrColor(context, C0114R.attr.colorControlActivated), DEFAULT_MODE);
            setPorterDuffColorFilter(ld.findDrawableByLayerId(16908301), ThemeUtils.getThemeAttrColor(context, C0114R.attr.colorControlActivated), DEFAULT_MODE);
            return drawable;
        } else if (tintDrawableUsingColorFilter(context, resId, drawable) || !failIfNotKnown) {
            return drawable;
        } else {
            return null;
        }
    }

    private Drawable loadDrawableFromDelegates(@NonNull Context context, @DrawableRes int resId) {
        if (this.mDelegates == null || this.mDelegates.isEmpty()) {
            return null;
        }
        if (this.mKnownDrawableIdTags != null) {
            String cachedTagName = (String) this.mKnownDrawableIdTags.get(resId);
            if (SKIP_DRAWABLE_TAG.equals(cachedTagName) || (cachedTagName != null && this.mDelegates.get(cachedTagName) == null)) {
                return null;
            }
        }
        this.mKnownDrawableIdTags = new SparseArray();
        if (this.mTypedValue == null) {
            this.mTypedValue = new TypedValue();
        }
        TypedValue tv = this.mTypedValue;
        Resources res = context.getResources();
        res.getValue(resId, tv, true);
        long key = createCacheKey(tv);
        Drawable dr = getCachedDrawable(context, key);
        if (dr != null) {
            return dr;
        }
        if (tv.string != null && tv.string.toString().endsWith(".xml")) {
            try {
                int type;
                XmlPullParser parser = res.getXml(resId);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    throw new XmlPullParserException("No start tag found");
                }
                String tagName = parser.getName();
                this.mKnownDrawableIdTags.append(resId, tagName);
                InflateDelegate delegate = (InflateDelegate) this.mDelegates.get(tagName);
                if (delegate != null) {
                    dr = delegate.createFromXmlInner(context, parser, attrs, context.getTheme());
                }
                if (dr != null) {
                    dr.setChangingConfigurations(tv.changingConfigurations);
                    if (addDrawableToCache(context, key, dr)) {
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception while inflating drawable", e);
            }
        }
        if (dr != null) {
            return dr;
        }
        this.mKnownDrawableIdTags.append(resId, SKIP_DRAWABLE_TAG);
        return dr;
    }

    private Drawable getCachedDrawable(@NonNull Context context, long key) {
        Drawable drawable = null;
        synchronized (this.mDrawableCacheLock) {
            LongSparseArray<WeakReference<ConstantState>> cache = (LongSparseArray) this.mDrawableCaches.get(context);
            if (cache == null) {
            } else {
                WeakReference<ConstantState> wr = (WeakReference) cache.get(key);
                if (wr != null) {
                    ConstantState entry = (ConstantState) wr.get();
                    if (entry != null) {
                        drawable = entry.newDrawable(context.getResources());
                    } else {
                        cache.delete(key);
                    }
                }
            }
        }
        return drawable;
    }

    private boolean addDrawableToCache(@NonNull Context context, long key, @NonNull Drawable drawable) {
        ConstantState cs = drawable.getConstantState();
        if (cs == null) {
            return DEBUG;
        }
        synchronized (this.mDrawableCacheLock) {
            LongSparseArray<WeakReference<ConstantState>> cache = (LongSparseArray) this.mDrawableCaches.get(context);
            if (cache == null) {
                cache = new LongSparseArray();
                this.mDrawableCaches.put(context, cache);
            }
            cache.put(key, new WeakReference(cs));
        }
        return true;
    }

    Drawable onDrawableLoadedFromResources(@NonNull Context context, @NonNull VectorEnabledTintResources resources, @DrawableRes int resId) {
        Drawable drawable = loadDrawableFromDelegates(context, resId);
        if (drawable == null) {
            drawable = resources.superGetDrawable(resId);
        }
        if (drawable != null) {
            return tintDrawable(context, resId, DEBUG, drawable);
        }
        return null;
    }

    static boolean tintDrawableUsingColorFilter(@NonNull Context context, @DrawableRes int resId, @NonNull Drawable drawable) {
        Mode tintMode = DEFAULT_MODE;
        boolean colorAttrSet = DEBUG;
        int colorAttr = 0;
        int alpha = -1;
        if (arrayContains(COLORFILTER_TINT_COLOR_CONTROL_NORMAL, resId)) {
            colorAttr = C0114R.attr.colorControlNormal;
            colorAttrSet = true;
        } else if (arrayContains(COLORFILTER_COLOR_CONTROL_ACTIVATED, resId)) {
            colorAttr = C0114R.attr.colorControlActivated;
            colorAttrSet = true;
        } else if (arrayContains(COLORFILTER_COLOR_BACKGROUND_MULTIPLY, resId)) {
            colorAttr = 16842801;
            colorAttrSet = true;
            tintMode = Mode.MULTIPLY;
        } else if (resId == C0114R.drawable.abc_list_divider_mtrl_alpha) {
            colorAttr = 16842800;
            colorAttrSet = true;
            alpha = Math.round(40.8f);
        } else if (resId == C0114R.drawable.abc_dialog_material_background) {
            colorAttr = 16842801;
            colorAttrSet = true;
        }
        if (!colorAttrSet) {
            return DEBUG;
        }
        if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
            drawable = drawable.mutate();
        }
        drawable.setColorFilter(getPorterDuffColorFilter(ThemeUtils.getThemeAttrColor(context, colorAttr), tintMode));
        if (alpha != -1) {
            drawable.setAlpha(alpha);
        }
        return true;
    }

    private void addDelegate(@NonNull String tagName, @NonNull InflateDelegate delegate) {
        if (this.mDelegates == null) {
            this.mDelegates = new ArrayMap();
        }
        this.mDelegates.put(tagName, delegate);
    }

    private void removeDelegate(@NonNull String tagName, @NonNull InflateDelegate delegate) {
        if (this.mDelegates != null && this.mDelegates.get(tagName) == delegate) {
            this.mDelegates.remove(tagName);
        }
    }

    private static boolean arrayContains(int[] array, int value) {
        for (int id : array) {
            if (id == value) {
                return true;
            }
        }
        return DEBUG;
    }

    static Mode getTintMode(int resId) {
        if (resId == C0114R.drawable.abc_switch_thumb_material) {
            return Mode.MULTIPLY;
        }
        return null;
    }

    ColorStateList getTintList(@NonNull Context context, @DrawableRes int resId) {
        return getTintList(context, resId, null);
    }

    ColorStateList getTintList(@NonNull Context context, @DrawableRes int resId, @Nullable ColorStateList customTint) {
        boolean useCache = customTint == null ? true : DEBUG;
        ColorStateList tint = useCache ? getTintListFromCache(context, resId) : null;
        if (tint == null) {
            if (resId == C0114R.drawable.abc_edit_text_material) {
                tint = AppCompatResources.getColorStateList(context, C0114R.color.abc_tint_edittext);
            } else if (resId == C0114R.drawable.abc_switch_track_mtrl_alpha) {
                tint = AppCompatResources.getColorStateList(context, C0114R.color.abc_tint_switch_track);
            } else if (resId == C0114R.drawable.abc_switch_thumb_material) {
                tint = AppCompatResources.getColorStateList(context, C0114R.color.abc_tint_switch_thumb);
            } else if (resId == C0114R.drawable.abc_btn_default_mtrl_shape) {
                tint = createDefaultButtonColorStateList(context, customTint);
            } else if (resId == C0114R.drawable.abc_btn_borderless_material) {
                tint = createBorderlessButtonColorStateList(context, customTint);
            } else if (resId == C0114R.drawable.abc_btn_colored_material) {
                tint = createColoredButtonColorStateList(context, customTint);
            } else if (resId == C0114R.drawable.abc_spinner_mtrl_am_alpha || resId == C0114R.drawable.abc_spinner_textfield_background_material) {
                tint = AppCompatResources.getColorStateList(context, C0114R.color.abc_tint_spinner);
            } else if (arrayContains(TINT_COLOR_CONTROL_NORMAL, resId)) {
                tint = ThemeUtils.getThemeAttrColorStateList(context, C0114R.attr.colorControlNormal);
            } else if (arrayContains(TINT_COLOR_CONTROL_STATE_LIST, resId)) {
                tint = AppCompatResources.getColorStateList(context, C0114R.color.abc_tint_default);
            } else if (arrayContains(TINT_CHECKABLE_BUTTON_LIST, resId)) {
                tint = AppCompatResources.getColorStateList(context, C0114R.color.abc_tint_btn_checkable);
            } else if (resId == C0114R.drawable.abc_seekbar_thumb_material) {
                tint = AppCompatResources.getColorStateList(context, C0114R.color.abc_tint_seek_thumb);
            }
            if (useCache && tint != null) {
                addTintListToCache(context, resId, tint);
            }
        }
        return tint;
    }

    private ColorStateList getTintListFromCache(@NonNull Context context, @DrawableRes int resId) {
        if (this.mTintLists == null) {
            return null;
        }
        SparseArray<ColorStateList> tints = (SparseArray) this.mTintLists.get(context);
        if (tints != null) {
            return (ColorStateList) tints.get(resId);
        }
        return null;
    }

    private void addTintListToCache(@NonNull Context context, @DrawableRes int resId, @NonNull ColorStateList tintList) {
        if (this.mTintLists == null) {
            this.mTintLists = new WeakHashMap();
        }
        SparseArray<ColorStateList> themeTints = (SparseArray) this.mTintLists.get(context);
        if (themeTints == null) {
            themeTints = new SparseArray();
            this.mTintLists.put(context, themeTints);
        }
        themeTints.append(resId, tintList);
    }

    private ColorStateList createDefaultButtonColorStateList(@NonNull Context context, @Nullable ColorStateList customTint) {
        return createButtonColorStateList(context, ThemeUtils.getThemeAttrColor(context, C0114R.attr.colorButtonNormal), customTint);
    }

    private ColorStateList createBorderlessButtonColorStateList(@NonNull Context context, @Nullable ColorStateList customTint) {
        return createButtonColorStateList(context, 0, null);
    }

    private ColorStateList createColoredButtonColorStateList(@NonNull Context context, @Nullable ColorStateList customTint) {
        return createButtonColorStateList(context, ThemeUtils.getThemeAttrColor(context, C0114R.attr.colorAccent), customTint);
    }

    private ColorStateList createButtonColorStateList(@NonNull Context context, @ColorInt int baseColor, @Nullable ColorStateList tint) {
        int i;
        states = new int[4][];
        int[] colors = new int[4];
        int colorControlHighlight = ThemeUtils.getThemeAttrColor(context, C0114R.attr.colorControlHighlight);
        int disabledColor = ThemeUtils.getDisabledThemeAttrColor(context, C0114R.attr.colorButtonNormal);
        states[0] = ThemeUtils.DISABLED_STATE_SET;
        if (tint != null) {
            disabledColor = tint.getColorForState(states[0], 0);
        }
        colors[0] = disabledColor;
        int i2 = 0 + 1;
        states[i2] = ThemeUtils.PRESSED_STATE_SET;
        if (tint == null) {
            i = baseColor;
        } else {
            i = tint.getColorForState(states[i2], 0);
        }
        colors[i2] = ColorUtils.compositeColors(colorControlHighlight, i);
        i2++;
        states[i2] = ThemeUtils.FOCUSED_STATE_SET;
        if (tint == null) {
            i = baseColor;
        } else {
            i = tint.getColorForState(states[i2], 0);
        }
        colors[i2] = ColorUtils.compositeColors(colorControlHighlight, i);
        i2++;
        states[i2] = ThemeUtils.EMPTY_STATE_SET;
        if (tint != null) {
            baseColor = tint.getColorForState(states[i2], 0);
        }
        colors[i2] = baseColor;
        i2++;
        return new ColorStateList(states, colors);
    }

    static void tintDrawable(Drawable drawable, TintInfo tint, int[] state) {
        if (!DrawableUtils.canSafelyMutateDrawable(drawable) || drawable.mutate() == drawable) {
            if (tint.mHasTintList || tint.mHasTintMode) {
                drawable.setColorFilter(createTintFilter(tint.mHasTintList ? tint.mTintList : null, tint.mHasTintMode ? tint.mTintMode : DEFAULT_MODE, state));
            } else {
                drawable.clearColorFilter();
            }
            if (VERSION.SDK_INT <= 23) {
                drawable.invalidateSelf();
                return;
            }
            return;
        }
        Log.d(TAG, "Mutated drawable is not the same instance as the input.");
    }

    private static PorterDuffColorFilter createTintFilter(ColorStateList tint, Mode tintMode, int[] state) {
        if (tint == null || tintMode == null) {
            return null;
        }
        return getPorterDuffColorFilter(tint.getColorForState(state, 0), tintMode);
    }

    public static PorterDuffColorFilter getPorterDuffColorFilter(int color, Mode mode) {
        PorterDuffColorFilter filter = COLOR_FILTER_CACHE.get(color, mode);
        if (filter != null) {
            return filter;
        }
        filter = new PorterDuffColorFilter(color, mode);
        COLOR_FILTER_CACHE.put(color, mode, filter);
        return filter;
    }

    private static void setPorterDuffColorFilter(Drawable d, int color, Mode mode) {
        if (DrawableUtils.canSafelyMutateDrawable(d)) {
            d = d.mutate();
        }
        if (mode == null) {
            mode = DEFAULT_MODE;
        }
        d.setColorFilter(getPorterDuffColorFilter(color, mode));
    }

    private void checkVectorDrawableSetup(@NonNull Context context) {
        if (!this.mHasCheckedVectorDrawableSetup) {
            this.mHasCheckedVectorDrawableSetup = true;
            Drawable d = getDrawable(context, C0114R.drawable.abc_vector_test);
            if (d == null || !isVectorDrawable(d)) {
                this.mHasCheckedVectorDrawableSetup = DEBUG;
                throw new IllegalStateException("This app has been built with an incorrect configuration. Please configure your build for VectorDrawableCompat.");
            }
        }
    }

    private static boolean isVectorDrawable(@NonNull Drawable d) {
        return ((d instanceof VectorDrawableCompat) || PLATFORM_VD_CLAZZ.equals(d.getClass().getName())) ? true : DEBUG;
    }
}
