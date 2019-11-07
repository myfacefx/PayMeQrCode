package com.everonet.payme.qrcode;

import android.app.Activity;
import android.os.Bundle;

import qrcode.PayCode;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String text = "https://sacctprodmobsandbox.z7.web.core.windows.net//d144dd62-1aa0-4c1a-9ea5-188f96cf171b";

    private PayCode payCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        payCode = findViewById(R.id.pay_code);
        payCode.drawQrCode(text, R.drawable.logo_payme);
    }
}
