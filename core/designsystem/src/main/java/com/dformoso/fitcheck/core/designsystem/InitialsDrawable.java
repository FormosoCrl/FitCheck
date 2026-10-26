package com.dformoso.fitcheck.core.designsystem;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Circulo de color con la inicial, para cuando alguien no tiene foto.
 *
 * <p>El color sale del nombre, no es aleatorio: si cambiara en cada pintada, la misma
 * persona saldria de un color distinto cada vez que se recicla una fila de la lista.
 */
public class InitialsDrawable extends Drawable {

    /** Tonos con suficiente contraste para texto blanco encima. */
    private static final int[] PALETTE = {
            0xFF0F766E, 0xFF7C3AED, 0xFFB45309, 0xFFBE123C,
            0xFF1D4ED8, 0xFF047857, 0xFF9333EA, 0xFFC2410C,
    };

    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String initial;

    public InitialsDrawable(@Nullable String name) {
        String safe = name == null ? "" : name.trim();
        this.initial = safe.isEmpty() ? "?" : safe.substring(0, 1).toUpperCase();

        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(colorFor(safe));

        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    }

    private static int colorFor(String name) {
        if (name.isEmpty()) {
            return PALETTE[0];
        }
        // Math.abs(Integer.MIN_VALUE) es negativo: se enmascara el signo en vez de
        // confiar en abs, o un hash concreto daria un indice negativo.
        int index = (name.hashCode() & 0x7FFFFFFF) % PALETTE.length;
        return PALETTE[index];
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        float radius = Math.min(bounds.width(), bounds.height()) / 2f;
        float cx = bounds.exactCenterX();
        float cy = bounds.exactCenterY();

        canvas.drawCircle(cx, cy, radius, circlePaint);

        textPaint.setTextSize(radius);
        float baseline = cy - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(initial, cx, baseline, textPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        circlePaint.setAlpha(alpha);
        textPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        circlePaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
