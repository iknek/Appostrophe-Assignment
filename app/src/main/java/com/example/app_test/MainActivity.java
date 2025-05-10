package com.example.app_test;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setCanvasSize();
    }

    /**
     * Clear image selection when clicking on canvas
     */
    public void imageViewClickHandler(View view){
        ImageLogic.clearSelection();
    }

    /**
     * Logic for button to add picture to our view.
     * TODO: Add dynamic web fetch api + categories
     */
    public void onClickAddPic(View view) {
        FrameLayout canvas = findViewById(R.id.hscroll_container);

        ImageView imageView = new ImageView(this);

        Drawable img = ResourcesCompat.getDrawable(getResources(), R.drawable.image2, null); // Loaded images this way so we can load with 1x size scaling

        imageView.setImageDrawable(img);
        imageView.setTag("image_" + System.currentTimeMillis());

        int imageWidth = img.getIntrinsicWidth();
        int imageHeight = img.getIntrinsicHeight();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageWidth, imageHeight);
        imageView.setLayoutParams(params);

        //TODO: Make where image is placed dynamic based on where user has scrolled
        imageView.setX(200);
        imageView.setY(200);

        //Init image item logic for when it's added.
        ImageLogic.initImage(imageView, canvas); // New call

        canvas.addView(imageView);
    }

    /**
     * Sets the size of our FrameLayout, and over-sized ImageView, based on device screen width.
     */
    private void setCanvasSize() {
        ImageView imageView = findViewById(R.id.image_view);
        FrameLayout container = findViewById(R.id.hscroll_container);

        imageView.post(() -> { //Defer layout until view hierarchy is ready
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = 4 * metrics.widthPixels;

            // Set widths
            ViewGroup.LayoutParams imageParams = imageView.getLayoutParams();
            imageParams.width = width;
            imageView.setLayoutParams(imageParams);

            ViewGroup.LayoutParams containerParams = container.getLayoutParams();
            containerParams.width = width;
            container.setLayoutParams(containerParams);
        });
    }

}