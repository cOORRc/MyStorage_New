package com.e.mystorerent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.e.mystorerent.pref.UndoListener;
import com.e.mystorerent.scan.ScanActivity;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoveActivity extends AppCompatActivity {

	private Button rem_bt_scanner, rem_bt_confirm, rem_bt_clearSnackBarWithAction;
	private EditText rem_et_dataPallet, rem_et_dataLocation;

	UndoListener undoListener_perf;
	private TextView tv_page_rem;
	private String Pallet_ID, Location_ID;
	String move_pat = "(^[PL])";
//	String move_loc = "(^[HO])";
	public static String move_sta_id, move_defulf_id = "0";
	public static String move_scan_contents_sheet, move_user_id, move_user_Fname;
	private RequestQueue move_Queue;

	//// SP
	SharedPreferences sp;
	SharedPreferences.Editor editor;
	final String PREFNAME = "Preferences";
	final String USER_ID = "User_id";
	final String USER_FName = "User_FName";

	private void getSharedPrefer() {
		sp = getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
		move_user_id = sp.getString(USER_ID, "");
		move_user_Fname = sp.getString(USER_FName, "");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity, menu);
		MenuItem menu_logout = menu.findItem(R.id.log_out);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.log_out) {
			editor = sp.edit();
			editor.clear();
			editor.commit();
			moveTaskToBack(true);
			startActivity(new Intent(MoveActivity.this, loginActivity.class)); // close this activity and return to preview activity (if there is any)
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rem_pat);
		move_Queue = Volley.newRequestQueue(this);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_name);
		TextView put_text_title = findViewById(R.id.text_title);
		put_text_title.setText(R.string.move_name);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
//			getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//			getSupportActionBar().setCustomView(R.layout.toolbar);
//			getSupportActionBar().setTitle(R.string.putaway_name);
		}
		getSharedPrefer();
		rem_init();
		requestStoragePermission();
	}

	public void onClick(View view) {
		if (view.getId() == R.id.bt_confirm) {
			if (rem_et_dataPallet.length() > 0 && rem_et_dataLocation.length() > 0) {
				rem_bt_confirm.setEnabled(true);
				rem_bt_confirm.setBackgroundResource(R.drawable.shape_button);
				rem_et_dataPallet.requestFocus();
				callApiMoveConfirm();
			}
		}

		if (view.getId() == R.id.bt_clear) {
			rem_et_dataPallet.getText().clear();
			rem_et_dataLocation.getText().clear();
			Snackbar snackbar = Snackbar.make(view, "Data deleted", Snackbar.LENGTH_SHORT);
//			snackbar.setAction(R.string.undo_string, new UndoListener(this));
			snackbar.getView().setBackgroundColor(0xFFFF0000);
			snackbar.show();
		}
	}

	@SuppressLint("MissingSuperCall")
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0 && resultCode == RESULT_OK) {
			String rem_contents_sheet = intent.getStringExtra("SCAN_RESULT");
			move_scan_contents_sheet = rem_contents_sheet;
			callApiMoveScanCheck();
		}
	}

	private void callApiMoveScanCheck() {
		String url_move = "https://api.albatrossthai.com/MyStorage/php/Move_Putaway_Check.php";
		String Text_Scan = move_scan_contents_sheet;
		HashMap<String, String> par_move = new HashMap<>();
		par_move.put("Text_Scan", Text_Scan);
		final JSONObject data_move = new JSONObject(par_move);
		JsonObjectRequest request = new JsonObjectRequest(url_move, data_move, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					JSONObject api_jsonObj = new JSONObject(String.valueOf(response));
					String status_id = api_jsonObj.getString("Status");
					move_sta_id = status_id;
					if (!move_sta_id.matches(move_defulf_id)) {
						Pattern patternPL = Pattern.compile(move_pat);
//						Pattern patternHO = Pattern.compile(move_loc);
						Matcher math_dataPL = patternPL.matcher(move_scan_contents_sheet);
//						Matcher math_dataHO = patternHO.matcher(move_scan_contents_sheet);
						Log.i("Scan2", move_scan_contents_sheet);
						if (math_dataPL.find()) {
							rem_et_dataPallet.setText(move_scan_contents_sheet);
							rem_et_dataPallet.setTextColor(Color.BLACK);
						}
						else {
							rem_et_dataLocation.setText(move_scan_contents_sheet);
							rem_et_dataLocation.setTextColor(Color.BLACK);
						}
					}
					if (move_sta_id.matches(move_defulf_id)) {
						String result_data = api_jsonObj.getString("ResultData");
						AlertDialog.Builder builder = new AlertDialog.Builder(MoveActivity.this, R.style.AlertDialog_info);
						builder.setTitle("Explain")
								.setIcon(R.drawable.ic_info)
								.setMessage(result_data)
								.create()
								.show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				error.printStackTrace();
			}
		});
		move_Queue.add(request);
	}

	private void callApiMoveConfirm() {
		String url_move = "https://api.albatrossthai.com/MyStorage/php/Confirm_Move_Putaway.php";
		String User_ID = move_user_id;
		Pallet_ID = rem_et_dataPallet.getText().toString().trim();
		Location_ID = rem_et_dataLocation.getText().toString().trim();
		HashMap<String, String> confirm_move = new HashMap<>();
		confirm_move.put("Pallet_ID", Pallet_ID);
		confirm_move.put("Location_ID", Location_ID);
		confirm_move.put("User_ID", User_ID);
		JSONObject confirm_data_move = new JSONObject(confirm_move);
		JsonObjectRequest request = new JsonObjectRequest(url_move, confirm_data_move, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					JSONObject api_jsonObj = new JSONObject(String.valueOf(response));
					String status_id = api_jsonObj.getString("tStatus");
					String status_text = api_jsonObj.getString("tStatus_Text");
					rem_et_dataPallet.getText().clear();
					rem_et_dataLocation.getText().clear();
					if (!status_id.matches(move_defulf_id)) {
						AlertDialog.Builder builder = new AlertDialog.Builder(MoveActivity.this, R.style.AlertDialog_complete);
						builder.setTitle("Complete")
								.setIcon(R.drawable.ic_done)
								.setMessage(status_text + Location_ID)
								.create()
								.show();
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(MoveActivity.this, R.style.AlertDialog_info);
						builder.setTitle("Explain")
								.setIcon(R.drawable.ic_info)
								.setMessage(status_text)
								.create()
								.show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				error.printStackTrace();
			}
		});
		move_Queue.add(request);
	}

	public void rem_init() {
		tv_page_rem = findViewById(R.id.title_namepage);
		tv_page_rem.setText(R.string.move_name);
		rem_bt_scanner = findViewById(R.id.bt_scan);
		rem_bt_confirm = findViewById(R.id.bt_confirm);
		rem_bt_clearSnackBarWithAction = findViewById(R.id.bt_clear);
		rem_et_dataPallet = findViewById(R.id.et_pallet_id);
		rem_et_dataPallet.requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(rem_et_dataPallet, InputMethodManager.SHOW_IMPLICIT);
		rem_et_dataLocation = findViewById(R.id.et_location_id);
		InputMethodManager imm_lo = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm_lo.showSoftInput(rem_et_dataLocation, InputMethodManager.SHOW_IMPLICIT);
		rem_et_dataPallet.addTextChangedListener(TextWatcher);
		rem_et_dataLocation.addTextChangedListener(TextWatcher);

	}

	public static String palletInput, locationInput;
	private android.text.TextWatcher TextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			palletInput = rem_et_dataPallet.getText().toString().trim();
			locationInput = rem_et_dataLocation.getText().toString().trim();
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (!palletInput.isEmpty() && locationInput.isEmpty()) {
				rem_et_dataLocation.requestFocus();
			}
			if (!palletInput.isEmpty() && !locationInput.isEmpty()) {
				rem_bt_confirm.setEnabled(true);
				rem_bt_confirm.setBackgroundResource(R.drawable.shape_button);
				rem_et_dataPallet.requestFocus();
				callApiMoveConfirm();

			}
		}
	};

	//  permission camera
	private int STORAGE_PERMISSION_CODE = 1;
	private void requestStoragePermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.READ_EXTERNAL_STORAGE)) {
			new AlertDialog.Builder(this)
					.setTitle("Permission needed")
					.setMessage("This permission is needed because of this and that")
					.setPositiveButton("ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ActivityCompat.requestPermissions(MoveActivity.this,
									new String[]{Manifest.permission.CAMERA}, STORAGE_PERMISSION_CODE);
						}
					})
					.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.create().show();

		} else {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.CAMERA}, STORAGE_PERMISSION_CODE);
		}
	}
}
