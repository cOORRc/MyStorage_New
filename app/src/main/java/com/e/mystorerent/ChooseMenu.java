package com.e.mystorerent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ChooseMenu extends AppCompatActivity {
	SharedPreferences sp;
	SharedPreferences.Editor editor;
	final String PREFNAME = "Preferences";
	final String USER_ID = "User_id";
	final String USER_FName = "User_FName";
	final String sss = "sss";

	private void getSharedPrefer() {
		sp = getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
		String user_id = sp.getString(USER_ID, "");
		String user_Fname = sp.getString(USER_FName, "");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_menu);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_name);
		TextView put_text_title = findViewById(R.id.text_title);
		put_text_title.setText(R.string.app);
		setSupportActionBar(toolbar);
		getSharedPrefer();
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public void onClick(View view) {
		if (view.getId() == R.id.bt_choose_putaway) {
			startActivity(new Intent(ChooseMenu.this, PutawayActivity.class));
		}
		if (view.getId() == R.id.bt_choose_remove) {
			startActivity(new Intent(ChooseMenu.this, MoveActivity.class));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}
}
