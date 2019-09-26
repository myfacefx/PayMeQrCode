package com.everonet.payme.qrcode;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import qrcode.PayCode;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String text = "https://qr.payme.hsbc.com.hk/2/ThisIsAnExamplePayCode";

    private PayCode payCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        payCode = findViewById(R.id.pay_code);
        payCode.drawQrCode(text, R.drawable.logo_payme);
    }
}
