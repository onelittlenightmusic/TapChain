package org.tapchain;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.tapchain.editor.ColorLib.ColorCode;
import org.tapchain.core.Actor;
import org.tapchain.core.ChainException;
import org.tapchain.core.D2Point;
import org.tapchain.core.IBoostable;
import org.tapchain.core.ICommit;
import org.tapchain.core.IPoint;
import org.tapchain.core.IStep;
import org.tapchain.core.IValue;
import org.tapchain.core.PhysicalPoint;
import org.tapchain.core.Self;
import org.tapchain.core.WorldPoint;
import org.tapchain.core.actors.ViewActor;
import org.tapchain.realworld.IIntentHandler;
import org.tapchain.realworld.OverlayPopupView;
import org.tapchain.viewlib.DrawLib;
import org.tapchain.viewlib.IShapeBoundary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("serial")
public class AndroidActor {
	static int TMP_INTENT_NUM = 130;
	static final int IMAGE_SEARCH = 123;
	static final int IMAGE_SEARCH2 = 124;

	public static void makeAlert(final Activity act, final String alert) {
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(act, alert, Toast.LENGTH_SHORT).show();
			}
		});

	}

	public static int intent_register(Activity activity, int TAG, IntentHandler h) {
        if(activity instanceof IIntentHandler)
            ((IIntentHandler) activity).addIntentHandler(TAG, h);
		return TAG;
	}

	public static int intent_register(Activity activity, IntentHandler h) {
		return intent_register(activity, ++TMP_INTENT_NUM, h);
	}

	public static void intent_start(Activity act, Intent i, Integer TAG) throws ChainException {
		try {
			if (TAG == null)
				act.startActivity(i);
			else
				act.startActivityForResult(i, TAG);
		} catch (ActivityNotFoundException e) {
			throw new ChainException("Intent Error on starting");
		}
	}

	public static void intent(Activity act, Intent i, IntentHandler h) throws ChainException {
		intent_start(act, i, intent_register(act, h));
	}

	public static void intent(Activity act, Intent i) throws ChainException {
		intent_start(act, i, null);
	}

	public static Intent getRecognizerIntent(String title) {
		Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		i.putExtra(RecognizerIntent.EXTRA_PROMPT, title); //
		return i;
	}

	private static TextToSpeech mTts;
	public static TextToSpeech initTTS(Activity activity) {
		if (mTts == null && activity != null)
			mTts = new TextToSpeech(activity,
					new TextToSpeech.OnInitListener() {
						@Override
						public void onInit(int status) {
							if (status == TextToSpeech.SUCCESS) {
								if (mTts.isLanguageAvailable(Locale.US) >= TextToSpeech.LANG_AVAILABLE) {
									mTts.setLanguage(Locale.US);
								}
							}
						}
					});
		return mTts;
	}

	public static void onTTSDestroy() {
		if(mTts != null) {
			mTts.shutdown();
		}
		mTts = null;
	}

    public interface IAndroidActivityOwner {
        public Activity getOwnActivity();
        public void setOwnActivity(Activity activity);
    }

    public static class AndroidControllable<VALUE, INPUT, OUTPUT, PARENT> extends Actor.Controllable<VALUE, INPUT, OUTPUT, PARENT>
        implements IAndroidActivityOwner {
        Activity act = null;
        public AndroidControllable() {
            super();
        }

        public AndroidControllable(Activity _act) throws ChainException {
            this();
            if (_act == null)
                throw new ChainException(this, "Cannot initialize: Acitivity is null");
            act = _act;
        }

        public Activity getOwnActivity() {
            return act;
        }

        @Override
        public void setOwnActivity(Activity activity) {
            act = activity;
        }
    }
	public static class AndroidSound extends Actor.Sound {
        private Activity act;
		MediaPlayer localplay = null;

		AndroidSound(Activity activity, String path) {
			super();
            act = activity;
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
			localplay = MediaPlayer.create(act, resource);
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
						interrupt(ControllableSignal.END);
					}
				});
			return false;
		}

		@Override
		public boolean reset_async_impl() {
			localplay.setOnSeekCompleteListener(new OnSeekCompleteListener() {
				@Override
				public void onSeekComplete(MediaPlayer mp) {
					interrupt(ControllableSignal.END);
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
		static SoundPool localplay = new SoundPool(10,
				AudioManager.STREAM_MUSIC, 0);
		int id = 0;
		float rate = 1.0f;
		int resource = 0;
        Activity act;


		AndroidSound2(Activity activity, String path) {
			super();
            act = activity;
			id = localplay.load(path, 1);
		}

		AndroidSound2(Activity activity, int resource) {
			super();
            act = activity;
			this.resource = resource;
		}

		@Override
		public boolean play_impl() {
			localplay.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId,
						int status) {
					localplay.play(id, 100, 100, 1, 0, rate);
					interrupt(ControllableSignal.END);
				}
			});
			return false;
		}

		@Override
		public boolean stop_impl() {
			localplay.unload(id);
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
			id = localplay.load(act, resource, 1);
			return false;
		}
	}

	public static class AndroidRecorder extends Actor.Recorder {
		final MediaRecorder recorder = new MediaRecorder();
		final String path;
        Activity act;

		@SuppressLint("SdCardPath")
		public AndroidRecorder(Activity activity) {
			super();
            act = activity;
			path = "/sdcard/com.bambooflower.test.multitouch2/test.3gp";
		}

		@Override
		public boolean record_start() throws ChainException {
			// make sure the directory we plan to store the recording _in exists
			File directory = new File(path).getParentFile();
			if (!directory.exists() && !directory.mkdirs()) {
				throw new ChainException(this,
						"Path to file could not be created.");
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
			makeAlert(act, "");
			return false;
		}

		@Override
		public boolean record_stop() {
			recorder.stop();
			recorder.release();
			return false;
		}
	}

	public static class AndroidView extends ViewActor implements
			Parcelable, IViewAndroidUser, IShapeBoundary, IAndroidActivityOwner {
		private Paint paint = new Paint();
		IShapeBoundary bound;
        private Activity act = null;

        public AndroidView() {
            super();
            __setPointObject(new PhysicalPoint());
            setShapeBoundary(this);
        }

        AndroidView(Activity _act) {
            this();
            act = _act;
            __setPointObject(new PhysicalPoint());
            setShapeBoundary(this);
        }

		Rect r = new Rect();

		public Rect getScreenRect(IPoint cp) {
			user_rect(cp, getRawSize(), r);
			return r;
		}

		public Rect getScreenRect() {
			return getScreenRect(getCenter());
		}

		RectF rf = new RectF();

		public RectF getScreenRectF() {
			user_rectF(getCenter(), getRawSize(), rf);
			return rf;
		}

		Rect rw = new Rect();

		public Rect getWorldRect() {
			user_rect(getCenter(), getRawSize(), rw);
			return rw;
		}
		
		@Override
		public boolean contains(IPoint iPoint, IPoint... ps) {
			return getWorldRect().contains((int) iPoint.x(), (int)iPoint.y());
		}
		
		public boolean contains(IPoint iPoint) {
			return bound.contains(iPoint, getCenter(), getSize()._get());
		}
		
		public void setShapeBoundary(IShapeBoundary sp) {
			bound = sp;
		}
		public void user_rect(IPoint center, IPoint iPoint, Rect r) {
			if(center != null) {
				int halfx = (int) (iPoint.x() / 2), halfy = (int) (iPoint.y() / 2);
				r.set((int) center.x() - halfx, (int) center.y() - halfy,
					(int) center.x() + halfx, (int) center.y() + halfy);
			} else {
				r.set(0, 0, (int) iPoint.x(), (int) iPoint.y());
			}
		}

		public void user_rectF(IPoint center, IPoint iPoint, RectF rf) {
			float halfx = iPoint.x() / 2, halfy = iPoint.y() / 2;
			rf.set(center.x() - halfx, center.y() - halfy, center.x() + halfx,
					center.y() + halfy);
		}

		private Drawable drawable =  new Drawable() {
			@Override
			public void draw(Canvas arg0) {
				view_user(arg0, getRawSize().multiplyNew(0.5f),
						getRawSize(), AndroidView.this.getAlpha());
			}

			@Override
			public int getOpacity() {
				return PixelFormat.TRANSLUCENT;
			}

			@Override
			public void setAlpha(int alpha) {
			}

			@Override
			public void setColorFilter(ColorFilter cf) {
			}

			@Override
			public int getIntrinsicWidth() {
				return (int) getRawSize().x();
			}

			@Override
			public int getIntrinsicHeight() {
				return (int) getRawSize().y();
			}
		};

		public Drawable getDrawable() {
			return drawable;
		}

		@Override
		public boolean view_user(Object canvas, IPoint sp, IPoint size,
				int alpha, float angle) {
			boolean rtn = false;
			Canvas c = (Canvas) canvas;
			c.save();
			c.rotate(angle, sp.x(), sp.y());
			rtn = view_user(c, sp, size, alpha);
			c.restore();
			return rtn;
		}

		@Override
		public boolean view_user(Canvas canvas, IPoint sp, IPoint iPoint,
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

		public Paint getPaint() {
			return paint;
		}

		public void setPaint(Paint paint) {
			this.paint = paint;
		}

		public AndroidView setColorCode(ColorCode colorCode) {
			getPaint().setColorFilter(AndroidColorCode.getColorMatrix(colorCode));
			return this;
		}

        @Override
        public Activity getOwnActivity() {
            return act;
        }

        @Override
        public void setOwnActivity(Activity activity) {
            act = activity;
        }
    }

	public interface IViewAndroidUser {
		public boolean view_user(Canvas canvas, IPoint sp, IPoint size,
				int alpha);
	}

	public static class AndroidImageView extends AndroidView {
		Bitmap bm_base = null;
		protected Bitmap bm_scaled = null;
		Matrix matrix = new Matrix();


        public AndroidImageView() {
            super();
        }

		public AndroidImageView(Activity activity) {
			super(activity);
		}

		public AndroidImageView(Activity activity, Integer resource) {
			super(activity);
            setImage(resource);
		}

		public void setImage(Integer resource) {
            setImage(BitmapFactory.decodeResource(getOwnActivity().getResources(),
                    resource));
		}

		protected void setImage(Bitmap b) {
			bm_base = b;
			setSize(null/* new WorldPoint(300f, 300f) */);
		}

		public void view_init() throws ChainException {
		}

		@Override
		public boolean view_user(Canvas canvas, IPoint sp, IPoint size,
				int alpha) {
			DrawLib.drawBitmapCenter(canvas, bm_scaled, sp, getPaint());
			return true;
		}

		public AndroidImageView setSize(IPoint size) {
			if (size == null) {
				if (bm_base != null)
					size = new WorldPoint(bm_base.getWidth(),
							bm_base.getHeight());
				else
					return this;
			}
			super.setSize(size);
			IPoint s = getSize()._get();
			bm_scaled = Bitmap.createScaledBitmap(bm_base, (int) s.x(),
					(int) s.y(), true);
			return this;
		}

		public AndroidImageView setPercent(WorldPoint percent) {
			super.setPercent(percent);
			matrix.postScale(((float) percent.x) / 100f,
					((float) percent.y) / 100f);
			bm_scaled = Bitmap.createBitmap(bm_base, 0, 0, bm_base.getWidth(),
					bm_base.getHeight(), matrix, true);
			return this;
		}

		@Override
		public void writeToParcel(Parcel arg0, int arg1) {
			arg0.writeParcelable(bm_base, 0);
			arg0.writeParcelable(bm_scaled, 0);
			arg0.writeInt((int) getCenter().x());
			arg0.writeInt((int) getCenter().y());
			arg0.writeInt((int) getRawSize().x());
			arg0.writeInt((int) getRawSize().y());
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
			bm_base = in
					.readParcelable(AndroidImageView.class.getClassLoader());
			bm_scaled = in.readParcelable(AndroidImageView.class
					.getClassLoader());
			setCenter(new WorldPoint(in.readInt(), in.readInt()));
			setSize(new WorldPoint(in.readInt(), in.readInt()));
			setColor(in.readInt());
		}

        @Override
        protected void removeViewFromAnimation() {
            super.removeViewFromAnimation();
            bm_scaled.recycle();
        }
    }

	public static class AndroidImageMovable extends AndroidImageView implements
			IBoostable {
		Bitmap bm_move = null, bm_stop = null;
		boolean moved = false;
		float vlimit = 0f;

		public AndroidImageMovable() {
			super();
			__setPointObject(new PhysicalPoint());
		}

		public AndroidImageMovable(Activity activity, Integer i, Integer j) {
			super(activity, i);
			bm_stop = bm_base;
			bm_move = BitmapFactory.decodeResource(getOwnActivity().getResources(), j);
		}

		@Override
		public void move_user(IPoint vp) {
			// __log(getCenter(),"VIEW POS:");
			if (!moved) {
				setImage(bm_move);
				moved = true;
			}
			setAngle(vp.theta());
		}

		@Override
		public boolean _set(IPoint d2p) {
			IPoint pos = d2p;
			if(d2p instanceof D2Point) {
				IPoint vp = ((D2Point) d2p).getVector();
				if (vlimit <= 0)
					super._set(vp);
				else {
					IPoint p = pos.subNew(_get()).setDif();
					if (vlimit < p.getAbs())
						super._set(p.ein().multiply(vlimit));
					else
						super._set(p);
				}
			} else {
				super._set(d2p);
			}
			return true;

		}

		@Override
		public void boost(float f) {
			vlimit = f;
		}

	}

	public static class AndroidCircle extends AndroidView {
		float start = 0f, end = 360f;

		public void view_init() throws ChainException {
			setSize(new WorldPoint(100f, 100f));
			getPaint().setAntiAlias(true);
			getPaint().setStyle(Style.STROKE);
		}

		@Override
		public boolean view_user(Canvas canvas, IPoint sp, IPoint size,
				int alpha) {
			getPaint().setColor(getColor());
			canvas.drawArc(getScreenRectF(), start, end, true, getPaint());
			// canvas.drawArc(getScreenRectF(), 180f, 360f, true, paint);
			return true;
		}

		public AndroidCircle setCircleAngle(float start, float end) {
			this.start = start;
			this.end = end;
			return this;
		}

	}

	public static class AndroidDashRect extends AndroidView {

		public void view_init() throws ChainException {
			getPaint().setAntiAlias(true);
			getPaint().setStyle(Style.STROKE);
			getPaint().setStrokeWidth(5);
			getPaint().setColor(0xffffffff);
		}

		@Override
		public boolean view_user(Canvas canvas, IPoint sp, IPoint size,
				int alpha) {
			getPaint().setColor(getColor());
			getPaint().setPathEffect(
					new DashPathEffect(new float[] { 0.4f * size.x(),
							0.6f * size.y() }, 0.2f * size.x()));
			canvas.drawRect(getScreenRectF(), getPaint());
			return true;
		}

	}

	public static class AndroidWindow extends AndroidView {

		@Override
		public boolean view_user(Canvas canvas, IPoint sp, IPoint size,
				int alpha) {
			return false;
		}

	}

	public static class AndroidWindowSurfaceView extends AndroidWindow {

	}

	public static class AndroidQuaker extends Actor.BasicQuaker {
        Activity act = null;
		public AndroidQuaker(Activity activity, int interval) {
			super(interval);
            act = activity;
		}

		public boolean quake_impl() {
			Vibrator vibrator = (Vibrator) act
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(getVal());
			return true;
		}

	}

	public static class AndroidAlert extends AndroidControllable<Self, String, Void, Void> {

        String alert = null;

		public AndroidAlert() {
			super();
			// setPullClass(String.class);
		}

        public AndroidAlert(Activity act, String _alert) throws ChainException {
            super(act);
            alert = _alert;
        }

		@Override
		public boolean actorRun(Actor act) throws ChainException {
			String b = alert;
            if(b == null)
                b = pull();
            L("makeAlert()").go(b);
			makeAlert(getOwnActivity(), b);
			return false;
		}
	}

	public static class AndroidCamera extends AndroidImageView implements
			IntentHandler {
		public AndroidCamera() {
			super();
			once();
		}

		@Override
		public void onIntent(int resultCode, Intent data) {
			if (resultCode != android.app.Activity.RESULT_OK) {
				error();
				return;
			}
			Bundle extras = data.getExtras();
			if (extras == null) {
				error();
			}
			Bitmap bitmap = (Bitmap) extras.get("data");
			if ((bitmap == null)) {
				error();
			}
			setImage(bitmap);
			try {
				super.view_init();
			} catch (ChainException e) {
				error();
			}
		}

		public void error() {
			makeAlert(getOwnActivity(), "No Picture");
		}

		@Override
		public void view_init() throws ChainException {
			Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent(getOwnActivity(), i, this);
		}
	}

	public static class AndroidSearch extends AndroidControllable implements
			IntentHandler {
		private static final String QUERY_URL = "https://www.google.com/search?tbm=isch&q=";

		public AndroidSearch() {
			super();
			once();
		}

        public AndroidSearch(Activity _act) throws ChainException {
            super(_act);
            once();
        }
		@Override
		public void onIntent(int resultCode, Intent data) throws ChainException {
			if (resultCode != android.app.Activity.RESULT_OK)
				return;
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			// refer the value
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < results.size(); i++) {
				buffer.append(results.get(i));
			}
			// show the value
			Toast.makeText(getOwnActivity(), buffer.toString(), Toast.LENGTH_LONG)
					.show();
			requestImageSearch(results.get(0));
		}

		@Override
		public void ctrlStart() throws ChainException, InterruptedException {
			intent_register(getOwnActivity(),IMAGE_SEARCH2, this);
			Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			i.putExtra(RecognizerIntent.EXTRA_PROMPT, "test"); //
			intent_start(getOwnActivity(), i, IMAGE_SEARCH2);
		}

		private void requestImageSearch(String key) throws ChainException {
			Uri uri = Uri.parse(QUERY_URL + key);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent(getOwnActivity(), intent);
		}
	}

	public static class AndroidImageSearch extends AndroidControllable implements
			IntentHandler {
		private static final String QUERY_URL = "https://www.google.com/search?tbm=isch&q=";

		public AndroidImageSearch() {
			super();
			once();
		}

		@Override
		public void onIntent(int resultCode, Intent data) {
			if (resultCode != android.app.Activity.RESULT_OK)
				return;
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			// refer the value
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < results.size(); i++) {
				buffer.append(results.get(i));
			}
			// show the value
			Toast.makeText(getOwnActivity(), buffer.toString(), Toast.LENGTH_LONG)
					.show();
			requestImageSearch(results.get(0));
		}

		@Override
		public void ctrlStart() throws ChainException, InterruptedException {
			intent_register(getOwnActivity(), IMAGE_SEARCH, this);
			Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			i.putExtra(RecognizerIntent.EXTRA_PROMPT, "test"); //
			intent_start(getOwnActivity(), i, IMAGE_SEARCH);
		}

		private ArrayList<Map<String, Object>> requestImageSearch(String key) {
			// Map<String, Object> temp;
			ArrayList<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
			String encodeKey = Uri.encode(key);
//			HttpClient client = new DefaultHttpClient();
//			String req = QUERY_URL + encodeKey;
//			// Log.w("JsonTest", "query :" + req);
//			HttpUriRequest httpUriReq = new HttpGet(req);
//			try {
//				HttpResponse res = client.execute(httpUriReq);
//				if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//					makeAlert(getOwnActivity(), "StatusCode = "
//							+ res.getStatusLine().getStatusCode());
//				} else {
//					String entity = EntityUtils.toString(res.getEntity());
//					makeAlert(getOwnActivity(), entity);
//				}
//
//			} catch (ClientProtocolException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
			return listData;
		}
	}

	public static class AndroidRecognizer extends AndroidControllable<Self, Void, String, Void> implements
			IntentHandler, IStep, IValue<String> {
		String text = "";

        public AndroidRecognizer() {
            super();
            // once();
            unsetAutoEnd();
        }
        public AndroidRecognizer(Activity act) throws ChainException {
            super(act);
            unsetAutoEnd();
			setAutoStart();
			setLoop(null);
        }

        @Override
		public void onIntent(int resultCode, Intent data) {
			if (resultCode != android.app.Activity.RESULT_OK) {
				_set("");
				return;
			}
			// get the returned value
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			// refer the value
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < results.size(); i++) {
				buffer.append(results.get(i));
			}
			_set(results.get(0));
			push(text);
		}

		@Override
		public void ctrlStart() throws ChainException, InterruptedException {
			intent(getOwnActivity(), getRecognizerIntent("Speak!!"), this);
		}

		@Override
		public boolean _set(String value) {
			text = value;
			return true;
		}

		@Override
		public String _get() {
			return text;
		}

		@Override
		public void onStep() {
			interruptStep();
		}

	}

	public static class AndroidButton extends AndroidView {

		// @Override
		public boolean isTouching(WorldPoint wp) {
			return false;
		}

		@Override
		public boolean view_user(Canvas canvas, IPoint sp, IPoint size,
				int alpha) {
			return false;
		}

	}


	public static class AndroidMail extends AndroidControllable<Self, String, Void, Void> {
		String dest = "";
		
		public AndroidMail() {
			super();
		}

		public AndroidMail(Activity act, String _dest) throws ChainException {
			super(act);
			setAutoStart();
			setAutoEnd();
			dest = _dest;
		}

		@Override
		public void ctrlStart() throws ChainException, InterruptedException {
			Intent i = new Intent(android.content.Intent.ACTION_SENDTO)
					.setData(Uri.parse(dest))
					.putExtra(android.content.Intent.EXTRA_SUBJECT, "")
					.putExtra(android.content.Intent.EXTRA_TEXT, pull());
			intent(getOwnActivity(), i);
		}
	}

	public static class AndroidOverlay extends AndroidView {
		OverlayPopupView p;
		View v;

		public AndroidOverlay(Activity activity) {
			super(activity);
			v = createObjectByUIThread();
			p = new OverlayPopupView(getOwnActivity());
		}

		protected View createObjectByUIThread() {
			return new ImageView(getOwnActivity());
		}

		protected View getObjectByUIThread() {
			return v;
		}

		@Override
		protected void addViewToAnimation() {
			p.setFocusable(true);
			p.setPopupView(getFace());
			move();
		}

		@Override
		protected void removeViewFromAnimation() {
			getOwnActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    p.dismiss();
                }
            });
		}

		protected View getFace() {
			ImageView v = (ImageView) getObjectByUIThread();
			Drawable d = this.getDrawable();
			// d.setBounds(100, 100, 200, 200);
			v.setImageDrawable(d);
			v.setFocusable(true);
			v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(final View v, final boolean hasFocus) {
					AndroidOverlay.this.interruptEnd();
				}
			});
			v.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					AndroidOverlay.this.interruptEnd();
					return true;
				}
			});
			return v;
		}

		public void move() {
            p.showMiddle();
		}
	}

	public static class AndroidTextInput extends AndroidOverlay {
		IValue<String> text;

		public AndroidTextInput(Activity activity, IValue<String> s) {
			super(activity);
			text = s;
		}

		@Override
		protected View createObjectByUIThread() {
			final EditText e = new EditText(getOwnActivity());
            e.setBackgroundColor(0xff333333);
            e.setWidth(200);
            e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(final View v, final boolean hasFocus) {

                    InputMethodManager inputMethodManager = (InputMethodManager) getOwnActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (hasFocus) {
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        // inputMethodManager.showSoftInputFromInputMethod(v.getWindowToken(),
                        // WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    } else {
                        inputMethodManager.hideSoftInputFromWindow(
                                v.getWindowToken(), 0);
                        text._set(e.getText().toString());
                        e.setText(text._get());
                        if (text instanceof ICommit)
                            ((ICommit) text)._commit();
                        AndroidTextInput.this.interruptEnd();
                    }
                }
            });
            return e;
        }

		@Override
		protected View getFace() {
			EditText e = (EditText) getObjectByUIThread();
			p.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			p.setFocusable(true);
			e.setFocusable(true);
			return e;
		}

	}

//	public static class AndroidImageOverlay extends AndroidImageView {
//		OverlayPopup p;
//
//		public AndroidImageOverlay(Activity activity, Integer i) {
//			super(activity, i);
//			// p.setFocusable(true);
//			// p.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//		}
//
//		@Override
//		protected void addViewToAnimation() {
//			p = new OverlayPopup(getOwnActivity());
//			p.setPopupView(getFace());
//			final IPoint screenp = ((TapChainView) getOwnActivity()).getCanvas()
//					.getScreenPosition(getCenter().x(), getCenter().y());
//			move(screenp);
//		}
//
//		@Override
//		protected void removeViewFromAnimation() {
//			getOwnActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    p.dismiss();
//                }
//            });
//		}
//
//		public View getFace() {
//			ImageView v = new ImageView(getOwnActivity());
//			v.setImageBitmap(bm_scaled);
//			return v;
//		}
//
//		@Override
//		public void move_user(IPoint vp) {
//			IPoint point = ((TapChainView) getOwnActivity()).getCanvas()
//					.getScreenPosition(getCenter().x(), getCenter().y());
//			move(point);
//		}
//

	public static class AndroidTTS extends AndroidControllable<Self, Void, Void, Void> implements
			IntentHandler {
		private String text;

		public AndroidTTS(Activity activity, String t) throws ChainException {
			super(activity);
			initTTS(activity);
			setAutoEnd();
			setAutoStart();
			once();
			text = t;
		}

		@Override
		public void ctrlStart() throws ChainException, InterruptedException {
			mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}


		@Override
		public void onIntent(int resultCode, Intent data) throws ChainException {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
			} else {
				Intent install = new Intent();
				install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				intent(getOwnActivity(), install);
			}
		}
	}

	public static void onDestroy() {
		onTTSDestroy();
	}
	
}
