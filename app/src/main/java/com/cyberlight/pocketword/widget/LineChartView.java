package com.cyberlight.pocketword.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Looper;
import android.text.TextPaint;
import android.util.AndroidRuntimeException;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.cyberlight.pocketword.model.LineChartData;
import com.cyberlight.pocketword.R;

import java.time.LocalDate;
import java.util.List;

public class LineChartView extends View {

    // 数据集
    private List<LineChartData> mDataList;

    // 折线
    private final Paint mLinePaint;
    private int mLineWidth;
    private int mLineColor;
    // 背景竖轴数值指示线
    private final Paint mBgLinePaint;
    private int mBgLineWidth;
    private int mBgLineColor;
    // 背景竖轴数值
    private final TextPaint mBgNumTextPaint;
    private int mBgNumTextSize;
    private int mBgNumTextColor;
    // 背景横轴日期
    private final TextPaint mBgDateTextPaint;
    private int mBgDateTextSize;
    private int mBgDateTextColor;
    // 详情数据点指示竖线
    private final Paint mDetailLinePaint;
    private int mDetailLineWidth;
    private int mDetailLineColor;
    // 详情数据点
    private final Paint mDetailPointStrokePaint;
    private int mDetailPointStrokeWidth;
    private int mDetailPointStrokeColor;
    private final Paint mDetailPointFillPaint;
    private int mDetailPointFillColor;
    // 详情文字
    private final TextPaint mDetailTextPaint;
    private int mDetailTextSize;
    private int mDetailTextColor;
    // 详情浮窗
    private final Paint mDetailRectPaint;
    private int mDetailRectColor;
    private int mDetailRectShadowColor;
    private int mDetailRectShadowRadius;

    // 背景数量指示线条数
    private int mBgLineCount;
    // 指示线与其左边指示数字间距
    private int mBgLineNumGap;
    // 指示线与底部日期间距
    private int mBgLineDateGap;
    // 详情数据点半径
    private int mDetailPointRadius;
    // 详情日期与数值的行间距
    private int mDetailDateNumGap;
    // 详情浮窗和竖直指示线间距
    private int mDetailLineRectGap;
    // 详情浮窗padding
    private int mDetailRectPadding;
    // 详情浮窗圆角尺寸
    private int mDetailRectRoundCorner;

    // 文本格式
    private BgNumFormat mBgNumFormat;
    private BgDateFormat mBgDateFormat;
    private DetailFormat mDetailFormat;

    // 整体绘制区域
    private final Rect mDrawRect;
    // 图表绘制区域
    private final Rect mTableDrawRect;
    // 绘制准备数据
    private int mBgNumsX;
    private final int[] mBgNumsYs;
    private final String[] mBgNumsTexts;
    private final int[] mBgLinesYs;
    private int mBgDatesY;
    private Point[] mPoints;
    private final RectF mDetailRectF;

    // 图表数值范围的最大值
    private int mMaxNum;
    // 触摸时显示详情的数据点下标
    // (-1表示未触摸、无详情数据点)
    private int mDetailIndex = -1;

    public LineChartView(Context context) {
        this(context, null);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.DefaultLineChartView);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgNumTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mBgDateTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mDetailLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDetailPointStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDetailPointFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDetailTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mDetailRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawRect = new Rect();
        mTableDrawRect = new Rect();
        mDetailRectF = new RectF();
        initAttrs(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
        mBgNumsYs = new int[mBgLineCount];
        mBgNumsTexts = new String[mBgLineCount];
        mBgLinesYs = new int[mBgLineCount];
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.LineChartView, defStyleAttr, defStyleRes);
        try {
            mLineWidth = a.getDimensionPixelSize(
                    R.styleable.LineChartView_lineWidth, -1);
            mLineColor = a.getColor(
                    R.styleable.LineChartView_lineColor, Color.BLACK);
            mBgLineWidth = a.getDimensionPixelSize(
                    R.styleable.LineChartView_bgLineWidth, -1);
            mBgLineColor = a.getColor(
                    R.styleable.LineChartView_bgLineColor, Color.BLACK);
            mBgNumTextSize = a.getDimensionPixelSize(
                    R.styleable.LineChartView_bgNumTextSize, -1);
            mBgNumTextColor = a.getColor(
                    R.styleable.LineChartView_bgNumTextColor, Color.BLACK);
            mBgDateTextSize = a.getDimensionPixelSize(
                    R.styleable.LineChartView_bgDateTextSize, -1);
            mBgDateTextColor = a.getColor(
                    R.styleable.LineChartView_bgDateTextColor, Color.BLACK);
            mDetailLineWidth = a.getDimensionPixelSize(
                    R.styleable.LineChartView_detailLineWidth, -1);
            mDetailLineColor = a.getColor(
                    R.styleable.LineChartView_detailLineColor, Color.BLACK);
            mDetailPointStrokeWidth = a.getDimensionPixelSize(
                    R.styleable.LineChartView_detailPointStrokeWidth, -1);
            mDetailPointStrokeColor = a.getColor(
                    R.styleable.LineChartView_detailPointStrokeColor, Color.BLACK);
            mDetailPointFillColor = a.getColor(
                    R.styleable.LineChartView_detailPointFillColor, Color.BLACK);
            mDetailTextSize = a.getDimensionPixelSize(
                    R.styleable.LineChartView_detailTextSize, -1);
            mDetailTextColor = a.getColor(
                    R.styleable.LineChartView_detailTextColor, Color.BLACK);
            mDetailRectColor = a.getColor(
                    R.styleable.LineChartView_detailRectColor, Color.BLACK);
            mDetailRectShadowColor = a.getColor(
                    R.styleable.LineChartView_detailRectShadowColor, Color.BLACK);
            mDetailRectShadowRadius = a.getDimensionPixelSize(
                    R.styleable.LineChartView_detailRectShadowRadius, -1);
            mBgLineCount = a.getInt(
                    R.styleable.LineChartView_bgLineCount, 0);
            mBgLineNumGap = a.getDimensionPixelSize(
                    R.styleable.LineChartView_bgLineNumGap, -1);
            mBgLineDateGap = a.getDimensionPixelSize(
                    R.styleable.LineChartView_bgLineDateGap, -1);
            mDetailPointRadius = a.getDimensionPixelSize(
                    R.styleable.LineChartView_detailPointRadius, -1);
            mDetailDateNumGap = a.getDimensionPixelSize(
                    R.styleable.LineChartView_detailDateNumGap, -1);
            mDetailLineRectGap = a.getDimensionPixelSize(
                    R.styleable.LineChartView_detailLineRectGap, -1);
            mDetailRectPadding = a.getDimensionPixelSize(
                    R.styleable.LineChartView_detailRectPadding, -1);
            mDetailRectRoundCorner = a.getDimensionPixelSize(
                    R.styleable.LineChartView_detailRectRoundCorner, -1);
        } finally {
            a.recycle();
        }
    }

    private void initPaint() {
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mBgLinePaint.setStrokeWidth(mBgLineWidth);
        mBgLinePaint.setColor(mBgLineColor);

        mBgNumTextPaint.setTextSize(mBgNumTextSize);
        mBgNumTextPaint.setColor(mBgNumTextColor);
        mBgNumTextPaint.setTextAlign(Paint.Align.RIGHT);

        mBgDateTextPaint.setTextSize(mBgDateTextSize);
        mBgDateTextPaint.setColor(mBgDateTextColor);
        mBgDateTextPaint.setTextAlign(Paint.Align.CENTER);

        mDetailLinePaint.setStrokeWidth(mDetailLineWidth);
        mDetailLinePaint.setColor(mDetailLineColor);
        mDetailLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mDetailPointStrokePaint.setStrokeWidth(mDetailPointStrokeWidth);
        mDetailPointStrokePaint.setColor(mDetailPointStrokeColor);
        mDetailPointStrokePaint.setStyle(Paint.Style.STROKE);

        mDetailPointFillPaint.setColor(mDetailPointFillColor);
        mDetailPointFillPaint.setStyle(Paint.Style.FILL);

        mDetailTextPaint.setTextSize(mDetailTextSize);
        mDetailTextPaint.setColor(mDetailTextColor);

        mDetailRectPaint.setColor(mDetailRectColor);
        mDetailRectPaint.setShadowLayer(mDetailRectShadowRadius, 0, 0, mDetailRectShadowColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = resolveSize(getPaddingLeft() + getPaddingRight(), widthMeasureSpec);
        int measuredHeight = resolveSize(getPaddingTop() + getPaddingBottom(), heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 计算整个控件绘制的区域
        mDrawRect.set(getPaddingLeft(),
                getPaddingTop(),
                w - getPaddingRight(),
                h - getPaddingBottom());
        // 绘制前计算
        prepareDraw();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDataList == null || mDataList.size() == 0) return;
        // 从上至下绘制表格线和数值
        for (int i = 0; i < mBgLineCount; i++) {
            canvas.drawLine(mTableDrawRect.left, mBgLinesYs[i], mTableDrawRect.right, mBgLinesYs[i], mBgLinePaint);
            canvas.drawText(mBgNumsTexts[i], mBgNumsX, mBgNumsYs[i], mBgNumTextPaint);
        }
        // 绘制折线
        Point p1 = mPoints[0];
        for (int i = 1; i < mPoints.length; i++) {
            Point p2 = mPoints[i];
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, mLinePaint);
            p1 = p2;
        }
        // 绘制头尾日期
        String firstDateStr = mBgDateFormat != null
                ? mBgDateFormat.format(mDataList.get(0).date)
                : mDataList.get(0).date.toString();
        String lastDateStr = mBgDateFormat != null
                ? mBgDateFormat.format(mDataList.get(mDataList.size() - 1).date)
                : mDataList.get(mDataList.size() - 1).date.toString();
        canvas.drawText(firstDateStr, mPoints[0].x, mBgDatesY, mBgDateTextPaint);
        canvas.drawText(lastDateStr, mPoints[mPoints.length - 1].x, mBgDatesY, mBgDateTextPaint);
        // 用户触摸时绘制数据点详情
        if (mDetailIndex != -1) {
            Point p = mPoints[mDetailIndex];
            canvas.drawLine(p.x, mTableDrawRect.top, p.x, mTableDrawRect.bottom, mDetailLinePaint);
            canvas.drawCircle(p.x, p.y, mDetailPointRadius, mDetailPointFillPaint);
            canvas.drawCircle(p.x, p.y, mDetailPointRadius, mDetailPointStrokePaint);
            // 绘制详情浮动窗口
            LineChartData data = mDataList.get(mDetailIndex);
            String dateStr = mDetailFormat != null
                    ? mDetailFormat.dateFormat(data.date)
                    : data.date.toString();
            String numStr = mDetailFormat != null
                    ? mDetailFormat.numFormat(data.num)
                    : String.valueOf(data.num);
            Paint.FontMetrics fontMetrics = mDetailTextPaint.getFontMetrics();
            final int dateW = Math.round(mDetailTextPaint.measureText(dateStr)) + 1;
            final int numW = Math.round(mDetailTextPaint.measureText(numStr)) + 1;
            final int numH = Math.round(fontMetrics.bottom - fontMetrics.top);
            final int rectH = numH * 2 + mDetailDateNumGap + mDetailRectPadding * 2;
            final int rectW = Math.max(dateW, numW) + mDetailRectPadding * 2;
            mDetailRectF.left = p.x - mDetailLineRectGap - rectW;
            mDetailRectF.top = p.y - rectH / 2f;
            mDetailRectF.right = p.x - mDetailLineRectGap;
            mDetailRectF.bottom = mDetailRectF.top + rectH;
            if (mDetailRectF.left < mTableDrawRect.left) {
                mDetailRectF.left = p.x + mDetailLineRectGap;
                mDetailRectF.right = p.x + mDetailLineRectGap + rectW;
            }
            if (mDetailRectF.top < mTableDrawRect.top) {
                mDetailRectF.top = mTableDrawRect.top;
                mDetailRectF.bottom = mTableDrawRect.top + rectH;
            } else if (mDetailRectF.bottom > mTableDrawRect.bottom) {
                mDetailRectF.top = mTableDrawRect.bottom - rectH;
                mDetailRectF.bottom = mTableDrawRect.bottom;
            }
            canvas.drawRoundRect(mDetailRectF, mDetailRectRoundCorner, mDetailRectRoundCorner, mDetailRectPaint);
            // 绘制详情窗口内文字
            final int dateY = Math.round(mDetailRectF.centerY() - fontMetrics.bottom - mDetailDateNumGap / 2f);
            final int numY = Math.round(mDetailRectF.centerY() - fontMetrics.top + mDetailDateNumGap / 2f);
            canvas.drawText(dateStr, mDetailRectF.left + mDetailRectPadding, dateY, mDetailTextPaint);
            canvas.drawText(numStr, mDetailRectF.left + mDetailRectPadding, numY, mDetailTextPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int emphasizeIndex;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                emphasizeIndex = calcEmphasizeIndex(Math.round(event.getX()));
                break;
            default:
                emphasizeIndex = -1;
        }
        if (mDetailIndex != emphasizeIndex) {
            mDetailIndex = emphasizeIndex;
            invalidate();
        }
        return true;
    }

    private void prepareDraw() {
        if (mDataList == null || mDataList.size() == 0) return;
        // 计算文字尺寸
        Paint.FontMetrics numTextFontMetrics = mBgNumTextPaint.getFontMetrics();
        final int numTextHeight = Math.round(numTextFontMetrics.bottom - numTextFontMetrics.top);
        Paint.FontMetrics dateTextFontMetrics = mBgDateTextPaint.getFontMetrics();
        final int dateTextWidth = Math.round(mBgDateTextPaint.measureText("88/88")) + 1;
        final int dateTextHeight = Math.round(dateTextFontMetrics.bottom - dateTextFontMetrics.top);
        int numTextWidth = 0;// 在下面计算数值文字同时计算
        // 计算各条指示线的对应数值
        final int step = mMaxNum / (mBgLineCount - 1);
        for (int i = 0; i < mBgLineCount; i++) {
            String numStr = mBgNumFormat != null
                    ? mBgNumFormat.format(mMaxNum - step * i)
                    : String.valueOf(mMaxNum - step * i);
            mBgNumsTexts[i] = numStr;
            numTextWidth = Math.max(Math.round(mBgNumTextPaint.measureText(numStr)) + 1, numTextWidth);
        }
        // 计算表格绘制范围
        mTableDrawRect.set(mDrawRect.left + numTextWidth + mBgLineNumGap,
                mDrawRect.top + numTextHeight / 2,
                mDrawRect.right - dateTextWidth / 2,
                mDrawRect.bottom - Math.max(numTextHeight / 2, dateTextHeight + mBgLineDateGap));
        final int verticalGap = mTableDrawRect.height() / (mBgLineCount - 1);
        final int horizontalGap = mTableDrawRect.width() / (mDataList.size() - 1);
        // 修正表格绘制范围
        mTableDrawRect.bottom = mTableDrawRect.top + verticalGap * (mBgLineCount - 1);
        mTableDrawRect.right = mTableDrawRect.left + horizontalGap * (mDataList.size() - 1);
        // 计算数值x坐标
        mBgNumsX = mTableDrawRect.left - mBgLineNumGap;
        // 计算日期y坐标
        mBgDatesY = mTableDrawRect.bottom + mBgLineDateGap - Math.round(dateTextFontMetrics.top);
        // 从上至下计算各表格线和数值的y坐标
        for (int i = 0; i < mBgLineCount; i++) {
            mBgLinesYs[i] = mTableDrawRect.top + verticalGap * i;
            mBgNumsYs[i] = (int) (mBgLinesYs[i] - (numTextFontMetrics.ascent + numTextFontMetrics.descent) / 2.0f);
        }
        // 从左至右计算各个数据点
        for (int i = 0; i < mDataList.size(); i++) {
            mPoints[i] = new Point(mTableDrawRect.left + horizontalGap * i,
                    (int) (mTableDrawRect.bottom - (float) mDataList.get(i).num * mTableDrawRect.height() / mMaxNum));
        }
    }

    // 计算触摸时要突出展示的数据的下标
    private int calcEmphasizeIndex(int touchX) {
        if (mDataList != null) {
            final int horizontalGap = mTableDrawRect.width() / (mDataList.size() - 1);
            int xMin = mPoints[0].x - horizontalGap / 2;
            for (int i = 0; i < mPoints.length; i++) {
                int xMax = xMin + horizontalGap;
                if (touchX >= xMin && touchX < xMax) {
                    return i;
                }
                xMin = xMax;
            }
        }
        return -1;
    }

    private void updateMaxNum() {
        // 计算图表的最大数值
        int maxNum = 0;
        for (int i = 0; i < mDataList.size(); i++) {
            maxNum = Math.max(maxNum, mDataList.get(i).num);
        }
        int divisor = 10;
        while (maxNum / divisor >= 10) {
            divisor *= 10;
        }
        // 不愧是我
        mMaxNum = maxNum / divisor * divisor
                + ((maxNum % divisor / (divisor / 10)) + 1) * (divisor / 10);
        // 如果最大值分不成整数，进行修正
        if (mMaxNum % (mBgLineCount - 1) != 0) {
            mMaxNum = (mMaxNum / (mBgLineCount - 1) + 1) * (mBgLineCount - 1);
        }
    }

    private void checkMainThread() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new CalledFromWrongThreadException("Only the "
                    + "UI thread can touch me.");
        }
    }

    public void setBgNumFormat(BgNumFormat bgNumFormat) {
        checkMainThread();
        mBgNumFormat = bgNumFormat;
        if (mDataList == null || mDataList.size() == 0) return;
        prepareDraw();
        invalidate();
    }

    public void setBgDateFormat(BgDateFormat bgDateFormat) {
        checkMainThread();
        mBgDateFormat = bgDateFormat;
        if (mDataList == null || mDataList.size() == 0) return;
        prepareDraw();
        invalidate();
    }

    public void setDetailFormat(DetailFormat detailFormat) {
        checkMainThread();
        mDetailFormat = detailFormat;
        if (mDataList == null || mDataList.size() == 0) return;
        prepareDraw();
        invalidate();
    }

    public void setDataList(List<LineChartData> dataList) {
        checkMainThread();
        mDataList = dataList;
        if (mDataList == null || mDataList.size() == 0) return;
        mPoints = new Point[mDataList.size()];
        updateMaxNum();
        prepareDraw();
        invalidate();
    }

    public interface BgNumFormat {
        String format(int num);
    }

    public interface BgDateFormat {
        String format(LocalDate date);
    }

    public interface DetailFormat {
        String dateFormat(LocalDate date);

        String numFormat(int num);
    }

    public static final class CalledFromWrongThreadException extends AndroidRuntimeException {
        public CalledFromWrongThreadException(String msg) {
            super(msg);
        }
    }

}