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
    private static final float SNAP_THRESHOLD = 10f;
    private static List<ImageView> addedImages = new ArrayList<>();; //Used for checking snapping later
    private static ArrayList<Integer> positions;

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
     */
    private static void handleSnapping(View draggedView, DragEvent event){
        // Track all 4 sides (X/Y locations) whilst moving the image
        float leftBorder = event.getX() - draggedView.getWidth() / 2f;
        float topBorder = event.getY() - draggedView.getHeight() / 2f;
        float rightBorder = leftBorder + draggedView.getWidth();
        float bottomBorder = topBorder + draggedView.getHeight();

        //Check if image is close to canvas size bounds or dividers
        checkCanvasSnapping(draggedView, rightBorder, leftBorder, topBorder, bottomBorder);

        // And check the position compared to all other added images:
        checkImageToImageSnapping(draggedView, rightBorder, leftBorder, topBorder, bottomBorder);

        draggedView.setTranslationX(leftBorder);
        draggedView.setTranslationY(topBorder);
    }

    /**
     * Check all other images on the canvas, and snap if below threshold
     */
    private static void checkImageToImageSnapping(View draggedView, float rightBorder, float leftBorder, float topBorder, float bottomBorder){
        for (ImageView other : addedImages) {
            if (other == draggedView) continue; // Skip our own image, for obvious reasons...

            float otherLeft = other.getX();
            float otherTop = other.getY();
            float otherRight = otherLeft + other.getWidth();
            float otherBottom = otherTop + other.getHeight();

            // X-axis alignment
            if (Math.abs(rightBorder - otherLeft) <= SNAP_THRESHOLD) {
                leftBorder = otherLeft - draggedView.getWidth(); // Align right edge to left edge
                animateMagnetX(draggedView, leftBorder);
            }
            else if (Math.abs(leftBorder - otherRight) <= SNAP_THRESHOLD) {
                leftBorder = otherRight; // Align left edge to right edge
                animateMagnetX(draggedView, leftBorder);
            }
            // Y-axis alignment
            if (Math.abs(topBorder - otherBottom) <= SNAP_THRESHOLD){
                topBorder = otherBottom;
                animateMagnetY(draggedView, topBorder);
            }
            else if (Math.abs(bottomBorder - otherTop) <= SNAP_THRESHOLD){
                topBorder = otherTop - draggedView.getHeight();
                animateMagnetY(draggedView, topBorder);
            }
            break;
        }
    }

    /**
     * Check if:
     * a) image left or right side is close to horizontal dividers or parent left/right borders
     * b) close to the parent top/bottom borders
     * */
    private static void checkCanvasSnapping(View draggedView, float rightBorder, float leftBorder, float topBorder, float bottomBorder){
        for (int boundary: positions) {
            if (Math.abs(rightBorder - boundary) <= SNAP_THRESHOLD) {
                animateMagnetX(draggedView, (boundary - draggedView.getWidth()));
                return;
            } else if (Math.abs(leftBorder - boundary) <= SNAP_THRESHOLD) {
                animateMagnetX(draggedView, boundary);
                return;
            }
        }
        //Top of container snapping
        if (Math.abs(topBorder) <= SNAP_THRESHOLD) {
            animateMagnetY(draggedView, 0f);
            return;
        }
        // Bottom
        float containerHeight = ((View) draggedView.getParent()).getHeight();
        if (Math.abs(bottomBorder - containerHeight) <= SNAP_THRESHOLD) {
            topBorder = containerHeight - draggedView.getHeight();
            animateMagnetY(draggedView, topBorder);
            return;
        }
    }

    public static void setCanvasSplitPositions(ArrayList<Integer> pos){
        positions = pos;
        positions.add(positions.get(3) + (positions.get(1))); //add right-side end of screen boundary
    }

    /**
     * Animation to more "gently" snap X-coordinates.
     */
    private static void animateMagnetX(View draggedView, float newX){
        draggedView.setX(newX);
        draggedView.animate()
                .translationX(newX)
                .setDuration(20)
                .start();
    }

    /**
     * Animation to more "gently" snap Y-coordinates together.
    */
    private static void animateMagnetY(View draggedView, float newY){
        draggedView.setY(newY);
        draggedView.animate()
                .translationY(newY)
                .setDuration(20)
                .start();
    }
}
