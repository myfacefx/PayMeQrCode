package qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import com.everonet.payme.qrcode.R;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.PorterDuff.Mode.SRC_IN;

public class PayCode extends View {

    private static final String TAG = PayCode.class.getSimpleName();

    private static final int color = Color.rgb(219, 0, 17);
    private static final float logoPercent = 0.25f;
    private static final float iconPercent = 0.12f;
    private static final float logoClipPercent = 0.27f;
    private static final float iconClipPercent = 0.13f;

    private float width = 500;
    private float margin = 16;

    private String text;
    private Paint paintDot;
    private Paint paintEye;
    private Paint paintBg;
    private QRCode qrCode;
    private Bitmap bitmapLogo = BitmapFactory.decodeResource(getResources(), R.drawable.logo_payme);
    private Bitmap bitmapIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_payme);

    public PayCode(Context context, String text) {
        super(context);
        this.text = text;

        init();
    }

    private void init() {
        paintDot = new Paint();
        paintEye = new Paint();
        paintBg = new Paint();
        qrCode = new QRCode();
        qrCode.setTypeNumber(6);
        qrCode.setErrorCorrectionLevel(ErrorCorrectionLevel.H);
        qrCode.addData(text);
        qrCode.make();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int cellCount = qrCode.getModuleCount();
        Log.i(TAG, "cellCount : " + cellCount);
        float cellRadius = ((width - (margin * 2)) / cellCount) / 2;
        float offset = cellRadius + margin;
        float logoLeftClip = (width / 2) - (((width - 2 * margin) * logoClipPercent) / 2);
        float logoRightClip = (width / 2) + (((width - 2 * margin) * logoClipPercent) / 2);
        float iconClip = width - cellRadius - margin - ((width - 2 * margin) * iconClipPercent);
        //设置画笔颜色
        paintDot.setColor(color);
        //让画出的图形是实心的
        paintDot.setStyle(Paint.Style.FILL);
        //设置画笔颜色
        paintEye.setColor(color);
        //让画出的图形是空心的
        paintEye.setStyle(Paint.Style.STROKE);
        //设置画笔粗细
        paintEye.setStrokeWidth(2.5f * cellRadius);

        // white background
        paintBg.setColor(Color.WHITE);
        canvas.drawRect(0, 0, width, width, paintBg);

        // top left eye
        canvas.drawRoundRect(offset, offset, offset + 12 * cellRadius, offset + 12 * cellRadius, cellRadius, cellRadius, paintEye);
        canvas.drawRoundRect(offset + 3 * cellRadius, offset + 3 * cellRadius, offset + 9 * cellRadius, offset + 9 * cellRadius, cellRadius, cellRadius, paintDot);

        // top right eye
        float offsetY = offset + 2 * (cellCount - 7) * cellRadius;
        canvas.drawRoundRect(offsetY, offset, offsetY + 12 * cellRadius, offset + 12 * cellRadius, cellRadius, cellRadius, paintEye);
        canvas.drawRoundRect(offsetY + 3 * cellRadius, offset + 3 * cellRadius, offsetY + 9 * cellRadius, offset + 9 * cellRadius, cellRadius, cellRadius, paintDot);

        // bottom left eye
        float offsetX = offset + 2 * (cellCount - 7) * cellRadius;
        canvas.drawRoundRect(offset, offsetX, offset + 12 * cellRadius, offsetX + 12 * cellRadius, cellRadius, cellRadius, paintEye);
        canvas.drawRoundRect(offset + 3 * cellRadius, offsetX + 3 * cellRadius, offset + 9 * cellRadius, offsetX + 9 * cellRadius, cellRadius, cellRadius, paintDot);

        // PayMe icon in bottom right
        float iconWidth = ((width - 2 * margin) * iconPercent);
        float ratio = iconWidth / bitmapIcon.getWidth();
        float xOffset = width - margin - iconWidth;
        float yOffset = width - margin - iconWidth;
        Bitmap icon = scaleBitmap(bitmapIcon, ratio);
        if (icon != null) {
            // Apple design guidelines state that corner radius is 80px for a 512px icon
            float radius = iconWidth * (80f / 512);
            canvas.drawBitmap(getRoundedCornerBitmap(icon, radius), xOffset, yOffset, paintDot);
        }
        // business logo in the middle
        float logoWidth = ((width - 2 * margin) * logoPercent);
        float ratioLogo = logoWidth / bitmapLogo.getWidth();
        Bitmap logo = scaleBitmap(bitmapLogo, ratioLogo);
        if (logo != null) {
            canvas.drawBitmap(logo, (width - logoWidth) / 2, (width - logoWidth) / 2, paintDot);
        }

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

                boolean isDark = qrCode.isDark(r, c);
                Log.i(TAG, isDark ? "1" : "0");
                if (isDark) {
                    canvas.drawCircle(x, y, cellRadius, paintDot);
                }
            }

        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthPixels = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightPixels = View.MeasureSpec.getSize(heightMeasureSpec);
        Log.i(TAG, "widthPixels=" + widthPixels + " , heightPixels=" + heightPixels);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 按比例缩放图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    private Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 将图片处理成圆角
     *
     * @param bitmap
     * @param roundPx
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, final float roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
