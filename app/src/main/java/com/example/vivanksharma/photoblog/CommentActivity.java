package com.example.vivanksharma.photoblog;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar commentToolbar;
    private EditText comment_field;
    private ImageButton comment_post_btn;
    private String blog_post_id;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUid;
    private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        commentToolbar = findViewById(R.id.commentToolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");


        firebaseAuth = FirebaseAuth.getInstance();
        currentUid = firebaseAuth.getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        comment_list = findViewById(R.id.commentList);
        blog_post_id = getIntent().getStringExtra("blog_post_id");
        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentsRecyclerAdapter);
        //RecyclerView Firebase list
        firebaseFirestore.collection("Posts/"+blog_post_id+"/Comments")
                .addSnapshotListener(CommentActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty())
                {


                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            Log.i("Comment","Comment added");
                            String commentId = doc.getDocument().getId();
                            Log.i("Comment","Comment added"+commentId);
                            Comments comments = doc.getDocument().toObject(Comments.class);
                            commentsList.add(comments);
                            commentsRecyclerAdapter.notifyDataSetChanged();


                        }
                    }

                }
            }
        });


        blog_post_id = getIntent().getStringExtra("blog_post_id");
        comment_field = findViewById(R.id.commentInput);
        comment_post_btn = findViewById(R.id.commentSendBtn);

        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment_message = comment_field.getText().toString();

                if(!TextUtils.isEmpty(comment_message)){
                    Map<String,Object> commentsMap = new HashMap<>();
                    commentsMap.put("message",comment_message);
                    commentsMap.put("user_id",currentUid);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());
                    firebaseFirestore.collection("Posts/"+blog_post_id+"/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(!task.isSuccessful())
                            {
                                Toast.makeText(CommentActivity.this,"Error Posting Comment : "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }else{
                                comment_field.setText("");
                                Toast.makeText(CommentActivity.this,"Successfully Posted Comment",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });


    }
}
