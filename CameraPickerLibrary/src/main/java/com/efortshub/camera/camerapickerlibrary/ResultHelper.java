package com.efortshub.camera.camerapickerlibrary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ResultHelper {
    public static final int CAMERA_PICKER_REQUEST_CODE = 965;


    public static void getImageFromCameraOrGallery(Activity activity) {
        ActivityCompat.startActivityForResult(activity, new Intent(activity.getApplicationContext(), CameraActivity.class), CAMERA_PICKER_REQUEST_CODE, null);

    }

    public static Bitmap getBitmapImageFromResult(int requestCode, int resultCode, Activity activity) {
Bitmap bitmap = null;

        if (requestCode==CAMERA_PICKER_REQUEST_CODE && resultCode== activity.RESULT_OK){
                try {

                    File file = new File(activity.getApplicationContext().getExternalFilesDir(null), "tmp");
                    FileInputStream fis = new FileInputStream(file);
                    byte[] bytes = new byte[fis.available()];
                    fis.read(bytes);
                    fis.close();
                     bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                } catch (IOException e) {
                    e.printStackTrace();

                }


        }

        return bitmap;
    }

}
