package org.tapchain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.tapchain.R;
import org.tapchain.core.Actor;
import org.tapchain.core.Actor.Controllable;
import org.tapchain.core.Actor.SimpleActor;
import org.tapchain.core.ActorChain.IView;
import org.tapchain.core.Connector;
import org.tapchain.core.Hippo;
import org.tapchain.core.IPoint;
import org.tapchain.core.IntentHandler;
import org.tapchain.core.ScreenPoint;
import org.tapchain.core.WorldPoint;
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
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.Toast;

@SuppressWarnings("serial")
public class AndroidActor {
	static TapChainView activity = null;
	private static IWindow w = null;
	static int TMP_INTENT_NUM = 130;
	static final int IMAGE_SEARCH = 123;
	static final int IMAGE_SEARCH2 = 124;

	// 1.Initialization
	// 2.Getters and setters
	public static void setActivity(TapChainView act) {
		activity = act;
	}

	public static IWindow getWindow() {
		return activity.getActorWindow();
	}

	public static Resources getResources() {
		return activity.getResources();
	}

	public static void makeAlert(final String alert) {
		activity.postMQ(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, alert, Toast.LENGTH_SHORT).show();
			}
		});

	}

	public static int intent_register(int TAG, IntentHandler h) {
		activity.addIntentHandler(TAG, h);
		return TAG;
	}

	public static int intent_register(IntentHandler h) {
		return intent_register(++TMP_INTENT_NUM, h);
	}

	public static void intent_start(Intent i, Integer TAG) {
		try {
			if(TAG == null)
				activity.startActivity(i);
			else
				activity.startActivityForResult(i, TAG);
		} catch (ActivityNotFoundException e) {
			makeAlert("Intent Error on starting");
		}
	}
	
	public static void intent(Intent i, IntentHandler h) {
		intent_start(i, intent_register(h));
	}
	
	public static void intent(Intent i) {
		intent_start(i, null);
	}

	public static Intent getRecognizerIntent(String title) {
		Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		i.putExtra(RecognizerIntent.EXTRA_PROMPT, title); // 
		return i;
	}

	// 3.Changing state
	// 4.Termination
	// 5.Local classes
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
							interrupt(ControllableSignal.END);
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
			makeAlert("");
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
			user_rect(getCenter(), getSize(), r);
			return r;
		}

		RectF rf = new RectF();

		public RectF getScreenRectF() {
			user_rectF(getCenter(), getSize(), rf);
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
					view_user(arg0, new ScreenPoint(getSize().x / 2,
							getSize().y / 2), getSize(), getAlpha());
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
		public boolean view_user(Object canvas, IPoint sp, WorldPoint size,
				int alpha, float angle) {
			boolean rtn = false;
			Canvas c = (Canvas) canvas;
			c.save();
			c.rotate(angle, sp.x(), sp.y());
			rtn = view_user(c, sp, size, alpha);
			c.restore();
			return rtn;
		}

		public boolean view_user(Canvas canvas, IPoint sp,
				WorldPoint size, int alpha) {
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

		public AndroidImageView() {
			super();
		}
		
		public AndroidImageView(Integer resource) {
			setImage(BitmapFactory.decodeResource(activity.getResources(),
					resource));
		}

		public void setImage(Bitmap b) {
			bm_base = b;
			bm_scaled = bm_base;
			setCenter(new WorldPoint(100, 100));
		}
		
		public void view_init() throws ChainException {
			int resource;
			while (bm_base == null) {
				resource = (Integer) pull();
				setImage(BitmapFactory.decodeResource(activity.getResources(),
						resource));
				break;
			}
		}
//		@Override
//		public void view_move() {
//			__log(getCenter(),"VIEW POS:");
//		}
//
		@Override
		public boolean view_user(Canvas canvas, IPoint sp,
				WorldPoint size, int alpha) {
			canvas.drawBitmap(bm_scaled, sp.x(), sp.y(), paint);
			return true;
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
			bm_base = in
					.readParcelable(AndroidImageView.class.getClassLoader());
			bm_scaled = in.readParcelable(AndroidImageView.class
					.getClassLoader());
			setCenter(new WorldPoint(in.readInt(), in.readInt()));
			setSize(new WorldPoint(in.readInt(), in.readInt()));
			setColor(in.readInt());
		}
		
		public AndroidImageView setColorFilter(float[] colorTransform) {

		    ColorMatrix colorMatrix = new ColorMatrix();
		    colorMatrix.setSaturation(0f); //Remove Colour 
		    colorMatrix.set(colorTransform); //Apply the Red

		    ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
		    paint.setColorFilter(colorFilter);
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
		public boolean view_user(Canvas canvas, IPoint sp,
				WorldPoint size, int alpha) {
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

	public static class AndroidDashRect extends AndroidView {

		public void view_init() throws ChainException {
			setSize(new WorldPoint(100, 100));
			paint.setAntiAlias(true);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(5);
			paint.setColor(0xffffffff);
		}

		@Override
		public boolean view_user(Canvas canvas, IPoint sp,
				WorldPoint size, int alpha) {
			paint.setColor(getColor());
			paint.setPathEffect(new DashPathEffect(new float[] {0.4f*size.x(),0.6f*size.y()}, 0.2f*size.x()));
			canvas.drawRect(getScreenRectF(),paint);
			// canvas.drawArc(getScreenRectF(), 180f, 360f, true, paint);
			return true;
		}

	}

	public static class AndroidWindow extends AndroidView {

		@Override
		public boolean view_user(Canvas canvas, IPoint sp,
				WorldPoint size, int alpha) {
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
			vibrator.vibrate(getVal());
			return true;
		}

	}

	public static class AndroidAlert extends SimpleActor {
		@Override
		public boolean actorRun(Actor act) throws ChainException {
			final String b = (String) pull();
			makeAlert(b);
			return true;
		}
	}

	public static class AndroidCamera extends AndroidImageView implements IntentHandler {
		public AndroidCamera() {
			super();
			disableLoop();
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
			makeAlert("No Picture");
		}
		@Override
		public void view_init() throws ChainException {
			Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent(i, this);
		}
	}

	public static class AndroidSearch extends Controllable implements IntentHandler {
		  private static final String QUERY_URL = "https://www.google.com/search?tbm=isch&q=";

		public AndroidSearch() {
			super();
			disableLoop();
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
			Toast.makeText(activity, buffer.toString(),
					Toast.LENGTH_LONG).show();
			requestImageSearch(results.get(0));
		}
		@Override
		public void ctrlStart() throws ChainException, InterruptedException {
			intent_register(IMAGE_SEARCH2, this);
			Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			i.putExtra(RecognizerIntent.EXTRA_PROMPT, "test"); // 
			intent_start(i, IMAGE_SEARCH2);
		}
		 private void requestImageSearch(String key) {
			 Uri uri = Uri.parse(QUERY_URL+key); 
			 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//			 intent.setData(uri);
			 intent(intent);
		 }
	}

	public static class AndroidImageSearch extends Controllable implements
			IntentHandler {
		private static final String QUERY_URL = "https://www.google.com/search?tbm=isch&q=";

		public AndroidImageSearch() {
			super();
			disableLoop();
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
			Toast.makeText(activity, buffer.toString(), Toast.LENGTH_LONG)
					.show();
			requestImageSearch(results.get(0));
		}

		@Override
		public void ctrlStart() throws ChainException,
				InterruptedException {
			intent_register(IMAGE_SEARCH, this);
			Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			i.putExtra(RecognizerIntent.EXTRA_PROMPT, "test"); //
			intent_start(i, IMAGE_SEARCH);
		}

		private ArrayList requestImageSearch(String key) {
			Map<String, Object> temp;
			ArrayList<Map> listData = new ArrayList<Map>();
			String encodeKey = Uri.encode(key);
			HttpClient client = new DefaultHttpClient();
			String req = QUERY_URL + encodeKey;
			Log.v("JsonTest", "query :" + req);
			HttpUriRequest httpUriReq = new HttpGet(req);
			try {
				HttpResponse res = client.execute(httpUriReq);
				if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					makeAlert("StatusCode = "
							+ res.getStatusLine().getStatusCode());
				} else {
					String entity = EntityUtils.toString(res.getEntity());
					makeAlert(entity);
				}

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return listData;
		}
	}

	public static class AndroidRecognizer extends Controllable implements IntentHandler {
		public AndroidRecognizer() {
			super();
			disableLoop();
		}

		@Override
		public void onIntent(int resultCode, Intent data) {
			if (resultCode != android.app.Activity.RESULT_OK) {
				push("");
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
			push(results.get(0));
			// show the value
			Toast.makeText(activity, results.get(0),
					Toast.LENGTH_LONG).show();
		}
		
		@Override
		public void ctrlStart() throws ChainException, InterruptedException {
			intent(getRecognizerIntent("Message:"), this);
		}
	}
	
	public static class AndroidOverlayPopup extends Actor.Controllable {
		View v = null;
		PopupWindow p;

		AndroidOverlayPopup() {
			super();
			p = new PopupWindow();
			p.setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		}

		public void setView(View v) {
			this.v = v;
			p.setContentView(v);
		}

		public void show(int x, int y) {
			if (!p.isShowing())
				p.showAtLocation(activity.findViewById(0x00001235),
						Gravity.NO_GRAVITY, x - v.getWidth() / 2,
						y - v.getHeight() / 2);
			else
				p.update(x - v.getWidth() / 2, y - v.getHeight() / 2, -1, -1);
		}

		@Override
		public void ctrlStart() throws ChainException, InterruptedException {
		}
	}

	public static class AndroidButton extends AndroidView {

		// @Override
		public boolean isTouching(WorldPoint wp) {
			return false;
		}

		@Override
		public boolean view_user(Canvas canvas, IPoint sp,
				WorldPoint size, int alpha) {
			return false;
		}

	}

	public static class AndroidNumberView extends AndroidImageView implements
			IPathListener {
		int __num = 1;

		public AndroidNumberView() {
			super();
			setImage(BitmapFactory.decodeResource(AndroidActor.getResources(),
					R.drawable.bubble4));
			getInPack(PackType.HEAP).setUserPathListener(this);
		}

		@Override
		public void OnPushed(Connector p, Object obj)
				throws InterruptedException {
			__num++;// = (Integer)obj;
			// validate();
		}

		@Override
		public void view_init() throws ChainException {
			__num = (Integer) pull();
		}

		@Override
		public boolean view_user(Canvas canvas, IPoint sp,
				WorldPoint size, int alpha) {
			for (int i = 0; i < __num; i++)
				super.view_user(canvas, sp.plus(new WorldPoint(0, 50 * i)), size, alpha);
			return false;
		}

	}
	
	public static class AndroidMail extends Controllable implements IntentHandler {
		Hippo<String> title = new Hippo<String>();
		String dest = "";
		public AndroidMail(String _dest) {
			super();
			disableLoop();
			dest = _dest;
		}
//		@Override
//		public void onAdd(Manager m) {
//			
//		}
		@Override
		public void onIntent(int resultCode, Intent data) {
			if (resultCode != android.app.Activity.RESULT_OK) {
				title.sync_push("");
				return;
			}
			// get the returned value
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			title.sync_push(results.get(0));
		}

		@Override
		public void ctrlStart() throws ChainException, InterruptedException {
			intent(getRecognizerIntent("Email title:"), this);
			Intent i = new Intent(android.content.Intent.ACTION_SENDTO)
			.setData(Uri.parse(dest))
			.putExtra(android.content.Intent.EXTRA_SUBJECT, title.sync_pop())
			.putExtra(android.content.Intent.EXTRA_TEXT, (String)AndroidMail.this.pull());
//			String aEmailList[] = { "o-sanmail@docomo.ne.jp","hiroyuki.osaki@gmail.com" };
//			i.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
//			i.putExtra(android.content.Intent.EXTRA_SUBJECT, "from Android");
//			i.putExtra(android.content.Intent.EXTRA_TEXT, "");
//			String aEmailCCList[] = { "user3@fakehost.com","user4@fakehost.com"};
//			String aEmailBCCList[] = { "user5@fakehost.com" };
//			i.putExtra(android.content.Intent.EXTRA_CC, aEmailCCList);
//			i.putExtra(android.content.Intent.EXTRA_BCC, aEmailBCCList);
			intent(i);

		}
	}
	
	public static class AndroidMail2 extends Controllable {
		public AndroidMail2() {
			super();
			disableLoop();
		}

		@Override
		public void ctrlStart() throws ChainException, InterruptedException {
			Intent i = new Intent(android.content.Intent.ACTION_SEND);
			i.setType("plain/text");
			String aEmailList[] = { "o-sanmail@docomo.ne.jp","hiroyuki.osaki@gmail.com" };
			i.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
			i.putExtra(android.content.Intent.EXTRA_SUBJECT, "from Android");
			i.putExtra(android.content.Intent.EXTRA_TEXT, "test mail from android, notifying a train time");
			intent(i);
		}
	}
	
}
