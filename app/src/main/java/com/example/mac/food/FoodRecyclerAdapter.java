package com.example.mac.food;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mac.food.Food;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import de.hdodenhof.circleimageview.CircleImageView;

public class FoodRecyclerAdapter extends RecyclerView.Adapter<FoodRecyclerAdapter.ViewHolder> {

    public List<Food> food_list = new ArrayList<>();
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public FoodRecyclerAdapter(List<Food> food_list){
        this.food_list = food_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
          holder.setIsRecyclable(false);

          final String foodId = food_list.get(position).FoodId;
          final String currentUserId = firebaseAuth.getCurrentUser().getUid();
          String title_data = food_list.get(position).getTitle();
          String desc_data = food_list.get(position).getDesc();
          holder.setTitleText(title_data);
          holder.setDescText(desc_data);


          String image_url = food_list.get(position).getImage_url();
          String thumbUri = food_list.get(position).getImage_thumb();
          Log.d("image", image_url);
          holder.setFoodImage(image_url, thumbUri);

          String user_id = food_list.get(position).getUser_id();
           firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    holder.setUserData(userName, userImage);
                } else {

                    //Firebase Exception

                }

            }
        });

        try {
            long millisecond = food_list.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        firebaseFirestore.collection("Foods/" + foodId + "/Saves").addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()){

                    int count = documentSnapshots.size();

                    holder.updateSaveCount(count);

                } else {

                    holder.updateSaveCount(0);

                }

            }
        });
//
//
        firebaseFirestore.collection("Foods/" + foodId + "/Saves").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if(documentSnapshot.exists()){

                    holder.foodSaveBtn.setImageDrawable(context.getResources().getDrawable(R.mipmap.action_bookmark_red));

                } else {

                    holder.foodSaveBtn.setImageDrawable(context.getResources().getDrawable(R.mipmap.action_bookmark));

                }

            }
        });
//
        holder.foodSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Foods/" + foodId + "/Saves").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){

                            Map<String, Object> savesMap = new HashMap<>();
                            savesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Foods/" + foodId + "/Saves").document(currentUserId).set(savesMap);

                        } else {

                            firebaseFirestore.collection("Foods/" + foodId + "/Saves").document(currentUserId).delete();

                        }

                    }
                });
                firebaseFirestore.collection("Users/" + currentUserId + "/Saves").document(foodId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){

                            Map<String, Object> savesMap = new HashMap<>();
                            savesMap.put("timestamp", FieldValue.serverTimestamp());
                            savesMap.put("food_id", foodId);
                            firebaseFirestore.collection("Users/" + currentUserId + "/Saves").document(foodId).set(savesMap);

                        } else {

                            firebaseFirestore.collection("Users/" + currentUserId + "/Saves").document(foodId).delete();

                        }

                    }
                });;
            }
        });

    }


    @Override
    public int getItemCount() {
        return food_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView titleView;
        private TextView descView;
        private ImageView foodImageView;
        private TextView foodDate;
        private CardView cardView;

        private TextView foodUserName;
        private CircleImageView foodUserImage;

        private ImageView foodSaveBtn;
        private TextView foodSaveCount;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            cardView = mView.findViewById(R.id.main_food_post);
            foodSaveBtn = mView.findViewById(R.id.food_save_btn);

        }

        public void setTitleText(String titleText){

            titleView = mView.findViewById(R.id.food_title);
            titleView.setText(titleText);

        }

        public void setDescText(String descText){

            descView = mView.findViewById(R.id.food_desc);
            descView.setText(descText);

        }

        public void setFoodImage(String downloadUri, String thumbUri){

            foodImageView = mView.findViewById(R.id.food_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.food_placeholder);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(
                    Glide.with(context).load(thumbUri)
            ).into(foodImageView);

        }

        public void setTime(String date) {

            foodDate = mView.findViewById(R.id.food_date);
            foodDate.setText(date);

        }

        public void setUserData(String name, String image){

            foodUserImage = mView.findViewById(R.id.food_user_image);
            foodUserName = mView.findViewById(R.id.food_user_name);

            foodUserName.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(foodUserImage);

        }

        public void updateSaveCount(int count){

            foodSaveCount = mView.findViewById(R.id.food_save_count);
            foodSaveCount.setText(count + " Saves");

        }

    }

}