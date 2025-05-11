package com.example.app_test;

import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class SnappingHandler {
    private static final float SNAP_THRESHOLD = 10f;
    private static List<ImageView> addedImages = new ArrayList<>(); //Used for checking snapping later
    private static ArrayList<Integer> positions;
    private static SnapLineDraw snapLineDraw;

    public static void setSnapLineDraw(SnapLineDraw view) {
        snapLineDraw = view;
    }

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
                if(snapLineDraw != null) snapLineDraw.showLine(boundary, 0, true);
                return;
            } else if (Math.abs(leftBorder - boundary) <= SNAP_THRESHOLD) {
                animateMagnetX(draggedView, boundary);
                if(snapLineDraw != null) snapLineDraw.showLine(boundary, 0, true);
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
    private static void checkImageToImageSnapping(View draggedView, float rightBorder, float leftBorder, float topBorder,   float bottomBorder) {

        float bestXDistance  = SNAP_THRESHOLD;
        float bestYDistance  = SNAP_THRESHOLD;
        float bestXTarget    = leftBorder;  // where to set X
        float bestYTarget    = topBorder;   // where to set Y
        float bestXBoundary  = 0;           // where to draw vertical line
        float bestYBoundary  = 0;           // where to draw horizontal line
        boolean snapX = false, snapY = false;

        for (ImageView other : addedImages) {
            if (other == draggedView) continue;

            float oL = other.getX();
            float oT = other.getY();
            float oR = oL + other.getWidth();
            float oB = oT + other.getHeight();

            // → X‐axis snaps
            // a) dragged’s right to other’s left
            float dist = Math.abs(rightBorder - oL);
            if (dist < bestXDistance) {
                bestXDistance   = dist;
                bestXTarget     = oL - draggedView.getWidth();
                bestXBoundary   = oL;
                snapX           = true;
            }
            // b) dragged’s left to other’s right
            dist = Math.abs(leftBorder - oR);
            if (dist < bestXDistance) {
                bestXDistance   = dist;
                bestXTarget     = oR;
                bestXBoundary   = oR;
                snapX           = true;
            }

            // → Y‐axis snaps
            // a) dragged’s top to other’s bottom
            dist = Math.abs(topBorder - oB);
            if (dist < bestYDistance) {
                bestYDistance   = dist;
                bestYTarget     = oB;
                bestYBoundary   = oB;
                snapY           = true;
            }
            // b) dragged’s bottom to other’s top
            dist = Math.abs(bottomBorder - oT);
            if (dist < bestYDistance) {
                bestYDistance   = dist;
                bestYTarget     = oT - draggedView.getHeight();
                bestYBoundary   = oT;
                snapY           = true;
            }
        }

        // Apply snapping
        if (snapX) {
            animateMagnetX(draggedView, bestXTarget);
            snapLineDraw.showLine(bestXBoundary, 0, true);
        }
        else if (snapY) {
            animateMagnetY(draggedView, bestYTarget);
            snapLineDraw.showLine(0, bestYBoundary, false);
        }
        else {
            snapLineDraw.hideLine();
        }
    }

    public static void hideSnapLine(){
        if (snapLineDraw != null){
            snapLineDraw.hideLine();
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
