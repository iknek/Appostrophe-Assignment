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
    private boolean isTabOpen = false;

    public ClickController(Activity activity) {
        this.activity = activity;
    }

    /**
     * Handler for (opening panel) picking background color
     */
    public void backgroundColorButtonHandler(View view){
        LinearLayout panel = activity.findViewById(R.id.background_panel);
        isTabOpen = true;
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
     * Close color picker if open, and clear image selection : when clicking on canvas
     */
    public void imageViewClickHandler(View view){
        if (isTabOpen) {
            LinearLayout panel = activity.findViewById(R.id.background_panel);
            panel.animate()
                    .translationY(panel.getHeight())
                    .setDuration(200)
                    .withEndAction(() -> {
                        panel.setVisibility(View.GONE);
                        isTabOpen = false;
                    })
                    .start();
        }
        ImageLogic.clearSelection();
    }

    /**
     * Logic for button to add picture to our view.
     * TODO: Add dynamic web fetch api + categories, move out of here!!
     */
    public void onClickAddPic(View view) {
        FrameLayout canvas = activity.findViewById(R.id.hscroll_container);

        ImageView imageView = new ImageView(activity);

        setImageDimensions(imageView);

        //TODO: Make where image is placed dynamic based on where user has scrolled
        imageView.setX(200);
        imageView.setY(200);

        //Init image item logic for when it's added.
        ImageLogic.initImage(imageView, canvas);
        canvas.addView(imageView);

        CharSequence text = "Added Image!"; //Maybe something for SCRL to implement? If images are loaded in the same place, they get stacked on top of each other and you might forget ;)
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(activity, text, duration);
        toast.show();
    }

    private void setImageDimensions(ImageView imageView){
        Drawable img = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.image2, null); // Loaded images this way so we can load with 1x size scaling
        imageView.setImageDrawable(img);
        int imageWidth = img.getIntrinsicWidth();
        int imageHeight = img.getIntrinsicHeight();
        imageView.setLayoutParams(new FrameLayout.LayoutParams(imageWidth, imageHeight));
    }

    /**
     * Delete item
     */
    public void onDeletePic(View view){
        ImageLogic.deleteImage();
    }
}
