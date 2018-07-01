package com.example.vivanksharma.photoblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public List<User> user_list;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private TextView blogUserName;
    private CircleImageView blogUserImage;


    public BlogRecyclerAdapter(List<BlogPost> blog_list,List<User> user_list){
        this.blog_list = blog_list;
        this.user_list = user_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_item,viewGroup,false);
        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        //viewHolder.setIsRecyclable(false);
        String desc_data = blog_list.get(i).getDesc();
        String title_data = blog_list.get(i).getBlog_title();
        final String blogPostId = blog_list.get(i).BlogPostId;
        final String currentUID = firebaseAuth.getUid();


        viewHolder.setDesc_view(desc_data,title_data);
        String image_url = blog_list.get(i).getImage_url();
        viewHolder.setImage(image_url);
        String user_id = blog_list.get(i).getUser_id();

        String user_name = user_list.get(i).getName();
        String user_image = user_list.get(i).getImage();
        viewHolder.setUserData(user_name,user_image);



        Log.i("Date","Date is "+blog_list.get(i).getTimestamp().toString());
        long milliseconds = blog_list.get(i).getTimestamp().getTime();
        Date date = new Date(milliseconds);
        DateFormat dF = new SimpleDateFormat("dd:MM:yy  HH:mm");
        String dateString = dF.format(date).toString();
        viewHolder.setTime(dateString);

        //Get Like Count
        if(blogPostId!=null){
        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(documentSnapshots!=null) {
                    if (!documentSnapshots.isEmpty()) {
                        int count = documentSnapshots.size();
                        //Log.i("Count","sf"+count);
                        viewHolder.updateLikesCount(count);
                    } else {
                        viewHolder.updateLikesCount(0);
                    }
                }
            }
        });}

        //Get comment count
        if(blogPostId!=null){
            firebaseFirestore.collection("Posts/"+blogPostId+"/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if(documentSnapshots!=null) {
                        if (!documentSnapshots.isEmpty()) {
                            int count = documentSnapshots.size();
                            viewHolder.updateCommentCount(count);
                        } else {
                            viewHolder.updateLikesCount(8);
                        }
                    }
                }
            });}
        //Get Likes
        if(currentUID!=null){
        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(documentSnapshot!=null) {
                    if (documentSnapshot.exists()) {
                        viewHolder.blogLikebtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));
                    } else {
                        viewHolder.blogLikebtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_grey));
                    }
                }
            }
        });}

        //Likes feature
        viewHolder.blogLikebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists())
                        {
                            Map<String,Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUID).set(likesMap);
                        }else{
                            Map<String,Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUID).delete();
                        }
                    }
                });

            }
        });

        viewHolder.blogComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(context,CommentActivity.class);
                commentIntent.putExtra("blog_post_id",blogPostId);
                context.startActivity(commentIntent);
            }
        });


    }

    @Override
    public int getItemCount() {

        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView desc_view,title_view;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView likesCount;
        private ImageView blogLikebtn;
        private ImageView blogComment;
        private TextView blogCount;
        private TextView commentCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView=itemView;
            blogLikebtn = itemView.findViewById(R.id.blog_like_btn);
            blogComment = itemView.findViewById(R.id.blogCommentBtn);



        }

        public void setDesc_view(String descText,String title){
            desc_view = mView.findViewById(R.id.blogDescription);
            title_view = mView.findViewById(R.id.blogTitle);
            desc_view.setText(descText);
            title_view.setText(title);
        }

        public void setImage(String downloadURI)
        {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.b2b2b2);
            blogImageView = mView.findViewById(R.id.blogImage);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadURI).into(blogImageView);
        }

        public void setTime(String date)
        {
            blogDate = mView.findViewById(R.id.blogDate);
            blogDate.setText(date);
        }

        public void setUserData(String userName,String userImage)
        {
            blogUserImage = mView.findViewById(R.id.blogUserImage);
            blogUserName = mView.findViewById(R.id.blogUserName);
            blogUserName.setText(userName);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.baseline_account_circle_black_48);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(userImage).into(blogUserImage);
        }

        public void updateLikesCount(int n)
        {
            likesCount = mView.findViewById(R.id.blogLikeCount);
            likesCount.setText(n+" likes");
        }

        public void updateCommentCount(int i)
        {
            commentCount = mView.findViewById(R.id.blogCommentCount);
            commentCount.setText(i+" comments");
        }
    }
}
