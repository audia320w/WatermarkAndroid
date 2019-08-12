package io.zhuliang.watermark.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * @author ZhuLiang
 * @since 2019/08/10 09:29
 */
public class WatermarkView extends AppCompatImageView implements ViewTreeObserver.OnGlobalLayoutListener {

    private int mDegrees;
    private Paint mTextPaint;
    private Paint mTextBoundsPaint;
    private Paint mPointPaint;
    private Rect mTextBounds;
    private Paint mBitmapBoundsPaint;

    private String mWatermarkText = "Watermark";

    private int mSpacing;

    private boolean once = true;
    private float initScale;
    private final Matrix scaleMatrix = new Matrix();

    public WatermarkView(Context context) {
        this(context, null);
    }

    public WatermarkView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatermarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(50);
        mTextPaint.setColor(Color.RED);

        mTextBoundsPaint = new Paint();
        mTextBoundsPaint.setStyle(Paint.Style.STROKE);
        mTextBoundsPaint.setStrokeWidth(4);
        mTextBoundsPaint.setColor(Color.GREEN);

        mPointPaint = new Paint();
        mPointPaint.setColor(Color.BLUE);

        mTextBounds = new Rect();

        mBitmapBoundsPaint = new Paint();
        mBitmapBoundsPaint.setStyle(Paint.Style.STROKE);
        mBitmapBoundsPaint.setStrokeWidth(4);
        mBitmapBoundsPaint.setColor(Color.MAGENTA);

        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        if (once) {

            Drawable d = getDrawable();
            if (d == null) {
                return;
            }
            int width = getWidth();
            int height = getHeight();
            // 拿到图片的宽和高
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();
            float scale = Math.min(width * 1f / dw, height * 1f / dh);
            initScale = scale;
            // 图片移动至屏幕中心
            scaleMatrix.postTranslate((width - dw) / 2f, (height - dh) / 2f);
            scaleMatrix.postScale(scale, scale, width / 2f, height / 2f);
            setImageMatrix(scaleMatrix);
            once = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable d = getDrawable();
        if (d == null) {
            return;
        }
        int width = getWidth();
        int height = getHeight();
        float dw = d.getIntrinsicWidth() * initScale;
        float dh = d.getIntrinsicHeight() * initScale;
        canvas.drawRect((width - dw) / 2f,
                (height - dh) / 2f,
                (width - dw) / 2f + dw,
                (height - dh) / 2f + dh,
                mBitmapBoundsPaint);

        mTextPaint.getTextBounds(mWatermarkText, 0, mWatermarkText.length(), mTextBounds);

        int verticalSpacing = Math.max(mTextBounds.width(), mTextBounds.height()) - mTextBounds.height() + mSpacing;
        int horizontalSpacing = mSpacing;

        // 计算列数
        int columns = (int) Math.ceil(getWidth() * 1.0f / mTextBounds.width());
        // 计算行数
        int rows = (int) Math.ceil(getHeight() * 1.0f / verticalSpacing);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                canvas.save();
                int left = mTextBounds.width() * j + horizontalSpacing * j;
                int top = mTextBounds.height() * i + verticalSpacing * i;
                canvas.rotate(mDegrees, left + mTextBounds.width() / 2f, top + mTextBounds.height() / 2f);
                canvas.drawText(mWatermarkText, 0, mWatermarkText.length(), left, top + mTextBounds.height(), mTextPaint);
                canvas.drawRect(left, top,
                        left + mTextBounds.width(), top + mTextBounds.height(),
                        mTextBoundsPaint);
                canvas.drawCircle(left + mTextBounds.width() / 2f, top + mTextBounds.height() / 2f,
                        5, mPointPaint);
                canvas.restore();
            }
        }

    }

    public void setWatermarkRotation(int degrees) {
        mDegrees = degrees;
        invalidate();
    }

    public void setWatermarkColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    public void setWatermarkText(String text) {
        mWatermarkText = text;
        invalidate();
    }

    public void setWatermarkTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
        invalidate();
    }

    public void setWatermarkSpacing(int spacing) {
        mSpacing = spacing;
        invalidate();
    }
}
