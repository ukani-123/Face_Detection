package com.example.facedetection;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageEditActivity extends AppCompatActivity {
    Bitmap mOriginalBitmap;
    Bitmap mOperationalBitmap = null;
    FaceDetector detector;
    InputImage image;
    StickerView stickerView;
    TextAdapter textAdapter;
    RecyclerView rvBG;
    ImageView ivBackground, mainImageView, imgDone,ivCloseStickerOperation,ivCloseBackgroundRcv;
    FrameLayout flMainLayout,llOperation;
    BgAdapter bgAdapter;
    ArrayList<Integer> backgroundList = new ArrayList<>();
    LinearLayout llBG, llStickerOperation,llBackgroundRcv;
    Dialog dialog;

    boolean doubleBackToExitPressedOnce;
    private MLImageSegmentationAnalyzer analyzer;

    public static String saveToAlbum(Bitmap bitmap, final Context context) {
        File file = null;
        String fileName = System.currentTimeMillis() + ".jpg";
        File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DCIM");
        if (root.mkdirs() || root.isDirectory()) {
            file = new File(root, fileName);
        }
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

            os.flush();

        } catch (FileNotFoundException e) {
            Log.e("TAG", e.getMessage());
        } catch (IOException e) {
            Log.e("TAG", e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                Log.e("TAG", e.getMessage());
            }
        }

        if (file == null) {
            return "";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String path = null;
            try {
                path = file.getCanonicalPath();
            } catch (IOException e) {
                Log.e("TAG", e.getMessage());
            }
            MediaScannerConnection.scanFile(context, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(uri);
                    context.sendBroadcast(mediaScanIntent);
                }
            });
        } else {
            String relationDir = file.getParent();
            File file1 = new File(relationDir);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file1.getAbsoluteFile())));
        }
        return root + "/" + fileName;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        findiviewId();
        initilize();
        clicks();


        try {
            mOriginalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(getIntent().getStringExtra("DATA")));
            mOperationalBitmap = mOriginalBitmap;
            detectFace(mOperationalBitmap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //   mainImageView.setImageBitmap(mOriginalBitmap);

        ArrayList<StickerOperationModel> stickerOperationList = new ArrayList<>();
        stickerOperationList.add(new StickerOperationModel("Outline", R.drawable.user));
        stickerOperationList.add(new StickerOperationModel("Shadow", R.drawable.background));
        stickerOperationList.add(new StickerOperationModel("Opacity", R.drawable.opacity));
        stickerOperationList.add(new StickerOperationModel("Cutout", R.drawable.scissors));
        stickerOperationList.add(new StickerOperationModel("Duplicate", R.drawable.duplicate));
        stickerOperationList.add(new StickerOperationModel("Flip", R.drawable.flip));

        stickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            @Override
            public void onStickerAdded(@NonNull Sticker sticker) {

            }
            @Override
            public void onStickerClicked(@NonNull Sticker sticker) {
                slideUp(llStickerOperation);
            }

            @Override
            public void onStickerDeleted(@NonNull Sticker sticker) {


            }

            @Override
            public void onStickerDragFinished(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerTouchedDown(@NonNull Sticker sticker) {

            }


            @Override
            public void onStickerZoomFinished(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerFlipped(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerDoubleTapped(@NonNull Sticker sticker) {

            }
        });

        ivCloseStickerOperation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                slideDown(llStickerOperation);

            }
        });
        ivCloseBackgroundRcv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                slideDown(llBackgroundRcv);

            }
        });

        BgAdapter.setOnSelectBG(new BgAdapter.onSelectBG() {
            @Override
            public void onBGSelect(int pos) {
                ivBackground.setImageResource(pos);
            }
        });


        imgDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stickerView.setHandlingSticker(null);
                new SaveFileTask(ImageEditActivity.this, flMainLayout) {
                    @Override
                    public void OnSaveFile(String path) {
                        try {
                            Toast.makeText(ImageEditActivity.this, "Save", Toast.LENGTH_SHORT).show();
                            Uri uri = Uri.parse(path);
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            Uri screenshotUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + uri);
                            sharingIntent.setType("image/jpeg");
                            sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                            startActivity(Intent.createChooser(sharingIntent, "Share image using"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.execute();
            }
        });


        backgroundList.add(R.drawable.bg_one);
        backgroundList.add(R.drawable.bg_two);
        backgroundList.add(R.drawable.bg_three);
        backgroundList.add(R.drawable.bg_four);
        backgroundList.add(R.drawable.bg_five);
        backgroundList.add(R.drawable.bg_six);
        backgroundList.add(R.drawable.bg_seven);
        backgroundList.add(R.drawable.bg_eight);
        backgroundList.add(R.drawable.bg_nine);
    }

    private void clicks() {
        llBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUp(llBackgroundRcv);
            }
        });
    }

    private void initilize() {
        bgAdapter = new BgAdapter(ImageEditActivity.this, backgroundList);
        rvBG.setAdapter(bgAdapter);
    }

    @SuppressLint("WrongViewCast")
    private void findiviewId() {
        stickerView = findViewById(R.id.stickerView);
        ivBackground = findViewById(R.id.ivBackground);
        mainImageView = findViewById(R.id.mainImageView);
        imgDone = findViewById(R.id.imgDone);
        flMainLayout = findViewById(R.id.flMainLayout);
        rvBG = findViewById(R.id.rvBG);
        llBG = findViewById(R.id.llBG);
        llStickerOperation = findViewById(R.id.llStickerOperation);
        llOperation = findViewById(R.id.llOperation);
        ivCloseStickerOperation = findViewById(R.id.ivCloseStickerOperation);
        ivCloseBackgroundRcv = findViewById(R.id.ivCloseBackgroundRcv);
        llBackgroundRcv = findViewById(R.id.llBackgroundRcv);
    }

    public void exitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ImageEditActivity.this);

        builder.setMessage("are you sure!!");

        builder.setTitle("Exit !");

        builder.setCancelable(false);

        builder.setPositiveButton("Discard changes", (DialogInterface.OnClickListener) (dialog, which) -> {
            finish();
        });
        builder.setNegativeButton("Keep Editing", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void person() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ImageEditActivity.this);

        builder.setMessage("person not detected");

        builder.setTitle("Not detected");

        builder.setCancelable(false);

        builder.setPositiveButton("change image", (DialogInterface.OnClickListener) (dialog, which) -> {
            finish();
        });
        builder.setNegativeButton("cancle", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void bitmap() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ImageEditActivity.this);

        builder.setMessage("Image not found");

        builder.setTitle("wrong !!");

        builder.setCancelable(false);

        builder.setPositiveButton("change Image", (DialogInterface.OnClickListener) (dialog, which) -> {
            finish();
        });
        builder.setNegativeButton("cancel", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void detectFace(Bitmap bitmap) {
        FaceDetectorOptions highAccuracyOpts = new FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL).setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build();


        try {
            image = InputImage.fromBitmap(bitmap, 0);
            detector = FaceDetection.getClient(highAccuracyOpts);

        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog = new Dialog(ImageEditActivity.this);
        dialog.setContentView(R.layout.dialog_loader);
        dialog.getWindow().setBackgroundDrawableResource(R.color.white);
        dialog.show();

        detector.process(image).addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(@NonNull List<Face> faces) {
                String resultText = "";
                int i = 1;
                for (Face face : faces) {
                    resultText = resultText.concat("\nFACE NUMBER. " + i + ": ").concat("\nSmile: " + face.getSmilingProbability() * 100 + "%").concat("\nleft eye open: " + face.getLeftEyeOpenProbability() * 100 + "%").concat("\nright eye open " + face.getRightEyeOpenProbability() * 100 + "%");
                    i++;
                }

                if (faces.size() == 0) {
                    // showDialog();
                    // If image does not contain person show error message here
                    person();

                } else {

                    createImageTransactor();
                }
            }
        }).addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImageEditActivity.this, "Oops, Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createImageTransactor() {
        this.analyzer = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(new MLImageSegmentationSetting.Factory().setAnalyzerType(0).create());
        if (isChosen(mOriginalBitmap)) {
            this.analyzer.asyncAnalyseFrame(new MLFrame.Creator().setBitmap(mOriginalBitmap).create()).addOnSuccessListener(new com.huawei.hmf.tasks.OnSuccessListener<MLImageSegmentation>() {
                public void onSuccess(MLImageSegmentation mlImageSegmentationResults) {

                    if (mlImageSegmentationResults != null) {
                        dialog.dismiss();
                        Bitmap cutBitmap = mlImageSegmentationResults.getForeground();
                        gone(mainImageView);
                        stickerView.addSticker(new DrawableSticker(new BitmapDrawable(getResources(), cutBitmap)));
                        return;
                    }
                    // Display error message if fail to detect cut bitmap
                    bitmap();
                }
            }).addOnFailureListener(new com.huawei.hmf.tasks.OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    finish();
                    // Display error message if fail to detect cut bitmap
                    bitmap();
                }
            });
            return;
        }
        Toast.makeText(getApplicationContext(), "Please Select Picture", Toast.LENGTH_SHORT).show();
    }

    private boolean isChosen(Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        }
        return true;
    }

    private Bitmap changeBitmapBackground(Bitmap originalBitmap, int newColor) {
        Bitmap newBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), originalBitmap.getConfig());
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(newColor);
        canvas.drawBitmap(originalBitmap, 0, 0, null);
        return newBitmap;
    }

    public void gone(View view) {

        view.setVisibility(View.GONE);
    }

    public void slideDown(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_slide_down);
            view.startAnimation(animation);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.GONE);
                }
            }, animation.getDuration());
        }
    }
    public void slideUp(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up);
            view.startAnimation(animation);
        }
    }


    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        if (rvBG.getVisibility() == View.VISIBLE) {
            llOperation.setVisibility(View.VISIBLE);
            rvBG.setVisibility(View.GONE);
            doubleBackToExitPressedOnce = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000); // Adjust the time interval as needed
        } else {
            Toast.makeText(this, "Press Again to Exit", Toast.LENGTH_SHORT).show();
            llOperation.setVisibility(View.VISIBLE);
            rvBG.setVisibility(View.VISIBLE);
        }
    }
//    public void txtAdapter() {
//        ArrayList<TextTool> textTool = new ArrayList<>();
//        textTool.add(new TextTool("color", R.drawable.color));
//        textTool.add(new TextTool("Bg Color", R.drawable.bgcolor));
//        textTool.add(new TextTool("Font", R.drawable.font));
//        textTool.add(new TextTool("Align", R.drawable.center));
//        rvBG.setAdapter(textAdapter = new TextAdapter(textTool, MainActivity.this, new TextInter() {
//            @Override
//            public void onClickText(int position) {
//                switch (position) {
//                    case 0:
//                        colorRecyclerView.setAdapter(new ColorAdapter(MainActivity.this, fetchTextStickerColor(), new ColorInter() {
//                            @Override
//                            public void onclickColor(int position) {
//                                if (stickerView.getCurrentSticker() instanceof TextSticker) {
//                                    TextSticker operational = (TextSticker) stickerView.getCurrentSticker();
//                                    operational.setTextColor(fetchTextStickerColor().get(position));
//                                    stickerView.replace(operational);
//                                    stickerView.invalidate();
//                                }
//                            }
//                        }));
//                        break;
//                    case 1:
//                        stickerUnLock();
//                        visible(colorLayout);
//                        gone(fontLayout, subRecyclerView);
//                        colorRecyclerView.setAdapter(new ColorAdapter(MainActivity.this, fetchTextStickerColor(), new ColorInter() {
//                            @Override
//                            public void onclickColor(int position) {
//                                if (stickerView.getCurrentSticker() instanceof TextSticker) {
//                                    TextSticker operational = (TextSticker) stickerView.getCurrentSticker();
//                                    operational.setTextBackgroundColor(fetchTextStickerColor().get(position));
//                                    stickerView.replace(operational);
//                                    stickerView.invalidate();
//                                }
//                            }
//                        }));
//                        break;
//                    case 2:
//                        stickerUnLock();
//                        visible(fontLayout);
//                        gone(colorLayout, subRecyclerView);
//                        fontRecyclerView.setAdapter(new FontAdapter(MainActivity.this, new FontClick() {
//                            @Override
//                            public void onFontClick(Typeface typeface) {
//                                if (stickerView.getCurrentSticker() instanceof TextSticker) {
//                                    TextSticker operational = (TextSticker) stickerView.getCurrentSticker();
//                                    operational.setTypeface(typeface);
//                                    stickerView.replace(operational);
//                                    stickerView.invalidate();
//                                }
//                            }
//                        }));
//                        break;
//                    case 3:
//                        stickerUnLock();
//                        textTool.clear();
//                        textTool.add(new TextTool("color", R.drawable.color));
//                        textTool.add(new TextTool("Bg Color", R.drawable.bgcolor));
//                        textTool.add(new TextTool("Font", R.drawable.font));
//                        if (stickerView.getCurrentSticker() instanceof TextSticker) {
//                            TextSticker operational = (TextSticker) stickerView.getCurrentSticker();
//                            if (click == 0) {
//                                textTool.add(new TextTool("Left", R.drawable.left));
//                                operational.setTextAlign(Layout.Alignment.ALIGN_NORMAL);
//                                click++;
//                            } else if (click == 1) {
//                                textTool.add(new TextTool("Center", R.drawable.center));
//                                operational.setTextAlign(Layout.Alignment.ALIGN_CENTER);
//                                click++;
//                            } else if (click == 2) {
//                                textTool.add(new TextTool("Right", R.drawable.right));
//                                operational.setTextAlign(Layout.Alignment.ALIGN_OPPOSITE);
//                                click = 0;
//                            }
//                            stickerView.replace(operational);
//                            stickerView.invalidate();
//                        }
//                        textAdapter.notifyDataSetChanged();
//                        break;
//                }
//            }
//        }));
//
//    }

}
