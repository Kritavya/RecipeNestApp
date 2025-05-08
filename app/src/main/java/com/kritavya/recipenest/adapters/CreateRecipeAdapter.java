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
import android.widget.Toast;

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
                // Validate required fields
                if (validateBasicInfo()) {
                saveBasicInfoData();
                ((CreateRecipeActivity)context).goToNextStep();
                }
            });
        }
    }
    
    private boolean validateBasicInfo() {
        if (etRecipeName == null || etRecipeName.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Please enter a recipe name", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (dataListener != null && dataListener.getSelectedImageUris().isEmpty()) {
            Toast.makeText(context, "Please select an image for your recipe", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
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
                // Validate that at least one ingredient is added
                if (validateIngredients()) {
                ((CreateRecipeActivity)context).goToNextStep();
                }
            });
        }
    }
    
    private boolean validateIngredients() {
        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            Toast.makeText(context, "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
        
        // Display the recipe image if available
        ImageView recipeImagePreview = view.findViewById(R.id.recipeImagePreview);
        if (recipeImagePreview != null) {
            if (recipe != null && !recipe.getImageUrls().isEmpty()) {
                recipeImagePreview.setVisibility(View.VISIBLE);
                Glide.with(context)
                    .load(recipe.getImageUrls().get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .into(recipeImagePreview);
            } else if (dataListener != null && !dataListener.getSelectedImageUris().isEmpty()) {
                recipeImagePreview.setVisibility(View.VISIBLE);
                Glide.with(context)
                    .load(dataListener.getSelectedImageUris().get(0))
                    .into(recipeImagePreview);
            }
        }
        
        // Set up add step button
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
                // Validate that at least one instruction is added
                if (validateInstructions()) {
                ((CreateRecipeActivity)context).goToNextStep();
                }
            });
        }
    }
    
    private boolean validateInstructions() {
        if (recipe == null || recipe.getInstructions() == null || recipe.getInstructions().isEmpty()) {
            Toast.makeText(context, "Please add at least one instruction", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
        
        // Create new instruction or find existing one with the same title
        Recipe.Instruction instruction = null;
        if (recipe != null) {
            // Check if we already have an instruction with this title
            for (Recipe.Instruction existingInstruction : recipe.getInstructions()) {
                if (existingInstruction.getTitle().equals(title)) {
                    instruction = existingInstruction;
                    break;
                }
            }
        }
        
        if (instruction == null) {
            // Create new instruction
            instruction = new Recipe.Instruction(title);
            if (recipe != null) {
                recipe.addInstruction(instruction);
            }
        }
        
        // Add step to instruction
        instruction.addStep(step);
        
        // Notify listener
        if (dataListener != null) {
            dataListener.onRecipeDataChanged(recipe);
        }
        
        // Clear step input field but keep the title
        etInstructionStep.setText("");
        
        // Update the display
        displayInstructions();
    }
    
    private void displayInstructions() {
        if (instructionsList == null || recipe == null) {
            return;
        }
        
        // Clear the list
        instructionsList.removeAllViews();
        
        // Add each instruction
        if (recipe.getInstructions() != null && !recipe.getInstructions().isEmpty()) {
            for (int i = 0; i < recipe.getInstructions().size(); i++) {
                Recipe.Instruction instruction = recipe.getInstructions().get(i);
                View instructionView = layoutInflater.inflate(R.layout.item_instruction, instructionsList, false);
                
                // Set instruction title
                TextView tvTitle = instructionView.findViewById(R.id.tvInstructionTitle);
                tvTitle.setText(instruction.getTitle());
                
                // Set instruction steps
                TextView tvSteps = instructionView.findViewById(R.id.tvInstructionSteps);
                if (instruction.getSteps() != null && !instruction.getSteps().isEmpty()) {
                    StringBuilder stepsText = new StringBuilder();
                    for (int j = 0; j < instruction.getSteps().size(); j++) {
                        stepsText.append("• ").append(instruction.getSteps().get(j));
                        if (j < instruction.getSteps().size() - 1) {
                            stepsText.append("\n");
                        }
                    }
                    tvSteps.setText(stepsText.toString());
                }
                
                // Delete button
                ImageButton btnDelete = instructionView.findViewById(R.id.btnDeleteInstruction);
                if (btnDelete != null) {
                    final int index = i;
                    btnDelete.setOnClickListener(v -> {
                        recipe.getInstructions().remove(index);
                        displayInstructions();
                    if (dataListener != null) {
                        dataListener.onRecipeDataChanged(recipe);
                    }
                    });
                }
                
                // Add to container
                instructionsList.addView(instructionView);
            }
        }
    }
    
    private void setupAdditionalInfoPage(RecipeViewHolder holder, int position) {
        View view = holder.itemView;
        
        // Get references to views
        etPrepTime = view.findViewById(R.id.etPrepTime);
        etCookTime = view.findViewById(R.id.etCookTime);
        etServings = view.findViewById(R.id.etServings);
        etVideoUrl = view.findViewById(R.id.etVideoUrl);
        spinnerDifficulty = view.findViewById(R.id.spinnerDifficulty);
        spinnerCuisineType = view.findViewById(R.id.spinnerCuisineType);
        
        // Display the recipe image if available
        ImageView recipeImagePreview = view.findViewById(R.id.recipeImagePreview);
        if (recipeImagePreview != null) {
            if (recipe != null && !recipe.getImageUrls().isEmpty()) {
                recipeImagePreview.setVisibility(View.VISIBLE);
                Glide.with(context)
                    .load(recipe.getImageUrls().get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .into(recipeImagePreview);
            } else if (dataListener != null && !dataListener.getSelectedImageUris().isEmpty()) {
                recipeImagePreview.setVisibility(View.VISIBLE);
                Glide.with(context)
                    .load(dataListener.getSelectedImageUris().get(0))
                    .into(recipeImagePreview);
            }
        }
        
        // Set up spinners
        if (spinnerDifficulty != null) {
            String[] difficulties = new String[]{"Easy", "Medium", "Hard"};
            ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(context, 
                android.R.layout.simple_spinner_item, difficulties);
            difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDifficulty.setAdapter(difficultyAdapter);
            
            // Select saved value if exists
            if (recipe != null && recipe.getDifficulty() != null) {
                for (int i = 0; i < difficulties.length; i++) {
                    if (difficulties[i].equals(recipe.getDifficulty())) {
                        spinnerDifficulty.setSelection(i);
                        break;
                    }
                }
            }
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
                if (validateAdditionalInfo()) {
                saveAdditionalInfoData();
                ((CreateRecipeActivity)context).goToNextStep();
                }
            });
        }
    }
    
    private boolean validateAdditionalInfo() {
        if (etPrepTime == null || etPrepTime.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Please enter preparation time", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (etCookTime == null || etCookTime.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Please enter cooking time", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (etServings == null || etServings.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Please enter number of servings", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Note: Video URL is optional
        
        return true;
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
        View view = holder.itemView;
        
        // Recipe image
        ImageView recipeImage = view.findViewById(R.id.recipeImage);
        if (recipe != null && !recipe.getImageUrls().isEmpty()) {
            Glide.with(context)
                .load(recipe.getImageUrls().get(0))
                .placeholder(R.drawable.placeholder_image)
                .into(recipeImage);
        } else if (dataListener != null && !dataListener.getSelectedImageUris().isEmpty()) {
            Glide.with(context)
                .load(dataListener.getSelectedImageUris().get(0))
                .into(recipeImage);
        }
        
        // Recipe Preview Image
        ImageView ivRecipePreview = view.findViewById(R.id.ivRecipePreview);
        if (ivRecipePreview != null) {
            if (recipe != null && !recipe.getImageUrls().isEmpty()) {
                Glide.with(context)
                    .load(recipe.getImageUrls().get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .into(ivRecipePreview);
            } else if (dataListener != null && !dataListener.getSelectedImageUris().isEmpty()) {
                Glide.with(context)
                    .load(dataListener.getSelectedImageUris().get(0))
                    .into(ivRecipePreview);
            }
        }
        
        // Recipe name
        TextView tvRecipeName = view.findViewById(R.id.tvRecipeName);
        if (recipe != null && recipe.getName() != null) {
            tvRecipeName.setText(recipe.getName());
        }
        
        // Recipe time
        TextView tvEstimatedTime = view.findViewById(R.id.tvEstimatedTime);
        if (recipe != null) {
            int prepTime = recipe.getPrepTime();
            int cookTime = recipe.getCookTime();
            int totalTime = prepTime + cookTime;
            tvEstimatedTime.setText(totalTime + " Minutes");
        }
        
        // Ingredient count
        TextView tvIngredientCount = view.findViewById(R.id.tvIngredientCount);
        if (recipe != null && recipe.getIngredients() != null) {
            tvIngredientCount.setText(String.valueOf(recipe.getIngredients().size()));
        }
        
        // Steps count
        TextView tvStepCount = view.findViewById(R.id.tvStepCount);
        if (recipe != null && recipe.getInstructions() != null && tvStepCount != null) {
            tvStepCount.setText(String.valueOf(recipe.getInstructions().size()));
        }
        
        // Check for total time TextView
        TextView tvTotalTime = view.findViewById(R.id.tvTotalTime);
        if (recipe != null && tvTotalTime != null) {
            int prepTime = recipe.getPrepTime();
            int cookTime = recipe.getCookTime();
            int totalTime = prepTime + cookTime;
            tvTotalTime.setText(totalTime + " min");
        }
        
        // Description
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        if (recipe != null && recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
            tvDescription.setText(recipe.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
            View descriptionSection = view.findViewById(R.id.descriptionSection);
            if (descriptionSection != null) {
                descriptionSection.setVisibility(View.VISIBLE);
            }
        } else if (tvDescription != null) {
            tvDescription.setVisibility(View.GONE);
            View descriptionSection = view.findViewById(R.id.descriptionSection);
            if (descriptionSection != null) {
                descriptionSection.setVisibility(View.GONE);
            }
        }
        
        // Ingredients
        LinearLayout ingredientsList = view.findViewById(R.id.reviewIngredientsList);
        if (ingredientsList != null) {
            ingredientsList.removeAllViews();
            if (recipe != null && recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                for (Recipe.Ingredient ingredient : recipe.getIngredients()) {
                    View ingredientView = layoutInflater.inflate(R.layout.item_review_ingredient, ingredientsList, false);
                    TextView tvIngredientName = ingredientView.findViewById(R.id.tvIngredientName);
                    TextView tvIngredientAmount = ingredientView.findViewById(R.id.tvIngredientAmount);
                    
                    tvIngredientName.setText(ingredient.getName());
                    tvIngredientAmount.setText(ingredient.getAmount() + " " + ingredient.getUnit());
                    
                    ingredientsList.addView(ingredientView);
                }
            }
        }
        
        // Instructions (How to)
        LinearLayout instructionsList = view.findViewById(R.id.reviewInstructionsList);
        TextView instructionsTitle = view.findViewById(R.id.instructionsTitle);
        
        if (instructionsList != null) {
            instructionsList.removeAllViews();
            
            if (recipe != null && recipe.getInstructions() != null && !recipe.getInstructions().isEmpty()) {
                // Make sure the instructions title is visible
                if (instructionsTitle != null) {
                    instructionsTitle.setVisibility(View.VISIBLE);
                }
                
                // Make sure the container is visible
                View instructionsSection = view.findViewById(R.id.instructionsSection);
                if (instructionsSection != null) {
                    instructionsSection.setVisibility(View.VISIBLE);
                }
                
                int stepNumber = 1;
                for (Recipe.Instruction instruction : recipe.getInstructions()) {
                    View instructionView = layoutInflater.inflate(R.layout.item_review_instruction, instructionsList, false);
                    TextView tvStepNumber = instructionView.findViewById(R.id.tvStepNumber);
                    TextView tvInstructionTitle = instructionView.findViewById(R.id.tvInstructionTitle);
                    TextView tvInstructionStep = instructionView.findViewById(R.id.tvInstructionStep);
                    
                    tvStepNumber.setText("Step " + stepNumber);
                    tvInstructionTitle.setText(instruction.getTitle());
                    
                    // Handle the steps as a list
                    if (instruction.getSteps() != null && !instruction.getSteps().isEmpty()) {
                        StringBuilder stepsText = new StringBuilder();
                        for (int i = 0; i < instruction.getSteps().size(); i++) {
                            stepsText.append(instruction.getSteps().get(i));
                            if (i < instruction.getSteps().size() - 1) {
                                stepsText.append("\n\n");
                            }
                        }
                        tvInstructionStep.setText(stepsText.toString());
                    }
                    
                    instructionsList.addView(instructionView);
                    stepNumber++;
                }
            } else {
                // Hide instructions section if empty
                if (instructionsTitle != null) {
                    instructionsTitle.setVisibility(View.GONE);
                }
                
                View instructionsSection = view.findViewById(R.id.instructionsSection);
                if (instructionsSection != null) {
                    instructionsSection.setVisibility(View.GONE);
                }
            }
        }
        
        // Submit button
        Button submitButton = findSubmitButton(view);
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> {
                if (dataListener != null) {
                ((CreateRecipeActivity)context).finishRecipeCreation();
                }
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
        List<Uri> getSelectedImageUris();
    }
} 