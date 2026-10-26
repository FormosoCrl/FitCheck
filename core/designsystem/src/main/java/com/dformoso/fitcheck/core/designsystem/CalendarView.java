package com.dformoso.fitcheck.core.designsystem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.color.MaterialColors;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Calendario con tres modos: semanal, mensual y anual.
 *
 * <p>No conoce el dominio: recibe un {@code Map<LocalDate, Integer>} y colorea los días
 * proporcionalmente al valor máximo. En esta app el valor son series, pero la View no lo
 * sabe ni le importa.
 */
public class CalendarView extends View {

    public enum Mode { WEEK, MONTH, YEAR }

    private static final int COLS = 7;
    private static final int MAX_MONTH_ROWS = 6;
    private static final int MAX_LEVEL = 4;

    private final Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dayTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint miniLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint miniDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint miniRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint weekRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint weekCountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tempRect = new RectF();

    private Mode mode = Mode.MONTH;
    private LocalDate anchor;
    private LocalDate today;
    private Map<LocalDate, Integer> data = Collections.emptyMap();
    private int maxCount;
    @Nullable private LocalDate selectedDay;
    @Nullable private Consumer<LocalDate> onDayClick;

    private int primaryColor;
    private int containerColor;
    private int onPrimaryColor;
    private int onSurfaceColor;
    private int onSurfaceVariantColor;
    private int surfaceVariantColor;

    private float density;
    private float headerHeight;
    private float cellSize;

    public CalendarView(Context context) { this(context, null); }
    public CalendarView(Context context, @Nullable AttributeSet attrs) { this(context, attrs, 0); }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;
        float sp = getResources().getDisplayMetrics().scaledDensity;
        today = LocalDate.now();
        anchor = today;

        headerPaint.setTextSize(12f * sp);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        dayTextPaint.setTextSize(13f * sp);
        dayTextPaint.setTextAlign(Paint.Align.CENTER);

        circlePaint.setStyle(Paint.Style.FILL);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(2f * density);

        selRingPaint.setStyle(Paint.Style.STROKE);
        selRingPaint.setStrokeWidth(1.5f * density);

        miniLabelPaint.setTextSize(10f * sp);
        miniLabelPaint.setTextAlign(Paint.Align.CENTER);
        miniLabelPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        miniDotPaint.setStyle(Paint.Style.FILL);

        miniRingPaint.setStyle(Paint.Style.STROKE);
        miniRingPaint.setStrokeWidth(1.5f * density);

        weekRectPaint.setStyle(Paint.Style.FILL);

        weekCountPaint.setTextSize(10f * sp);
        weekCountPaint.setTextAlign(Paint.Align.CENTER);

        resolveColors();
        headerHeight = headerPaint.getTextSize() * 2.2f;
    }

    private void resolveColors() {
        primaryColor = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary);
        containerColor = MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorPrimaryContainer);
        onPrimaryColor = MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorOnPrimary);
        onSurfaceColor = MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorOnSurface);
        onSurfaceVariantColor = MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorOnSurfaceVariant);
        surfaceVariantColor = MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorSurfaceVariant);

        headerPaint.setColor(onSurfaceVariantColor);
        ringPaint.setColor(primaryColor);
        selRingPaint.setColor(onSurfaceVariantColor);
        miniLabelPaint.setColor(onSurfaceVariantColor);
        miniRingPaint.setColor(primaryColor);
        weekCountPaint.setColor(onSurfaceVariantColor);
    }

    // ------------------------------------------------------------------ API pública

    public void setMode(Mode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            requestLayout();
            invalidate();
        }
    }

    public Mode getMode() { return mode; }

    public void setAnchor(LocalDate anchor) {
        if (this.anchor.equals(anchor)) return;
        this.anchor = anchor;
        requestLayout();
        invalidate();
    }

    public LocalDate getAnchor() { return anchor; }

    /**
     * Día resaltado. Lo fija la pantalla, no la View: quien decide qué día se está mirando
     * es el ViewModel, y así el resalte sobrevive a girar la pantalla o a volver de otro mes.
     */
    public void setSelectedDay(@Nullable LocalDate day) {
        if (java.util.Objects.equals(selectedDay, day)) return;
        this.selectedDay = day;
        invalidate();
    }

    @Nullable
    public LocalDate getSelectedDay() { return selectedDay; }

    public void setToday(LocalDate today) {
        this.today = today;
        invalidate();
    }

    public void setData(Map<LocalDate, Integer> data) {
        this.data = data == null ? Collections.emptyMap() : data;
        maxCount = 0;
        for (Integer v : this.data.values()) {
            if (v != null && v > maxCount) maxCount = v;
        }
        invalidate();
    }

    public void setOnDayClickListener(@Nullable Consumer<LocalDate> listener) {
        this.onDayClick = listener;
    }

    public String getDisplayLabel() {
        Locale locale = Locale.getDefault();
        if (mode == Mode.WEEK) {
            LocalDate start = weekStart();
            LocalDate end = start.plusDays(6);
            String month = end.getMonth().getDisplayName(TextStyle.SHORT, locale);
            return start.getDayOfMonth() + " – " + end.getDayOfMonth() + " "
                    + capitalize(month, locale) + " " + end.getYear();
        } else if (mode == Mode.MONTH) {
            YearMonth ym = YearMonth.from(anchor);
            String month = ym.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, locale);
            return capitalize(month, locale) + " " + ym.getYear();
        } else {
            return String.valueOf(anchor.getYear());
        }
    }

    // ------------------------------------------------------------------ medida

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int available = width - getPaddingLeft() - getPaddingRight();
        cellSize = available / (float) COLS;

        float height;
        if (mode == Mode.WEEK) {
            height = headerHeight + cellSize * 1.6f;
        } else if (mode == Mode.MONTH) {
            height = headerHeight + MAX_MONTH_ROWS * cellSize;
        } else {
            float miniWidth = available / 3f;
            float miniCell = miniWidth / COLS;
            float miniLabel = miniLabelPaint.getTextSize() * 2.2f;
            float miniMonth = miniLabel + MAX_MONTH_ROWS * miniCell;
            height = 4 * miniMonth + 3 * 12 * density;
        }
        height += getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, (int) Math.ceil(height));
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (mode == Mode.WEEK) {
            drawWeekdayHeader(canvas);
            drawWeekCells(canvas);
        } else if (mode == Mode.MONTH) {
            drawWeekdayHeader(canvas);
            drawMonthCells(canvas);
        } else {
            drawYear(canvas);
        }
    }

    private void drawWeekdayHeader(Canvas canvas) {
        Locale locale = Locale.getDefault();
        float y = getPaddingTop() + headerHeight * 0.65f;
        for (int col = 0; col < COLS; col++) {
            String label = DayOfWeek.of(col + 1)
                    .getDisplayName(TextStyle.NARROW_STANDALONE, locale);
            float x = getPaddingLeft() + col * cellSize + cellSize / 2f;
            canvas.drawText(label.toUpperCase(locale), x, y, headerPaint);
        }
    }

    private void drawMonthCells(Canvas canvas) {
        YearMonth ym = YearMonth.from(anchor);
        int firstDow = ym.atDay(1).getDayOfWeek().getValue();
        int daysInMonth = ym.lengthOfMonth();
        float startY = getPaddingTop() + headerHeight;

        for (int day = 1; day <= daysInMonth; day++) {
            int offset = (firstDow - 1) + (day - 1);
            int col = offset % COLS;
            int row = offset / COLS;
            float cx = getPaddingLeft() + col * cellSize + cellSize / 2f;
            float cy = startY + row * cellSize + cellSize / 2f;
            float radius = cellSize * 0.4f;

            LocalDate date = ym.atDay(day);
            int count = count(date);
            boolean isToday = date.equals(today);
            boolean isSelected = date.equals(selectedDay);

            if (count > 0) {
                circlePaint.setColor(intensityColor(count));
                canvas.drawCircle(cx, cy, radius, circlePaint);
            }

            if (isToday) {
                canvas.drawCircle(cx, cy, radius, ringPaint);
            } else if (isSelected) {
                canvas.drawCircle(cx, cy, radius, selRingPaint);
            }

            dayTextPaint.setColor(dayTextColor(count, isToday));
            float textY = cy - (dayTextPaint.descent() + dayTextPaint.ascent()) / 2f;
            canvas.drawText(String.valueOf(day), cx, textY, dayTextPaint);
        }
    }

    private void drawWeekCells(Canvas canvas) {
        LocalDate start = weekStart();
        float startY = getPaddingTop() + headerHeight;
        float cellHeight = cellSize * 1.6f;
        float corner = 12f * density;
        float gap = 3f * density;

        for (int col = 0; col < COLS; col++) {
            LocalDate date = start.plusDays(col);
            int count = count(date);
            boolean isToday = date.equals(today);

            float left = getPaddingLeft() + col * cellSize + gap;
            float top = startY + gap;
            float right = left + cellSize - 2 * gap;
            float bottom = top + cellHeight - 2 * gap;
            tempRect.set(left, top, right, bottom);

            weekRectPaint.setColor(count > 0 ? intensityColor(count) : surfaceVariantColor);
            canvas.drawRoundRect(tempRect, corner, corner, weekRectPaint);

            if (isToday) {
                canvas.drawRoundRect(tempRect, corner, corner, ringPaint);
            } else if (date.equals(selectedDay)) {
                canvas.drawRoundRect(tempRect, corner, corner, selRingPaint);
            }

            float cx = (left + right) / 2f;

            dayTextPaint.setColor(count > 0 ? dayTextColor(count, false) : onSurfaceColor);
            float dayY = top + cellHeight * 0.38f;
            canvas.drawText(String.valueOf(date.getDayOfMonth()), cx, dayY, dayTextPaint);

            if (count > 0) {
                weekCountPaint.setColor(dayTextColor(count, false));
                float countY = top + cellHeight * 0.68f;
                canvas.drawText(String.valueOf(count), cx, countY, weekCountPaint);
            }
        }
    }

    private void drawYear(Canvas canvas) {
        int year = anchor.getYear();
        float available = getWidth() - getPaddingLeft() - getPaddingRight();
        float miniWidth = available / 3f;
        float miniCell = miniWidth / COLS;
        float miniLabel = miniLabelPaint.getTextSize() * 2.2f;
        float miniMonth = miniLabel + MAX_MONTH_ROWS * miniCell;
        float gap = 12f * density;
        Locale locale = Locale.getDefault();

        for (int month = 1; month <= 12; month++) {
            int gridCol = (month - 1) % 3;
            int gridRow = (month - 1) / 3;
            float baseX = getPaddingLeft() + gridCol * miniWidth;
            float baseY = getPaddingTop() + gridRow * (miniMonth + gap);

            YearMonth ym = YearMonth.of(year, month);
            String label = capitalize(
                    ym.getMonth().getDisplayName(TextStyle.SHORT_STANDALONE, locale), locale);
            canvas.drawText(label, baseX + miniWidth / 2f, baseY + miniLabel * 0.7f,
                    miniLabelPaint);

            int firstDow = ym.atDay(1).getDayOfWeek().getValue();
            int daysInMonth = ym.lengthOfMonth();
            float gridY = baseY + miniLabel;
            float dotRadius = miniCell * 0.35f;

            for (int day = 1; day <= daysInMonth; day++) {
                int offset = (firstDow - 1) + (day - 1);
                int col = offset % COLS;
                int row = offset / COLS;
                float cx = baseX + col * miniCell + miniCell / 2f;
                float cy = gridY + row * miniCell + miniCell / 2f;

                LocalDate date = ym.atDay(day);
                int count = count(date);

                miniDotPaint.setColor(count > 0 ? intensityColor(count) : surfaceVariantColor);
                canvas.drawCircle(cx, cy, dotRadius, miniDotPaint);

                if (date.equals(today)) {
                    canvas.drawCircle(cx, cy, dotRadius, miniRingPaint);
                } else if (date.equals(selectedDay)) {
                    canvas.drawCircle(cx, cy, dotRadius, selRingPaint);
                }
            }
        }
    }

    // ------------------------------------------------------------------ toque

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (onDayClick == null) return super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            LocalDate tapped = dateFromTouch(event.getX(), event.getY());
            if (tapped != null) {
                selectedDay = tapped;
                invalidate();
                performClick();
                onDayClick.accept(tapped);
            }
        }
        return true;
    }

    @Override
    public boolean performClick() { return super.performClick(); }

    @Nullable
    private LocalDate dateFromTouch(float x, float y) {
        if (mode == Mode.MONTH) {
            return dateFromMonthTouch(x, y);
        } else if (mode == Mode.WEEK) {
            int col = (int) ((x - getPaddingLeft()) / cellSize);
            if (col < 0 || col >= COLS) return null;
            return weekStart().plusDays(col);
        }
        return dateFromYearTouch(x, y);
    }

    /**
     * Día tocado en la vista anual.
     *
     * <p>Hay dos rejillas encajadas: primero se localiza el mini-mes dentro de la cuadrícula
     * de 3x4, y dentro de él la celda del día. Las medidas se recalculan igual que en
     * {@link #drawYear}; si una cambia allí, tiene que cambiar aquí.
     */
    @Nullable
    private LocalDate dateFromYearTouch(float x, float y) {
        float available = getWidth() - getPaddingLeft() - getPaddingRight();
        float miniWidth = available / 3f;
        float miniCell = miniWidth / COLS;
        float miniLabel = miniLabelPaint.getTextSize() * 2.2f;
        float miniMonth = miniLabel + MAX_MONTH_ROWS * miniCell;
        float gap = 12f * density;

        int gridCol = (int) ((x - getPaddingLeft()) / miniWidth);
        if (gridCol < 0 || gridCol > 2) return null;

        int gridRow = (int) ((y - getPaddingTop()) / (miniMonth + gap));
        if (gridRow < 0 || gridRow > 3) return null;

        int month = gridRow * 3 + gridCol + 1;
        if (month < 1 || month > 12) return null;

        YearMonth ym = YearMonth.of(anchor.getYear(), month);
        float baseX = getPaddingLeft() + gridCol * miniWidth;
        float gridY = getPaddingTop() + gridRow * (miniMonth + gap) + miniLabel;

        int col = (int) ((x - baseX) / miniCell);
        int row = (int) ((y - gridY) / miniCell);
        if (col < 0 || col >= COLS || row < 0 || row >= MAX_MONTH_ROWS) return null;

        int firstDow = ym.atDay(1).getDayOfWeek().getValue();
        int dayIndex = row * COLS + col - (firstDow - 1) + 1;
        if (dayIndex < 1 || dayIndex > ym.lengthOfMonth()) return null;
        return ym.atDay(dayIndex);
    }

    @Nullable
    private LocalDate dateFromMonthTouch(float x, float y) {
        YearMonth ym = YearMonth.from(anchor);
        int firstDow = ym.atDay(1).getDayOfWeek().getValue();
        float startY = getPaddingTop() + headerHeight;
        int col = (int) ((x - getPaddingLeft()) / cellSize);
        int row = (int) ((y - startY) / cellSize);
        if (col < 0 || col >= COLS || row < 0 || row >= MAX_MONTH_ROWS) return null;
        int dayIndex = row * COLS + col - (firstDow - 1) + 1;
        if (dayIndex < 1 || dayIndex > ym.lengthOfMonth()) return null;
        return ym.atDay(dayIndex);
    }

    // ------------------------------------------------------------------ color

    private int count(LocalDate date) {
        Integer v = data.get(date);
        return v == null ? 0 : v;
    }

    private int intensityColor(int count) {
        if (count <= 0 || maxCount <= 0) return surfaceVariantColor;
        int level = Math.min(MAX_LEVEL,
                Math.max(1, (int) Math.ceil((double) count / maxCount * MAX_LEVEL)));
        return ColorUtils.blendARGB(containerColor, primaryColor, level / (float) MAX_LEVEL);
    }

    private int dayTextColor(int count, boolean isToday) {
        if (count <= 0 && !isToday) return onSurfaceColor;
        if (count <= 0) return primaryColor;
        double luminance = ColorUtils.calculateLuminance(intensityColor(count));
        return luminance > 0.5 ? onSurfaceColor : onPrimaryColor;
    }

    private LocalDate weekStart() {
        return anchor.minusDays(anchor.getDayOfWeek().getValue() - 1L);
    }

    private static String capitalize(String s, Locale locale) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(locale) + s.substring(1);
    }
}
