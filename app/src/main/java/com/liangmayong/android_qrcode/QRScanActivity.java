package com.liangmayong.android_qrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.zxing.Result;
import com.liangmayong.base.BaseActivity;
import com.liangmayong.base.support.binding.annotations.BindTitle;
import com.liangmayong.base.widget.iconfont.Icon;
import com.liangmayong.qrcode.view.DecodeScanView;

/**
 * QRScanActivity
 */
@BindTitle("Scan")
public class QRScanActivity extends BaseActivity {
    private DecodeScanView scanView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        scanView = (DecodeScanView) findViewById(R.id.scanView);
        scanView.setOnDecodeScanListener(new DecodeScanView.OnDecodeScanListener() {
            @Override
            public boolean handleDecode(Result result, Bitmap barcode) {
                return false;
            }
        });
        getDefaultToolbar().leftOne().iconToLeft(Icon.icon_back).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getDefaultToolbar().rightOne().text("Flash").clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scanView.isEnableFlash()) {
                    scanView.disableFlash();
                    Log.e("TAG", "disableFlash:" + scanView.getFlashMode());
                } else {
                    scanView.enableFlash();
                    Log.e("TAG", "enableFlash:" + scanView.getFlashMode());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanView.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanView.onPause(this);
    }

    @Override
    protected void onDestroy() {
        scanView.onDestroy(this);
        super.onDestroy();
    }
}