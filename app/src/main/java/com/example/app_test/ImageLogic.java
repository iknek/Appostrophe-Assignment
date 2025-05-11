package com.example.app_test;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

public class ImageLogic {
    private static ImageView selectedImage = null;
    private static List<ImageView> addedImages = new ArrayList<>(); //Used for checking snapping later

    /**
     * Logic for addding picture to our view.
     * TODO: Add dynamic web fetch api + categories, move out of here!!
     */
    protected static void createImage(Activity activity, View view){
        FrameLayout canvas = activity.findViewById(R.id.hscroll_container);

        ImageView imageView = new ImageView(activity);
        String imageID = (String) view.getTag();
        getImageDrawable(imageView, imageID, activity);

        //TODO: Make where image is placed dynamic based on where user has scrolled
        imageView.setX(200);
        imageView.setY(200);

        //Init image item logic for when it's added.
        initImage(imageView, canvas);
        canvas.addView(imageView);

        CharSequence text = "Added Image!"; //Maybe something for SCRL to implement? If images are loaded in the same place, they get stacked on top of each other and you might forget ;)
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(activity, text, duration);
        toast.show();
    }

    /**
     * Loads image using button tag (id) and sets dimensions
     */
    private static void getImageDrawable(ImageView imageView, String id, Activity activity){
        int resId = activity.getResources().getIdentifier(id, "drawable", activity.getPackageName());
        Drawable img = ResourcesCompat.getDrawable(activity.getResources(), resId, null);
        imageView.setImageDrawable(img);

        int imageWidth = (img.getIntrinsicWidth()/5);
        int imageHeight = img.getIntrinsicHeight()/5;
        imageView.setLayoutParams(new FrameLayout.LayoutParams(imageWidth, imageHeight));
    }

    /**
     * Init method for when image is added.
     */
    private static void initImage(ImageView imageView, FrameLayout canvas) {
        setSelectionLogic(imageView);
        setDragLogic(imageView);
        // Set drop listener only once
        if (canvas.getTag() == null) {
            setDropListener(canvas);
            canvas.setTag("listener_set");
        }
        addedImages.add(imageView);
        SnappingHandler.setAddedImages(addedImages);
    }

    /**
     * Clear image selection when clicking on canvas
     */
    public static void clearSelection() {
        if (selectedImage != null) {
            selectedImage.setColorFilter(null);
            selectedImage = null;
        }
    }

    /**
     * Delete selected image
     */
    public static void deleteImage() {
        if (selectedImage != null) {
            ((FrameLayout) selectedImage.getParent()).removeView(selectedImage);
            addedImages.remove(selectedImage);
            selectedImage = null;
        }
    }

    /**
     * Logic for handling highlighting of items (images) when selected/unselected.
     * @param imageView = image selected by tapping on it.
     */
    private static void setSelectionLogic(ImageView imageView) {
        imageView.setOnClickListener(v -> {
            if (selectedImage != null) selectedImage.setColorFilter(null);

            if (selectedImage == v) {
                selectedImage.setColorFilter(null);
                selectedImage = null;
            } else {
                selectedImage = (ImageView) v;
                selectedImage.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
            }
        });
    }

    /**
     * Logic for dragging images across our oversized screen
     * @param imageView = image selected by tapping on it.
     */
    private static void setDragLogic(ImageView imageView) {
        imageView.setOnLongClickListener(v -> {
            if (selectedImage != v) return false;
            final View view = v;

            View.DragShadowBuilder transparentShadow = new View.DragShadowBuilder(view) {
                @Override
                public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                    int w = view.getWidth();
                    int h = view.getHeight();
                    outShadowSize.set(w, h);
                    outShadowTouchPoint.set(w/2, h/2);
                }
                @Override
                public void onDrawShadow(Canvas canvas) {
                    // Don't draw a shadow
                }
            };

            ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
            ClipData dragData = new ClipData(
                    (CharSequence) v.getTag(),
                    new String[]{ ClipDescription.MIMETYPE_TEXT_PLAIN },
                    item
            );

            v.startDragAndDrop(dragData, transparentShadow, v, 0);
            return true;
        });

    }

    /**
     * Logic for "dropping" image by lifting finger from it.
     */
    private static void setDropListener(View dropTarget) {
        dropTarget.setOnDragListener((v, event) -> {
            View draggedView = (View) event.getLocalState();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                case DragEvent.ACTION_DRAG_LOCATION:
                    SnappingHandler.handleSnapping(draggedView, event);
                    return true;
                case DragEvent.ACTION_DROP:
                    float x = event.getX() - draggedView.getWidth() / 2f;
                    float y = event.getY() - draggedView.getHeight() / 2f;
                    draggedView.setTranslationX(x);
                    draggedView.setTranslationY(y);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    SnappingHandler.hideSnapLine();
                    return true;
            }
            return false;
        });
    }


}
