package com.liangmayong.qrcode.bitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;

import java.util.Hashtable;

/**
 * Created by LiangMaYong on 2017/1/5.
 */
final class DecodeBitmapReader {

    private MultiFormatReader reader;

    private Hashtable hints = null;

    public DecodeBitmapReader() {
        this(null);
    }

    public DecodeBitmapReader(Hashtable hints) {
        this.reader = new MultiFormatReader();
        this.hints = hints;
    }

    public Result decode(BinaryBitmap bitmap) throws ReaderException {
        if (hints == null) {
            hints = DecodeBitmapFormatManager.getAllFormats();
        }
        return reader.decode(bitmap, hints);
    }

    public Result decode(BinaryBitmap bitmap, Hashtable hashtable) throws ReaderException {
        if (hints == null) {
            hints = DecodeBitmapFormatManager.getAllFormats();
        }
        if (hashtable != null) {
            hints.putAll(hashtable);
        }
        return reader.decode(bitmap, hints);
    }

    public void reset() {
        reader.reset();
    }
}
