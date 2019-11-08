package com.damo.linh.ocrdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
        //用明文ak,sk初始化
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                alertText("AK,SK方式获取token失败", error.getMessage());
            }
        }, getApplicationContext(), "KgLmVS7vuicGcX8The7eLpVS", "g7jkbcLqxze3qtYKnvjuPfPQZhPGzyuT");
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkTokenStatus()) {
                    return;
                }
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, getSaveFile(getApplication()).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
                startActivityForResult(intent, 111);
            }
        });
        findViewById(R.id.tv1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkTokenStatus()) {
                    return;
                }
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, getSaveFile(getApplication()).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
                startActivityForResult(intent, 222);
            }
        });
        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkTokenStatus()) {
                    return;
                }
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, getSaveFile(getApplication()).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
                startActivityForResult(intent, 333);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 识别成功回调,身份证
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            OcrRequestParams param = new OcrRequestParams();
            File file = new File(getApplicationContext().getFilesDir(), "pic.jpg");
            param.setImageFile(new File(file.getAbsolutePath()));
            // front:身份证含照片的一面;back:身份证带国徽的一面
            param.putParam("id_card_side", "front");
            // 是否检测图像旋转角度,默认检测,即:true.朝向是指输入图像是正常方向,逆时针旋转90/180/270度.可选值包括:true:检测旋转角度; false:不检测旋转角度.
            param.putParam("detect_direction", "true");
            // 是否开启身份证风险类型(身份证复印件,临时身份证,身份证翻拍,修改过的身份证)功能,默认不开启,即:false.可选值:true 开启;false 不开启
            param.putParam("detect_risk", "false");
            // 是否检测头像内容,默认不检测.可选值:true 检测头像并返回头像的 base64 编码及位置信息
            param.putParam("detect_photo", "false");
            OCR.getInstance(this).recognizeCommon(param, new OnResultListener<OcrResponseResult>() {
                @Override
                public void onResult(OcrResponseResult result) {
                    alertText("", result.getJsonRes());
                }

                @Override
                public void onError(OCRError error) {
                    alertText("", error.toString());
                    Log.e("TAG", "onError:" + error.toString());
                }
            }, "https://aip.baidubce.com/rest/2.0/ocr/v1/idcard?");
        }
        // 识别成功回调,银行卡
        if (requestCode == 222 && resultCode == Activity.RESULT_OK) {
            OcrRequestParams param = new OcrRequestParams();
            File file = new File(getApplicationContext().getFilesDir(), "pic.jpg");
            param.setImageFile(new File(file.getAbsolutePath()));
            OCR.getInstance(this).recognizeCommon(param, new OnResultListener<OcrResponseResult>() {
                @Override
                public void onResult(OcrResponseResult result) {
                    alertText("", result.getJsonRes());
                }

                @Override
                public void onError(OCRError error) {
                    alertText("", error.toString());
                    Log.e("TAG", "onError:" + error.toString());
                }
            }, "https://aip.baidubce.com/rest/2.0/ocr/v1/bankcard?");
        }
        // 识别成功回调,自定义票据
        if (requestCode == 333 && resultCode == Activity.RESULT_OK) {
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
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.setTitle(title).setMessage(message).setPositiveButton("确定", null).show();
            }
        });
    }
}