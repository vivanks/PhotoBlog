package com.example.vivanksharma.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FloatingActionButton addPostButton;
    private String currentuserId;
    private BottomNavigationView mainBottomNavigation;
    private HomeFragment homeFragment;
    private AccountFragment accountFragment;
    private NotificationFragment notificationFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mainBottomNavigation = findViewById(R.id.mainBottomNavigation);

        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();



        firebaseAuth = FirebaseAuth.getInstance();
        mainToolBar = findViewById(R.id.main_toolbar);
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentuserId = firebaseAuth.getUid();
        setSupportActionBar(mainToolBar);


        mainToolBar.setTitle("Photo Blog");
        addPostButton = findViewById(R.id.addPostButton);


        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,NewPostActivity.class));

            }
        });
        replace(homeFragment);

        mainBottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId())
                {
                    case R.id.bottomHome:
                        replace(homeFragment);
                        return true;
                    
                        default:
                            return false;
                }


            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser==null)
        {
            sendToLogin();
        }else{
            firebaseFirestore.collection("Users").document(currentuserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful())
                    {
                        if(!task.getResult().exists()){
                            startActivity(new Intent(MainActivity.this,SetupAccount.class));
                            finish();
                        }
                    }else{
                        Toast.makeText(MainActivity.this,"Error"+task.getException().getMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void sendToLogin() {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.logout_button:
                logout();
                return true;
            case R.id.setting_button:
                sendToSetup();

                default:
                    return false;
        }

    }

    private void sendToSetup() {
        startActivity(new Intent(MainActivity.this,SetupAccount.class));

    }

    private void logout() {

        firebaseAuth.signOut();
        sendToLogin();

    }

    private void replace(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();
    }
}
