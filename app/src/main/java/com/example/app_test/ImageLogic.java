package com.example.app_test;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class ImageLogic {
    private static ImageView selectedImage = null;
    private static List<ImageView> addedImages = new ArrayList<>(); //Used for checking snapping later

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
            if (selectedImage != null) {
                selectedImage.setColorFilter(null);
            }
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
                case DragEvent.ACTION_DRAG_LOCATION: {
                    SnappingHandler.handleSnapping(draggedView, event);
                    return true;
                }
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
