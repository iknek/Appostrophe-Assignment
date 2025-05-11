package com.example.app_test;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ClickController clickController;
    private ArrayList<Integer> positions = new ArrayList<>();
    private int screenWidth;
    private int scrollAreaHeight;

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
        handleDeviceScreenDimensions();
        clickController = new ClickController(this);
        setClickController();
        createCanvas();

        SnapLineDraw snapLine = new SnapLineDraw(this);
        ViewGroup root = findViewById(R.id.hscroll_container);
        root.addView(snapLine, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        SnappingHandler.setSnapLineDraw(snapLine);
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
        HorizontalScrollView scrollView = findViewById(R.id.horizontal_scroll);

        scrollView.post(() -> {
            scrollAreaHeight = scrollView.getHeight();
        });

        imageView.post(() -> {
            // Set canvas & background image size
            ViewGroup.LayoutParams imageParams = imageView.getLayoutParams();
            imageParams.width = 4*screenWidth;
            imageView.setLayoutParams(imageParams);

            ViewGroup.LayoutParams canvasParams = canvas.getLayoutParams();
            canvasParams.height = scrollAreaHeight;
            canvas.setLayoutParams(canvasParams);

            // Add 4 dividers at 25% intervals
            positions.add(0);
            positions.add(screenWidth);
            positions.add(2 * screenWidth);
            positions.add(3 * screenWidth);

            SnappingHandler.setCanvasSplitPositions(positions);

            for (int x : positions) {
                View divider = new View(this);
                FrameLayout.LayoutParams dividerParams = new FrameLayout.LayoutParams(4, MATCH_PARENT);
                dividerParams.leftMargin = x;
                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(Color.BLACK);
                canvas.addView(divider);
            }
        });
    }

    private void handleDeviceScreenDimensions(){
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = windowManager.getMaximumWindowMetrics().getBounds().width();
    }
}