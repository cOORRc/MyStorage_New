package com.e.mystorerent;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * Some older devices needs a small delay between UI widget updates
	 * and a change of the status and navigation bar.
	 */
	private static final int UI_ANIMATION_DELAY = 300;
	private final Handler mHideHandler = new Handler();
	private View mContentView;
	private Button mDummy;

	private View mControlsView;
	private final Runnable mShowPart2Runnable = new Runnable() {
		@Override
		public void run() {
			// Delayed display of UI elements
			ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				actionBar.show();
			}
			mControlsView.setVisibility(View.VISIBLE);
		}
	};
	private boolean mVisible;
	private final Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			hide();
		}
	};
	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */

	private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
				if (isConnected()) {
					Intent goto_login = new Intent(getApplication(),loginActivity.class);
					startActivity(goto_login);
					finish();
				} else {
					Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
				}
			}
			return false;
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);

		mVisible = true;
		mControlsView = findViewById(R.id.fullscreen_content_controls);
		mContentView = findViewById(R.id.fullscreen_content);
		YoYo.with(Techniques.FadeIn)  //Yoyo   FadeIn
				.duration(2000)
				.repeat(0)
				.playOn(findViewById(R.id.fullscreen_content));
		// Set up the user interaction to manually show or hide the system UI.
		mContentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toggle();
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	private void toggle() {
		YoYo.with(Techniques.Bounce)  //Yoyo   FadeIn
			.duration(1500)
			.repeat(0)
			.playOn(findViewById(R.id.fullscreen_content));
		if (mVisible) {

			hide();
		} else {
			show();
		}
	}

	private void hide() {
		// Hide UI first
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
		mControlsView.setVisibility(View.GONE);
		mVisible = false;
		// Schedule a runnable to remove the status and navigation bar after a delay
		mHideHandler.removeCallbacks(mShowPart2Runnable);
	}

	@SuppressLint("InlinedApi")
	private void show() {
		// Show the system bar
		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		mVisible = true;
		// Schedule a runnable to display UI elements after a delay
		mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
	}

	/**
	 * Schedules a call to hide() in delay milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	public boolean isConnected() {
		boolean connected = false;
		try {
			ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo nInfo = cm.getActiveNetworkInfo();
			connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
			return connected;
		} catch (Exception e) {
			Log.e("Connectivity Exception", e.getMessage());
		}
		return connected;
	}
}
