package com.example.vivanksharma.photoblog;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;
    private ImageButton newPostImage;
    private EditText newPostTitle,newPostDesc;
    private Button newPostButton;
    private Uri postImageUri=null;
    private ProgressBar progressBar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = firebaseAuth.getInstance();
        newPostImage = findViewById(R.id.newPostImage);
        progressBar = findViewById(R.id.progressBar2);
        newPostTitle = findViewById(R.id.newPostTitle);
        newPostDesc = findViewById(R.id.newPostDescription);
        newPostButton = findViewById(R.id.postNewPostButton);

        newPostToolbar = findViewById(R.id.newPostToolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);


            }
        });

        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("Reached","Till here"+postImageUri);
                final String desc = newPostDesc.getText().toString();
                final String title = newPostTitle.getText().toString();
                if(postImageUri!=null && !TextUtils.isEmpty(desc) && !TextUtils.isEmpty(title))
                {
                    Log.i("Reached","Till here");
                    progressBar.setVisibility(View.VISIBLE);
                    String randomName = UUID.randomUUID().toString();
                    StorageReference filePath = storageReference.child("post_images").child(randomName);
                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful())
                            {

                                String downloadUrl = task.getResult().getDownloadUrl().toString();
                                Map<String,Object> postMap = new HashMap<>();
                                postMap.put("image_url",downloadUrl);
                                postMap.put("blog_title",title);
                                postMap.put("desc",desc);
                                postMap.put("user_id",firebaseAuth.getUid());
                                postMap.put("timestamp",FieldValue.serverTimestamp());
                                firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(NewPostActivity.this,"Post added succesfully",Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(NewPostActivity.this,MainActivity.class));
                                            finish();
                                        }else{
                                            Toast.makeText(NewPostActivity.this,"Failed to upload",Toast.LENGTH_LONG).show();
                                        }
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }else{
                                Toast.makeText(NewPostActivity.this,"Failed to upload",Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });


                }else{
                    if(TextUtils.isEmpty(desc))
                    {

                    }
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
