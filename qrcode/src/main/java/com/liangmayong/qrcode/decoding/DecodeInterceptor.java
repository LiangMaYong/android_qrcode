package com.liangmayong.qrcode.decoding;

/**
 * Created by LiangMaYong on 2017/1/10.
 */

public interface DecodeInterceptor {
    boolean onDecode(byte[] data, int width, int height);
}
