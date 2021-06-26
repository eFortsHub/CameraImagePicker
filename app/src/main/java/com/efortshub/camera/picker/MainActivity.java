package com.efortshub.camera.picker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.efortshub.camera.camerapickerlibrary.CameraActivity;
import com.efortshub.camera.picker.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btn.setOnClickListener(v->{
            ActivityCompat.startActivityForResult(MainActivity.this, new Intent(getApplicationContext(), CameraActivity.class), 114, null);
        });





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode==114 && resultCode== RESULT_OK){
            if (data!=null){
                try {

                    File file = new File(getExternalFilesDir(null), "tmp");
                    FileInputStream fis = new FileInputStream(file);
                    byte[] bytes = new byte[fis.available()];
                    fis.read(bytes);
                    fis.close();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    binding.image.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();

                }

            }

        }else {
            Toast.makeText(this, "action cancelled by the user", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}