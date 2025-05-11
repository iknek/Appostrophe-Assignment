package com.example.app_test;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.core.content.res.ResourcesCompat;

/**
 * Button controller class.
 */
public class ClickController{
    private final Activity activity;
    private boolean isColorTabOpen = false;
    private boolean isImageSelectTabOpen = false;
    public ClickController(Activity activity) {
        this.activity = activity;
    }

    /**
     * Handler for (opening panel) picking background color
     */
    public void backgroundColorButtonHandler(View view){
        LinearLayout panel = activity.findViewById(R.id.background_panel);
        isColorTabOpen = true;
        if (panel.getVisibility() == View.GONE) {
            panel.setVisibility(View.VISIBLE);
            panel.setTranslationY(panel.getHeight());
            panel.post(() -> {
                panel.setTranslationY(panel.getHeight());
                panel.animate().translationY(0).setDuration(200).start();
            });
        } else {
            panel.animate()
                    .translationY(panel.getHeight())
                    .setDuration(200)
                    .withEndAction(() -> panel.setVisibility(View.GONE))
                    .start();
        }
    }

    /**
     * Set background color onClick
     */
    public void onColorClick(View view) {
        String colorHex = (String) view.getTag();
        int color = Color.parseColor(colorHex);
        activity.findViewById(R.id.image_view).setBackgroundColor(color);
    }

    /**
     * Close the open panel (color or image picker)
     */
    public void imageViewClickHandler(View view){
        if (isColorTabOpen) {
            LinearLayout panel = activity.findViewById(R.id.background_panel);
            clearPanel(panel);
        }
        if(isImageSelectTabOpen){
            LinearLayout panel = activity.findViewById(R.id.image_selector);
            clearPanel(panel);
        }
    }

    /**
     * Helper method, reduced duplicate code in imageViewClickHandler
     */
    private void clearPanel(LinearLayout panel){
        panel.animate()
                .translationY(panel.getHeight())
                .setDuration(200)
                .withEndAction(() -> {
                    panel.setVisibility(View.GONE);
                    isColorTabOpen = false;
                })
                .start();
        ImageLogic.clearSelection();
    }

    /**
     * Open image select panel
     */
    public void imageSelectClickHandler(View view){
        LinearLayout panel = activity.findViewById(R.id.image_selector);
        isImageSelectTabOpen = true;
        if (panel.getVisibility() == View.GONE) {
            panel.setVisibility(View.VISIBLE);
            panel.setTranslationY(panel.getHeight());
            panel.post(() -> {
                panel.setTranslationY(panel.getHeight());
                panel.animate().translationY(0).setDuration(200).start();
            });
        } else {
            panel.animate()
                .translationY(panel.getHeight())
                .setDuration(200)
                .withEndAction(() -> panel.setVisibility(View.GONE))
                .start();
        }
    }

    /**
     * Delegates click action for creating image to ImageLogic
     */
    public void onClickAddPic(View view) {
        ImageLogic.createImage(activity,view);
    }


    /**
     * Delete item
     */
    public void onDeletePic(View view){
        ImageLogic.deleteImage();
    }
}
