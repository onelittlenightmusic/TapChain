package org.tapchain;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.tapchain.AnimationChain.*;
import org.tapchain.Chain.ChainException;
import org.tapchain.TapChainEditor.IWindow;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;


@SuppressWarnings("serial")
public class AndroidPiece {
	static TapChainView activity = null;
	static IWindow w = null;
	static final int VOICE_REQUEST = 121;
	static BasicPiece recognized = new BasicPiece();
	
	public static void setActivity(TapChainView act) {
		activity = act;
	}

	public static void setWindow(IWindow window) {
		w = window;
	}
	
	public static void makeAlert(final String alert) {
		activity.mq.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, alert,
						Toast.LENGTH_SHORT).show();
			}
		});

	}

	public static class AndroidSound extends BasicSound {
		MediaPlayer localplay = null;

		AndroidSound(String path) {
			super();
			localplay = new MediaPlayer();
			try {
				localplay.setDataSource(path);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		AndroidSound(int resource) {
			super();
			localplay = MediaPlayer.create(activity, resource);
			// super.setLoop(null);
		}

		@Override
		public boolean play_impl() {
			localplay.start();
			return false;
		}

		@Override
		public boolean stop_impl() {
			if (localplay.isPlaying())
				localplay.pause();
			return false;
		}

		@Override
		public boolean wait_end_impl() throws InterruptedException {
			if (localplay.isPlaying())
				localplay.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						finish(false);
						Log.d("BasicSound", "WAIT Ended");
					}
				});
			// end.sync_pop();
			return false;
		}

		@Override
		public boolean reset_async_impl() {
			localplay.setOnSeekCompleteListener(new OnSeekCompleteListener() {
				@Override
				public void onSeekComplete(MediaPlayer mp) {
					finish(false);
				}
			});
			return false;
		}

		@Override
		public boolean reset_sound_impl() {
			try {
				localplay.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	public static class AndroidSound2 extends BasicSound {
		static SoundPool localplay = new SoundPool(10,
				AudioManager.STREAM_MUSIC, 0);
		int id = 0;
		float rate = 1.0f;
		int resource = 0;
//		CountDownLatch c = new CountDownLatch(1);

		AndroidSound2(String path) {
			super();
			id = localplay.load(path, 1);
		}

		AndroidSound2(int resource) {
			super();
			this.resource = resource;
		}

		@Override
		public boolean play_impl() {
			localplay.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					localplay.play(id, 100, 100, 1, 0, rate);
					finish(false);
				}
			});
			return false;
		}

		@Override
		public boolean stop_impl() {
//			localplay.autoPause();
			return false;
		}

		@Override
		public boolean wait_end_impl() throws InterruptedException {
//			finish(false);
			return true;
		}

		@Override
		public boolean reset_async_impl() {
			return false;
		}

		@Override
		public boolean reset_sound_impl() {
			id = localplay.load(activity, resource, 1);
			return false;
		}
	}

	public static class AndroidRecorder extends BasicRecorder {
		final MediaRecorder recorder = new MediaRecorder();
		final String path;

		public AndroidRecorder() {
			super();
			path = "/sdcard/com.bambooflower.test.multitouch2/test.3gp";
		}

		@Override
		public boolean record_start() throws ChainException {
			// String state = android.os.Environment.getExternalStorageState();
			// if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			// throw new ChainException("SD Card is not mounted.  It is " +
			// state
			// + ".");
			// }

			// make sure the directory we plan to store the recording in exists
			File directory = new File(path).getParentFile();
			if (!directory.exists() && !directory.mkdirs()) {
				throw new ChainException(this, "Path to file could not be created.");
			}

			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(path);
			try {
				recorder.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			recorder.start();
			makeAlert("録音を開始しました。");
			return false;
		}

		@Override
		public boolean record_stop() {
			recorder.stop();
			recorder.release();
			return false;
		}
	}

	public static class AndroidView extends BasicView {
		Paint paint = new Paint();
		AndroidView() {
			this(w);
		}
		
		AndroidView(IWindow v) {
			super();
			setWindow(v);
		}
		
		Rect r = new Rect();
		public Rect getScreenRect() {
			user_rect(getCenter().getScreenPoint(w), getSize(), r);
			return r;
		}
		
		RectF rf = new RectF();
		public RectF getScreenRectF() {
			user_rectF(getCenter().getScreenPoint(w), getSize(), rf);
			return rf;
		}
		
		Rect rw = new Rect();
		public Rect getWorldRect() {
			user_rect(getCenter(), getSize(), rw);
			return rw;
		}
		
		public void user_rect(HeroicPoint center, WorldPoint size, Rect r) {
			int halfx = size.x()/2, halfy = size.y()/2;
			r.set(center.x()-halfx, center.y()-halfy, center.x()+halfx, center.y()+halfy);
		}

		public void user_rectF(HeroicPoint center, WorldPoint size, RectF rf) {
			int halfx = size.x()/2, halfy = size.y()/2;
			rf.set(center.x()-halfx, center.y()-halfy, center.x()+halfx, center.y()+halfy);
		}

		public Drawable getDrawable() {
			return new Drawable() {
				@Override
				public void draw(Canvas arg0) {
					view_user(arg0, new ScreenPoint(getSize().x / 2, getSize().y / 2),
							getSize(), getAlpha(), getAngle());
				}

				@Override
				public int getOpacity() {
					return PixelFormat.OPAQUE;
				}

				@Override
				public void setAlpha(int alpha) {
				}

				@Override
				public void setColorFilter(ColorFilter cf) {
				}

				@Override
				public int getIntrinsicWidth() {
					return getSize().x;
				}

				@Override
				public int getIntrinsicHeight() {
					return getSize().y;
				}
			};
		}

		@Override
		public boolean view_user(Object canvas, ScreenPoint sp, WorldPoint size,
				int alpha, float angle) {
			boolean rtn = false;
			Canvas c = (Canvas)canvas;
			c.save();
			c.rotate(angle, sp.x(), sp.y());
			rtn = view_user(c, sp, size, alpha);
			c.restore();
			return rtn;
		}

		public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
				int alpha) {
			return false;
		}

}
	
	public static class AndroidImageView extends AndroidView {
		Bitmap bm_base = null, bm_scaled = null;
		Matrix matrix = new Matrix();

		AndroidImageView() {
			super();
		}

		public void view_init() throws ChainException {
			int resource;
			while (true) {
				resource = (Integer) pull();
				break;
			}
			bm_base = BitmapFactory.decodeResource(activity.getResources(),
					resource);
			bm_scaled = bm_base;
			_wp = new WorldPoint(100, 100);
		}

		@Override
		public boolean view_user(Canvas canvas, ScreenPoint sp,
				WorldPoint size, int alpha) {
			canvas.drawBitmap(bm_scaled, sp.x, sp.y, paint);
			return true;
		}

		public AndroidImageView setPercent(WorldPoint persent) {
			super.setPercent(persent);
			matrix.postScale(((float) _percent.x) / 100f,
					((float) _percent.y) / 100f);
			bm_scaled = Bitmap.createBitmap(bm_base, 0, 0, bm_base.getWidth(),
					bm_base.getHeight(), matrix, true);
			return this;
		}
	}
	
	
	public static class AndroidCircle extends AndroidView {
		float start = 0f, end = 360f;
		public void view_init() throws ChainException {
			setSize(new WorldPoint(100, 100));
			paint.setAntiAlias(true);
			paint.setStyle(Style.STROKE);
		}
		@Override
		public boolean view_user(Canvas canvas, ScreenPoint sp,
				WorldPoint size, int alpha) {
			paint.setColor(getColor());
			canvas.drawArc(getScreenRectF(), start, end, true, paint);
//			canvas.drawArc(getScreenRectF(), 180f, 360f, true, paint);
			return true;
		}
		
		public AndroidCircle setCircleAngle(float start, float end) {
			this.start = start;
			this.end = end;
			return this;
		}

	}

	public static class AndroidWindow extends AndroidView {

		@Override
		public boolean view_user(Canvas canvas, ScreenPoint sp,
				WorldPoint size, int alpha) {
			return false;
		}

	}

	public static class AndroidWindowSurfaceView extends AndroidWindow {

	}

	public static class AndroidQuaker extends BasicQuaker {
		public AndroidQuaker(int interval) {
			super(interval);
		}

		public boolean quake_impl() {
			Vibrator vibrator = (Vibrator) activity
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(val);
			return true;
		}

	}

	public static class AndroidAlert extends BasicPiece {
		@Override
		public boolean effect_run() throws ChainException {
			final String b = (String)pull();
			makeAlert(b);
			return true;
		}
	}
	
	public static class AndroidIntentSender extends Control {
		Intent intent = null;
		public AndroidIntentSender() {
			super();
		}
		public AndroidIntentSender intent_init(Intent i) {
			intent = i;
			return this;
		}
		public AndroidIntentSender ctrlReset() {
			try {
				activity.startActivityForResult(intent, VOICE_REQUEST); // Intent発行
			} catch (ActivityNotFoundException e) {
				makeAlert("");
			}
			return this;
		}
		public AndroidIntentSender ctrlStart() throws ChainException {
			final String b = (String)pull();
			makeAlert(b);
			return this;
		}
	}

	public static class AndroidRecognizer extends AndroidIntentSender {
		public AndroidRecognizer() {
			super();
			connectToPush(recognized);
			Intent i = new Intent(
			RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
			RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			i.putExtra(RecognizerIntent.EXTRA_PROMPT, "サンプル音声入力"); // ダイアログに表示される文字列
			intent_init(i);
		}
//		public AndroidRecognizer ctrlReset() {
//			try {
//				Intent intent = new Intent(
//						RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//				intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "サンプル音声入力"); // ダイアログに表示される文字列
//				activity.startActivityForResult(intent, VOICE_REQUEST); // Intent発行
//			} catch (ActivityNotFoundException e) {
//				makeAlert("音声入力に対応していません。");
//			}
//			return this;
//		}
//		public AndroidRecognizer ctrlStart() throws ChainException {
//			final String b = (String)pull();
//			makeAlert(b);
//			return this;
//		}
	}

	public static class AndroidButton extends AndroidView {

//		@Override
		public boolean isTouching(WorldPoint wp) {
			return false;
		}

		@Override
		public boolean view_user(Canvas canvas, ScreenPoint sp,
				WorldPoint size, int alpha) {
			return false;
		}

	}
}
