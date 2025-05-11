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
    private static ArrayList<Integer> halfBoundarySnapList = new ArrayList<>();

    public static void setSnapLineDraw(SnapLineDraw view) {
        snapLineDraw = view;
    }

    /**
     * Method for handling snapping
     * @param draggedView = Image we are dragging
     * @param event = ACTION_DRAG_LOCATION used for getting state
     */
    static void handleSnapping(View draggedView, DragEvent event){
        if (snapLineDraw != null) snapLineDraw.clearLines();
        // Track all 4 borders/sides (X/Y locations) whilst moving the image
        float left = event.getX() - draggedView.getWidth() / 2f;
        float top = event.getY() - draggedView.getHeight() / 2f;
        float right = left + draggedView.getWidth();
        float bottom = top + draggedView.getHeight();

        //Check if image is close to canvas size bounds or dividers
        checkCanvasSnapping(draggedView, right, left, top, bottom);
        checkImageToImageSnapping(draggedView, right, left, top, bottom);

        draggedView.setTranslationX(left);
        draggedView.setTranslationY(top);
    }

    /**
     * Check if:
     * a) image left or right side is close to horizontal dividers (including page horizontal ends)
     * or image at:
     * b) top or bottom of page
     * c) in the horizontal middle of a container
     * d) vertical page middle
     */
    private static void checkCanvasSnapping(View draggedView, float rightBorder, float leftBorder, float topBorder, float bottomBorder){
        for (int boundary: positions) {
            if (Math.abs(rightBorder - boundary) <= SNAP_THRESHOLD)
                animateRenderX(draggedView,boundary - draggedView.getWidth(), boundary);
            else if (Math.abs(leftBorder - boundary) <= SNAP_THRESHOLD)
                animateRenderX(draggedView,boundary, boundary);
        }

        //Top of page snapping
        if (Math.abs(topBorder) <= SNAP_THRESHOLD)
            animateRenderY(draggedView,0,0);

        // Bottom of page snapping
        float containerHeight = ((View) draggedView.getParent()).getHeight();
        if (Math.abs(bottomBorder - containerHeight) <= SNAP_THRESHOLD)
            animateRenderY(draggedView,containerHeight - draggedView.getHeight(),containerHeight);

        // Horizontal middle of container snapping
        float viewWidth = draggedView.getWidth();
        for (int boundary: halfBoundarySnapList) {
            if (Math.abs((leftBorder + viewWidth / 2f) - boundary) <= SNAP_THRESHOLD)
                animateRenderX(draggedView,(boundary-viewWidth / 2f), boundary);
        }

        // Vertical middle of page snapping
        float imageVCenter = topBorder + draggedView.getHeight() / 2f;
        float canvasVCenter = containerHeight / 2f;
        if (Math.abs(imageVCenter - canvasVCenter) <= SNAP_THRESHOLD)
            animateRenderY(draggedView,(canvasVCenter - draggedView.getHeight() / 2f), canvasVCenter);
    }

    /**
     * Loop through all other images on the canvas and:
     * find each image within the snap threshold:
     * check if its closer than any other checked image (to the dragged image)
     * snap to closest image (or don't if there aren't any)
     */
    private static void checkImageToImageSnapping(View draggedView, float rightBorder, float leftBorder, float topBorder,   float bottomBorder) {
        float bestXDistance = SNAP_THRESHOLD, bestYDistance  = SNAP_THRESHOLD;
        float bestXTarget    = leftBorder;  // where to set X
        float bestYTarget    = topBorder;   // where to set Y
        float bestXBoundary  = 0, bestYBoundary  = 0; // where to draw vertical and horizontal line
        boolean snapX = false, snapY = false;

        float draggedCenterX = leftBorder + draggedView.getWidth() / 2f;
        float draggedCenterY = topBorder + draggedView.getHeight() / 2f;

        for (ImageView other : addedImages) {
            if (other == draggedView) continue;

            float oLeft = other.getX(), oTop = other.getY();
            float oRight = oLeft + other.getWidth(), oBottom = oTop + other.getHeight();
            float oCenterX = oLeft + other.getWidth() / 2f, oCenterY = oTop + other.getHeight() / 2f;

            float[][] xCandidates = {
                    {Math.abs(rightBorder - oLeft), oLeft - draggedView.getWidth(), oLeft},
                    {Math.abs(leftBorder - oRight), oRight, oRight},
                    {Math.abs(draggedCenterX - oLeft), oLeft - draggedView.getWidth() / 2f, oLeft},
                    {Math.abs(draggedCenterX - oRight), oRight - draggedView.getWidth() / 2f, oRight},
                    {Math.abs(draggedCenterX - oCenterX), oCenterX - draggedView.getWidth() / 2f, oCenterX}
            };

            float[][] yCandidates = {
                    {Math.abs(topBorder - oCenterY), oCenterY, oCenterY},
                    {Math.abs(bottomBorder - oCenterY), oCenterY - draggedView.getHeight(), oCenterY},
                    {Math.abs(topBorder - oBottom), oBottom, oBottom},
                    {Math.abs(bottomBorder - oTop), oTop - draggedView.getHeight(), oTop},
                    {Math.abs(draggedCenterY - oCenterY), oCenterY - draggedView.getHeight() / 2f, oCenterY},
                    {Math.abs(draggedCenterY - oTop), oTop - draggedView.getHeight() / 2f, oTop},
                    {Math.abs(draggedCenterY - oBottom), oBottom - draggedView.getHeight() / 2f, oBottom}
            };

            for (float[] x : xCandidates) {
                if (x[0] < bestXDistance) {
                    bestXDistance = x[0];
                    bestXTarget = x[1];
                    bestXBoundary = x[2];
                    snapX = true;
                }
            }

            for (float[] y : yCandidates) {
                if (y[0] < bestYDistance) {
                    bestYDistance = y[0];
                    bestYTarget = y[1];
                    bestYBoundary = y[2];
                    snapY = true;
                }
            }
        }

        // Apply snapping
        if (snapX) animateRenderX(draggedView,bestXTarget,bestXBoundary);
        if (snapY) animateRenderY(draggedView, bestYTarget,bestYBoundary);
    }

    public static void hideSnapLine(){
        if (snapLineDraw != null){
            snapLineDraw.clearLines();
        }
    }

    /**
     * Animate to more "gently" snap X-coordinates together,
     * and draw snap lines.
     */
    private static void animateRenderX(View view, float newX, float lineX) {
        view.setX(newX);
        view.animate().translationX(newX).setDuration(20).start();
        if (snapLineDraw != null) snapLineDraw.showLine(lineX, 0, true);
    }

    /**
     * Animate to more "gently" snap Y-coordinates together,
     * and draw snap lines.
     */
    private static void animateRenderY(View view, float newY, float lineY) {
        view.setY(newY);
        view.animate().translationY(newY).setDuration(20).start();
        if (snapLineDraw != null) snapLineDraw.showLine(0, (int) lineY, false);
    }

    /**
     * Sets the visible and invisible boundary lists.
     * @param pos = visible boundary list
     */
    public static void setCanvasSplitPositions(ArrayList<Integer> pos){
        positions = new ArrayList<>(pos);
        int screenWidth = positions.get(1);
        positions.add(screenWidth*4); //add right side end of screen boundary

        // Also add "invisible" boundaries for snapping in the middle of 2 boundaries
        halfBoundarySnapList.add(screenWidth/2);
        halfBoundarySnapList.add(screenWidth + screenWidth/2);
        halfBoundarySnapList.add(2*screenWidth + screenWidth/2);
    }

    protected static void setAddedImages(List<ImageView> images){
        addedImages = images;
    }

}
