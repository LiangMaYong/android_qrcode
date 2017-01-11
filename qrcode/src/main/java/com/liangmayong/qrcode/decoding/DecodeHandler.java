/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liangmayong.qrcode.decoding;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.liangmayong.qrcode.R;
import com.liangmayong.qrcode.camera.CameraManager;
import com.liangmayong.qrcode.camera.PlanarYUVLuminanceSource;

import java.util.Hashtable;

final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final DecodeScanInterface scanInterface;
    private final MultiFormatReader multiFormatReader;

    DecodeHandler(DecodeScanInterface activity, Hashtable<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.scanInterface = activity;
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.decode) {
            decode((byte[]) message.obj, message.arg1, message.arg2);
        } else if (message.what == R.id.decode_quit) {
            Looper.myLooper().quit();
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        if (scanInterface.getDecodeInterceptor() != null) {
            boolean flag = scanInterface.getDecodeInterceptor().onDecode(data, width, height);
            if (flag) {
                return;
            }
        }
        long start = System.currentTimeMillis();
        Result rawResult = null;

        //modify here
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
        BinaryBitmap binaryBitmap;
        try {
            binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            rawResult = multiFormatReader.decodeWithState(binaryBitmap);
        } catch (Exception re) {
            // continue
            try {
                binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                rawResult = multiFormatReader.decodeWithState(binaryBitmap);
            } catch (Exception e) {
            }
        } finally {
            multiFormatReader.reset();
        }

        if (rawResult != null) {
            long end = System.currentTimeMillis();
            long time = end - start;
            Log.d(TAG, "Found barcode (" + time + " ms):\n" + rawResult.toString());
            Log.d(TAG, "Found barcode Format:\n" + rawResult.getBarcodeFormat().name());
            Message message = Message.obtain(scanInterface.getHandler(), R.id.decode_succeeded, rawResult);
            Bundle bundle = new Bundle();
            Bitmap bitmap = source.renderCroppedGreyscaleBitmap();
            if (bitmap != null) {
                bundle.putParcelable(DecodeThread.BARCODE_BITMAP, bitmap);
            }
            bundle.putLong(DecodeThread.DERCODE_TIME, time);
            message.setData(bundle);
            message.sendToTarget();
        } else {
            Message message = Message.obtain(scanInterface.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
    }

}
