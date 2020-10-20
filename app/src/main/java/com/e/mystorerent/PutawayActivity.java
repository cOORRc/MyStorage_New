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

public class PutawayActivity extends AppCompatActivity {

	private Button bt_scanner, bt_confirm, bt_clearSnackBarWithAction;
	private EditText et_dataPallet, et_dataLocation;
	private RequestQueue put_Queue;

	UndoListener undoListener_perf;
	private TextView tv_page_put;

	String pat = "(^[PL])";
//	String loc = "(^[HO])";
	public static String sta_id, defulf_id = "0";
	public static String putaway_scan_contents_sheet, user_id, user_Fname;

	//// SP
	SharedPreferences sp;
	SharedPreferences.Editor editor;
	final String PREFNAME = "Preferences";
	final String USER_ID = "User_id";
	final String USER_FName = "User_FName";

	private void getSharedPrefer() {
		sp = getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
		user_id = sp.getString(USER_ID, "");
		user_Fname = sp.getString(USER_FName, "");
//		Toast.makeText(this, "sp :  " + user_id + "," + user_Fname, Toast.LENGTH_SHORT).show();
	}

	//// black page
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.log_out) {
			editor = sp.edit();
			editor.clear();
			editor.commit();
			Log.d("TAG", "putaway_logout");
			moveTaskToBack(true);
			startActivity(new Intent(PutawayActivity.this, loginActivity.class)); // close this activity and return to preview activity (if there is any)
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

//	private static String intent_user_id ,intent_userFName;
////	private void ResultIntent() {
////		Intent intent = getIntent();
////		intent_user_id = intent.getStringExtra("Result_user_ID");
////		intent_userFName = intent.getStringExtra("Result_user_fName");
////		Log.i("putaway","Result : userID : " + intent_user_id +
////				"\n full name : " + intent_userFName );
////		Toast.makeText(this, "onResume " +intent_user_id+
////				"," +intent_userFName , Toast.LENGTH_SHORT).show();
////
////	}

	@SuppressLint("WrongConstant")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		put_Queue = Volley.newRequestQueue(this);
		setContentView(R.layout.activity_rem_pat);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_name);
		TextView put_text_title = findViewById(R.id.text_title);
		put_text_title.setText(R.string.putaway_name);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}
		getSharedPrefer();
		requestStoragePermission();
		initView();
	}

	private void initView() {
		tv_page_put = findViewById(R.id.title_namepage);
		tv_page_put.setText(R.string.putaway_name);
		bt_scanner = findViewById(R.id.bt_scan);
		et_dataPallet = findViewById(R.id.et_pallet_id);
		et_dataPallet.requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(et_dataPallet, InputMethodManager.SHOW_IMPLICIT);
		et_dataLocation = findViewById(R.id.et_location_id);
		InputMethodManager imm_lo = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm_lo.showSoftInput(et_dataLocation, InputMethodManager.SHOW_IMPLICIT);

		et_dataPallet.addTextChangedListener(TextWatcher);
		et_dataLocation.addTextChangedListener(TextWatcher);
		bt_confirm = findViewById(R.id.bt_confirm);
		bt_clearSnackBarWithAction = findViewById(R.id.bt_clear);
	}

	public static String palletInput, locationInput;
	private TextWatcher TextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			palletInput = et_dataPallet.getText().toString().trim();
			locationInput = et_dataLocation.getText().toString().trim();
//			bt_confirm.setEnabled(!palletInput.isEmpty() && !locationInput.isEmpty());
//			bt_confirm.setBackgroundResource(R.drawable.shape_button);
//			bt_confirm.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
////					callApiConfirm();
//					Toast.makeText(getApplicationContext(), "click ", Toast.LENGTH_SHORT).show();
//				}
//			});
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (!palletInput.isEmpty() && locationInput.isEmpty()) {
				et_dataLocation.requestFocus();
			}
			if (!palletInput.isEmpty() && !locationInput.isEmpty()) {
				bt_confirm.setEnabled(true);
				bt_confirm.setBackgroundResource(R.drawable.shape_button);
				et_dataPallet.requestFocus();
				callApiConfirm();

			}
//			Toast.makeText(getApplicationContext(), "changed ", Toast.LENGTH_SHORT).show();
		}
	};

	public void onClick(View view) {
//		if (view.getId() == R.id.bt_scan) {
//			if (ContextCompat.checkSelfPermission(this,
//					Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//				requestStoragePermission();
//				Log.d("TAG", "STORAGE_PERMISSION_CODE " + STORAGE_PERMISSION_CODE);
//			} else {
//				Log.d("TAG", "STORAGE_PERMISSION_CODE " + STORAGE_PERMISSION_CODE);
//				Intent scanner_stor = new Intent(getApplication(), ScanActivity.class);
//				startActivityForResult(scanner_stor, 0);
//			}
//
//		}

		if (view.getId() == R.id.bt_clear) {
			et_dataPallet.getText().clear();
			et_dataLocation.getText().clear();
			Snackbar snackbar = Snackbar.make(view, "Data deleted", Snackbar.LENGTH_SHORT);
//			snackbar.setAction(R.string.undo_string, new UndoListener(this));
			snackbar.getView().setBackgroundColor(0xFFFF0000);
			snackbar.show();
		}
	}

	@SuppressLint("MissingSuperCall")
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0 && resultCode == RESULT_OK) {
//        if (requestCode == REQUEST_QR_SCAN && resultCode == RESULT_OK) {
			String contents_sheet = intent.getStringExtra("SCAN_RESULT");
			putaway_scan_contents_sheet = contents_sheet;
			Log.i("Scan1", putaway_scan_contents_sheet);
			callApiPutawayScanCheck();
		}
	}

	private void callApiPutawayScanCheck() {
		String url = "https://api.albatrossthai.com/MyStorage/php/Putaway_ScanCheck.php";
		String Text_Scan = putaway_scan_contents_sheet;
		HashMap<String, String> par_putaway = new HashMap<>();
		par_putaway.put("Text_Scan", Text_Scan);
		final JSONObject data_putaway = new JSONObject(par_putaway);
		JsonObjectRequest request = new JsonObjectRequest(url, data_putaway, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					JSONObject api_jsonObj = new JSONObject(String.valueOf(response));
					Log.i("api_putaway", String.valueOf(response));
					String status_id = api_jsonObj.getString("Status");
					Log.i("api_status_id", status_id);
					sta_id = status_id;
					Log.i("api_sta_id", sta_id);

					if (!sta_id.matches(defulf_id)) {
						Log.i("api", "matches 1 ");
						Pattern patternPL = Pattern.compile(pat);
//						Pattern patternHO = Pattern.compile(loc);
						Matcher math_dataPL = patternPL.matcher(putaway_scan_contents_sheet);
//						Matcher math_dataHO = patternHO.matcher(putaway_scan_contents_sheet);
						Log.i("Scan2", putaway_scan_contents_sheet);
						if (math_dataPL.find()) {
//							Toast.makeText(PutawayActivity.this, "chack_PL :" + math_dataPL, Toast.LENGTH_SHORT).show();
							et_dataPallet.setText(putaway_scan_contents_sheet);
							et_dataPallet.setTextColor(Color.BLACK);
						}
						else {
//							Toast.makeText(PutawayActivity.this, "chack_HO :" + math_dataHO, Toast.LENGTH_SHORT).show();
							et_dataLocation.setText(putaway_scan_contents_sheet);
							et_dataLocation.setTextColor(Color.BLACK);
						}
					}
					if (sta_id.matches(defulf_id)) {

						String result_data = api_jsonObj.getString("ResultData");
						Log.i("api_putaway", "00000 : " + putaway_scan_contents_sheet);
						et_dataPallet.getText().clear();
						et_dataLocation.getText().clear();

						AlertDialog.Builder builder = new AlertDialog.Builder(PutawayActivity.this, R.style.AlertDialog_info);
						builder.setTitle("Explain")
								.setIcon(R.drawable.ic_info)
								.setMessage(result_data)
								.create()
								.show();
						builder.wait(200);
					}

				} catch (JSONException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				error.printStackTrace();
			}
		});
		put_Queue.add(request);
	}

	private void callApiConfirm() {
		String url = "https://api.albatrossthai.com/MyStorage/php/Confirm_Putaway.php";
		String User_ID = user_id;
		String Pallet_ID = et_dataPallet.getText().toString().trim();
		String Location_ID = et_dataLocation.getText().toString().trim();
//		Toast.makeText(this, "User_ID" + User_ID, Toast.LENGTH_SHORT).show();
		HashMap<String, String> confirm_putaway = new HashMap<>();
		confirm_putaway.put("Pallet_ID", Pallet_ID);
		confirm_putaway.put("Location_ID", Location_ID);
		confirm_putaway.put("User_ID", User_ID);
		JSONObject confirm_data_putaway = new JSONObject(confirm_putaway);
		JsonObjectRequest request = new JsonObjectRequest(url, confirm_data_putaway, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					JSONObject api_jsonObj = new JSONObject(String.valueOf(response));
					String status_id = api_jsonObj.getString("tStatus");
					String status_text = api_jsonObj.getString("tStatus_Text");
					Log.i("api_status", status_id + status_text);
					et_dataPallet.getText().clear();
					et_dataLocation.getText().clear();
					if (!status_id.matches(defulf_id)) {
						AlertDialog.Builder builder = new AlertDialog.Builder(PutawayActivity.this, R.style.AlertDialog_complete);
						builder.setTitle("Complete")
								.setIcon(R.drawable.ic_done)
								.setMessage(status_text)
								.create()
								.show();
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(PutawayActivity.this, R.style.AlertDialog_info);
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
		put_Queue.add(request);
	}

	//	permission camera
	private final int STORAGE_PERMISSION_CODE = 1;

	public void requestStoragePermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.READ_EXTERNAL_STORAGE)) {
			new AlertDialog.Builder(this)
					.setTitle("Permission needed")
					.setMessage("This permission is needed because of this and that")
					.setPositiveButton("ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ActivityCompat.requestPermissions(PutawayActivity.this,
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
