package com.example.app_test;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private ClickController clickController;

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
        clickController = new ClickController(this);
        setClickController();
        createCanvas();
    }

    public void setClickController(){
        findViewById(R.id.loadImageButton).setOnClickListener(clickController::onClickAddPic);
        findViewById(R.id.deleteImageButton).setOnClickListener(clickController::onDeletePic);
        findViewById(R.id.image_view).setOnClickListener(clickController::imageViewClickHandler);
        findViewById(R.id.backgroundColorSetButton).setOnClickListener(clickController::backgroundColorButtonHandler);
    }

    public void onColorClick(View view) {
        clickController.onColorClick(view);
    }

    /**
     * Sets the size of our FrameLayout, and over-sized ImageView, based on device screen width.
     * Also sets our 4 dividers.
     */
    private void createCanvas() {
        ImageView imageView = findViewById(R.id.image_view);
        FrameLayout canvas = findViewById(R.id.hscroll_container);

        imageView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int screenWidth = metrics.widthPixels;
            int totalWidth = 4 * screenWidth;

            // Set canvas & background image size
            ViewGroup.LayoutParams imageParams = imageView.getLayoutParams();
            imageParams.width = totalWidth;
            imageView.setLayoutParams(imageParams);

            ViewGroup.LayoutParams canvasParams = canvas.getLayoutParams();
            canvasParams.width = totalWidth;
            canvas.setLayoutParams(canvasParams);

            // Add 4 dividers at 25% intervals
            int[] positions = {
                    0,
                    screenWidth,
                    2 * screenWidth,
                    3 * screenWidth
            };

            for (int x : positions) {
                View divider = new View(this);
                FrameLayout.LayoutParams dividerParams = new FrameLayout.LayoutParams(4, FrameLayout.LayoutParams.MATCH_PARENT);
                dividerParams.leftMargin = x;
                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(Color.BLACK); // or any visible color
                canvas.addView(divider);
            }
        });
    }

}