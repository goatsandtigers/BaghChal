package com.goatsandtigers.goatsandtigers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LevelCompleteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createOuterLayout());
    }

    private LinearLayout createOuterLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setBackgroundResource(R.drawable.congrats);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(buildTextView());
        if (GameState.isGameCompleted()) {
            layout.addView(buildNewGameButton());
        } else {
            layout.addView(buildNextLevelButton());
        }
        layout.setGravity(Gravity.CENTER);
        return layout;
    }

    private TextView buildTextView() {
        TextView tv = new TextView(this);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setGravity(Gravity.CENTER);
        int level = GameState.getLevel(this);
        if (level < GameState.NUM_LEVELS) {
            tv.setText("Now try level " + level);
        } else {
            tv.setText("You have completed Bagha Chal");
        }
        return tv;
    }

    private Button buildNextLevelButton() {
        Button nextLevelButton = new Button(this);
        nextLevelButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        nextLevelButton.setTypeface(null, Typeface.BOLD);
        nextLevelButton.setText("Next Level");
        nextLevelButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startNextLevel();
            }
        });
        return nextLevelButton;
    }

    private Button buildNewGameButton() {
        Button newGameButton = new Button(this);
        newGameButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        newGameButton.setTypeface(null, Typeface.BOLD);
        newGameButton.setText("Play again");
        newGameButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startNextLevel();
            }
        });
        return newGameButton;
    }

    private void startNextLevel() {
        Intent myIntent = new Intent(this, GoatsAndTigersActivity.class);
        startActivity(myIntent);
    }

}
