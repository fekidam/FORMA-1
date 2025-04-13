package com.example.forma_1;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {
    private List<Driver> driverList;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    public DriverAdapter(List<Driver> driverList) {
        this.driverList = driverList;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);
        holder.driverName.setText(driver.getName());
        holder.teamName.setText(driver.getTeam());
        holder.nationality.setText(driver.getNationality());

        String imageName = driver.getImageName();
        StorageReference imageRef = storageRef.child("drivers/" + imageName);

        Log.d("DriverAdapter", "Attempting to load image: " + imageName);

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(holder.itemView.getContext())
                    .load(uri)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.f1)
                    .error(R.drawable.f1)
                    .into(holder.driverImage);
        }).addOnFailureListener(e -> {
            Log.e("DriverAdapter", "Failed to load image: " + imageName + ", " + e.getMessage());
            holder.driverImage.setImageResource(R.drawable.f1);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DriverDetailActivity.class);
            intent.putExtra("DRIVER", driver);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView driverName, teamName, nationality;
        ImageView driverImage;

        DriverViewHolder(View itemView) {
            super(itemView);
            driverName = itemView.findViewById(R.id.driverName);
            teamName = itemView.findViewById(R.id.teamName);
            nationality = itemView.findViewById(R.id.nationality);
            driverImage = itemView.findViewById(R.id.driverImage);
        }
    }
}
