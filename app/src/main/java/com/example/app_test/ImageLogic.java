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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

public class ImageLogic {
    private static ImageView selectedImage = null;
    private static List<ImageView> addedImages = new ArrayList<>(); //Used for checking snapping later
    static List<String> loadedImageUrls = null;

    static boolean hasLoaded = false;
    public static boolean hasLoaded(boolean loaded){
        if(loaded){
            hasLoaded = true;
        }
        return hasLoaded;
    }
    /**
     * Logic for addding picture to our view.
     * TODO: Add dynamic web fetch api + categories, move out of here!!
     */
    static void loadImageSelectorIcons(Activity activity, List<String> imageUrls) {
        loadedImageUrls = imageUrls;
        int[] buttonIds = {
                R.id.pic1, R.id.pic2, R.id.pic3, R.id.pic4,
                R.id.pic5, R.id.pic6, R.id.pic7, R.id.pic8
        };

        for (int i = 0; i < buttonIds.length && i <= imageUrls.size(); i++) {
            ImageButton btn = activity.findViewById(buttonIds[i]);
            String url = imageUrls.get(i);

            Glide.with(activity)
                    .asDrawable()
                    .load(url)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                            btn.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });
        }
    }

    /**
     * Loads image using button tag (id) and sets dimensions
     */
    protected static void createImage(Activity activity, View view){
        if(!hasLoaded){
            Toast.makeText(activity, "Images not loaded from network yet!", Toast.LENGTH_SHORT).show();
            return;
        }
        FrameLayout canvas = activity.findViewById(R.id.hscroll_container);
        int imageID = Integer.valueOf(view.getTag().toString());

        ImageView imageView = new ImageView(activity);

        Glide.with(activity)
                .load(loadedImageUrls.get(imageID-1))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);

                        int originalWidth = resource.getIntrinsicWidth();
                        int originalHeight = resource.getIntrinsicHeight();

                        // Scale down to 1/2 of original size
                        int scaledWidth = originalWidth / 2;
                        int scaledHeight = originalHeight / 2;

                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(scaledWidth, scaledHeight);
                        imageView.setLayoutParams(params);

                        imageView.setX(200); imageView.setY(200);

                        initImage(imageView, canvas);
                        canvas.addView(imageView);
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        // Handle cleanup if needed
                    }
                });
        Toast.makeText(activity, "Added Images!", Toast.LENGTH_SHORT).show();
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
