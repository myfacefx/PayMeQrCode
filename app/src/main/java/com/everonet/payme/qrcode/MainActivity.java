package com.everonet.payme.qrcode;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import qrcode.PayCode;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String text = "https://qr.payme.hsbc.com.hk/2/ThisIsAnExamplePayCode";

    private ConstraintLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.container);

        PayCode payCode = new PayCode(this, text);
        container.addView(payCode);
    }


    private int suggestTypeNumber(String text) {
        int length = text.length();
        Log.i(TAG, "text length : " + length);
        if (length <= 32) {
            return 3;
        } else if (length <= 46) {
            return 4;
        } else if (length <= 60) {
            return 5;
        } else if (length <= 74) {
            return 6;
        } else if (length <= 86) {
            return 7;
        } else if (length <= 108) {
            return 8;
        } else if (length <= 130) {
            return 9;
        } else if (length <= 151) {
            return 10;
        } else if (length <= 177) {
            return 11;
        } else if (length <= 203) {
            return 12;
        } else if (length <= 241) {
            return 13;
        } else if (length <= 258) {
            return 14;
        } else if (length <= 292) {
            return 15;
        } else {
            return 40;
        }
    }

    private Bitmap big(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(5f, 5f);//长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }
}
