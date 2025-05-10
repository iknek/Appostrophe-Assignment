package com.example.app_test;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

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
        createCanvas();
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

        // Would probably be nicer if these lines were moved to ImageLogic, or at the least abstracted out into their own method....
        Drawable img = ResourcesCompat.getDrawable(getResources(), R.drawable.image2, null); // Loaded images this way so we can load with 1x size scaling
        imageView.setImageDrawable(img);
        int imageWidth = img.getIntrinsicWidth();
        int imageHeight = img.getIntrinsicHeight();

        imageView.setLayoutParams(new FrameLayout.LayoutParams(imageWidth, imageHeight));

        //TODO: Make where image is placed dynamic based on where user has scrolled
        imageView.setX(200);
        imageView.setY(200);

        //Init image item logic for when it's added.
        ImageLogic.initImage(imageView, canvas);
        canvas.addView(imageView);

        CharSequence text = "Added Image!"; //Maybe something for SCRL to implement? If images are loaded in the same place, they get stacked on top of each other and you might forget ;)
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    /**
     * Delete item
     */
    public void onDeletePic(View view){
        ImageLogic.deleteImage();
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