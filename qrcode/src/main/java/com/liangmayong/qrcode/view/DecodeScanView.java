package com.liangmayong.qrcode.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.liangmayong.qrcode.R;
import com.liangmayong.qrcode.camera.CameraManager;
import com.liangmayong.qrcode.decoding.DecodeCaptureViewHandler;
import com.liangmayong.qrcode.decoding.DecodeInterceptor;
import com.liangmayong.qrcode.decoding.DecodeScanInterface;
import com.liangmayong.qrcode.decoding.InactivityTimer;

import java.io.IOException;
import java.util.Vector;

public class DecodeScanView extends FrameLayout implements SurfaceHolder.Callback, DecodeScanInterface {

    public interface OnDecodeScanListener {
        boolean handleDecode(Result result, Bitmap barcode);
    }

    public interface OnOpenCameraListener {
        void onOpen(DecodeScanView scanView);
    }

    public interface OnResultIntentListener {
        Intent onResultIntent(Result result, Bitmap barcode);
    }

    private DecodeCaptureViewHandler handler;
    private DecodeViewfinderView viewfinderView;
    private SurfaceView surfaceView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean vibrate;
    private InactivityTimer inactivityTimer;
    private Activity activity;
    private OnDecodeScanListener decodeScanListener;
    private OnOpenCameraListener openCameraListener;
    private OnResultIntentListener resultIntentListener;
    private DecodeInterceptor interceptor;
    private float beepVolume = 0.10f;
    private long vibrateDuration = 200L;
    private int resultCode = Activity.RESULT_OK;

    public DecodeScanView(Context context) {
        super(context);
        init(context, null);
    }

    public DecodeScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DecodeScanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DecodeScanView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            CameraManager.init(getContext().getApplicationContext());
            RelativeLayout relativeLayout = new RelativeLayout(context);
            surfaceView = new SurfaceView(context, attrs);
            surfaceView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            relativeLayout.addView(surfaceView);
            viewfinderView = new DecodeViewfinderView(context, attrs);
            viewfinderView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            relativeLayout.addView(viewfinderView);
            relativeLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            addView(relativeLayout);
            hasSurface = false;
        } else {
            TextView textView = new TextView(context);
            textView.setText("DecodeScanView");
            textView.setTextSize(25);
            textView.setTextColor(0xffffffff);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            addView(textView);
            setBackgroundColor(0xff333333);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            CameraManager.get().requestAutoFocus();
        }
        return super.onTouchEvent(event);
    }

    /**
     * setScanColor
     *
     * @param scanColor scanColor
     */
    public void setScanColor(int scanColor) {
        if (viewfinderView != null) {
            viewfinderView.setScanColor(scanColor);
        }
    }

    /**
     * setRectColor
     *
     * @param rectColor rectColor
     */
    public void setRectColor(int rectColor) {
        if (viewfinderView != null) {
            viewfinderView.setRectColor(rectColor);
        }
    }

    /**
     * setResultCode
     *
     * @param resultCode resultCode
     */
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * getResultCode
     *
     * @return resultCode
     */
    public int getResultCode() {
        return resultCode;
    }

    /**
     * setBeepVolume
     *
     * @param beepVolume beepVolume default = 0.1f
     */
    public void setBeepVolume(float beepVolume) {
        this.beepVolume = beepVolume;
    }

    /**
     * setRectWeight
     *
     * @param width  width
     * @param height height
     */
    public void setRectWeight(float width, float height) {
        if (viewfinderView != null) {
            viewfinderView.setRectWeight(width, height);
        }
    }

    /**
     * setScanVertical
     *
     * @param vertical vertical
     */
    public void setScanVertical(boolean vertical) {
        if (viewfinderView != null) {
            viewfinderView.setVertical(vertical);
        }
    }


    /**
     * isScanVertical
     *
     * @return
     */
    public boolean isScanVertical() {
        if (viewfinderView != null) {
            return viewfinderView.isVertical();
        }
        return false;
    }

    /**
     * gridColor
     *
     * @param gridColor gridColor
     */
    public void setGridColor(int gridColor) {
        if (viewfinderView != null) {
            viewfinderView.setGridColor(gridColor);
        }
    }

    /**
     * setMaskColor
     *
     * @param maskColor maskColor
     */
    public void setMaskColor(int maskColor) {
        if (viewfinderView != null) {
            viewfinderView.setMaskColor(maskColor);
        }
    }

    /**
     * setFramePadding
     *
     * @param framePadding framePadding
     */
    public void setFramePadding(int framePadding) {
        if (viewfinderView != null) {
            viewfinderView.setFramePadding(framePadding);
        }
    }


    /**
     * getScreenWidth
     *
     * @return screen height
     */
    public int getScreenHeight() {
        return CameraManager.get().getScreenHeight();
    }

    /**
     * getScreenWidth
     *
     * @return screen width
     */
    public int getScreenWidth() {
        return CameraManager.get().getScreenWidth();
    }

    /**
     * setFramingMinAndMax
     *
     * @param minWidth  minWidth
     * @param minHeight minHeight
     * @param maxWidth  maxWidth
     * @param maxHeight maxHeight
     */
    public void setFramingMinAndMax(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        CameraManager.get().setFramingMinAndMax(minWidth, minHeight, maxWidth, maxHeight);
    }

    /**
     * setWithoutStatusBar
     *
     * @param withoutStatusBar withoutStatusBar
     */
    public void setWithoutStatusBar(boolean withoutStatusBar) {
        CameraManager.get().setWithoutStatusBar(withoutStatusBar);
    }

    /**
     * setScreenRate
     *
     * @param screenRate screenRate
     */
    public void setScreenRate(int screenRate) {
        if (viewfinderView != null) {
            viewfinderView.setScreenRate(screenRate);
        }
    }

    /**
     * drawResultBitmap
     *
     * @param barcode barcode
     */
    public void drawResultBitmap(Bitmap barcode) {
        if (viewfinderView != null) {
            viewfinderView.drawResultBitmap(barcode);
        }
    }

    /**
     * onResume
     *
     * @param activity activity
     */
    public void onResume(Activity activity) {
        this.activity = activity;
        inactivityTimer = new InactivityTimer(activity);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    /**
     * onPause
     *
     * @param activity activity
     */
    public void onPause(Activity activity) {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    /**
     * onDestroy
     *
     * @param activity activity
     */
    public void onDestroy(Activity activity) {
        if (inactivityTimer != null) {
            inactivityTimer.shutdown();
        }
    }

    @Override
    public DecodeViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    @Override
    public void handleDecode(Result result, Bitmap barcode, long decode_time) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        String formatString = result.getBarcodeFormat().name();
        if (resultString.equals("")) {
            restartPreview();
        } else {
            boolean flag = false;
            if (decodeScanListener != null) {
                flag = decodeScanListener.handleDecode(result, barcode);
            }
            if (!flag) {
                Intent resultIntent = null;
                if (resultIntentListener != null) {
                    resultIntent = resultIntentListener.onResultIntent(result, barcode);
                }
                if (resultIntent == null) {
                    resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", resultString);
                    bundle.putString("format", formatString);
                    bundle.putLong("time", decode_time);
                    bundle.putParcelable("bitmap", barcode);
                    resultIntent.putExtras(bundle);
                }
                activity.setResult(resultCode, resultIntent);
                activity.finish();
            }
        }
    }


    /**
     * setDecodeInterceptor
     *
     * @param interceptor interceptor
     */
    public void setDecodeInterceptor(DecodeInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    /**
     * setOnDecodeScanListener
     *
     * @param decodeScanListener decodeScanListener
     */
    public void setOnDecodeScanListener(OnDecodeScanListener decodeScanListener) {
        this.decodeScanListener = decodeScanListener;
    }

    /**
     * setOnResultIntentListener
     *
     * @param resultIntentListener resultIntentListener
     */
    public void setOnResultIntentListener(OnResultIntentListener resultIntentListener) {
        this.resultIntentListener = resultIntentListener;
    }

    /**
     * setOnOpenCameraListener
     *
     * @param openCameraListener openCameraListener
     */
    public void setOnOpenCameraListener(OnOpenCameraListener openCameraListener) {
        this.openCameraListener = openCameraListener;
    }

    /**
     * restartPreview
     */
    public void restartPreview() {
        if (handler != null) {
            handler.obtainMessage(R.id.restart_preview).sendToTarget();
        }
    }

    /**
     * enableFlash
     */
    public void enableFlash() {
        CameraManager.get().enableFlash();
    }

    /**
     * disableFlash
     */
    public void disableFlash() {
        CameraManager.get().disableFlash();
    }

    /**
     * autoFlash
     */
    public void autoFlash() {
        CameraManager.get().autoFlash();
    }

    /**
     * getFlashMode
     *
     * @return flash mode
     */
    public String getFlashMode() {
        return CameraManager.get().getFlashMode();
    }

    /**
     * isEnableFlash
     *
     * @return isEnableFlash
     */
    public boolean isEnableFlash() {
        return CameraManager.get().isEnableFlash();
    }

    /**
     * isAutoFlash
     *
     * @return isAutoFlash
     */
    public boolean isAutoFlash() {
        return CameraManager.get().isAutoFlash();
    }

    /**
     * isDisableFlash
     *
     * @return isDisableFlash
     */
    public boolean isDisableFlash() {
        return CameraManager.get().isDisableFlash();
    }

    /**
     * initCamera
     *
     * @param surfaceHolder surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            if (openCameraListener != null) {
                openCameraListener.onOpen(this);
            }
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new DecodeCaptureViewHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public DecodeCaptureViewHandler getHandler() {
        return handler;
    }

    @Override
    public void onDrawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    @Override
    public DecodeInterceptor getDecodeInterceptor() {
        return interceptor;
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            if (activity != null) {
                ((Activity) getContext()).setVolumeControlStream(AudioManager.STREAM_MUSIC);
            } else if (getContext() instanceof Activity) {
                ((Activity) getContext()).setVolumeControlStream(AudioManager.STREAM_MUSIC);
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(beepVolume, beepVolume);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(vibrateDuration);
        }
    }

    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
}
