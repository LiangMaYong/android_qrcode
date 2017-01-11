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

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import android.content.Intent;
import android.net.Uri;
import com.google.zxing.BarcodeFormat;

final class DecodeFormatManager {

  private static final Pattern COMMA_PATTERN = Pattern.compile(",");

  static final Vector<BarcodeFormat> PRODUCT_FORMATS;
  static final Vector<BarcodeFormat> ONE_D_FORMATS;
  static final Vector<BarcodeFormat> QR_CODE_FORMATS;
  static final Vector<BarcodeFormat> DATA_MATRIX_FORMATS;
  static {
    PRODUCT_FORMATS = new Vector<BarcodeFormat>(5);
    PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
    PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
    PRODUCT_FORMATS.add(BarcodeFormat.EAN_13);
    PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
    PRODUCT_FORMATS.add(BarcodeFormat.RSS_14);
    ONE_D_FORMATS = new Vector<BarcodeFormat>(PRODUCT_FORMATS.size() + 4);
    ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
    ONE_D_FORMATS.add(BarcodeFormat.CODE_39);
    ONE_D_FORMATS.add(BarcodeFormat.CODE_93);
    ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
    ONE_D_FORMATS.add(BarcodeFormat.ITF);
    QR_CODE_FORMATS = new Vector<BarcodeFormat>(1);
    QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
    DATA_MATRIX_FORMATS = new Vector<BarcodeFormat>(1);
    DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX);
  }

  private DecodeFormatManager() {}

  static Vector<BarcodeFormat> parseDecodeFormats(Intent intent) {
    List<String> scanFormats = null;
    String scanFormatsString = intent.getStringExtra(Scan.SCAN_FORMATS);
    if (scanFormatsString != null) {
      scanFormats = Arrays.asList(COMMA_PATTERN.split(scanFormatsString));
    }
    return parseDecodeFormats(scanFormats, intent.getStringExtra(Scan.MODE));
  }

  static Vector<BarcodeFormat> parseDecodeFormats(Uri inputUri) {
    List<String> formats = inputUri.getQueryParameters(Scan.SCAN_FORMATS);
    if (formats != null && formats.size() == 1 && formats.get(0) != null){
      formats = Arrays.asList(COMMA_PATTERN.split(formats.get(0)));
    }
    return parseDecodeFormats(formats, inputUri.getQueryParameter(Scan.MODE));
  }

  private static Vector<BarcodeFormat> parseDecodeFormats(Iterable<String> scanFormats,
                                                          String decodeMode) {
    if (scanFormats != null) {
      Vector<BarcodeFormat> formats = new Vector<BarcodeFormat>();
      try {
        for (String format : scanFormats) {
          formats.add(BarcodeFormat.valueOf(format));
        }
        return formats;
      } catch (IllegalArgumentException iae) {
        // ignore it then
      }
    }
    if (decodeMode != null) {
      if (Scan.PRODUCT_MODE.equals(decodeMode)) {
        return PRODUCT_FORMATS;
      }
      if (Scan.QR_CODE_MODE.equals(decodeMode)) {
        return QR_CODE_FORMATS;
      }
      if (Scan.DATA_MATRIX_MODE.equals(decodeMode)) {
        return DATA_MATRIX_FORMATS;
      }
      if (Scan.ONE_D_MODE.equals(decodeMode)) {
        return ONE_D_FORMATS;
      }
    }
    return null;
  }



  public static final class Scan {
    /**
     * Send this intent to open the Barcodes app in scanning mode, find a barcode, and return
     * the results.
     */
    public static final String ACTION = "com.google.zxing.client.android.SCAN";

    /**
     * By default, sending Scan.ACTION will decode all barcodes that we understand. However it
     * may be useful to limit scanning to certain formats. Use Intent.putExtra(MODE, value) with
     * one of the values below ({@link #PRODUCT_MODE}, {@link #ONE_D_MODE}, {@link #QR_CODE_MODE}).
     * Optional.
     *
     * Setting this is effectively shorthnad for setting explicit formats with {@link #SCAN_FORMATS}.
     * It is overridden by that setting.
     */
    public static final String MODE = "SCAN_MODE";

    /**
     * Comma-separated list of formats to scan for. The values must match the names of
     * {@link BarcodeFormat}s, such as {@link BarcodeFormat#EAN_13}.
     * Example: "EAN_13,EAN_8,QR_CODE"
     *
     * This overrides {@link #MODE}.
     */
    public static final String SCAN_FORMATS = "SCAN_FORMATS";

    /**
     * Decode only UPC and EAN barcodes. This is the right choice for shopping apps which get
     * prices, reviews, etc. for products.
     */
    public static final String PRODUCT_MODE = "PRODUCT_MODE";

    /**
     * Decode only 1D barcodes (currently UPC, EAN, Code 39, and Code 128).
     */
    public static final String ONE_D_MODE = "ONE_D_MODE";

    /**
     * Decode only QR codes.
     */
    public static final String QR_CODE_MODE = "QR_CODE_MODE";

    /**
     * Decode only Data Matrix codes.
     */
    public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";

    private Scan() {
    }
  }

}
