package com.example.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Data.TempData;
import com.example.myscanapp.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.view.ViewfinderView;

/**
 * 扫描界面
 * 
 * @author dell
 * 
 */
public class CaptureActivity extends Activity implements Callback {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private TextView scantext;
	private TextView counttext;
	private Intent intent;
	private ImageView back;
	private ImageView light;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.capture);
		initview();
		if(TempData.prelist.isEmpty()){
			getFromAssets("code.txt");
		}

	}

    public void getFromAssets(String fileName){ 
        try { 
             InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) ); 
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            while((line = bufReader.readLine()) != null)
                TempData.prelist.add(line);
            System.out.println(TempData.prelist);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
       
}

	public void initview() {
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		scantext = (TextView) findViewById(R.id.scan_text);
		counttext = (TextView) findViewById(R.id.count_text);
		scantext.setText("正在为" + TempData.name + "出库");
		counttext.setText("已录入" + TempData.count + "商品");
		light = (ImageView) findViewById(R.id.light);
		light.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CameraManager.get().flashHandler();

			}
		});
		back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				onBackPressed();
			}
		});
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	public void onBackPressed() {
		if (TempData.count == 0) {
			intent = new Intent(this, MainActivity.class);
		} else {
			intent = new Intent(this, NoteActivity.class);
		}
		startActivity(intent);
		finish();
	};

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();

		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = false;   //这里改为true，可以设置震动

		// quit the scan view

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * 处理扫描结果
	 * 
	 * @param result
	 * @param
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		//判断编码前缀是否是规定的标识
		Boolean legal=IsPrefixLegal(resultString);
		// FIXME
		if (resultString.equals("")) {
			Toast.makeText(CaptureActivity.this, "扫码失败", Toast.LENGTH_SHORT)
					.show();
		} else if (!legal) {
			AlertDialog resutlDialog = new AlertDialog.Builder(
					CaptureActivity.this).create();
			resutlDialog.setCanceledOnTouchOutside(false);
			resutlDialog.setTitle("提示");
			resutlDialog.setMessage("请勿扫描其他商品");
			resutlDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							dialog.dismiss();

							if (handler != null) // 实现连续扫描
								handler.restartPreviewAndDecode();
						}
					});
			resutlDialog.show();
		}
		else {
			if (TempData.templist.contains(resultString)) {
				AlertDialog resutlDialog = new AlertDialog.Builder(
						CaptureActivity.this).create();
				resutlDialog.setCanceledOnTouchOutside(false);
				resutlDialog.setTitle("提示");
				resutlDialog.setMessage("该商品已被扫描过");
				resutlDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								dialog.dismiss();

								if (handler != null) // 实现连续扫描
									handler.restartPreviewAndDecode();
							}
						});
				resutlDialog.show();

			} else {
				intent = new Intent(CaptureActivity.this, NoteActivity.class);
				intent.putExtra("sn", resultString);
				TempData.add(resultString);
				startActivity(intent);
				finish();
			}
		}
	}

	private Boolean IsPrefixLegal(String resultString) {
		
		for(String pre : TempData.prelist){
			if(resultString.startsWith(pre)){
				return true;
			}
		}
		
		return false;
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

}