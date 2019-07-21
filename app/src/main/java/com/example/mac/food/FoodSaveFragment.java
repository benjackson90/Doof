package com.example.mac.food;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class FoodSaveFragment extends Fragment {
    private RecyclerView food_list_view;
    private List<Food> food_list;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FoodRecyclerAdapter foodRecyclerAdapter;
    private DocumentSnapshot lastVisible;

    private Boolean isFirstPageFirstLoad = true;

    public FoodSaveFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_food_save, container, false);

        food_list = new ArrayList<>();
        food_list_view = (RecyclerView) view.findViewById(R.id.food_list_view);

        firebaseAuth = FirebaseAuth.getInstance();

        foodRecyclerAdapter = new FoodRecyclerAdapter(food_list);
        food_list_view.setAdapter(foodRecyclerAdapter);
        food_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        food_list_view.setHasFixedSize(true);

        if(firebaseAuth.getCurrentUser() != null) {

            final String currentUserId = firebaseAuth.getCurrentUser().getUid();

            firebaseFirestore = FirebaseFirestore.getInstance();

            firebaseFirestore.collection("Users/" + currentUserId + "/Saves").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("datatatat", document.getId() + " => " + document.getData());
                                    final String foodId = document.getId();

                                    firebaseFirestore.collection("Foods").document(foodId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if(task.isSuccessful()){
                                                DocumentSnapshot doc = task.getResult();
                                                Food food = doc.toObject(Food.class).withId(foodId);
                                                if (isFirstPageFirstLoad) {
                                                      food_list.add(food);

                                                } else {
                                                       food_list.add(0, food);

                                                }
                                                foodRecyclerAdapter.notifyDataSetChanged();

                                            } else {


                                            }

                                        }
                                    });

                                }
                            } else {
                                Log.d("errror", "Error getting documents: ", task.getException());
                            }
                        }
                    });

        }

        // Inflate the layout for this fragment
        return view;
    }

}
