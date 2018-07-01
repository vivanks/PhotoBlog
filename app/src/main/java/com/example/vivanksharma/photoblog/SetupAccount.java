package com.example.vivanksharma.photoblog;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupAccount extends AppCompatActivity {



    private String user_name;
    private String user_id;
    private Toolbar setupToolbar;
    private CircleImageView circleImageView;
    private Uri mainImageUri = null;
    private EditText setupName;
    private Button setupButton;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar setupProgress;
    private FirebaseFirestore firebaseFirestore;
    private ImageView setupImage;

    private Boolean isChanged = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_account);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        setupName = findViewById(R.id.editText);
        setupButton = findViewById(R.id.setup_button);
        setupProgress = findViewById(R.id.setup_progress);
        user_id = firebaseAuth.getCurrentUser().getUid();
        setupImage = findViewById(R.id.setup_image);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists())
                    {
                        setupName.setText(""+task.getResult().getString("name"));
                        RequestOptions placeHolder = new RequestOptions();
                        placeHolder.placeholder(R.mipmap.default_image);
                        Glide.with(SetupAccount.this).setDefaultRequestOptions(placeHolder).load(task.getResult().getString("image")).into(setupImage);
                        Toast.makeText(SetupAccount.this,"Data Exists",Toast.LENGTH_LONG).show();
                        mainImageUri = Uri.parse(task.getResult().getString("image"));
                    }else{
                        Toast.makeText(SetupAccount.this,"Data don't Exists",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(SetupAccount.this,"Error: "+task.getException().getMessage().toString(),Toast.LENGTH_LONG).show();
                }
            }
        });

        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                user_name = setupName.getText().toString();
                if (!TextUtils.isEmpty(user_name) && mainImageUri != null) {
                        if (isChanged) {
                        setupProgress.setVisibility(View.VISIBLE);
                        user_id = firebaseAuth.getCurrentUser().getUid();
                        StorageReference image_path = storageReference.child("profile_images").child(user_id + "jpg");
                        image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {
                                    storeFireStore(task);
                                } else {
                                    Toast.makeText(SetupAccount.this,
                                            "Error: " + task.getException().getMessage().toString(),
                                            Toast.LENGTH_LONG).show();
                                    setupProgress.setVisibility(View.INVISIBLE);
                                }

                            }
                        });
                    }
                 else {
                    storeFireStore(null);
                }
            }
            }
        });


        circleImageView = findViewById(R.id.setup_image);
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(SetupAccount.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(SetupAccount.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else{
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(SetupAccount.this);

                }
            }
        });

        setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");
    }

    private void storeFireStore(@NonNull Task<UploadTask.TaskSnapshot> task) {

        Uri download_uri;
        if(task!=null)
        {
             download_uri = task.getResult().getDownloadUrl();
        }else{
             download_uri = mainImageUri;
        }

        Map<String,String> userMap = new HashMap<>();
        userMap.put("name",user_name);
        userMap.put("image",download_uri.toString());
        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SetupAccount.this,"Image Uploaded !",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SetupAccount.this,MainActivity.class));
                    finish();
                }else{
                    Toast.makeText(SetupAccount.this,"Error: "+task.getException().getMessage().toString(),Toast.LENGTH_LONG).show();
                }
                setupProgress.setVisibility(View.INVISIBLE);
            }
        });
        Toast.makeText(SetupAccount.this,"Image Uploaded !",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                setupImage.setImageURI(mainImageUri);
                isChanged=true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
