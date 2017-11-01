package ru.mhistory.common.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.annotation.AttrRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import ru.mhistory.MobileHistoryApp;

public class UiUtil {
    public static boolean isInMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void ensureMainThread(@NonNull String exceptionMsg) {
        if (!isInMainThread()) {
            throw new IllegalStateException(exceptionMsg);
        }
    }

    public static int getThemedResId(@NonNull Context context, @AttrRes int attr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        final int resId = a.getResourceId(0, 0);
        a.recycle();
        return resId;
    }

    public static float getDimension(@NonNull Context context,
                                     @StyleRes int styleResId,
                                     @AttrRes int attr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(styleResId, new int[]{attr});
        float size = a.getDimension(0, 0);
        a.recycle();
        return size;
    }

    public static int dpToPx(int dp) {
        return (int) ((MobileHistoryApp.getContext().getResources()
                .getDisplayMetrics().density * dp) + 0.5);
    }

    public static float dipToPx(@NonNull Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static int getDimensionInDp(@NonNull Context context, @DimenRes int dimenResId) {
        return (int) (context.getResources().getDimension(dimenResId) /
                context.getResources().getDisplayMetrics().density);
    }

    public static int getDisplayHeight(@NonNull Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point.y;
    }

    @Nullable
    public static Bitmap drawableToBitmap(@NonNull Context context,
                                          @DrawableRes int drawableRes) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // TODO:
        }
        return bitmap;
    }

    private UiUtil() {
    }
}
