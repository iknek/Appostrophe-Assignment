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
     * Open image select
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
     * Logic for button to add picture to our view.
     * TODO: Add dynamic web fetch api + categories, move out of here!!
     */
    public void onClickAddPic(View view) {
        FrameLayout canvas = activity.findViewById(R.id.hscroll_container);

        ImageView imageView = new ImageView(activity);
        String imageID = (String) view.getTag();
        setImageDimensions(imageView, imageID);

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

    private void setImageDimensions(ImageView imageView, String id){
        int resId = activity.getResources().getIdentifier(id, "drawable", activity.getPackageName());
        Drawable img = ResourcesCompat.getDrawable(activity.getResources(), resId, null);
        imageView.setImageDrawable(img);

        int imageWidth = (img.getIntrinsicWidth()/5);
        int imageHeight = img.getIntrinsicHeight()/5;
        imageView.setLayoutParams(new FrameLayout.LayoutParams(imageWidth, imageHeight));
    }

    /**
     * Delete item
     */
    public void onDeletePic(View view){
        ImageLogic.deleteImage();
    }
}
