package com.e.mystorerent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.e.mystorerent.ui.login.ValidationTextWatcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class loginActivity extends AppCompatActivity {
	private Toolbar toolbar;
	private static TextView login_text_title;
	private ImageView login_img_logo;
	private TextView login_text_singIn;
	private TextInputLayout login_username_layout, login_password_layout;
	private TextInputEditText login_username_edit, login_password_edit;
	private Button butt_sgnIn;
	private EditText mTextView;

	private RequestQueue mQueue;
	public static String sta_text, intent_user_id, intent_user_fName;
	public static String sta_id = "0";

	SharedPreferences sp;
	SharedPreferences.Editor editor;
	final String PREFNAME = "Preferences";
	final String USER_ID = "User_id";
	final String USER_FName = "User_FName";

	@Override
	public void onBackPressed() {
		Toast.makeText(this, "Please wait a moment", Toast.LENGTH_SHORT).show();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				sp.edit().clear().commit();
				moveTaskToBack(true);
				finish();
				System.exit(0);
			}
		}, 1500);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mQueue = Volley.newRequestQueue(this);

		setContentView(R.layout.login_fragment);
		toolbar = findViewById(R.id.toolbar_name);
		login_text_title = findViewById(R.id.text_title);
		login_text_title.setText(R.string.app);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setSupportActionBar(toolbar);
		initView();
		sp = getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
		editor = sp.edit();
		sp.edit().clear().commit();
	}

	private void initView() {
		login_img_logo = findViewById(R.id.img_login);
		login_text_singIn = findViewById(R.id.text_sign_in);
		login_username_layout = findViewById(R.id.text_input_user);
		login_password_layout = findViewById(R.id.text_input_pass);
		login_username_edit = findViewById(R.id.edittext_user);
		login_password_edit = findViewById(R.id.edittext_pass);
		login_username_edit.addTextChangedListener(new ValidationTextWatcher(login_username_edit));
		login_password_edit.addTextChangedListener(new ValidationTextWatcher(login_password_edit));

		butt_sgnIn = findViewById(R.id.bt_sign);
		butt_sgnIn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String Username = login_username_edit.getText().toString();
				String Password = login_password_edit.getText().toString();
				if (validateInput(Username, Password)) {
				}
			}
		});
	}

	private boolean validateInput(String Username, String Password) {
		if (Username.isEmpty()) {
			login_username_layout.setError(getString(R.string.text_username_cannot_be_empty));
			return false;
		}
		if (Password.isEmpty()) {
			login_password_layout.setError(getString(R.string.text_password_cannot_be_empty));
			return false;
		}
		if (!Username.isEmpty() && Username.length() < 4) {
			login_password_layout.setError(null);
			login_username_layout.setError(getString(R.string.text_username_helper));
			return false;
		}
		if ((!Username.isEmpty() && Username.length() >= 4) && Password.isEmpty()) {
			login_username_layout.setError(null);
			login_password_edit.setError(getString(R.string.text_password_cannot_be_empty));
			return false;
		}
		if ((!Username.isEmpty() && Username.length() >= 4) && (!Password.isEmpty() && Password.length() < 4)) {
			login_username_layout.setError(null);
			login_password_layout.setError(getString(R.string.text_password_must_be_more_five));
			return false;
		}
		if (!Username.isEmpty() && Username.length() < 4 && !Password.isEmpty() && Password.length() >= 4) {
			login_password_layout.setError(null);
			login_username_layout.setError(getString(R.string.text_username_helper));
			return false;
		}
		if (!Username.isEmpty() && Username.length() < 4 && !Password.isEmpty() && Password.length() < 4) {
			login_username_layout.setError(getString(R.string.text_username_helper));
			login_password_layout.setError(getString(R.string.text_password_must_be_more_five));
			return false;
		}
		if ((!Username.isEmpty() && Username.length() >= 4) && (!Password.isEmpty() && Password.length() >= 4)) {
			login_username_layout.setError(null);
			login_password_layout.setError(null);
			json_dataLogin();
			return false;
		}
		return false;
	}

	private void json_dataLogin() {
		String url = "https://api.albatrossthai.com/MyStorage/php/login.php";
		String username_box = login_username_edit.getText().toString().trim();
		String password_box = login_password_edit.getText().toString().trim();
		HashMap<String, String> par_login = new HashMap<>();
		par_login.put("User_Name", username_box);
		par_login.put("Password", password_box);
		JSONObject data_login = new JSONObject(par_login);
		JsonObjectRequest request = new JsonObjectRequest(url, data_login, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					JSONObject api_jsonObj = new JSONObject(String.valueOf(response));
					String status_id = api_jsonObj.getString("Status");
					JSONArray ja_ResultData = api_jsonObj.getJSONArray("ResultData");
					ArrayList<String> ar_resultData = new ArrayList<String>();
					for (int i = 0; i < ja_ResultData.length(); i++) {
						JSONObject unpack_resulData = ja_ResultData.getJSONObject(i);
						String user_id = unpack_resulData.getString("user_id");
						String user_fName = unpack_resulData.getString("user_fName");
						ar_resultData.add(user_id);
						ar_resultData.add(user_fName);
					}
					String str_user_id = ar_resultData.get(0);
					String str_user_full_name = ar_resultData.get(1);
					sta_id = status_id;
					intent_user_id = str_user_id;
					intent_user_fName = str_user_full_name;
					editor.putString(USER_ID, intent_user_id);
					editor.putString(USER_FName, intent_user_fName);
					editor.commit();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (sta_id.equals("1")) {
					Log.i("api_login ", "staNumber");
					Intent goto_choose = new Intent(loginActivity.this, ChooseMenu.class);
					startActivity(goto_choose);
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity.this,R.style.AlertDialog_error);
					builder.setTitle("Sign-in error")
							.setMessage("Username or Password incurred. please try again")
							.create()
							.show();
				}
			}

		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				error.printStackTrace();
			}
		});
		mQueue.add(request);
	}

	@Override
	public void onResume() {
		super.onResume();
		sp.edit().clear().commit();
		login_username_edit.getText().clear();
		login_password_edit.getText().clear();

	}

}

