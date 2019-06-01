package com.plus.filme.fabiosilva.filme.model;

public class Review {
    private String mAuthor;
    private String mContent;

    public Review(String author, String content){
        mAuthor = author;
        mContent = content;
    }

    public String getContent() {
        return mContent;
    }

    public String getAuthor(){
        return mAuthor;
    }
}
