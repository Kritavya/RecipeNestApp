package com.kritavya.recipenest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.PagerAdapter;

import com.kritavya.recipenest.R;
import com.kritavya.recipenest.models.OnboardingItem;

import java.util.List;

public class OnboardingAdapter extends PagerAdapter {

    private final Context context;
    private final List<OnboardingItem> onboardingItems;
    private ViewPager viewPager;

    public OnboardingAdapter(Context context, List<OnboardingItem> onboardingItems, ViewPager viewPager) {
        this.context = context;
        this.onboardingItems = onboardingItems;
        this.viewPager = viewPager;
    }

    @Override
    public int getCount() {
        return onboardingItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout;
        
        // Different layouts for different onboarding screens
        OnboardingItem item = onboardingItems.get(position);
        
        switch (position) {
            case 0: // First screen - Browse thousands of recipes
                layout = (ViewGroup) inflater.inflate(R.layout.slide_browse_recipes_content, container, false);
                ensureLeftAlignment(layout);
                break;
                
            case 1: // Second screen - Personalized feed
                layout = (ViewGroup) inflater.inflate(R.layout.slide_personalized_feed_content, container, false);
                ensureLeftAlignment(layout);
                break;
                
            case 2: // Third screen - Save your favorite recipe
                layout = (ViewGroup) inflater.inflate(R.layout.slide_onboarding4_content, container, false);
                ensureLeftAlignment(layout);
                break;
                
            case 3: // Fourth screen - Share with friends
                layout = (ViewGroup) inflater.inflate(R.layout.slide_share_friends_content, container, false);
                ensureLeftAlignment(layout);
                break;
                
            default:
                layout = (ViewGroup) inflater.inflate(R.layout.slide_browse_recipes_content, container, false);
                ensureLeftAlignment(layout);
                break;
        }
        
        // Hide navigation buttons and dots that might be in the layouts
        Button btnNext = layout.findViewById(R.id.btn_next);
        Button btnSkip = layout.findViewById(R.id.btn_skip);
        Button btnContinue = layout.findViewById(R.id.btn_continue);
        LinearLayout dotsRow = layout.findViewById(R.id.dots_row);
        
        if (btnNext != null) btnNext.setVisibility(View.GONE);
        if (btnSkip != null) btnSkip.setVisibility(View.GONE);
        if (btnContinue != null) btnContinue.setVisibility(View.GONE);
        if (dotsRow != null) dotsRow.setVisibility(View.GONE);

        container.addView(layout);
        return layout;
    }
    
    // Helper method to ensure left alignment of titles and descriptions
    private void ensureLeftAlignment(ViewGroup layout) {
        TextView title = layout.findViewById(R.id.tv_title);
        TextView description = layout.findViewById(R.id.tv_description);
        TextView whatsInside = layout.findViewById(R.id.tv_whats_inside);
        TextView subheading = layout.findViewById(R.id.tv_subheading);
        TextView step = layout.findViewById(R.id.tv_step);
        
        if (title != null) {
            title.setGravity(android.view.Gravity.START);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) title.getLayoutParams();
            params.gravity = android.view.Gravity.START;
            title.setLayoutParams(params);
        }
        
        if (description != null) {
            description.setGravity(android.view.Gravity.START);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) description.getLayoutParams();
            params.gravity = android.view.Gravity.START;
            description.setLayoutParams(params);
        }
        
        if (whatsInside != null) {
            whatsInside.setGravity(android.view.Gravity.START);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) whatsInside.getLayoutParams();
            params.gravity = android.view.Gravity.START;
            whatsInside.setLayoutParams(params);
        }
        
        if (subheading != null) {
            subheading.setGravity(android.view.Gravity.START);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) subheading.getLayoutParams();
            params.gravity = android.view.Gravity.START;
            subheading.setLayoutParams(params);
        }
        
        if (step != null) {
            step.setGravity(android.view.Gravity.START);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) step.getLayoutParams();
            params.gravity = android.view.Gravity.START;
            step.setLayoutParams(params);
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
} 