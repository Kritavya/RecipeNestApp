package com.kritavya.recipenest.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.kritavya.recipenest.CreateRecipeActivity;
import com.kritavya.recipenest.R;
import com.kritavya.recipenest.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class CreateRecipeAdapter extends RecyclerView.Adapter<CreateRecipeAdapter.RecipeViewHolder> {

    private Context context;
    private int[] layouts;
    private LayoutInflater layoutInflater;
    private ViewPager2 viewPager;
    private Recipe recipe;
    private RecipeDataListener dataListener;
    
    // View references for data collection
    private EditText etRecipeName, etRecipeDescription, etPrepTime, etCookTime, etServings, etVideoUrl;
    private Spinner spinnerDifficulty, spinnerCuisineType;
    private EditText etIngredientName, etIngredientAmount;
    private Spinner spinnerUnit;
    private EditText etInstructionTitle, etInstructionStep;
    private LinearLayout ingredientsList, instructionsList;
    private ImageView recipeImagePreview;
    private CardView uploadImageArea;

    public CreateRecipeAdapter(Context context, int[] layouts) {
        this.context = context;
        this.layouts = layouts;
        this.layoutInflater = LayoutInflater.from(context);
    }
    
    public void setViewPager(ViewPager2 viewPager) {
        this.viewPager = viewPager;
    }
    
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
    
    public void setDataListener(RecipeDataListener listener) {
        this.dataListener = listener;
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
                // Review page - save all data first to ensure it's up to date
                saveBasicInfoData();
                saveIngredientsData();
                saveInstructionsData();
                saveAdditionalInfoData();
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
                    ((CreateRecipeActivity) context).finish();
                });
            } else {
                // Other pages - go to previous page
                btnBackArrow.setOnClickListener(v -> {
                    ((CreateRecipeActivity)context).goToPreviousStep();
                });
            }
        }
        
        // Alternative back button ID in review page
        ImageButton btnBack = holder.itemView.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                ((CreateRecipeActivity)context).goToPreviousStep();
            });
        }
    }
    
    private void setupBasicInfoPage(RecipeViewHolder holder, int position) {
        View view = holder.itemView;
        
        // Get reference to views
        etRecipeName = view.findViewById(R.id.etRecipeName);
        etRecipeDescription = view.findViewById(R.id.etRecipeDescription);
        recipeImagePreview = view.findViewById(R.id.recipeImagePreview);
        uploadImageArea = view.findViewById(R.id.uploadImageArea);
        
        // Restore data if available
        if (recipe != null) {
            if (recipe.getName() != null) {
                etRecipeName.setText(recipe.getName());
            }
            if (recipe.getDescription() != null) {
                etRecipeDescription.setText(recipe.getDescription());
            }
        }
        
        // Set up upload image area
        uploadImageArea.setOnClickListener(v -> {
            // Request image pick from activity
            if (dataListener != null) {
                dataListener.onImagePickRequested();
            }
        });
        
        // Set up continue button
        Button btnContinueStep1 = view.findViewById(R.id.btnContinueStep1);
        if (btnContinueStep1 != null) {
            btnContinueStep1.setOnClickListener(v -> {
                saveBasicInfoData();
                ((CreateRecipeActivity)context).goToNextStep();
            });
        }
    }
    
    private void saveBasicInfoData() {
        if (recipe != null) {
            if (etRecipeName != null) {
                recipe.setName(etRecipeName.getText().toString().trim());
            }
            if (etRecipeDescription != null) {
                recipe.setDescription(etRecipeDescription.getText().toString().trim());
            }
            
            // Notify listener
            if (dataListener != null) {
                dataListener.onRecipeDataChanged(recipe);
            }
        }
    }

    private void setupIngredientsPage(RecipeViewHolder holder, int position) {
        View view = holder.itemView;
        
        // Get references to views
        etIngredientName = view.findViewById(R.id.etIngredientName);
        etIngredientAmount = view.findViewById(R.id.etIngredientAmount);
        spinnerUnit = view.findViewById(R.id.spinnerUnit);
        ingredientsList = view.findViewById(R.id.ingredientsList);
        
        // Set up the unit spinner
        if (spinnerUnit != null) {
            String[] units = new String[]{"g", "kg", "ml", "L", "cup", "tbsp", "tsp", "pcs", "bunch"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, 
                android.R.layout.simple_spinner_item, units);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUnit.setAdapter(adapter);
        }
        
        // Set up add ingredient button
        Button btnAddIngredient = view.findViewById(R.id.btnAddIngredient);
        if (btnAddIngredient != null) {
            btnAddIngredient.setOnClickListener(v -> {
                addIngredient();
            });
        }
        
        // Display existing ingredients
        displayIngredients();
        
        // Set up continue button
        Button continueButton = findContinueButton(view);
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                ((CreateRecipeActivity)context).goToNextStep();
            });
        }
    }
    
    private void addIngredient() {
        if (etIngredientName == null || etIngredientAmount == null || spinnerUnit == null) {
            return;
        }
        
        String name = etIngredientName.getText().toString().trim();
        String amount = etIngredientAmount.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();
        
        if (name.isEmpty() || amount.isEmpty()) {
            return;
        }
        
        // Create new ingredient
        Recipe.Ingredient ingredient = new Recipe.Ingredient(name, amount, unit);
        
        // Add to recipe
        if (recipe != null) {
            recipe.addIngredient(ingredient);
            
            // Notify listener
            if (dataListener != null) {
                dataListener.onRecipeDataChanged(recipe);
            }
            
            // Clear input fields
            etIngredientName.setText("");
            etIngredientAmount.setText("");
            
            // Update the list
            displayIngredients();
        }
    }
    
    private void displayIngredients() {
        if (ingredientsList == null || recipe == null) {
            return;
        }
        
        // Clear the list
        ingredientsList.removeAllViews();
        
        // Add each ingredient
        for (Recipe.Ingredient ingredient : recipe.getIngredients()) {
            View itemView = layoutInflater.inflate(R.layout.item_ingredient, ingredientsList, false);
            
            TextView tvIngredientName = itemView.findViewById(R.id.tvIngredientName);
            TextView tvIngredientAmount = itemView.findViewById(R.id.tvIngredientAmount);
            ImageButton btnDeleteIngredient = itemView.findViewById(R.id.btnDeleteIngredient);
            
            tvIngredientName.setText(ingredient.getName());
            tvIngredientAmount.setText(ingredient.getAmount() + " " + ingredient.getUnit());
            
            // Handle delete
            btnDeleteIngredient.setOnClickListener(v -> {
                recipe.getIngredients().remove(ingredient);
                
                // Notify listener
                if (dataListener != null) {
                    dataListener.onRecipeDataChanged(recipe);
                }
                
                // Update the list
                displayIngredients();
            });
            
            ingredientsList.addView(itemView);
            
            // Add a divider except for the last item
            if (recipe.getIngredients().indexOf(ingredient) < recipe.getIngredients().size() - 1) {
                View divider = new View(context);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(context.getResources().getColor(R.color.colorDivider));
                ingredientsList.addView(divider);
            }
        }
    }
    
    private void setupInstructionsPage(RecipeViewHolder holder, int position) {
        View view = holder.itemView;
        
        // Get references to views
        etInstructionTitle = view.findViewById(R.id.etInstructionTitle);
        etInstructionStep = view.findViewById(R.id.etInstructionStep);
        instructionsList = view.findViewById(R.id.instructionsList);
        
        // Set up add instruction step button
        Button btnAddStep = view.findViewById(R.id.btnAddStep);
        if (btnAddStep != null) {
            btnAddStep.setOnClickListener(v -> {
                addInstructionStep();
            });
        }
        
        // Display existing instructions
        displayInstructions();
        
        // Set up continue button
        Button continueButton = findContinueButton(view);
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                ((CreateRecipeActivity)context).goToNextStep();
            });
        }
    }
    
    private void addInstructionStep() {
        if (etInstructionTitle == null || etInstructionStep == null) {
            return;
        }
        
        String title = etInstructionTitle.getText().toString().trim();
        String step = etInstructionStep.getText().toString().trim();
        
        if (title.isEmpty() || step.isEmpty()) {
            return;
        }
        
        // Check if we already have an instruction with this title
        Recipe.Instruction existingInstruction = null;
        if (recipe != null) {
            for (Recipe.Instruction instruction : recipe.getInstructions()) {
                if (instruction.getTitle().equals(title)) {
                    existingInstruction = instruction;
                    break;
                }
            }
        }
        
        if (existingInstruction != null) {
            // Add step to existing instruction
            existingInstruction.addStep(step);
        } else {
            // Create new instruction
            Recipe.Instruction instruction = new Recipe.Instruction(title);
            instruction.addStep(step);
            
            // Add to recipe
            if (recipe != null) {
                recipe.addInstruction(instruction);
            }
        }
        
        // Notify listener
        if (dataListener != null && recipe != null) {
            dataListener.onRecipeDataChanged(recipe);
        }
        
        // Clear input fields
        etInstructionStep.setText("");
        
        // Update the list
        displayInstructions();
    }
    
    private void displayInstructions() {
        if (instructionsList == null || recipe == null) {
            return;
        }
        
        // Clear the list
        instructionsList.removeAllViews();
        
        // Add each instruction
        for (Recipe.Instruction instruction : recipe.getInstructions()) {
            View headerView = layoutInflater.inflate(R.layout.item_instruction_header, instructionsList, false);
            TextView tvInstructionTitle = headerView.findViewById(R.id.tvInstructionTitle);
            tvInstructionTitle.setText(instruction.getTitle());
            
            instructionsList.addView(headerView);
            
            // Add each step
            for (String step : instruction.getSteps()) {
                View stepView = layoutInflater.inflate(R.layout.item_instruction_step, instructionsList, false);
                TextView tvStepText = stepView.findViewById(R.id.tvStepText);
                ImageButton btnDeleteStep = stepView.findViewById(R.id.btnDeleteStep);
                
                tvStepText.setText(step);
                
                // Handle delete
                btnDeleteStep.setOnClickListener(v -> {
                    instruction.getSteps().remove(step);
                    
                    // If no steps left, remove the instruction
                    if (instruction.getSteps().isEmpty()) {
                        recipe.getInstructions().remove(instruction);
                    }
                    
                    // Notify listener
                    if (dataListener != null) {
                        dataListener.onRecipeDataChanged(recipe);
                    }
                    
                    // Update the list
                    displayInstructions();
                });
                
                instructionsList.addView(stepView);
            }
            
            // Add a divider except for the last instruction
            if (recipe.getInstructions().indexOf(instruction) < recipe.getInstructions().size() - 1) {
                View divider = new View(context);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 8));
                divider.setBackgroundColor(context.getResources().getColor(R.color.colorLightBackground));
                instructionsList.addView(divider);
            }
        }
    }
    
    private void setupAdditionalInfoPage(RecipeViewHolder holder, int position) {
        // Additional info page setup code
        View view = holder.itemView;
        
        // Get references to views
        etPrepTime = view.findViewById(R.id.etPrepTime);
        etCookTime = view.findViewById(R.id.etCookTime);
        etServings = view.findViewById(R.id.etServings);
        etVideoUrl = view.findViewById(R.id.etVideoUrl);
        spinnerDifficulty = view.findViewById(R.id.spinnerDifficulty);
        spinnerCuisineType = view.findViewById(R.id.spinnerCuisineType);
        
        // Set up spinners
        if (spinnerDifficulty != null) {
            String[] difficulties = new String[]{"Easy", "Medium", "Hard"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, 
                android.R.layout.simple_spinner_item, difficulties);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDifficulty.setAdapter(adapter);
        }
        
        if (spinnerCuisineType != null) {
            String[] cuisines = new String[]{"American", "Asian", "European", "Italian", "Mexican", "Middle Eastern", "Indian", "Other"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, 
                android.R.layout.simple_spinner_item, cuisines);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCuisineType.setAdapter(adapter);
        }
        
        // Restore data if available
        if (recipe != null) {
            if (etPrepTime != null) {
                etPrepTime.setText(recipe.getPrepTime() > 0 ? String.valueOf(recipe.getPrepTime()) : "");
            }
            if (etCookTime != null) {
                etCookTime.setText(recipe.getCookTime() > 0 ? String.valueOf(recipe.getCookTime()) : "");
            }
            if (etServings != null) {
                etServings.setText(recipe.getServings() > 0 ? String.valueOf(recipe.getServings()) : "");
            }
            if (etVideoUrl != null && recipe.getVideoUrl() != null) {
                etVideoUrl.setText(recipe.getVideoUrl());
            }
        }
        
        // Set up continue button
        Button continueButton = findContinueButton(view);
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                saveAdditionalInfoData();
                ((CreateRecipeActivity)context).goToNextStep();
            });
        }
    }
    
    private void saveAdditionalInfoData() {
        if (recipe != null) {
            if (etPrepTime != null && !etPrepTime.getText().toString().isEmpty()) {
                recipe.setPrepTime(Integer.parseInt(etPrepTime.getText().toString()));
            }
            
            if (etCookTime != null && !etCookTime.getText().toString().isEmpty()) {
                recipe.setCookTime(Integer.parseInt(etCookTime.getText().toString()));
            }
            
            if (etServings != null && !etServings.getText().toString().isEmpty()) {
                recipe.setServings(Integer.parseInt(etServings.getText().toString()));
            }
            
            if (etVideoUrl != null && !etVideoUrl.getText().toString().isEmpty()) {
                recipe.setVideoUrl(etVideoUrl.getText().toString().trim());
            }
            
            if (spinnerDifficulty != null) {
                recipe.setDifficulty(spinnerDifficulty.getSelectedItem().toString());
            }
            
            if (spinnerCuisineType != null) {
                recipe.setCuisineType(spinnerCuisineType.getSelectedItem().toString());
            }
            
            // Notify listener
            if (dataListener != null) {
                dataListener.onRecipeDataChanged(recipe);
            }
        }
    }
    
    private void setupReviewPage(RecipeViewHolder holder, int position) {
        // Review page setup code
        View view = holder.itemView;
        
        // Set up the review page with recipe data
        TextView tvRecipeName = view.findViewById(R.id.tvRecipeName);
        TextView tvIngredientCount = view.findViewById(R.id.tvIngredientCount);
        TextView tvStepCount = view.findViewById(R.id.tvStepCount);
        TextView tvTotalTime = view.findViewById(R.id.tvTotalTime);
        TextView tvEstimatedTime = view.findViewById(R.id.tvEstimatedTime);
        ImageView ivRecipePreview = view.findViewById(R.id.ivRecipePreview);
        ImageView recipeImage = view.findViewById(R.id.recipeImage);
        LinearLayout reviewIngredientsContainer = view.findViewById(R.id.reviewIngredientsContainer);
        LinearLayout reviewInstructionsContainer = view.findViewById(R.id.reviewInstructionsContainer);
        
        if (recipe != null) {
            if (tvRecipeName != null) {
                tvRecipeName.setText(recipe.getName());
            }
            
            if (tvIngredientCount != null) {
                tvIngredientCount.setText(String.valueOf(recipe.getIngredients().size()));
            }
            
            int stepCount = 0;
            for (Recipe.Instruction instruction : recipe.getInstructions()) {
                stepCount += instruction.getSteps().size();
            }
            
            if (tvStepCount != null) {
                tvStepCount.setText(String.valueOf(stepCount));
            }
            
            int totalTime = recipe.getPrepTime() + recipe.getCookTime();
            if (tvTotalTime != null) {
                tvTotalTime.setText(totalTime + " min");
            }
            
            if (tvEstimatedTime != null) {
                tvEstimatedTime.setText(totalTime + " Minutes");
            }
            
            // Show recipe image if available
            Uri latestImageUri = null;
            if (!recipe.getImageUrls().isEmpty()) {
                String imageUrl = recipe.getImageUrls().get(0);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    if (ivRecipePreview != null) {
                        Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_food)
                            .into(ivRecipePreview);
                    }
                    
                    if (recipeImage != null) {
                        Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_food)
                            .into(recipeImage);
                    }
                }
            } else if (!((CreateRecipeActivity)context).getSelectedImageUris().isEmpty()) {
                // If we have selected images but not yet uploaded to Cloudinary
                latestImageUri = ((CreateRecipeActivity)context).getSelectedImageUris().get(0);
                if (ivRecipePreview != null && latestImageUri != null) {
                    Glide.with(context)
                        .load(latestImageUri)
                        .placeholder(R.drawable.placeholder_food)
                        .into(ivRecipePreview);
                }
                
                if (recipeImage != null && latestImageUri != null) {
                    Glide.with(context)
                        .load(latestImageUri)
                        .placeholder(R.drawable.placeholder_food)
                        .into(recipeImage);
                }
            }
            
            // Populate ingredients
            if (reviewIngredientsContainer != null) {
                reviewIngredientsContainer.removeAllViews();
                
                for (Recipe.Ingredient ingredient : recipe.getIngredients()) {
                    // Create ingredient row
                    LinearLayout ingredientRow = new LinearLayout(context);
                    ingredientRow.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    ingredientRow.setOrientation(LinearLayout.HORIZONTAL);
                    ingredientRow.setPadding(0, 24, 0, 24); // 8dp vertical padding
                    
                    // Ingredient name
                    TextView nameView = new TextView(context);
                    LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    nameView.setLayoutParams(nameParams);
                    nameView.setText(ingredient.getName());
                    nameView.setTextColor(context.getResources().getColor(android.R.color.black));
                    
                    // Ingredient amount and unit
                    TextView amountView = new TextView(context);
                    amountView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    amountView.setText(ingredient.getAmount() + " " + ingredient.getUnit());
                    amountView.setTextColor(context.getResources().getColor(android.R.color.black));
                    
                    // Add views to row
                    ingredientRow.addView(nameView);
                    ingredientRow.addView(amountView);
                    
                    // Add row to container
                    reviewIngredientsContainer.addView(ingredientRow);
                    
                    // Add divider if not the last ingredient
                    if (recipe.getIngredients().indexOf(ingredient) < recipe.getIngredients().size() - 1) {
                        View divider = new View(context);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1));
                        divider.setBackgroundColor(context.getResources().getColor(R.color.dividerColor));
                        reviewIngredientsContainer.addView(divider);
                    }
                }
            }
            
            // Populate instructions
            if (reviewInstructionsContainer != null) {
                reviewInstructionsContainer.removeAllViews();
                
                int instructionNumber = 1;
                for (Recipe.Instruction instruction : recipe.getInstructions()) {
                    // Instruction title
                    TextView titleView = new TextView(context);
                    titleView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    titleView.setText(instructionNumber + ". " + instruction.getTitle());
                    titleView.setTextColor(context.getResources().getColor(android.R.color.black));
                    titleView.setTypeface(null, Typeface.BOLD);
                    titleView.setTextSize(16);
                    titleView.setPadding(0, 0, 0, 16); // 8dp bottom padding
                    reviewInstructionsContainer.addView(titleView);
                    
                    // Instruction steps
                    for (String step : instruction.getSteps()) {
                        TextView stepView = new TextView(context);
                        stepView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        stepView.setText("• " + step);
                        stepView.setTextColor(context.getResources().getColor(android.R.color.black));
                        stepView.setPadding(0, 0, 0, 8); // 4dp bottom padding
                        reviewInstructionsContainer.addView(stepView);
                    }
                    
                    // Add spacing between instructions
                    if (recipe.getInstructions().indexOf(instruction) < recipe.getInstructions().size() - 1) {
                        View spacer = new View(context);
                        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 16));
                        reviewInstructionsContainer.addView(spacer);
                    }
                    
                    instructionNumber++;
                }
            }
        }
        
        // Set up submit button
        Button submitButton = view.findViewById(R.id.btnFinish);
        if (submitButton == null) {
            // Try to find a button that might be used for submission
            submitButton = findSubmitButton(view);
        }
        
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> {
                // Submit recipe and finish
                ((CreateRecipeActivity)context).finishRecipeCreation();
            });
        }
    }
    
    /**
     * Update the recipe data from the current step
     */
    public void saveCurrentStepData(int position) {
        switch (position) {
            case 0:
                saveBasicInfoData();
                break;
            case 1:
                saveIngredientsData();
                break;
            case 2:
                saveInstructionsData();
                break;
            case 3:
                saveAdditionalInfoData();
                break;
        }
    }
    
    private void saveIngredientsData() {
        // Data is saved when ingredients are added/removed via buttons
        // Just notify the listener about any possible changes
        if (dataListener != null) {
            dataListener.onRecipeDataChanged(recipe);
        }
    }
    
    private void saveInstructionsData() {
        // Data is saved when instructions are added/removed via buttons
        // Just notify the listener about any possible changes
        if (dataListener != null) {
            dataListener.onRecipeDataChanged(recipe);
        }
    }
    
    /**
     * Update image preview when an image is picked
     */
    public void updateImagePreview(Uri imageUri) {
        if (recipeImagePreview != null && uploadImageArea != null) {
            // Show the image preview
            recipeImagePreview.setVisibility(View.VISIBLE);
            Glide.with(context)
                .load(imageUri)
                .centerCrop()
                .into(recipeImagePreview);
            
            // Update the upload area text
            View uploadText = uploadImageArea.findViewById(R.id.uploadTextContainer);
            if (uploadText != null) {
                uploadText.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Helper method to find a continue button regardless of its specific ID
     */
    private Button findContinueButton(View view) {
        // Try specific IDs based on step
        int currentItem = viewPager.getCurrentItem();
        int[] buttonIds = {
            R.id.btnContinueStep1,
            R.id.btnContinueStep2,
            R.id.btnContinueStep3,
            R.id.btnContinueStep4
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
        return findButtonByText(view, "Submit", "Finish", "Complete", "Create Recipe");
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
                String buttonText = button.getText().toString().toLowerCase();
                
                for (String option : textOptions) {
                    if (buttonText.contains(option.toLowerCase())) {
                        return button;
                    }
                }
            } else if (child instanceof ViewGroup) {
                Button result = findButtonByText((ViewGroup) child, textOptions);
                if (result != null) {
                    return result;
                }
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
    
    /**
     * Interface for communication with the activity
     */
    public interface RecipeDataListener {
        void onRecipeDataChanged(Recipe recipe);
        void onImagePickRequested();
    }
} 