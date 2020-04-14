package com.example.mlkit_barcode_ocr2;

import android.graphics.Bitmap;

import android.util.Log;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mlkit_barcode_ocr2.RSA;
import com.example.mlkit_barcode_ocr2.utility.CameraImageGraphic;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.example.mlkit_barcode_ocr2.utility.GraphicOverlay;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import static java.util.Arrays.*;

/**
 * Barcode Detector Demo.
 */
public class BarcodeScanningProcessor extends com.example.mlkit_barcode_ocr.VisionProcessorBase<List<FirebaseVisionBarcode>> {

    private static final String TAG = "BarcodeScanProc";

    private final FirebaseVisionBarcodeDetector detector;
    String decrypted;
    String encrypted;
    String replacement;
    BarcodeGraphic graphic_indexed;
    Hashtable<String, BarcodeGraphic> code_graphic = new Hashtable<String, BarcodeGraphic>();
    Hashtable<String, String> code_decr = new Hashtable<String, String>();

    public BarcodeScanningProcessor() {
        // Note that if you know which format of barcode your app is dealing with, detection will be
        // faster to specify the supported barcode formats one by one, e.g.
        // new FirebaseVisionBarcodeDetectorOptions.Builder()
        //     .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
        //     .build();
        detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Barcode Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionBarcode>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionBarcode> barcodes,
            @NonNull com.example.mlkit_barcode_ocr2.utility.FrameMetadata frameMetadata,
            @NonNull com.example.mlkit_barcode_ocr2.utility.GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        for (int i = 0; i < barcodes.size(); ++i) {
            //list of barcodes
            FirebaseVisionBarcode barcode = barcodes.get(i);
            Log.i("barcode", barcode.getRawValue());
            //decrypt text
            encrypted = barcode.getRawValue();
                try {
                    decrypted = RSA.decrypt(barcode.getRawValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            if (decrypted!=null) {
                replacement = code_decr.get(decrypted);
                if (replacement == null) {
                        code_decr.put(decrypted, encrypted);
                        BarcodeGraphic barcodeGraphic = new BarcodeGraphic(graphicOverlay, barcode);
                        code_graphic.put(encrypted, barcodeGraphic);
                        graphicOverlay.add(barcodeGraphic);
                    }
                else{
                      if (encrypted.equals(replacement)) {
                                BarcodeGraphic barcodeGraphic = new BarcodeGraphic(graphicOverlay, barcode);
                                graphic_indexed = code_graphic.get(replacement);
                                Log.i("graph_indexed", graphic_indexed.toString());
                                graphicOverlay.postInvalidate();
                                if (graphic_indexed != null) {
                                    graphicOverlay.add(graphic_indexed);
                                }
                            }
                }
            }
//            BarcodeGraphic barcodeGraphic = new BarcodeGraphic(graphicOverlay, barcode);
//            graphicOverlay.add(barcodeGraphic);

        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Barcode detection failed " + e);
    }
}
