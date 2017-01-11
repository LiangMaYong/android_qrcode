package com.liangmayong.qrcode.bitmap;

import android.graphics.Bitmap;

import com.google.zxing.RGBLuminanceSource;

/**
 * Created by LiangMaYong on 2017/1/5.
 */
final class DecodeBitmapLuminanceSource {

    public static RGBLuminanceSource create(Bitmap bitmap) {
        Bitmap scanBitmap = Bitmap.createBitmap(bitmap);
        int px[] = new int[scanBitmap.getWidth() * scanBitmap.getHeight()];
        scanBitmap.getPixels(px, 0, scanBitmap.getWidth(), 0, 0,
                scanBitmap.getWidth(), scanBitmap.getHeight());
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(scanBitmap.getWidth(), scanBitmap.getHeight(), px);
        scanBitmap.recycle();
        scanBitmap = null;
        return rgbLuminanceSource;
    }
}
