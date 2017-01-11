package com.liangmayong.qrcode.bitmap;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;

/**
 * Created by LiangMaYong on 2017/1/5.
 */
public final class DecodeBitmapHandler {

    private static volatile DecodeBitmapHandler ourInstance = null;

    public static DecodeBitmapHandler getInstance() {
        if (ourInstance == null) {
            synchronized (DecodeBitmapHandler.class) {
                ourInstance = new DecodeBitmapHandler();
            }
        }
        return ourInstance;
    }

    private static final String TAG = DecodeBitmapHandler.class.getSimpleName();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////


    private DecodeBitmapHandler() {
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null && msg.obj instanceof MessageDecode) {
                MessageDecode messageDecode = (MessageDecode) msg.obj;
                messageDecode.decodeListener.onDecode(messageDecode.result);
            }
        }
    };

    public interface OnDecodeBitmapListener {
        void onDecode(Result result);
    }

    private class MessageDecode {
        private Result result = null;
        private OnDecodeBitmapListener decodeListener = null;

        public MessageDecode(Result result, OnDecodeBitmapListener decodeListener) {
            this.result = result;
            this.decodeListener = decodeListener;
        }
    }

    public void decode(final Bitmap bitmap, final OnDecodeBitmapListener decodeListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                threadDecode(DecodeBitmapLuminanceSource.create(bitmap), decodeListener);
            }
        }).start();
    }

    private void threadDecode(final LuminanceSource source, final OnDecodeBitmapListener decodeListener) {
        Looper.prepare();
        long start = System.currentTimeMillis();
        Result rawResult = null;
        DecodeBitmapReader decodeReader = new DecodeBitmapReader();
        Hashtable hashtable = new Hashtable();
        try {
            if (source != null) {
                rawResult = decodeReader.decode(new BinaryBitmap(new GlobalHistogramBinarizer(source)), hashtable);
            }
        } catch (Exception re) {
            try {
                if (source != null) {
                    rawResult = decodeReader.decode(new BinaryBitmap(new HybridBinarizer(source)), hashtable);
                }
            } catch (Exception r) {
            }
        } finally {
            decodeReader.reset();
            decodeReader = null;
        }
        if (rawResult != null) {
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
        }
        handler.obtainMessage(0, new MessageDecode(rawResult, decodeListener)).sendToTarget();
        Looper.loop();
    }
}
