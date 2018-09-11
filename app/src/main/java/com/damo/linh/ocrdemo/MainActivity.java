package com.damo.linh.ocrdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.OcrRequestParams;
import com.baidu.ocr.sdk.model.OcrResponseResult;
import com.baidu.ocr.ui.camera.CameraActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private AlertDialog.Builder alertDialog;
    private boolean hasGotToken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alertDialog = new AlertDialog.Builder(this);
        //用明文ak，sk初始化
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                alertText("AK，SK方式获取token失败", error.getMessage());
            }
        }, getApplicationContext(), "KgLmVS7vuicGcX8The7eLpVS", "g7jkbcLqxze3qtYKnvjuPfPQZhPGzyuT");
        findViewById(R.id.tv).setOnClickListener(view -> {
            if (checkTokenStatus()) {
                return;
            }
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, getSaveFile(getApplication()
            ).getAbsolutePath());
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity
                    .CONTENT_TYPE_GENERAL);
            startActivityForResult(intent, 124);
        });
//        findViewById(R.id.tv1).setOnClickListener(view -> {
//            if (checkTokenStatus()) {
//                return;
//            }
//            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
//            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, getSaveFile(getApplication()
//            ).getAbsolutePath());
//            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
//            startActivityForResult(intent, 123);
//        });
        findViewById(R.id.tv2).setOnClickListener(view -> {
            if (checkTokenStatus()) {
                return;
            }
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, getSaveFile(getApplication()
            ).getAbsolutePath());
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity
                    .CONTENT_TYPE_GENERAL);
            startActivityForResult(intent, 122);
        });
        findViewById(R.id.tv3).setOnClickListener(view -> {
            if (checkTokenStatus()) {
                return;
            }
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, getSaveFile(getApplication()
            ).getAbsolutePath());
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity
                    .CONTENT_TYPE_GENERAL);
            startActivityForResult(intent, 121);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 识别成功回调，增值税发票
        if (requestCode == 124 && resultCode == Activity.RESULT_OK) {
            OcrRequestParams param = new OcrRequestParams();
            File file = new File(getApplicationContext().getFilesDir(), "pic.jpg");
            param.setImageFile(new File(file.getAbsolutePath()));
            OCR.getInstance(this).recognizeVatInvoice(param, new
                    OnResultListener<OcrResponseResult>() {
                        @Override
                        public void onResult(OcrResponseResult result) {
                            alertText("", result.getJsonRes());
                        }

                        @Override
                        public void onError(OCRError error) {
                            alertText("", error.toString());
                            Log.e("TAG", "onError: " + error.toString());
                        }
                    });
        }
        // 识别成功回调，通用票据识别
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            OcrRequestParams param = new OcrRequestParams();
            File file = new File(getApplicationContext().getFilesDir(), "pic.jpg");
            param.setImageFile(new File(file.getAbsolutePath()));
            param.putParam("detect_direction", "true");
            OCR.getInstance(this).recognizeReceipt(param, new
                    OnResultListener<OcrResponseResult>() {
                        @Override
                        public void onResult(OcrResponseResult result) {
                            alertText("", result.getJsonRes());
                        }

                        @Override
                        public void onError(OCRError error) {
                            alertText("", error.toString());
                            Log.e("TAG", "onError: " + error.toString());
                        }
                    });
        }
        // 识别成功回调，火车票据识别
        if (requestCode == 122 && resultCode == Activity.RESULT_OK) {
            OcrRequestParams param = new OcrRequestParams();
            File file = new File(getApplicationContext().getFilesDir(), "pic.jpg");
            param.setImageFile(new File(file.getAbsolutePath()));
            OCR.getInstance(this).recognizeCommon(param, new
                    OnResultListener<OcrResponseResult>() {
                        @Override
                        public void onResult(OcrResponseResult result) {
                            alertText("", result.getJsonRes());
                        }

                        @Override
                        public void onError(OCRError error) {
                            alertText("", error.toString());
                            Log.e("TAG", "onError: " + error.toString());
                        }
                    },"https://aip.baidubce.com/rest/2.0/ocr/v1/train_ticket?");
        }
        // 识别成功回调，打车票据识别
        if (requestCode == 121 && resultCode == Activity.RESULT_OK) {
            OcrRequestParams param = new OcrRequestParams();
            File file = new File(getApplicationContext().getFilesDir(), "pic.jpg");
            param.setImageFile(new File(file.getAbsolutePath()));
            OCR.getInstance(this).recognizeCommon(param, new
                    OnResultListener<OcrResponseResult>() {
                        @Override
                        public void onResult(OcrResponseResult result) {
                            alertText("", result.getJsonRes());
                        }

                        @Override
                        public void onError(OCRError error) {
                            alertText("", error.toString());
                            Log.e("TAG", "onError: " + error.toString());
                        }
                    },"https://aip.baidubce.com/rest/2.0/ocr/v1/taxi_receipt?");
        }
    }

    public static File getSaveFile(Context context) {
        return new File(context.getFilesDir(), "pic.jpg");
    }

    private boolean checkTokenStatus() {
        if (!hasGotToken) {
            Toast.makeText(getApplicationContext(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return !hasGotToken;
    }

    private void alertText(final String title, final String message) {
        this.runOnUiThread(() -> alertDialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show());
    }
}