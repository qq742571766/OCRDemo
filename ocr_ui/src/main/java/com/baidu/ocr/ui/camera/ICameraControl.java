package com.baidu.ocr.ui.camera;

import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

public interface ICameraControl {
    int FLASH_MODE_OFF = 0;
    int FLASH_MODE_TORCH = 1;
    int FLASH_MODE_AUTO = 2;

    @IntDef({FLASH_MODE_TORCH, FLASH_MODE_OFF, FLASH_MODE_AUTO})
    @interface FlashMode {
    }

    interface OnTakePictureCallback {
        void onPictureTaken(byte[] data);
    }

    void setDetectCallback(OnDetectPictureCallback callback);

    interface OnDetectPictureCallback {
        int onDetect(byte[] data, int rotation);
    }

    void start();

    void stop();

    void pause();

    void resume();

    View getDisplayView();

    Rect getPreviewFrame();

    void takePicture(OnTakePictureCallback callback);

    void setPermissionCallback(PermissionCallback callback);

    void setDisplayOrientation(@CameraView.Orientation int displayOrientation);

    void refreshPermission();

    AtomicBoolean getAbortingScan();

    void setFlashMode(@FlashMode int flashMode);

    @FlashMode
    int getFlashMode();
}