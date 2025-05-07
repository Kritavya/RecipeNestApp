package com.kritavya.recipenest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.kritavya.recipenest.CreateRecipeActivity;
import com.kritavya.recipenest.R;

public class CreateRecipeAdapter extends RecyclerView.Adapter<CreateRecipeAdapter.RecipeViewHolder> {

    private Context context;
    private int[] layouts;
    private LayoutInflater layoutInflater;
    private ViewPager2 viewPager;

    public CreateRecipeAdapter(Context context, int[] layouts) {
        this.context = context;
        this.layouts = layouts;
        this.layoutInflater = LayoutInflater.from(context);
    }
    
    public void setViewPager(ViewPager2 viewPager) {
        this.viewPager = viewPager;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(viewType, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        // Setup view depending on position
        switch (position) {
            case 0:
                // Basic info page
                setupBasicInfoPage(holder, position);
                break;
            case 1:
                // Ingredients page
                setupIngredientsPage(holder, position);
                break;
            case 2:
                // Instructions page
                setupInstructionsPage(holder, position);
                break;
            case 3:
                // Additional info page
                setupAdditionalInfoPage(holder, position);
                break;
            case 4:
                // Review page
                setupReviewPage(holder, position);
                break;
        }
        
        // Set up back button for all pages
        ImageButton btnBackArrow = holder.itemView.findViewById(R.id.btnBackArrow);
        if (btnBackArrow != null) {
            if (position == 0) {
                // First page - clicking back should exit activity
                btnBackArrow.setOnClickListener(v -> {
                    // Exit activity
                    ((android.app.Activity) context).finish();
                });
            } else {
                // Other pages - go to previous page
                btnBackArrow.setOnClickListener(v -> {
                    ((CreateRecipeActivity)context).goToPreviousStep();
                });
            }
        }
    }
    
    private void setupBasicInfoPage(RecipeViewHolder holder, int position) {
        View view = holder.itemView;
        
        // Set up continue button
        Button btnContinueStep1 = view.findViewById(R.id.btnContinueStep1);
        if (btnContinueStep1 != null) {
            btnContinueStep1.setOnClickListener(v -> {
                ((CreateRecipeActivity)context).goToNextStep();
            });
        }
    }

    private void setupIngredientsPage(RecipeViewHolder holder, int position) {
        View view = holder.itemView;
        
        // Set up the quantity spinner
        Spinner spinnerQuantity = view.findViewById(R.id.spinnerQuantity);
        if (spinnerQuantity != null) {
            String[] quantities = new String[]{"Quantity", "100g", "200g", "1 cup", "2 cups", "1 tbsp", "2 tbsp"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, 
                android.R.layout.simple_spinner_item, quantities);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerQuantity.setAdapter(adapter);
        }
        
        // Set up add ingredient button
        Button btnAddIngredient = view.findViewById(R.id.btnAddIngredient);
        if (btnAddIngredient != null) {
            btnAddIngredient.setOnClickListener(v -> {
                // Add a new ingredient item
                // This would be implemented in a real app
            });
        }
        
        // Set up add instruction button
        Button btnAddInstruction = view.findViewById(R.id.btnAddInstruction);
        if (btnAddInstruction != null) {
            btnAddInstruction.setOnClickListener(v -> {
                // Add a new instruction item
                // This would be implemented in a real app
            });
        }
        
        // Set up add header button
        Button btnAddHeader = view.findViewById(R.id.btnAddHeader);
        if (btnAddHeader != null) {
            btnAddHeader.setOnClickListener(v -> {
                // Add a new header
                // This would be implemented in a real app
            });
        }
        
        // Set up edit button
        TextView tvEdit = view.findViewById(R.id.tvEdit);
        if (tvEdit != null) {
            tvEdit.setOnClickListener(v -> {
                // Enable editing mode
                // This would be implemented in a real app
            });
        }
        
        // Set up continue button - look for a generic continue button
        Button continueButton = findContinueButton(view);
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                ((CreateRecipeActivity)context).goToNextStep();
            });
        }
    }
    
    private void setupInstructionsPage(RecipeViewHolder holder, int position) {
        View view = holder.itemView;
        
        // Set up video upload area
        View videoUploadArea = view.findViewById(R.id.videoUploadArea);
        if (videoUploadArea != null) {
            videoUploadArea.setOnClickListener(v -> {
                // Launch video picker
                // This would be implemented in a real app
            });
        }
        
        // Set up continue button - look for a generic continue button
        Button continueButton = findContinueButton(view);
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                ((CreateRecipeActivity)context).goToNextStep();
            });
        }
    }
    
    private void setupAdditionalInfoPage(RecipeViewHolder holder, int position) {
        // Additional info page setup code
        View view = holder.itemView;
        
        // Set up continue button - look for a generic continue button
        Button continueButton = findContinueButton(view);
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                ((CreateRecipeActivity)context).goToNextStep();
            });
        }
    }
    
    private void setupReviewPage(RecipeViewHolder holder, int position) {
        // Review page setup code
        View view = holder.itemView;
        
        // Set up submit button - could be different ID
        Button submitButton = view.findViewById(R.id.btnFinish);
        if (submitButton == null) {
            // Try to find a button that might be used for submission
            submitButton = findSubmitButton(view);
        }
        
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> {
                // Submit recipe and finish
                ((android.app.Activity)context).finish();
            });
        }
    }
    
    /**
     * Helper method to find a continue button regardless of its specific ID
     */
    private Button findContinueButton(View view) {
        // Try specific IDs based on step
        int currentItem = viewPager.getCurrentItem();
        int[] buttonIds = {
            R.id.btnContinueStep1
            // The other steps don't have specific continue buttons with IDs
        };
        
        // If we have a specific ID for this step, try it
        if (currentItem < buttonIds.length) {
            Button btn = view.findViewById(buttonIds[currentItem]);
            if (btn != null) return btn;
        }
        
        // Try to find any button with text containing "continue" or "next"
        return findButtonByText(view, "Continue", "Next");
    }
    
    /**
     * Helper method to find a submit button regardless of its specific ID
     */
    private Button findSubmitButton(View view) {
        // Try to find any button with text containing "submit" or "finish"
        return findButtonByText(view, "Submit", "Finish", "Complete");
    }
    
    /**
     * Helper method to find a button by its text content
     */
    private Button findButtonByText(View view, String... textOptions) {
        if (!(view instanceof ViewGroup)) return null;
        
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            
            if (child instanceof Button) {
                Button button = (Button) child;
                CharSequence buttonText = button.getText();
                if (buttonText != null) {
                    String text = buttonText.toString().toLowerCase();
                    for (String option : textOptions) {
                        if (text.contains(option.toLowerCase())) {
                            return button;
                        }
                    }
                }
            } else if (child instanceof ViewGroup) {
                Button found = findButtonByText((ViewGroup) child, textOptions);
                if (found != null) return found;
            }
        }
        
        return null;
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return layouts[position];
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
} 