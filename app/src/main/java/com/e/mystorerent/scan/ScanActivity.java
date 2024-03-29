package com.e.mystorerent.scan;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.e.mystorerent.R;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
	private ZXingScannerView mScannerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
		setContentView(mScannerView);
	}

	@Override
	public void onResume() {
		super.onResume();
		mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
		mScannerView.startCamera();          // Start camera on resume
	}

	@Override
	public void onPause() {
		super.onPause();
		mScannerView.stopCamera();           // Stop camera on pause
	}

	@Override
	public void handleResult(Result rawResult) {
		// Do something with the result here
		Intent result = new Intent();
		result.putExtra("SCAN_RESULT", rawResult.getText());
		result.putExtra("SCAN_RESULT_FORMAT", rawResult.getBarcodeFormat().toString());
		setResult(Activity.RESULT_OK, result);
		finish();
		onBackPressed();
	}
}