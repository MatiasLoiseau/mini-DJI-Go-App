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
        byte[] bytes = getIntent().getExtras().getByteArray("bytes");

        Bitmap frame = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageView.setImageBitmap(frame);
    }
}