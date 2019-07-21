package com.example.mac.food;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class FoodId {
    @Exclude
    public String FoodId;

    public <T extends FoodId> T withId(@NonNull final String id) {
        this.FoodId = id;
        return (T) this;
    }
}
