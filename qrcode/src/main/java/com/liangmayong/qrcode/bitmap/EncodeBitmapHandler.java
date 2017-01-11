package com.liangmayong.qrcode.bitmap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by LiangMaYong on 2017/1/5.
 */
public class EncodeBitmapHandler {


    private static volatile EncodeBitmapHandler ourInstance = null;

    public static EncodeBitmapHandler getInstance() {
        if (ourInstance == null) {
            synchronized (EncodeBitmapHandler.class) {
                ourInstance = new EncodeBitmapHandler();
            }
        }
        return ourInstance;
    }


    public interface OnEncodeBitmapListener {
        void onEncode(Bitmap bitmap);
    }

    private class MessageEncode {
        private Bitmap bitmap = null;
        private OnEncodeBitmapListener encodeBitmapListener = null;

        public MessageEncode(Bitmap bitmap, OnEncodeBitmapListener encodeBitmapListener) {
            this.bitmap = bitmap;
            this.encodeBitmapListener = encodeBitmapListener;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null && msg.obj instanceof MessageEncode) {
                MessageEncode messageEncode = (MessageEncode) msg.obj;
                messageEncode.encodeBitmapListener.onEncode(messageEncode.bitmap);
            }
        }
    };


    private final Map<EncodeHintType, Object> HINTS = new EnumMap<>(EncodeHintType.class);

    private EncodeBitmapHandler() {
        HINTS.put(EncodeHintType.CHARACTER_SET, "utf-8");
        HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        HINTS.put(EncodeHintType.MARGIN, 1);
    }

    public void encode(String content, int width, int height, OnEncodeBitmapListener listener) {
        encode(content, width, height, 0xff333333, 0xffffffff, listener);
    }

    public void encode(String content, int width, int height, int qrColor, int bgColor, OnEncodeBitmapListener listener) {
        encode(content, width, height, qrColor, bgColor, (Bitmap) null, listener);
    }

    public void encode(String content, int width, int height, int qrColor, int bgColor, Drawable drawable, OnEncodeBitmapListener listener) {
        encode(content, width, height, qrColor, bgColor, drawableToBitamp(drawable), listener);
    }

    public void encode(final String content, final int width, final int height, final int qrColor, final int bgColor, final Bitmap logo, final OnEncodeBitmapListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    if (content == null || "".equals(content) || content.length() < 1) {
                        return;
                    }
                    BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, HINTS);
                    int[] pixels = new int[width * height];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            if (bitMatrix.get(x, y)) {
                                pixels[y * width + x] = qrColor;
                            } else {
                                pixels[y * width + x] = bgColor;
                            }
                        }
                    }
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
                    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
                    if (logo != null) {
                        int x = width * 15 / 40;
                        int y = height * 15 / 40;
                        int w = width * 5 / 20;
                        int h = height * 5 / 20;
                        Canvas canvas = new Canvas(bitmap);
                        drawImage(canvas, logo, x, y, w, h);
                    }
                    handler.obtainMessage(0, new MessageEncode(bitmap, listener)).sendToTarget();
                } catch (WriterException e) {
                }
                Looper.loop();
            }
        }).start();
    }


    private Bitmap drawableToBitamp(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    private void drawImage(Canvas canvas, Bitmap bitmap, int x, int y, int w, int h) {
        Rect dst = new Rect();
        dst.left = x;
        dst.top = y;
        dst.right = x + w;
        dst.bottom = y + h;
        canvas.drawBitmap(bitmap, null, dst, null);
        dst = null;
    }
}
