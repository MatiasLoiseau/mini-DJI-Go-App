package jcg.mini_dji_go_app.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class Analyzer implements ImageAnalysis.Analyzer {

    Context context;
    String rawValue;
    BarcodeScannerOptions options;

    public Analyzer(Context context) {
        this.context = context;
        rawValue = "NO CODE DETECTED";
        options = new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_QR_CODE,
                                Barcode.FORMAT_AZTEC)
                        .build();
    }

    public String analyze(Bitmap bitmap) {

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        //InputImage image = InputImage.fromBitmap(bitmap, rotationDegree);
        // Pass image to an ML Kit Vision API
        // ...

        //BarcodeScanner scanner = BarcodeScanning.getClient(options);
        BarcodeScanner scanner = BarcodeScanning.getClient();

        processImage(scanner, image);

        return rawValue;
    }



    void processImage(BarcodeScanner scanner, InputImage image){

        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(this::getInfoFromBarcodes)
                .addOnFailureListener(e -> { });
    }

    void getInfoFromBarcodes(List<Barcode> barcodes){

        if (barcodes.size() == 0)
            Toast.makeText(context, "Error al escanear Imagen" , Toast.LENGTH_SHORT).show();

        else
            for (Barcode barcode: barcodes) {

                Rect bounds = barcode.getBoundingBox();
                Point[] corners = barcode.getCornerPoints();

                rawValue = barcode.getRawValue();

                int valueType = barcode.getValueType();

            }
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {

    }
}
