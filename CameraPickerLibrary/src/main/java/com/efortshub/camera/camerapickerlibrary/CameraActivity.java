package com.efortshub.camera.camerapickerlibrary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.efortshub.camera.camerapickerlibrary.databinding.ActivityCameraBinding;
import com.efortshub.camera.camerapickerlibrary.databinding.ActivityCameraImageCapturedBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CameraActivity extends AppCompatActivity {

    private ActivityCameraBinding binding;
    private ActivityCameraImageCapturedBinding capturedBinding;
    ExecutorService cameraExecutor;
    ListenableFuture<ProcessCameraProvider> listenableFuture;
    private static final String TAG = "efortshub";
    FlashMode flashMode = FlashMode.FLASH_OFF;
    private static   boolean isBackCamera = true;


    private enum FlashMode{
        FLASH_ON,
        FLASH_OFF
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        capturedBinding = ActivityCameraImageCapturedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();





        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            //if camera permission granted
            binding.getRoot().post(()->{
                loadCamera();

                binding.btnFlipCamera.setOnClickListener(v -> {
                    recreate();

                });


            });
        }else {
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 112);
        }




    }

    private void loadCamera() {
        AtomicInteger mode = new AtomicInteger(CameraSelector.LENS_FACING_BACK);
        if (isBackCamera){
            mode.set(CameraSelector.LENS_FACING_FRONT);
            isBackCamera = false;
        }else {
            mode.set(CameraSelector.LENS_FACING_BACK);
            isBackCamera = true;
        }

        listenableFuture = ProcessCameraProvider.getInstance(CameraActivity.this);
        listenableFuture.addListener(()->{

            //preview
            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());

            //cameraSelector
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(mode.get())
                    .build();

            //imageCapture
            ImageCapture imageCapture = new ImageCapture.Builder()
                    .setTargetRotation(binding.cameraPreview.getDisplay().getRotation())
                    .build();


            try {
                ProcessCameraProvider processCameraProvider = listenableFuture.get();

                Camera camera = processCameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);


                binding.flash.setOnClickListener(v->{

                    runOnUiThread(()->{


                    switch (flashMode){
                        case FLASH_ON:
                            flashMode = FlashMode.FLASH_OFF;
                            camera.getCameraControl().enableTorch(false);
                            binding.flash.setImageDrawable(ContextCompat.getDrawable(CameraActivity.this, R.drawable.ic_baseline_flash_off_24));



                            break;
                        case FLASH_OFF:
                            flashMode = FlashMode.FLASH_ON;
                            camera.getCameraControl().enableTorch(true);

                            binding.flash.setImageDrawable(ContextCompat.getDrawable(CameraActivity.this, R.drawable.ic_baseline_flash_on_24));

                            break;
                        default:
                    }

                });


                });

                binding.btnClickPhoto.setOnClickListener(v -> {

                    imageCapture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
                        @Override
                        public void onCaptureSuccess(@NonNull ImageProxy image) {

                            // get byte array from imageProxy

                            ImageProxy.PlaneProxy[] planeProxy = image.getPlanes();
                            ByteBuffer byteBuffer = planeProxy[0].getBuffer();
                            byte[] bytes = new byte[byteBuffer.remaining()];
                            byteBuffer.get(bytes);


                            //get bitmap image from byte array
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            
                            //apply bitmap to ui
                            runOnUiThread(()->{
                                capturedBinding.ivPreview.setImageBitmap(bitmap);

                                setContentView(capturedBinding.getRoot());

                                capturedBinding.ivCancel.setOnClickListener(v-> recreate());

                                capturedBinding.ivDone.setOnClickListener(v->{

                                    File file = new File(getExternalFilesDir(null), "tmp");

                                    if (file.exists()){
                                        file.delete();
                                    }

                                    try {
                                        FileOutputStream fos = new FileOutputStream(file);
                                        fos.write(bytes);
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    Intent i = new Intent();
                                    setResult(RESULT_OK,i);
                                    Toast.makeText(CameraActivity.this, "image saved in gallery...", Toast.LENGTH_SHORT).show();
                                    finish();
                                });

                            });








                            super.onCaptureSuccess(image);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            super.onError(exception);
                            Log.d(TAG, "onError: message : "+exception.getMessage()+" code: "+exception.getImageCaptureError());
                        }
                    });


                });



            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }


        }, ContextCompat.getMainExecutor(CameraActivity.this));



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==112){

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                loadCamera();
            }else{
                Toast.makeText(this, "Please enable camera permission to access camera...", Toast.LENGTH_SHORT).show();
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}