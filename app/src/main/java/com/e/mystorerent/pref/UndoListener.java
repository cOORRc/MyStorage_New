package com.e.mystorerent.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
public class UndoListener implements View.OnClickListener {
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	Context _context;

	// shared pref mode
	int PRIVATE_MODE = 0;

	// Shared preferences file name
	private static final String PREF_NAME = "for_undo";

	private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

	public UndoListener(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	@Override
	public void onClick(View v) {

	}
}
