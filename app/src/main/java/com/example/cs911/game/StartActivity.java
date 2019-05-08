package com.example.cs911.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class StartActivity extends Activity {
    public final static String MESSAGE = "com.example.cathy.project.MESSAGE";

    /**
     * Initializes the activity and sets the content view
     * @param savedInstanceState - the activity's previously frozen state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //hides title
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //hides title bar

        setContentView(R.layout.activity_start);

    }

    /**
     * Called when the user clicks the 'Play' button
     * Starts the Activity - takes the user to the player lobby
     * @param view - the View that was clicked
     */
    public void goToGame(View view){
        Intent intent = new Intent(this, WifiDirectActivity.class);
        startActivity(intent);
    }
}
/**
    MediaPlayer mediaPlayer= MediaPlayer.create(StartActivity.this,R.raw.gamesound);
        mediaPlayer.start();
 */