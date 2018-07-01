package com.example.vivanksharma.photoblog;

import android.util.Log;

import java.sql.Timestamp;
import java.util.Date;

public class BlogPost extends BlogPostId {


    public String user_id,image_url,blog_title,desc;




    public Date timestamp;
    public BlogPost() {}

    public BlogPost(String user_id, String image_url, String blog_title, String desc,Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.blog_title = blog_title;
        this.desc = desc;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getBlog_title() {
        return blog_title;
    }

    public void setBlog_title(String blog_title) {
        this.blog_title = blog_title;
    }

    public String getDesc() {
        //Log.i("Description",""+desc);
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }



}
