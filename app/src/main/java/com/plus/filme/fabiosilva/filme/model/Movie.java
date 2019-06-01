package com.plus.filme.fabiosilva.filme.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "favorite")
public class Movie {
    @PrimaryKey
    @ColumnInfo(name = "id")
    private int id;
    private String title;
    private String gender;
    private String date;
    private String resume;
    private Double rate;
    private String image;
    private String fullImage;

    @Ignore
    public Movie(int id, String title, Double rate, String image) {
        this.id = id;
        this.title = title;
        this.rate = rate;
        this.image = image;
    }

    public Movie(int id, String title, Double rate, String gender, String date, String resume, String image, String fullImage) {
        this.id = id;
        this.title = title;
        this.rate = rate;
        this.image = image;
        this.fullImage = fullImage;
        this.gender = gender;
        this.date = date;
        this.resume = resume;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGender() {
        return gender;
    }

    public String getDate() {
        return date;
    }

    public String getResume() {
        return resume;
    }

    public Double getRate() {
        return rate;
    }

    public String getImage() {
        return image;
    }

    public String getFullImage() {
        return fullImage;
    }
}
