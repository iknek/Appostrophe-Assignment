package com.example.app_test;

import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class SnappingHandler {
    private static final float SNAP_THRESHOLD = 10f;
    private static List<ImageView> addedImages = new ArrayList<>(); //Used for checking snapping later
    private static ArrayList<Integer> positions;
    /**
     * Method for handling snapping
     * @param draggedView = Image we are dragging
     * @param event = ACTION_DRAG_LOCATION used for getting state
     */
    static void handleSnapping(View draggedView, DragEvent event){
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
        }
    }

    /**
     * Loop through all other images on the canvas and:
     * find each image within the snap threshold:
     * check if its closer than any other checked image (to the dragged image)
     * snap to closest image (or don't if there aren't any)
     */
    private static void checkImageToImageSnapping(View draggedView, float rightBorder, float leftBorder, float topBorder, float bottomBorder){
        float bestXDistance = SNAP_THRESHOLD;
        float bestXTarget = leftBorder;  // default no-snap
        boolean snapX = false;

        float bestYDistance = SNAP_THRESHOLD;
        float bestYTarget = topBorder;   // default no-snap
        boolean snapY = false;

        for (ImageView other : addedImages) {
            if (other == draggedView) continue;

            float otherLeft = other.getX();
            float otherTop = other.getY();
            float otherRight = otherLeft + other.getWidth();
            float otherBottom = otherTop + other.getHeight();

            // X-axis alignment
            // 1) dragged's right edge to other's left edge
            float dist = Math.abs(rightBorder - otherLeft);
            if (dist < bestXDistance) {
                bestXDistance = dist;
                bestXTarget   = otherLeft - draggedView.getWidth();
                snapX         = true;
            }
            // 2) dragged's left edge to other's right edge
            dist = Math.abs(leftBorder - otherRight);
            if (dist < bestXDistance) {
                bestXDistance = dist;
                bestXTarget   = otherRight;
                snapX         = true;
            }

            // Y-axis alignment
            // 1) dragged's top edge to other's bottom edge
            dist = Math.abs(topBorder - otherBottom);
            if (dist < bestYDistance) {
                bestYDistance = dist;
                bestYTarget   = otherBottom;
                snapY         = true;
            }
            // 2) dragged's bottom edge to other's top edge
            dist = Math.abs(bottomBorder - otherRight);
            if (dist < bestYDistance) {
                bestYDistance = dist;
                bestYTarget   = otherRight - draggedView.getHeight();
                snapY         = true;
            }
        }
        // Apply best snap if available
        if (snapX) {
            animateMagnetX(draggedView, bestXTarget);
        }
        if (snapY) {
            animateMagnetY(draggedView, bestYTarget);
        }
    }

    public static void setCanvasSplitPositions(ArrayList<Integer> pos){
        positions = pos;
        positions.add(positions.get(3) + (positions.get(1))); //add right side end of screen boundary
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

    protected static void setAddedImages(List<ImageView> images){
        addedImages = images;
    }
}
