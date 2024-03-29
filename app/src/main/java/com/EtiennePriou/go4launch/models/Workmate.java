package com.EtiennePriou.go4launch.models;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Workmate {
    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    private PlaceToGo placeToGo;
    private Date dateCreated;

    public Workmate(){ }

    public Workmate(String uid, String username, String urlPicture, PlaceToGo placeToGo) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.placeToGo = placeToGo;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    @Nullable
    public String getUrlPicture() { return urlPicture; }
    public PlaceToGo getPlaceToGo() { return placeToGo; }
    @ServerTimestamp
    public Date getDateCreated() {
        return dateCreated;
    }

    // --- SETTERS ---

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPlaceToGo(PlaceToGo placeToGo) {
        this.placeToGo = placeToGo;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
