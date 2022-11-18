package com.example.class23a_hw_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    private AppCompatImageView main_IMG_lanes;

    private ExtendedFloatingActionButton left_ICN_arrow;
    private ExtendedFloatingActionButton right_ICN_arrow;

    private AppCompatImageView[][] obstacles;
    private AppCompatImageView[] carSpot;
    private AppCompatImageView[] crashSpot;
    private ShapeableImageView[] lives;

    /* The number of rows on screen */
    private final int ROWS = 7;
    /* The number of elements in a row */
    private final int COLS = 3;
    /* only relevant when car is allowed to move 1D (horizontally)*/
    private final int CAR_ROW = 5;

    private final int DELAY = 1000;
    private int currentCarPos;
    private GameManager gameManager;
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGame();
        gameManager = new GameManager(lives.length);
        startGame();

    }

    private void delayCrashAnimation() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                carSpot[currentCarPos].setVisibility(View.VISIBLE);
            }
        }, 2000);
    }

    private void startGame() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshUI();
                    }
                });
            }
        }, DELAY, DELAY);
    }

    private void refreshUI() {
        checkCrash();

        if (gameManager.isLose()) {
            Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show();
            for (ShapeableImageView heart : lives)
                heart.setVisibility(View.VISIBLE);
            gameManager.restart();
        } else {

            for (int i = ROWS - 1; i >= 0; i--) {
                for (int j = COLS - 1; j >= 0; j--) {
                    if (obstacles[i][j].getVisibility() == View.VISIBLE) {
                        if (i != ROWS - 1) {
                            obstacles[i + 1][j].setVisibility(View.VISIBLE);
                        }
                    }
                    obstacles[i][j].setVisibility(View.INVISIBLE);
                }
            }
            obstacles[0][ThreadLocalRandom.current().nextInt(COLS)].setVisibility(View.VISIBLE);

            for (int i = 0; i < COLS; i++) {
                if (i != currentCarPos) {
                    carSpot[i].setVisibility(View.INVISIBLE);
                } else {
                    carSpot[i].setVisibility(View.VISIBLE);
                }
                crashSpot[i].setVisibility(View.INVISIBLE);
            }
            for (int i = 0; i < gameManager.getCrashes(); i++)
                lives[i].setVisibility(View.INVISIBLE);
        }

    }

    private void checkCrash() {
        if (carSpot[currentCarPos].getVisibility() == View.VISIBLE &&
                obstacles[CAR_ROW][currentCarPos].getVisibility() == View.VISIBLE) {
            carSpot[currentCarPos].setVisibility(View.INVISIBLE);
            obstacles[CAR_ROW][currentCarPos].setVisibility(View.INVISIBLE);
            crashSpot[currentCarPos].setVisibility(View.VISIBLE);
            delayCrashAnimation();
            Toast.makeText(this, "Oh no!", Toast.LENGTH_SHORT).show();
            vibrate();
            gameManager.crashed();
        }
    }

    private void moveCar(int buttonId) {
        if (buttonId == R.id.left_ICN_arrow) {
            if (currentCarPos > 0) {
                carSpot[currentCarPos--].setVisibility(View.INVISIBLE);
                carSpot[currentCarPos].setVisibility(View.VISIBLE);
            }
        } else {
            if (currentCarPos < COLS - 1) {
                carSpot[currentCarPos++].setVisibility(View.INVISIBLE);
                carSpot[currentCarPos].setVisibility(View.VISIBLE);
            }
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
    }

    private void initGame() {
        findViews();
        loadImages();

        for (int i = 0; i < ROWS; i++) {
            for (AppCompatImageView obstacle : obstacles[i])
                obstacle.setVisibility(View.INVISIBLE);
        }

        for (AppCompatImageView car : carSpot)
            car.setVisibility(View.INVISIBLE);
        currentCarPos = (int) Math.floor(COLS / 2);
        carSpot[currentCarPos].setVisibility(View.VISIBLE);
        obstacles[0][currentCarPos].setVisibility(View.VISIBLE);

        for (AppCompatImageView crash : crashSpot)
            crash.setVisibility(View.INVISIBLE);

        initButton(left_ICN_arrow);
        initButton(right_ICN_arrow);

    }

    private void initButton(ExtendedFloatingActionButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveCar(button.getId());
            }
        });
    }

    private void findViews() {
        left_ICN_arrow = findViewById(R.id.left_ICN_arrow);
        right_ICN_arrow = findViewById(R.id.right_ICN_arrow);

        main_IMG_lanes = findViewById(R.id.main_IMG_lanes);

        findObstacles();
        findCars();
        findCrashes();
        findHearts();
    }

    private void findObstacles() {
        obstacles = new AppCompatImageView[][]{
                {findViewById(R.id.obstacle_IMG_pos_00),
                        findViewById(R.id.obstacle_IMG_pos_01),
                        findViewById(R.id.obstacle_IMG_pos_02),
                },

                {findViewById(R.id.obstacle_IMG_pos_10),
                        findViewById(R.id.obstacle_IMG_pos_11),
                        findViewById(R.id.obstacle_IMG_pos_12),

                },

                {findViewById(R.id.obstacle_IMG_pos_20),
                        findViewById(R.id.obstacle_IMG_pos_21),
                        findViewById(R.id.obstacle_IMG_pos_22),
                },

                {
                        findViewById(R.id.obstacle_IMG_pos_30),
                        findViewById(R.id.obstacle_IMG_pos_31),
                        findViewById(R.id.obstacle_IMG_pos_32),
                },

                {
                        findViewById(R.id.obstacle_IMG_pos_40),
                        findViewById(R.id.obstacle_IMG_pos_41),
                        findViewById(R.id.obstacle_IMG_pos_42),
                },

                {
                        findViewById(R.id.obstacle_IMG_pos_50),
                        findViewById(R.id.obstacle_IMG_pos_51),
                        findViewById(R.id.obstacle_IMG_pos_52),
                },

                {
                        findViewById(R.id.obstacle_IMG_pos_60),
                        findViewById(R.id.obstacle_IMG_pos_61),
                        findViewById(R.id.obstacle_IMG_pos_62),
                },
        };
    }

    private void findCars() {
        carSpot = new AppCompatImageView[]{
                findViewById(R.id.car_IMG_pos_0),
                findViewById(R.id.car_IMG_pos_1),
                findViewById(R.id.car_IMG_pos_2),
        };
    }

    private void findCrashes() {
        crashSpot = new AppCompatImageView[]{
                findViewById(R.id.crash_IMG_pos_0),
                findViewById(R.id.crash_IMG_pos_1),
                findViewById(R.id.crash_IMG_pos_2),
        };
    }

    private void findHearts() {
        lives = new ShapeableImageView[]{
                findViewById(R.id.main_heart_IMG_one),
                findViewById(R.id.main_heart_IMG_two),
                findViewById(R.id.main_heart_IMG_three),
        };
    }

    private void loadImages() {
        loadLandscape();
        loadCarImg();
        loadCrashImg();
        loadObstaclesImg();
    }

    private void loadLandscape() {
        Glide
                .with(this)
                .load(R.drawable.three_lane_highway)
                .into(main_IMG_lanes);
    }

    /*Binds images and views in a row
     * note that this function deals with AppCompactImageView
     *
     * @param cols number of elements in a row.
     * @param imageResource the image resource to bind.
     * @param view the view to bind the image to.
     * @return an array of views.
     * */
    private void loadRowOfImages(int imageResource, AppCompatImageView[] view) {
        for (int i = 0; i < COLS; i++) {
            Glide
                    .with(this)
                    .load(imageResource)
                    .into(view[i]);
        }
    }

    private void loadCrashImg() {
        loadRowOfImages(R.drawable.explosion, crashSpot);
    }

    private void loadCarImg() {
        loadRowOfImages(R.drawable.car_blue, carSpot);
    }

    private void loadObstaclesImg() {
        for (int i = 0; i < ROWS; i++)
            loadRowOfImages(R.drawable.granite_img, obstacles[i]);
    }

}