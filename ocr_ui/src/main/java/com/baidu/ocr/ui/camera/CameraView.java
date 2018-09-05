package com.baidu.ocr.ui.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.baidu.idcardquality.IDcardQualityProcess;
import com.baidu.ocr.ui.R;
import com.baidu.ocr.ui.util.DimensionUtil;
import com.baidu.ocr.ui.util.ImageUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CameraView extends FrameLayout {
    private int maskType;

    interface OnTakePictureCallback {
        void onPictureTaken(Bitmap bitmap);
    }

    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_HORIZONTAL = 90;
    public static final int ORIENTATION_INVERT = 270;
    public static final int NATIVE_AUTH_INIT_SUCCESS = 0;
    public static final int NATIVE_SOLOAD_FAIL = 10;
    public static final int NATIVE_AUTH_FAIL = 11;
    public static final int NATIVE_INIT_FAIL = 12;

    public void setInitNativeStatus(int initNativeStatus) {
        this.initNativeStatus = initNativeStatus;
    }

    private int initNativeStatus = NATIVE_AUTH_INIT_SUCCESS;

    @IntDef({ORIENTATION_PORTRAIT, ORIENTATION_HORIZONTAL, ORIENTATION_INVERT})
    @interface Orientation {
    }

    private CameraViewTakePictureCallback cameraViewTakePictureCallback = new
            CameraViewTakePictureCallback();
    private ICameraControl cameraControl;
    private View displayView;
    private MaskView maskView;
    private ImageView hintView;
    private TextView hintViewText;
    private LinearLayout hintViewTextWrapper;
    private boolean isEnableScan;

    public void setEnableScan(boolean enableScan) {
        isEnableScan = enableScan;
    }

    Handler uiHandler = new Handler(Looper.getMainLooper());

    public ICameraControl getCameraControl() {
        return cameraControl;
    }

    public void setOrientation(@Orientation int orientation) {
        cameraControl.setDisplayOrientation(orientation);
    }

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void start() {
        cameraControl.start();
        setKeepScreenOn(true);
    }

    public void stop() {
        cameraControl.stop();
        setKeepScreenOn(false);
    }

    public void takePicture(final File file, final OnTakePictureCallback callback) {
        cameraViewTakePictureCallback.file = file;
        cameraViewTakePictureCallback.callback = callback;
        cameraControl.takePicture(cameraViewTakePictureCallback);
    }

    private OnTakePictureCallback autoPictureCallback;

    public void setAutoPictureCallback(OnTakePictureCallback callback) {
        autoPictureCallback = callback;
    }

    public void setMaskType(@MaskView.MaskType int maskType) {
        maskView.setMaskType(maskType);
        maskView.setVisibility(VISIBLE);
        hintView.setVisibility(VISIBLE);
        int hintResourceId = R.drawable.bd_ocr_hint_align_id_card;
        this.maskType = maskType;
        boolean isNeedSetImage = true;
        switch (maskType) {
            case MaskView.MASK_TYPE_ID_CARD_FRONT:
                hintResourceId = R.drawable.bd_ocr_round_corner;
                isNeedSetImage = false;
                break;
            case MaskView.MASK_TYPE_ID_CARD_BACK:
                isNeedSetImage = false;
                hintResourceId = R.drawable.bd_ocr_round_corner;
                break;
            case MaskView.MASK_TYPE_BANK_CARD:
                hintResourceId = R.drawable.bd_ocr_hint_align_bank_card;
                break;
            case MaskView.MASK_TYPE_PASSPORT:
                hintView.setVisibility(INVISIBLE);
                break;
            case MaskView.MASK_TYPE_NONE:
            default:
                maskView.setVisibility(INVISIBLE);
                hintView.setVisibility(INVISIBLE);
                break;
        }
        if (isNeedSetImage) {
            hintView.setImageResource(hintResourceId);
            hintViewTextWrapper.setVisibility(INVISIBLE);
        }
        if (maskType == MaskView.MASK_TYPE_ID_CARD_FRONT && isEnableScan) {
            cameraControl.setDetectCallback(this::detect);
        }
        if (maskType == MaskView.MASK_TYPE_ID_CARD_BACK && isEnableScan) {
            cameraControl.setDetectCallback(this::detect);
        }
    }

    private int detect(byte[] data, final int rotation) {
        if (initNativeStatus != NATIVE_AUTH_INIT_SUCCESS) {
            showTipMessage(initNativeStatus);
            return 1;
        }
        if (cameraControl.getAbortingScan().get()) {
            return 0;
        }
        Rect previewFrame = cameraControl.getPreviewFrame();
        if (maskView.getWidth() == 0 || maskView.getHeight() == 0
                || previewFrame.width() == 0 || previewFrame.height() == 0) {
            return 0;
        }
        BitmapRegionDecoder decoder = null;
        try {
            decoder = BitmapRegionDecoder.newInstance(data, 0, data.length, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert decoder != null;
        int width = rotation % 180 == 0 ? decoder.getWidth() : decoder.getHeight();
        int height = rotation % 180 == 0 ? decoder.getHeight() : decoder.getWidth();
        Rect frameRect = maskView.getFrameRectExtend();
        int left = width * frameRect.left / maskView.getWidth();
        int top = height * frameRect.top / maskView.getHeight();
        int right = width * frameRect.right / maskView.getWidth();
        int bottom = height * frameRect.bottom / maskView.getHeight();
        if (previewFrame.top < 0) {
            int adjustedPreviewHeight = previewFrame.height() * getWidth() / previewFrame.width();
            int topInFrame = ((adjustedPreviewHeight - frameRect.height()) / 2) * getWidth() /
                    previewFrame.width();
            int bottomInFrame = ((adjustedPreviewHeight + frameRect.height()) / 2) * getWidth() /
                    previewFrame.width();
            top = topInFrame * height / previewFrame.height();
            bottom = bottomInFrame * height / previewFrame.height();
        } else {
            if (previewFrame.left < 0) {
                int adjustedPreviewWidth = previewFrame.width() * getHeight() / previewFrame
                        .height();
                int leftInFrame = ((adjustedPreviewWidth - maskView.getFrameRect().width()) / 2)
                        * getHeight() / previewFrame.height();
                int rightInFrame = ((adjustedPreviewWidth + maskView.getFrameRect().width()) / 2)
                        * getHeight() / previewFrame.height();
                left = leftInFrame * width / previewFrame.width();
                right = rightInFrame * width / previewFrame.width();
            }
        }
        Rect region = new Rect();
        region.left = left;
        region.top = top;
        region.right = right;
        region.bottom = bottom;
        if (rotation % 180 == 90) {
            int x = decoder.getWidth() / 2;
            int y = decoder.getHeight() / 2;
            int rotatedWidth = region.height();
            int rotated = region.width();
            region.left = x - rotatedWidth / 2;
            region.top = y - rotated / 2;
            region.right = x + rotatedWidth / 2;
            region.bottom = y + rotated / 2;
            region.sort();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        int maxPreviewImageSize = 2560;
        int size = Math.min(decoder.getWidth(), decoder.getHeight());
        size = Math.min(size, maxPreviewImageSize);
        options.inSampleSize = ImageUtil.calculateInSampleSize(options, size, size);
        options.inScaled = true;
        options.inDensity = Math.max(options.outWidth, options.outHeight);
        options.inTargetDensity = size;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = decoder.decodeRegion(region, options);
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap
                    .getHeight(), matrix, false);
            if (bitmap != rotatedBitmap) {
                bitmap.recycle();
            }
            bitmap = rotatedBitmap;
        }
        final int status;
        switch (maskType) {
            case MaskView.MASK_TYPE_ID_CARD_FRONT:
                status = IDcardQualityProcess.getInstance().idcardQualityDetectionImg(bitmap, true);
                break;
            case MaskView.MASK_TYPE_ID_CARD_BACK:
                status = IDcardQualityProcess.getInstance().idcardQualityDetectionImg(bitmap,
                        false);
                break;
            default:
                status = 1;
        }
        int SCAN_SUCCESS = 0;
        if (status == SCAN_SUCCESS) {
            if (!cameraControl.getAbortingScan().compareAndSet(false, true)) {
                bitmap.recycle();
                return 0;
            }
            autoPictureCallback.onPictureTaken(bitmap);
        }
        showTipMessage(status);
        return status;
    }

    private void showTipMessage(final int status) {
        uiHandler.post(() -> {
            if (status == 0) {
                hintViewText.setVisibility(View.INVISIBLE);
            } else if (!cameraControl.getAbortingScan().get()) {
                hintViewText.setVisibility(View.VISIBLE);
                hintViewText.setText(getScanMessage(status));
            }
        });
    }

    private String getScanMessage(int status) {
        String message;
        switch (status) {
            case 0:
                message = "";
                break;
            case 2:
                message = "身份证模糊，请重新尝试";
                break;
            case 3:
                message = "身份证反光，请重新尝试";
                break;
            case 4:
                message = "请将身份证前后反转再进行识别";
                break;
            case 5:
                message = "请拿稳镜头和身份证";
                break;
            case 6:
                message = "请将镜头靠近身份证";
                break;
            case 7:
                message = "请将身份证完整置于取景框内";
                break;
            case NATIVE_AUTH_FAIL:
                message = "本地质量控制授权失败";
                break;
            case NATIVE_INIT_FAIL:
                message = "本地模型加载失败";
                break;
            case NATIVE_SOLOAD_FAIL:
                message = "本地SO库加载失败";
                break;
            case 1:
            default:
                message = "请将身份证置于取景框内";
        }
        return message;
    }

    private void init() {
        cameraControl = new Camera1Control(getContext());
        displayView = cameraControl.getDisplayView();
        addView(displayView);
        maskView = new MaskView(getContext());
        addView(maskView);
        hintView = new ImageView(getContext());
        addView(hintView);
        hintViewTextWrapper = new LinearLayout(getContext());
        hintViewTextWrapper.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                DimensionUtil.dpToPx(25));
        lp.gravity = Gravity.CENTER;
        hintViewText = new TextView(getContext());
        hintViewText.setBackgroundResource(R.drawable.bd_ocr_round_corner);
        hintViewText.setAlpha(0.5f);
        hintViewText.setPadding(DimensionUtil.dpToPx(10), 0, DimensionUtil.dpToPx(10), 0);
        hintViewTextWrapper.addView(hintViewText, lp);
        hintViewText.setGravity(Gravity.CENTER);
        hintViewText.setTextColor(Color.WHITE);
        hintViewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        hintViewText.setText(getScanMessage(-1));
        addView(hintViewTextWrapper, lp);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        displayView.layout(left, 0, right, bottom - top);
        maskView.layout(left, 0, right, bottom - top);
        int hintViewWidth = DimensionUtil.dpToPx(250);
        int hintViewHeight = DimensionUtil.dpToPx(25);
        int hintViewLeft = (getWidth() - hintViewWidth) / 2;
        int hintViewTop = maskView.getFrameRect().bottom + DimensionUtil.dpToPx(16);
        hintViewTextWrapper.layout(hintViewLeft, hintViewTop, hintViewLeft + hintViewWidth,
                hintViewTop + hintViewHeight);
        hintView.layout(hintViewLeft, hintViewTop, hintViewLeft + hintViewWidth, hintViewTop +
                hintViewHeight);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Bitmap crop(File outputFile, byte[] data, int rotation) {
        try {
            Rect previewFrame = cameraControl.getPreviewFrame();
            if (maskView.getWidth() == 0 || maskView.getHeight() == 0 || previewFrame.width() ==
                    0 || previewFrame.height() == 0) {
                return null;
            }
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(data, 0, data.length,
                    true);
            int width = rotation % 180 == 0 ? decoder.getWidth() : decoder.getHeight();
            int height = rotation % 180 == 0 ? decoder.getHeight() : decoder.getWidth();
            Rect frameRect = maskView.getFrameRect();
            int left = width * frameRect.left / maskView.getWidth();
            int top = height * frameRect.top / maskView.getHeight();
            int right = width * frameRect.right / maskView.getWidth();
            int bottom = height * frameRect.bottom / maskView.getHeight();
            if (previewFrame.top < 0) {
                int adjustedPreviewHeight = previewFrame.height() * getWidth() / previewFrame
                        .width();
                int topInFrame = ((adjustedPreviewHeight - frameRect.height()) / 2) * getWidth()
                        / previewFrame.width();
                int bottomInFrame = ((adjustedPreviewHeight + frameRect.height()) / 2) * getWidth
                        () / previewFrame.width();
                top = topInFrame * height / previewFrame.height();
                bottom = bottomInFrame * height / previewFrame.height();
            } else {
                if (previewFrame.left < 0) {
                    int adjustedPreviewWidth = previewFrame.width() * getHeight() / previewFrame
                            .height();
                    int leftInFrame = ((adjustedPreviewWidth - maskView.getFrameRect().width()) /
                            2) * getHeight() / previewFrame.height();
                    int rightInFrame = ((adjustedPreviewWidth + maskView.getFrameRect().width())
                            / 2) * getHeight() / previewFrame.height();
                    left = leftInFrame * width / previewFrame.width();
                    right = rightInFrame * width / previewFrame.width();
                }
            }
            Rect region = new Rect();
            region.left = left;
            region.top = top;
            region.right = right;
            region.bottom = bottom;
            if (rotation % 180 == 90) {
                int x = decoder.getWidth() / 2;
                int y = decoder.getHeight() / 2;
                int rotatedWidth = region.height();
                int rotated = region.width();
                region.left = x - rotatedWidth / 2;
                region.top = y - rotated / 2;
                region.right = x + rotatedWidth / 2;
                region.bottom = y + rotated / 2;
                region.sort();
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            int maxPreviewImageSize = 2560;
            int size = Math.min(decoder.getWidth(), decoder.getHeight());
            size = Math.min(size, maxPreviewImageSize);
            options.inSampleSize = ImageUtil.calculateInSampleSize(options, size, size);
            options.inScaled = true;
            options.inDensity = Math.max(options.outWidth, options.outHeight);
            options.inTargetDensity = size;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = decoder.decodeRegion(region, options);
            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, false);
                if (bitmap != rotatedBitmap) {
                    bitmap.recycle();
                }
                bitmap = rotatedBitmap;
            }
            try {
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class CameraViewTakePictureCallback implements ICameraControl.OnTakePictureCallback {
        private File file;
        private OnTakePictureCallback callback;

        @Override
        public void onPictureTaken(final byte[] data) {
            CameraThreadPool.execute(() -> {
                final int rotation = ImageUtil.getOrientation(data);
                Bitmap bitmap = crop(file, data, rotation);
                callback.onPictureTaken(bitmap);
            });
        }
    }
}