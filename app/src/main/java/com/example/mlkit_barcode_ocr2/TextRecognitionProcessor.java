// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.example.mlkit_barcode_ocr2;

import android.graphics.Bitmap;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mlkit_barcode_ocr.VisionProcessorBase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.example.mlkit_barcode_ocr2.utility.CameraImageGraphic;
import com.example.mlkit_barcode_ocr2.utility.FrameMetadata;
import com.example.mlkit_barcode_ocr2.utility.GraphicOverlay;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

/**
 * Processor for the text recognition demo.
 */
public class TextRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {

    private static final String TAG = "TextRecProc";

    private final FirebaseVisionTextRecognizer detector;
    String encrypted;
    String decrypted;
    String replacement;
    private FirebaseVisionText.TextBlock decr;
    Hashtable<String, GraphicOverlay.Graphic> hashtable_decr = new Hashtable<String, GraphicOverlay.Graphic>();
    Hashtable<String, String> hashtable3 = new Hashtable<String, String>();
    GraphicOverlay.Graphic graphic_indexed;

    public TextRecognitionProcessor() {
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
        }
    }

    @Override
    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FirebaseVisionText results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay,
                    originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        List<FirebaseVisionText.TextBlock> blocks = results.getTextBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            GraphicOverlay.Graphic textGraphic3 = new TextGraphicBlock(graphicOverlay, blocks.get(i));
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                GraphicOverlay.Graphic textGraphic2 = new TextGraphicLine(graphicOverlay, lines.get(j));
                encrypted = blocks.get(i).getText().replace("\n", "").
                        replace("o", "0").
                        replace("O", "0").
                        replaceAll("\\s", "").
                        replace("l", "1").
                        replace("Z", "3").
                        replace("i", "1").
                        replace("ö", "6").
                        replace("B", "8").
                        replace("D", "b");
                Log.i("encrypted", encrypted);
                if (RSA.TestHex(encrypted)) {
                    Log.i("tested", encrypted);
                    //tested with 1024 RSA keys, the encrypted string in hex is 256 characters long
                    if (encrypted.length() == 256 || encrypted.length() == 512) {
                        Log.i("string", encrypted);
                        //decrypt
                        try {
                            decrypted = RSA.decrypt_ocr(encrypted);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                //replace text in the bounding box with decrypted text
//                if (decrypted == null) {
//                    graphicOverlay.add(textGraphic3);
//                    Log.i("decrypted", "nothing to decrypt");
//                } else {
//                    Log.i("decrypted", decrypted);
//                    GraphicOverlay.Graphic textGraphic4 = new TextGraphicDecrypted(graphicOverlay, blocks.get(i), decrypted);
//                    graphicOverlay.add(textGraphic4);
//                }
                if (decrypted!=null) {
                    //returns encrypted(0, 3)
                    replacement = hashtable3.get(decrypted);

                    if (replacement == null) {
                        if (encrypted.length() == 256 || encrypted.length() == 512) {
                            //hashtable.put(encrypted, decrypted);
                            hashtable3.put(decrypted, encrypted.substring(0, 3));
                            GraphicOverlay.Graphic textGraphic4 = new TextGraphicDecrypted(graphicOverlay, blocks.get(i), decrypted);
                            hashtable_decr.put(encrypted.substring(0, 3), textGraphic4);
                            //Pair<String, OcrGraphic> p = Pair.create(encrypted.substring(0, 3), graphic);
                            //hashtable_overlay.put(decrypted, p);
                            graphicOverlay.postInvalidate();
                            graphicOverlay.add(textGraphic4);
                            graphicOverlay.postInvalidate();
                        }

                    }
                    //}
                    else if (replacement != null) {
                        Log.i("replacement", replacement);
                        if (encrypted.length() == 256 || encrypted.length() == 512) {
                            if (encrypted.substring(0, 3).equals(replacement)) {
                                graphic_indexed = hashtable_decr.get(replacement);
                                Log.i("graph_indexed", graphic_indexed.toString());
                                graphicOverlay.postInvalidate();
                                if (graphic_indexed != null) {
                                    graphicOverlay.add(graphic_indexed);
                                    graphicOverlay.postInvalidate();
                                }
                            }
//                            if (encrypted.charAt(0)== '0') {
//                                graphic_indexed = hashtable_decr.get("054");
//                                graphicOverlay.postInvalidate();
//                                if (graphic_indexed != null) {
//                                    graphicOverlay.add(graphic_indexed);
//                                    graphicOverlay.postInvalidate();
//                                }
//                            }
//                            else if (encrypted.charAt(0)== '3'){
//                                graphic_indexed = hashtable_decr.get("3b4");
//                                graphicOverlay.postInvalidate();
//                                if (graphic_indexed != null) {
//                                    graphicOverlay.add(graphic_indexed);
//                                    graphicOverlay.postInvalidate();
//                                }
//                            }
//                            else if (encrypted.charAt(0)== '4'){
//                                graphic_indexed = hashtable_decr.get("4cf");
//                                graphicOverlay.postInvalidate();
//                                if (graphic_indexed != null) {
//                                    graphicOverlay.add(graphic_indexed);
//                                    graphicOverlay.postInvalidate();
//                                }
//                            }
//                            else if (encrypted.charAt(0)== '5'){
//                                graphic_indexed = hashtable_decr.get("5ce");
//                                graphicOverlay.postInvalidate();
//                                if (graphic_indexed != null) {
//                                    graphicOverlay.add(graphic_indexed);
//                                    graphicOverlay.postInvalidate();
//                                }
//                            }
//                            else if (encrypted.charAt(0)== 'b'){
//                                graphic_indexed = hashtable_decr.get("b17");
//                                graphicOverlay.postInvalidate();
//                                if (graphic_indexed != null) {
//                                    graphicOverlay.add(graphic_indexed);
//                                    graphicOverlay.postInvalidate();
//                                }
//                            }
                        }

                    }
                }

            }
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Text detection failed." + e);
    }
}