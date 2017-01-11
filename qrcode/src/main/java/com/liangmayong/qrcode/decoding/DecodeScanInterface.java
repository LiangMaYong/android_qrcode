package com.liangmayong.qrcode.decoding;

import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;
import com.liangmayong.qrcode.view.DecodeViewfinderView;

/**
 * Created by LiangMaYong on 2017/1/9.
 */

public interface DecodeScanInterface {

    DecodeViewfinderView getViewfinderView();

    Handler getHandler();

    void handleDecode(Result result, Bitmap barcode, long decode_time);

    void onDrawViewfinder();

    DecodeInterceptor getDecodeInterceptor();

}
