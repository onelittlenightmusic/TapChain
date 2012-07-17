package org.tapchain.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.tapchain.R;
import org.tapchain.TapChainView;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.IPathListener;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.TapChainEdit.IWindow;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.Toast;

@SuppressWarnings("serial")
public class AndroidActor {
	static TapChainView activity = null;
	static IWindow w = null;
	static final int VOICE_REQUEST = 121;
	static final int CAMERA_REQUEST = 122;
	static Actor recognized = new Actor();

	//1.Initialization
	//2.Getters and setters
	public static void setActivity(TapChainView act) {
		activity = act;
	}

	public static void setWindow(IWindow window) {
		w = window;
	}

	public static IWindow getWindow() {
		return w;
	}

	public static Resources getResources() {
		return activity.getResources();
	}
	
	public static ScreenPoint getScreenPoint(WorldPoint wp) {
		return wp.getScreenPoint(w);
	}

	public static void makeAlert(final String alert) {
		activity.postMQ(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, alert, Toast.LENGTH_SHORT).show();
			}
		});

	}

	//3.Changing state
	//4.Termination
	//5.Local classes
	public static class AndroidSound extends Actor.Sound {
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

	public static class AndroidSound2 extends Actor.Sound {
		static SoundPool localplay = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		int id = 0;
		float rate = 1.0f;
		int resource = 0;

		// CountDownLatch c = new CountDownLatch(1);

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
			// localplay.autoPause();
			return false;
		}

		@Override
		public boolean wait_end_impl() throws InterruptedException {
			// finish(false);
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

	public static class AndroidRecorder extends Actor.Recorder {
		final MediaRecorder recorder = new MediaRecorder();
		final String path;

		public AndroidRecorder() {
			super();
			path = "/sdcard/com.bambooflower.test.multitouch2/test.3gp";
		}

		@Override
		public boolean record_start() throws ChainException {
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
			makeAlert("�^�����J�n���܂����B");
			return false;
		}

		@Override
		public boolean record_stop() {
			recorder.stop();
			recorder.release();
			return false;
		}
	}

	public static class AndroidView extends Actor.ViewActor implements
		Parcelable {
		Paint paint = new Paint();

		AndroidView() {
			super();
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

		public void user_rect(IPoint center, WorldPoint size, Rect r) {
			int halfx = size.x() / 2, halfy = size.y() / 2;
			r.set(center.x() - halfx, center.y() - halfy, center.x() + halfx,
					center.y() + halfy);
		}

		public void user_rectF(IPoint center, WorldPoint size, RectF rf) {
			int halfx = size.x() / 2, halfy = size.y() / 2;
			rf.set(center.x() - halfx, center.y() - halfy, center.x() + halfx,
					center.y() + halfy);
		}

		public Drawable getDrawable() {
			return new Drawable() {
				@Override
				public void draw(Canvas arg0) {
					view_user(arg0, new ScreenPoint(getSize().x / 2, getSize().y / 2),
							getSize(), getAlpha());
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
		public boolean view_user(Object canvas, WorldPoint sp, WorldPoint size,
				int alpha, float angle) {
			boolean rtn = false;
			Canvas c = (Canvas) canvas;
			c.save();
			c.rotate(angle, sp.x(), sp.y());
			rtn = view_user(c, sp.getScreenPoint(w), size, alpha);
			c.restore();
			return rtn;
		}

		public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
				int alpha) {
			return false;
		}

		@Override
		public int describeContents() {
			return 0;
		}
		@Override
		public void writeToParcel(Parcel arg0, int arg1) {
		}

		public static final Parcelable.Creator<Parcelable> CREATOR = new Parcelable.Creator<Parcelable>() {
			public Parcelable createFromParcel(Parcel in) {
					AndroidView v = new AndroidView();
					return v;
			}
			public Parcelable[] newArray(int size) {
				return new AndroidView[size];
			}
		};
		void initFromParcel(Parcel in) {
		}
	}

	public static class AndroidImageView extends AndroidView {
		Bitmap bm_base = null, bm_scaled = null;
		Matrix matrix = new Matrix();

		AndroidImageView() {
			super();
		}
		
		public void setImage(Bitmap b) {
			bm_base = b;
			bm_scaled = bm_base;
			_wp = new WorldPoint(100, 100);
		}

		public void view_init() throws ChainException {
			int resource;
			while (bm_base == null) {
				resource = (Integer) pull();
				setImage(BitmapFactory.decodeResource(activity.getResources(), resource));
				break;
			}
		}

		@Override
		public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
				int alpha) {
			canvas.drawBitmap(bm_scaled, sp.x, sp.y, paint);
			return true;
		}

		public AndroidImageView setPercent(WorldPoint persent) {
			super.setPercent(persent);
			matrix
					.postScale(((float) _percent.x) / 100f, ((float) _percent.y) / 100f);
			bm_scaled = Bitmap.createBitmap(bm_base, 0, 0, bm_base.getWidth(),
					bm_base.getHeight(), matrix, true);
			return this;
		}

		@Override
		public void writeToParcel(Parcel arg0, int arg1) {
			arg0.writeParcelable(bm_base, 0);
			arg0.writeParcelable(bm_scaled, 0);
			arg0.writeInt(getCenter().x());
			arg0.writeInt(getCenter().y());
			arg0.writeInt(getSize().x());
			arg0.writeInt(getSize().y());
			arg0.writeInt(getColor());
		}

		public static final Parcelable.Creator<Parcelable> CREATOR = new Parcelable.Creator<Parcelable>() {
			public Parcelable createFromParcel(Parcel in) {
					AndroidView v = new AndroidImageView();
					v.initFromParcel(in);
					return v;
			}
			public Parcelable[] newArray(int size) {
				return new AndroidImageView[size];
			}
		};
		void initFromParcel(Parcel in) {
			bm_base = in.readParcelable(AndroidImageView.class.getClassLoader());
			bm_scaled = in.readParcelable(AndroidImageView.class.getClassLoader());
			setCenter(new WorldPoint(in.readInt(), in.readInt()));
			setSize(new WorldPoint(in.readInt(), in.readInt()));
			setColor(in.readInt());
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
		public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
				int alpha) {
			paint.setColor(getColor());
			canvas.drawArc(getScreenRectF(), start, end, true, paint);
			// canvas.drawArc(getScreenRectF(), 180f, 360f, true, paint);
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
		public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
				int alpha) {
			return false;
		}

	}

	public static class AndroidWindowSurfaceView extends AndroidWindow {

	}

	public static class AndroidQuaker extends Actor.BasicQuaker {
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

	public static class AndroidAlert extends Actor {
		@Override
		public boolean actorRun() throws ChainException {
			final String b = (String) pull();
			makeAlert(b);
			return true;
		}
	}

		public static void intent_init(Intent i, int TAG, IntentHandler h) {
			activity.addIntentHandler(TAG, h);
		}

		public static void intent_start(Intent i, int TAG) {
			try {
				activity.startActivityForResult(i, TAG); // Intent���s
			} catch (ActivityNotFoundException e) {
				makeAlert("");
			}
		}

	public static abstract class AndroidIntentHandler implements IntentHandler {
	}

	public static class AndroidCamera extends AndroidImageView {
		Intent i;
		CountDownLatch c = new CountDownLatch(1);
		public AndroidCamera() {
			super();
			i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			AndroidIntentHandler h = new AndroidIntentHandler() {
				@Override
				public void onIntent(int resultCode, Intent data) {
					if (resultCode != android.app.Activity.RESULT_OK)
						return;
					Bundle extras = data.getExtras();
					if (extras != null) {
						Bitmap bitmap = (Bitmap) extras.get("data");
						if ((bitmap != null)) {
							Log.w("TapChain","Camera OK");
							setImage(bitmap);
							c.countDown();
							// previewImage.setImageBitmap(bitmap);
							// previewImage.set
							// push(bitmap);
						}
					}
				}
			};
			intent_init(i, CAMERA_REQUEST, h);
		}
		public void init() {
			intent_start(i, CAMERA_REQUEST);
		}
		public void view_init() throws ChainException {
			try {
				c.await();
			} catch (InterruptedException e) {
				makeAlert("Failed to init Camera");
			}
			super.view_init();
		}
	}

	public static class AndroidRecognizer extends Actor.Controllable {
		CountDownLatch c = new CountDownLatch(1);
		Intent i;
		public AndroidRecognizer() {
			super();
			connectToPush(recognized);
			i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			i.putExtra(RecognizerIntent.EXTRA_PROMPT, "�T���v���������"); // �_�C�A���O�ɕ\������镶����
			intent_init(i, VOICE_REQUEST, new IntentHandler() {
				@Override
				public void onIntent(int resultCode, Intent data) {
					if (resultCode != android.app.Activity.RESULT_OK)
						return;
					// get the returned value
					ArrayList<String> results = data
							.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

					// refer the value
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < results.size(); i++) {
						buffer.append(results.get(i));
					}
					AndroidActor.recognized.push(buffer.toString());
					// show the value
					Toast.makeText(activity, buffer.toString(), Toast.LENGTH_LONG).show();
					c.countDown();
				}

			});
		}
		public void init() {
			intent_start(i, VOICE_REQUEST);
			try {
				c.await();
			} catch (InterruptedException e) {
				makeAlert("Failed to init Camera");
			}
		}
		// public AndroidRecognizer ctrlReset() {
		// try {
		// Intent intent = new Intent(
		// RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		// intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		// RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		// intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "�T���v���������"); //
		// �_�C�A���O�ɕ\������镶����
		// activity.startActivityForResult(intent, VOICE_REQUEST); // Intent���s
		// } catch (ActivityNotFoundException e) {
		// makeAlert("������͂ɑΉ����Ă��܂���B");
		// }
		// return this;
		// }
		// public AndroidRecognizer ctrlStart() throws ChainException {
		// final String b = (String)pull();
		// makeAlert(b);
		// return this;
		// }
		@Override
		public Actor ctrlStart() throws ChainException, InterruptedException {
			return this;
		}
	}

	public static class AndroidOverlayPopup extends Actor.Controllable {
		View v = null;
		PopupWindow p;
		AndroidOverlayPopup () {
			super();
			p=new PopupWindow();
			p.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
		public void setView(View v) {
			this.v = v;
			p.setContentView(v);
		}
		public void show(int x, int y) {
			if(!p.isShowing())
				p.showAtLocation(activity.findViewById(0x00001235), Gravity.NO_GRAVITY,
					x-v.getWidth()/2, y-v.getHeight()/2
					);
			else
				p.update(
						x-v.getWidth()/2, y-v.getHeight()/2,
						-1, -1);
		}
		@Override
		public Actor ctrlStart() throws ChainException, InterruptedException {
			return this;
		}
	}

	public static class AndroidButton extends AndroidView {

		// @Override
		public boolean isTouching(WorldPoint wp) {
			return false;
		}

		@Override
		public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
				int alpha) {
			return false;
		}

	}
	
	public static class AndroidNumberView extends AndroidImageView implements IPathListener {
		int __num = 1;
		public AndroidNumberView() {
			super();
			setImage(BitmapFactory.decodeResource(
					AndroidActor.getResources(), R.drawable.bubble4));
			getInPack(PackType.HEAP).setUserPathListener(this);
		}
		@Override
		public void OnPushed(Connector p, Object obj)
				throws InterruptedException {
			__num++;// = (Integer)obj;
//			validate();
		}
		
		@Override
		public void view_init() throws ChainException {
			__num = (Integer)pull();
		}
		@Override
		public boolean view_user(Canvas canvas, ScreenPoint sp, WorldPoint size,
				int alpha) {
			for(int i = 0; i < __num; i++)
				super.view_user(canvas, sp.plus(0, 50*i), size, alpha);
			return false;
		}

	}
}
