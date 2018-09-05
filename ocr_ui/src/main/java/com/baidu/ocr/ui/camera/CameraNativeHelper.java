package com.baidu.ocr.ui.camera;

import android.content.Context;

import com.baidu.idcardquality.IDcardQualityProcess;

public class CameraNativeHelper {
    public interface CameraNativeInitCallback {
        void onError(int errorCode, Throwable e);
    }

    public static void init(final Context ctx, final String token, final CameraNativeInitCallback
            cb) {
        CameraThreadPool.execute(() -> {
            int status;
            if (IDcardQualityProcess.getLoadSoException() != null) {
                status = CameraView.NATIVE_SOLOAD_FAIL;
                cb.onError(status, IDcardQualityProcess.getLoadSoException());
                return;
            }
            int authStatus = IDcardQualityProcess.init(token);
            if (authStatus != 0) {
                cb.onError(CameraView.NATIVE_AUTH_FAIL, null);
                return;
            }
            int initModelStatus = IDcardQualityProcess.getInstance()
                    .idcardQualityInit(ctx.getAssets(), "models");
            if (initModelStatus != 0) {
                cb.onError(CameraView.NATIVE_INIT_FAIL, null);
            }
        });
    }
}
