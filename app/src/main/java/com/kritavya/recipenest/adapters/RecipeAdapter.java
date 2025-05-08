package com.kritavya.recipenest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.kritavya.recipenest.R;
import com.kritavya.recipenest.models.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<Recipe> recipes;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;
    private boolean isHorizontalLayout;
    private boolean isUploadedRecipes;

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe);
    }
    
    public interface OnDeleteClickListener {
        void onDeleteClick(Recipe recipe, int position);
    }

    public RecipeAdapter(Context context, List<Recipe> recipes) {
        this(context, recipes, false);
    }

    public RecipeAdapter(Context context, List<Recipe> recipes, boolean isHorizontalLayout) {
        this.context = context;
        this.recipes = recipes;
        this.isHorizontalLayout = isHorizontalLayout;
    }
    
    public void setIsUploadedRecipes(boolean isUploadedRecipes) {
        this.isUploadedRecipes = isUploadedRecipes;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }
    
    public void removeRecipe(int position) {
        if (position >= 0 && position < recipes.size()) {
            recipes.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = isHorizontalLayout ? R.layout.item_recipe_horizontal : R.layout.item_recipe;
        View view = LayoutInflater.from(context).inflate(layoutRes, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        
        // Set recipe name
        holder.recipeName.setText(recipe.getName());
        
        // Set recipe image
        String imageUrl = null;
        if (recipe.getImageUrls() != null && !recipe.getImageUrls().isEmpty()) {
            imageUrl = recipe.getImageUrls().get(0);
        } else if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            imageUrl = recipe.getImageUrl();
        }
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_food)
                .into(holder.recipeImage);
        } else {
            holder.recipeImage.setImageResource(R.drawable.placeholder_food);
        }
        
        // Set recipe time (prep + cook)
        int totalTime = recipe.getPrepTime() + recipe.getCookTime();
        if (totalTime > 0) {
            holder.recipeTime.setText(totalTime + " min");
        } else {
            holder.recipeTime.setText("--");
        }
        
        // Set recipe rating
        holder.recipeRating.setText(String.format("%.1f", recipe.getRating()));
        
        // Show delete button only for uploaded recipes by the current user
        if (holder.btnDelete != null) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
            
            boolean canDelete = isUploadedRecipes && 
                    recipe.getUserId() != null && 
                    recipe.getUserId().equals(currentUserId);
            
            holder.btnDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
            
            // Set delete click listener
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(recipe, holder.getAdapterPosition());
                }
            });
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public ImageView recipeImage;
        public TextView recipeName, recipeTime, recipeRating;
        public ImageButton btnDelete;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeTime = itemView.findViewById(R.id.recipeTime);
            recipeRating = itemView.findViewById(R.id.recipeRating);
            btnDelete = itemView.findViewById(R.id.btnDeleteRecipe);
        }
    }
} 