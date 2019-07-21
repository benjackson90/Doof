package com.example.mac.food;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.appcompat.widget.Toolbar;
import id.zelory.compressor.Compressor;


public class NewFoodActivity extends AppCompatActivity {

    private Toolbar newFoodToolbar;

    private ImageView newFoodImage;
    private EditText newFoodTitle;
    private EditText newFoodDesc;
    private Button newFoodBtn;

    private Uri foodImageUri = null;

    private ProgressBar newFoodProgress;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_food);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        newFoodToolbar = findViewById(R.id.new_food_toolbar);
        setSupportActionBar(newFoodToolbar);
        getSupportActionBar().setTitle("Add New Food");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        newFoodImage = findViewById(R.id.new_food_image);
        newFoodTitle = findViewById(R.id.new_food_title);
        newFoodDesc = findViewById(R.id.new_food_desc);
        newFoodBtn = findViewById(R.id.food_btn);
        newFoodProgress = findViewById(R.id.new_food_progress);

        newFoodImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(NewFoodActivity.this);
            }
        });

        newFoodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = newFoodTitle.getText().toString();
                final String desc = newFoodDesc.getText().toString();

                if(!TextUtils.isEmpty(desc) && !TextUtils.isEmpty(title) && foodImageUri != null){

                    newFoodProgress.setVisibility(View.VISIBLE);

                    final String randomName = UUID.randomUUID().toString();

                    // PHOTO UPLOAD
                    File newImageFile = new File(foodImageUri.getPath());
                    try {

                        compressedImageFile = new Compressor(NewFoodActivity.this)
                                .setMaxHeight(720)
                                .setMaxWidth(720)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();

                    // PHOTO UPLOAD

                    final StorageReference filePath = storageReference.child("food_images").child(randomName + ".jpg");
                    filePath.putBytes(imageData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();

                                    if(task.isSuccessful()){

                                        File newThumbFile = new File(foodImageUri.getPath());
                                        try {

                                            compressedImageFile = new Compressor(NewFoodActivity.this)
                                                    .setMaxHeight(100)
                                                    .setMaxWidth(100)
                                                    .setQuality(1)
                                                    .compressToBitmap(newThumbFile);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] thumbData = baos.toByteArray();

                                        UploadTask uploadTask = storageReference.child("food_images/thumbs")
                                                .child(randomName + ".jpg").putBytes(thumbData);

                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                String downloadthumbUri = taskSnapshot.getTask().getResult().toString();

                                                Map<String, Object> foodMap = new HashMap<>();
                                                foodMap.put("image_url", downloadUri);
                                                foodMap.put("image_thumb", downloadthumbUri);
                                                foodMap.put("title", title);
                                                foodMap.put("desc", desc);
                                                foodMap.put("user_id", current_user_id);
                                                foodMap.put("timestamp", FieldValue.serverTimestamp());

                                                firebaseFirestore.collection("Foods").add(foodMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {

                                                        if(task.isSuccessful()){

                                                            Toast.makeText(NewFoodActivity.this, "Food was added", Toast.LENGTH_LONG).show();
                                                            Intent mainIntent = new Intent(NewFoodActivity.this, MainActivity.class);
                                                            startActivity(mainIntent);
                                                            finish();

                                                        } else {


                                                        }

                                                        newFoodProgress.setVisibility(View.INVISIBLE);

                                                    }
                                                });

                                            }

                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception e) {

                                            }
                                        });


                                    } else {
                                        newFoodProgress.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });

                    };
                });

            }
        };
    });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                foodImageUri = result.getUri();
                newFoodImage.setImageURI(foodImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }
}
