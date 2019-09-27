package qrcode;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.qrcode.R;

import androidx.annotation.Nullable;

public class PayCode extends View {

    private static final String TAG = PayCode.class.getSimpleName();

    private static final float logoPercent = 0.25f;
    private static final float iconPercent = 0.12f;
    private static final float logoClipPercent = 0.27f;
    private static final float iconClipPercent = 0.13f;

    private String text;
    private int color;
    private int backgroundColor;
    private float width;
    private float margin;
    private Bitmap bitmapLogo;
    private Bitmap bitmapIcon;

    private Paint paint;
    private QRCode qrCode;

    public PayCode(Context context) {
        this(context, null);
    }

    public PayCode(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PayCode(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PayCode);
        text = ta.getString(R.styleable.PayCode_text);
        color = ta.getColor(R.styleable.PayCode_color, Color.rgb(219, 0, 17));
        backgroundColor = ta.getColor(R.styleable.PayCode_backgroundColor, Color.WHITE);
        margin = ta.getDimension(R.styleable.PayCode_margin, 16);
        int resLogo = ta.getResourceId(R.styleable.PayCode_qrLogo, R.drawable.logo_payme);
        int resIcon = ta.getResourceId(R.styleable.PayCode_qrIcon, R.drawable.icon_payme);
        ta.recycle();

        bitmapLogo = BitmapFactory.decodeResource(getResources(), resLogo);
        bitmapIcon = BitmapFactory.decodeResource(getResources(), resIcon);

        paint = new Paint();
    }

    /**
     * generate QR code of text string
     * @param text the content
     * @param resId the image resource id
     */
    public void drawQrCode(String text, int resId) {
        drawQrCode(text, BitmapFactory.decodeResource(getResources(), resId));
    }

    /**
     * generate QR code of text string
     * @param text the content
     * @param bitmap the bitmap
     */
    public void drawQrCode(String text, Bitmap bitmap) {
        this.text = text;
        this.bitmapLogo = bitmap;
        invalidate();
    }

    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    public void setQrBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    public void setWidth(float width) {
        this.width = width;
        invalidate();
    }

    public void setMargin(float margin) {
        this.margin = margin;
        invalidate();
    }

    public void setBitmapLogo(Bitmap bitmapLogo) {
        this.bitmapLogo = bitmapLogo;
        invalidate();
    }

    public void setBitmapIcon(Bitmap bitmapIcon) {
        this.bitmapIcon = bitmapIcon;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //让画出的图形是实心的
        paint.setStyle(Paint.Style.FILL);

        // draw background color
        paint.setColor(backgroundColor);
        canvas.drawRect(0, 0, width, width, paint);

        if (TextUtils.isEmpty(text))
            return;

        qrCode = QRCode.getMinimumQRCode(text, ErrorCorrectionLevel.Q);

        int cellCount = qrCode.getModuleCount();
        Log.i(TAG, "cellCount : " + cellCount);
        float cellRadius = ((width - (margin * 2)) / cellCount) / 2;
        float offset = cellRadius + margin;
        float logoLeftClip = (width / 2) - (((width - 2 * margin) * logoClipPercent) / 2);
        float logoRightClip = (width / 2) + (((width - 2 * margin) * logoClipPercent) / 2);
        float iconClip = width - cellRadius - margin - ((width - 2 * margin) * iconClipPercent);

        // top left eye
        drawEye(canvas, margin, 0, 0, cellRadius);

        // top right eye
        float left = 2 * (cellCount - 7) * cellRadius;
        drawEye(canvas, margin, left, 0, cellRadius);

        // bottom left eye
        float top = 2 * (cellCount - 7) * cellRadius;
        drawEye(canvas, margin, 0, top, cellRadius);

        // PayMe icon in bottom right
        drawIcon(canvas);

        // business logo in the middle
        drawLogo(canvas);

        // draw dot
        for (int r = 0; r < cellCount; r++)
            for (int c = 0; c < cellCount; c++) {

                float x = c * cellRadius * 2 + offset;
                float y = r * cellRadius * 2 + offset;

                if (r < 7 && (c < 7 || c > cellCount - 8) || r > cellCount - 8 && c < 7) {
                    // don't draw cells over the "eyes"
                    continue;
                } else if (x >= iconClip && y >= iconClip) {
                    // don't draw cells over the icon
                    continue;
                } else if (x >= logoLeftClip && x < logoRightClip &&
                        y >= logoLeftClip && y < logoRightClip) {
                    // don't draw cells over the logo
                    continue;
                }

                if (qrCode.isDark(r, c)) {
                    //设置画笔颜色
                    paint.setColor(color);
                    canvas.drawCircle(x, y, cellRadius, paint);
                }
            }

        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMySize(500, widthMeasureSpec);
        int height = getMySize(500, heightMeasureSpec);

        if (width < height) {
            height = width;
            this.width = height;
        } else {
            width = height;
            this.width = width;
        }

        setMeasuredDimension(width, height);
    }

    /**
     * 获取尺寸
     *
     * @param defaultSize
     * @param measureSpec
     * @return
     */
    private int getMySize(int defaultSize, int measureSpec) {
        int mySize = defaultSize;

        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        Log.i(TAG, "mode=" + mode + " , size=" + size);

        switch (mode) {
            case MeasureSpec.UNSPECIFIED: {//如果没有指定大小，就设置为默认大小
                mySize = defaultSize;
                break;
            }
            case MeasureSpec.AT_MOST: //如果测量模式是最大取值为size
            case MeasureSpec.EXACTLY: {//如果是固定的大小，那就不要去改变它
                mySize = size;
                break;
            }
        }
        return mySize;
    }

    /**
     * 绘制定位眼
     *
     * @param canvas     画布
     * @param margin     与边框的距离
     * @param left       x 起点
     * @param top        y 起点
     * @param cellRadius 小圆点的半径
     */
    private void drawEye(Canvas canvas, float margin, float left, float top, float cellRadius) {

        // 绘制外圈
        paint.setColor(color); //设置画笔颜色
        final RectF out = new RectF(margin + left, margin + top, margin + left + 14 * cellRadius, margin + top + 14 * cellRadius);
        canvas.drawRoundRect(out, 2 * cellRadius, 2 * cellRadius, paint);

        // 绘制中间圈
        paint.setColor(backgroundColor);//设置画笔颜色
        final RectF middle = new RectF(margin + left + 2 * cellRadius, margin + top + 2 * cellRadius, margin + left + 12 * cellRadius, margin + top + 12 * cellRadius);
        canvas.drawRoundRect(middle, 2 * cellRadius, 2 * cellRadius, paint);

        // 绘制内圈
        paint.setColor(color);//设置画笔颜色
        final RectF inner = new RectF(margin + left + 4 * cellRadius, margin + top + 4 * cellRadius, margin + left + 10 * cellRadius, margin + top + 10 * cellRadius);
        canvas.drawRoundRect(inner, 2 * cellRadius, 2 * cellRadius, paint);
    }

    /**
     * 画右下角的图标
     *
     * @param canvas
     */
    private void drawIcon(Canvas canvas) {
        float iconWidth = ((width - 2 * margin) * iconPercent);
        float ratio = iconWidth / bitmapIcon.getWidth();
        float xOffset = width - margin - iconWidth;
        float yOffset = width - margin - iconWidth;
        Bitmap icon = BitmapUtil.scaleBitmap(bitmapIcon, ratio);
        if (icon != null) {
            // Apple design guidelines state that corner radius is 80px for a 512px icon
            float radius = iconWidth * (80f / 512);
            canvas.drawBitmap(BitmapUtil.getRoundedCornerBitmap(icon, radius), xOffset, yOffset, paint);
        }
    }

    /**
     * 画中间的图标
     *
     * @param canvas
     */
    private void drawLogo(Canvas canvas) {
        float logoWidth = ((width - 2 * margin) * logoPercent);
        float ratioLogo = logoWidth / bitmapLogo.getWidth();
        Bitmap logo = BitmapUtil.scaleBitmap(bitmapLogo, ratioLogo);
        if (logo != null) {
            canvas.drawBitmap(logo, (width - logoWidth) / 2, (width - logoWidth) / 2, paint);
        }
    }
}
