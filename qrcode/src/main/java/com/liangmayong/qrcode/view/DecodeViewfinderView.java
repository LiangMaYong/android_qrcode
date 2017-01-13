/*
 * Copyright (C) 2008 ZXing authors
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

package com.liangmayong.qrcode.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.liangmayong.qrcode.camera.CameraManager;

import java.util.Collection;
import java.util.HashSet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 */
public final class DecodeViewfinderView extends View {

    private static final long ANIMATION_DELAY = 10L;
    private static final int OPAQUE = 0xFF;
    private int screenRate;
    private static final int CORNER_WIDTH = 10;
    private static final int MIDDLE_LINE_WIDTH = 6;
    private static final int SPEEN_DISTANCE = 5;
    private Paint paint;
    private float slideTop;
    private float slideLeft;
    private Bitmap resultBitmap;
    private int maskColor = 0x60000000;
    private int scanColor = 0xff3399ff;
    private int resultColor = 0xb0000000;
    private int rectColor = 0x99ffffff;
    private int gridColor = 0x30ffffff;
    private int resultPointColor = 0xc0ffff00;
    private int framePadding = 10;
    private boolean isVertical = true;
    private float weight_width = 1f;
    private float weight_height = 1f;
    private int gridCount = 15;


    public void setScanColor(int scanColor) {
        this.scanColor = scanColor;
    }

    public void setRectColor(int rectColor) {
        this.rectColor = rectColor;
    }

    public void setGridColor(int gridColor) {
        this.gridColor = gridColor;
    }

    public void setFramePadding(int framePadding) {
        this.framePadding = framePadding;
    }

    public void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    public void setScreenRate(int screenRate) {
        this.screenRate = screenRate;
    }

    public void setRectWeight(float width, float height) {
        this.weight_width = width;
        this.weight_height = height;
    }

    public void setVertical(boolean vertical) {
        isVertical = vertical;
    }

    public boolean isVertical() {
        return isVertical;
    }

    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;

    private boolean isUp = false;
    private boolean isFirst;

    public DecodeViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        framePadding = dip2px(context, 15);
        screenRate = dip2px(context, 15);
        paint = new Paint();
        possibleResultPoints = new HashSet<ResultPoint>(5);
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        RectF frame = new RectF(CameraManager.get().getFramingRect());
        if (frame.bottom > canvas.getHeight() - framePadding) {
            frame.bottom = canvas.getHeight() - framePadding;
        }
        if (frame.top < framePadding) {
            frame.top = framePadding;
        }
        frame.left = frame.left + framePadding;
        frame.right = frame.right - framePadding;
        frame.top = frame.top + framePadding;
        frame.bottom = frame.bottom - framePadding;
        float absw = Math.abs(frame.right - frame.left);
        float absh = Math.abs(frame.bottom - frame.top);
        float w = absw;
        float h = absh;
        if (w * weight_height != h * weight_width) {
            h = w * weight_height / weight_width;
        }
        if (h > absh) {
            w = absh * weight_width / weight_height;
            h = absh;
        }
        if (h < absh) {
            frame.top += (absh - h) / 2;
            frame.bottom -= (absh - h) / 2;
        }
        if (w < absw) {
            frame.left += (absw - w) / 2;
            frame.right -= (absw - w) / 2;
        }
        if (frame == null) {
            return;
        }
        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top;
            slideLeft = frame.left;
        }

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
                paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
            drawRect(canvas, frame);
            drawScanLine(canvas, frame);
            drawGrid(canvas, frame);
            drawResultPoint(canvas, frame);
            drawScreenRate(canvas, frame);
            postInvalidateDelayed(ANIMATION_DELAY, (int) frame.left, (int) frame.top,
                    (int) frame.right, (int) frame.bottom);
        }
    }

    private void drawScanLine(Canvas canvas, RectF frame) {
        if (isVertical) {
            paint.setColor(scanColor);
            if (isUp) {
                slideTop -= SPEEN_DISTANCE;
            } else {
                slideTop += SPEEN_DISTANCE;
            }
            if (slideTop >= frame.bottom) {
                isUp = true;
            }
            if (slideTop <= frame.top) {
                isUp = false;
            }
            RectF rectF = new RectF(frame.left, slideTop - MIDDLE_LINE_WIDTH / 2,
                    frame.right, slideTop + MIDDLE_LINE_WIDTH / 2);
            paint.setAntiAlias(true);
            canvas.drawOval(rectF, paint);
        } else {
            paint.setColor(scanColor);
            if (isUp) {
                slideLeft -= SPEEN_DISTANCE;
            } else {
                slideLeft += SPEEN_DISTANCE;
            }
            if (slideLeft >= frame.right) {
                isUp = true;
            }
            if (slideLeft <= frame.left) {
                isUp = false;
            }
            RectF rectF = new RectF(slideLeft - MIDDLE_LINE_WIDTH / 2, frame.top,
                    slideLeft + MIDDLE_LINE_WIDTH / 2, frame.bottom);
            paint.setAntiAlias(true);
            canvas.drawOval(rectF, paint);
        }
    }

    private void drawRect(Canvas canvas, RectF frame) {
        paint.setColor(rectColor);
        canvas.drawLine(frame.left, frame.top, frame.right, frame.top,
                paint);
        canvas.drawLine(frame.left, frame.bottom, frame.right,
                frame.bottom, paint);
        canvas.drawLine(frame.left, frame.top, frame.left, frame.bottom,
                paint);
        canvas.drawLine(frame.right, frame.top, frame.right, frame.bottom,
                paint);
    }

    private void drawResultPoint(Canvas canvas, RectF frame) {
        Collection<ResultPoint> currentPossible = possibleResultPoints;
        Collection<ResultPoint> currentLast = lastPossibleResultPoints;
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new HashSet<ResultPoint>(5);
            lastPossibleResultPoints = currentPossible;
            paint.setAlpha(OPAQUE);
            paint.setColor(resultPointColor);
            for (ResultPoint point : currentPossible) {
                canvas.drawCircle(frame.left + point.getX(), frame.top
                        + point.getY(), 6.0f, paint);
            }
        }
        if (currentLast != null) {
            paint.setAlpha(OPAQUE / 2);
            paint.setColor(resultPointColor);
            for (ResultPoint point : currentLast) {
                canvas.drawCircle(frame.left + point.getX(), frame.top
                        + point.getY(), 3.0f, paint);
            }
        }
    }

    private void drawScreenRate(Canvas canvas, RectF frame) {
        paint.setColor(scanColor);

        // left - top
        canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.top - CORNER_WIDTH
                        / 2, frame.left + screenRate, frame.top + CORNER_WIDTH / 2,
                paint);
        canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.top - CORNER_WIDTH
                        / 2, frame.left + CORNER_WIDTH / 2, frame.top + screenRate,
                paint);

        // right - top
        canvas.drawRect(frame.right - screenRate, frame.top - CORNER_WIDTH / 2,
                frame.right + CORNER_WIDTH / 2, frame.top + CORNER_WIDTH / 2,
                paint);
        canvas.drawRect(frame.right - CORNER_WIDTH / 2, frame.top
                - CORNER_WIDTH / 2, frame.right + CORNER_WIDTH / 2, frame.top
                + screenRate, paint);

        // left - bottom
        canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.bottom
                - CORNER_WIDTH / 2, frame.left + screenRate, frame.bottom
                + CORNER_WIDTH / 2, paint);

        canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.bottom
                - screenRate, frame.left + CORNER_WIDTH / 2, frame.bottom
                + CORNER_WIDTH / 2, paint);

        // right - bottom
        canvas.drawRect(frame.right - screenRate, frame.bottom - CORNER_WIDTH
                / 2, frame.right + CORNER_WIDTH / 2, frame.bottom
                + CORNER_WIDTH / 2, paint);
        canvas.drawRect(frame.right - CORNER_WIDTH / 2, frame.bottom
                - screenRate, frame.right + CORNER_WIDTH / 2, frame.bottom
                + CORNER_WIDTH / 2, paint);
    }

    private void drawGrid(Canvas canvas, RectF frame) {
        paint.setColor(gridColor);
        if (isVertical) {
            float vreta = weight_height / weight_width;
            float hreta = 1;
            if (vreta < 1) {
                vreta = 1;
                hreta = weight_width / weight_height;
            }
            final float vspace = Math.abs(frame.bottom - frame.top) / gridCount * hreta;
            final float hspace = Math.abs(frame.right - frame.left) / gridCount * vreta;
            float vertz = vspace;
            float hortz = hspace;
            float mtop = 0;
            float mbottom = 0;
            if (isUp) {
                mtop = slideTop;
                mbottom = frame.bottom;
            } else {
                mtop = frame.top;
                mbottom = slideTop;
            }
            for (int i = 0; i < gridCount * Math.max(vreta, hreta); i++) {
                if (isUp) {
                    if (frame.bottom - vertz > mtop) {
                        canvas.drawLine(frame.left, frame.bottom - vertz,
                                frame.right, frame.bottom - vertz, paint);
                    }
                } else {
                    if (frame.top + vertz < mbottom) {
                        canvas.drawLine(frame.left, frame.top + vertz, frame.right,
                                frame.top + vertz, paint);
                    }
                }
                if (frame.left + hortz < frame.right) {
                    canvas.drawLine(frame.left + hortz, mtop, frame.left + hortz,
                            mbottom, paint);
                }
                vertz += vspace;
                hortz += hspace;
            }
        } else {
            float vreta = weight_height / weight_width;
            float hreta = 1;
            if (vreta < 1) {
                vreta = 1;
                hreta = weight_width / weight_height;
            }
            final float vspace = Math.abs(frame.right - frame.left) / gridCount * vreta;
            final float hspace = Math.abs(frame.bottom - frame.top) / gridCount * hreta;
            float vertz = vspace;
            float hortz = hspace;
            float mtop = 0;
            float mbottom = 0;
            if (isUp) {
                mtop = slideLeft;
                mbottom = frame.right;
            } else {
                mtop = frame.left;
                mbottom = slideLeft;
            }
            for (int i = 0; i < gridCount * Math.max(vreta, hreta);
                 i++) {
                if (!isUp) {
                    if (frame.left + vertz < mbottom) {
                        canvas.drawLine(frame.left + vertz, frame.top,
                                frame.left + vertz, frame.bottom, paint);
                    }
                } else {
                    if (frame.right - vertz >= mtop) {
                        canvas.drawLine(frame.right - vertz, frame.top, frame.right - vertz,
                                frame.bottom, paint);
                    }
                }
                if (frame.top + hortz < frame.bottom) {
                    canvas.drawLine(mtop, frame.top + hortz, mbottom, frame.top + hortz,
                            paint);
                }
                vertz += vspace;
                hortz += hspace;
            }
        }
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static final class DecodeViewfinderResultPointCallback implements ResultPointCallback {

        private final DecodeViewfinderView viewfinderView;

        public DecodeViewfinderResultPointCallback(DecodeViewfinderView viewfinderView) {
            this.viewfinderView = viewfinderView;
        }

        public void foundPossibleResultPoint(ResultPoint point) {
            viewfinderView.addPossibleResultPoint(point);
        }

    }
}
