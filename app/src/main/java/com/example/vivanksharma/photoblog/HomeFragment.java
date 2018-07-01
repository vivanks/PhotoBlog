package com.example.vivanksharma.photoblog;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private List<User> user_list;


    public HomeFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_home, container, false);
        blog_list_view = view.findViewById(R.id.blog_list_view);
        blog_list = new ArrayList<>();
        user_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list,user_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);



        //Just for removal of the slight delay caused by the recycling
        //blog_list_view.setDrawingCacheEnabled(true);
        //blog_list_view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()!=null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp",Query.Direction.DESCENDING).limit(3);
            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(final QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (documentSnapshots != null) {

                        if(!documentSnapshots.isEmpty()) {

                            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);
                                    Boolean isReachedBottom = !recyclerView.canScrollVertically(1);
                                    if (isReachedBottom) {
                                        loadMorePost();
                                    }
                                }
                            });

                            if (isFirstPageFirstLoad) {
                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            }

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    String blogUserId = doc.getDocument().getString("user_id");
                                    firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if (task.isSuccessful()) {
                                                User user = task.getResult().toObject(User.class);


                                                if (isFirstPageFirstLoad) {
                                                    user_list.add(user);
                                                    blog_list.add(blogPost);
                                                } else {
                                                    user_list.add(0, user);
                                                    blog_list.add(0, blogPost);

                                                }
                                                blogRecyclerAdapter.notifyDataSetChanged();

                                            } else {

                                            }
                                        }
                                    });
                                    //BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                                    Log.i("Got", "Documents");


                                }
                            }
                            isFirstPageFirstLoad = false;
                        }
                    }
                }

            });

        }


        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost()
    {
        Query next = firebaseFirestore.collection("Posts").orderBy("timestamp",Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        next.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty())
                    if (documentSnapshots != null) {
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId = doc.getDocument().getId();
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                Log.i("Got", "Documents");
                                String blogUserId = doc.getDocument().getString("user_id");
                                firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            User user = task.getResult().toObject(User.class);



                                                user_list.add(user);
                                                blog_list.add(blogPost);

                                            blogRecyclerAdapter.notifyDataSetChanged();

                                        }else{

                                        }
                                    }
                                });
                            }
                        }
                    }
            }

        });
    }


    @Override
    public void onDetach() {
        // this will make you scroll all the way down
        isFirstPageFirstLoad = true;
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        // this will rearrange them in desending order
        isFirstPageFirstLoad = true;
        super.onAttach(context);
    }
}
