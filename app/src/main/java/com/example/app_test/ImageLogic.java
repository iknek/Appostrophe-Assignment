package com.example.app_test;

import static java.lang.Thread.sleep;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class ImageLogic {

    private static ImageView selectedImage = null;
    private static final float SNAP_THRESHOLD = 10f;
    private static List<ImageView> addedImages = new ArrayList<>();; //Used for checking snapping later

    /**
     * Init method for when image is added.
    */
    public static void initImage(ImageView imageView, FrameLayout canvas) {
        setSelectionLogic(imageView);
        setDragLogic(imageView);
        // Set drop listener only once
        if (canvas.getTag() == null) {
            setDropListener(canvas);
            canvas.setTag("listener_set");
        }
        addedImages.add(imageView);
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
            if (selectedImage != null) {
                selectedImage.setColorFilter(null);
            }
            if (selectedImage == v) {
                selectedImage.setColorFilter(null);
                selectedImage = null;
            } else {
                selectedImage = (ImageView) v;
                selectedImage.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
                selectedImage.setImageLevel(1); //TODO: Set selected image above others it "intersects"
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
            ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
            ClipData dragData = new ClipData(
                    (CharSequence) v.getTag(),
                    new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                    item);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(dragData, shadowBuilder, v, 0);
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
                case DragEvent.ACTION_DRAG_LOCATION: {
                    handleSnapping(draggedView, event);
                    return true;
                }
                case DragEvent.ACTION_DROP:
                    float x = event.getX() - draggedView.getWidth() / 2f;
                    float y = event.getY() - draggedView.getHeight() / 2f;
                    draggedView.setTranslationX(x);
                    draggedView.setTranslationY(y);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
            }
            return false;
        });
    }

    /**
     * Method for handling snapping
     * @param draggedView = Image we are dragging
     * @param event = ACTION_DRAG_LOCATION used for getting state
     * TODO: Image goes woooo and flies off if it escapes screen bounds...
     */
    private static void handleSnapping(View draggedView, DragEvent event){
        // Track X and Y locations whilst moving the image
        float newX = event.getX() - draggedView.getWidth() / 2f;
        float newY = event.getY() - draggedView.getHeight() / 2f;

        // And check the position compared to all other added images:
        for (ImageView other : addedImages) {
            if (other == draggedView) continue; // Skip our own image, for obvious reasons...

            float otherLeft = other.getX();
            float otherTop = other.getY();
            float otherRight = otherLeft + other.getWidth();
            float otherBottom = otherTop + other.getHeight();

            float dragLeft = newX;
            float dragTop = newY;
            float dragRight = newX + draggedView.getWidth();
            float dragBottom = newY + draggedView.getHeight();

            // X-axis alignment
            if (Math.abs(dragRight - otherLeft) <= SNAP_THRESHOLD) {
                newX = otherLeft - draggedView.getWidth(); // Align right edge to left edge
                animateMagnetX(draggedView, newX);
            }
            else if (Math.abs(dragLeft - otherRight) <= SNAP_THRESHOLD) {
                newX = otherRight; // Align left edge to right edge
                animateMagnetX(draggedView, newX);
            }
            // Y-axis alignment
            if (Math.abs(dragTop - otherBottom) <= SNAP_THRESHOLD){
                newY = otherBottom;
                animateMagnetY(draggedView, newY);
            }
            else if (Math.abs(dragBottom - otherTop) <= SNAP_THRESHOLD){
                newY = otherTop;
                animateMagnetY(draggedView, newY);
            }
            break;
        }
        draggedView.setTranslationX(newX);
        draggedView.setTranslationY(newY);
    }

    /**
     * Animate to more "gently" snap images together.
     */
    private static void animateMagnetX(View draggedView, float newX){
        draggedView.setX(newX);
        draggedView.animate()
                .translationX(newX)
                .setDuration(20)
                .start();
    }

    /**
     * Animate to more "gently" snap images together.
    */
    private static void animateMagnetY(View draggedView, float newY){
        draggedView.setY(newY);
        draggedView.animate()
                .translationY(newY)
                .setDuration(20)
                .start();
    }
}
