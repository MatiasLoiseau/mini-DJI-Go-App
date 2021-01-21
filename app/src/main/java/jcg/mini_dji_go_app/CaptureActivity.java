package jcg.mini_dji_go_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;


public class CaptureActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        imageView =  findViewById(R.id.imageView);
        byte[] buf = (getIntent().getExtras()).getByteArray("buf");
        int width = (getIntent().getExtras()).getInt("width");
        int height = (getIntent().getExtras()).getInt("height");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuvImage = new YuvImage(buf,
                ImageFormat.NV21,
                width,
                height,
                null);

        yuvImage.compressToJpeg(new Rect(0,
                    0,
                    width,
                    height), 50, out);

        byte[] imageBytes = out.toByteArray();
        Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        imageView.setImageBitmap(image);

    }
}