package org.tapchain;

import java.util.ArrayList;
import java.util.HashMap;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.core.Factory;
import org.tapchain.core.IntentHandler;
import org.tapchain.core.ScreenPoint;
import org.tapchain.core.TapChainEdit;
import org.tapchain.core.WorldPoint;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.TapChainEdit.*;

import org.tapchain.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class TapChainView extends Activity implements SensorEventListener,
		IWindowCallback {
	static final boolean DEBUG = true;

	WritingView viewEditing;
	FrameLayout viewControl = null;
	SensorManager sensorManager;
	MediaPlayer player = null;
	private Sensor accelerometer;
	FrameLayout rootview = null;
	Handler mq = new Handler();
	FrameLayout frameLayout;
	TapChainEdit editor = new TapChainAndroidEdit();
	static final String VIEW_SELECT = "SELECT";

	static final String X = "LOCATIONX", Y = "LOCATIONY", V = "VIEWS";
	static final RectF rf = new RectF(0, 0, 100, 100);

	//1.Initialization
	@Override
	public void onSaveInstanceState(Bundle out) {
		RectF r = new RectF(rf);
		viewEditing.matrix.mapRect(r);
		out.putParcelable(X, r);
		out.putParcelableArray(V,
				editor.dictPiece.values().toArray(new Parcelable[0]));
	}

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if(savedInstanceState != null) {
		// view_edit.setXY(savedInstanceState.getInt(X),
		// savedInstanceState.getInt(Y));
		// for(Parcelable p : savedInstanceState.getParcelableArray(V)) {
		// Log.w("TapChain", String.format("%s => %s",
		// ((AndroidView)p).getName(),editor.dictionary.containsValue(p)?"OK":"NG"));
		// }
		// }
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Writing Window Initialization
		AndroidActor.setActivity(this);
		viewEditing = new WritingView(this, this);
		if (savedInstanceState != null) {
			viewEditing.matrix.setRectToRect(rf,
					(RectF) savedInstanceState.getParcelable(X),
					Matrix.ScaleToFit.FILL);
			viewEditing.matrix.invert(viewEditing.inverse);
			Parcelable[] p = savedInstanceState.getParcelableArray(V);
			int i = 0;
			for (IPieceView v : editor.dictPiece.values()) {
				v.setCenter(((AndroidView) p[i++]).getCenter());
			}
		}
		AndroidActor.setWindow(viewEditing);
		editor.setWindow(viewEditing);
		// Initialization of PieceFactory
		editor.setModelAction(this);

		rootview = new FrameLayout(this);
		frameLayout = new FrameLayout(this);
		setContentView(frameLayout);
		viewControl = new FrameLayout(this);
		LinearLayout view_bottom_left = new LinearLayout(this);
		view_bottom_left.setGravity(Gravity.LEFT | Gravity.BOTTOM);
		Button button_start = new Button(this);
		Button button_up = new Button(this);
		Button button_clear = new Button(this);
		Button button_freeze = new Button(this);
		Button button_refresh = new Button(this);
		button_start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewEditing.PressButton();
			}
		});
		button_start.setText(">");
		button_up.setText("-");
		button_up.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editor.Mode(EditMode.REMOVE);
			}
		});
		button_clear.setText("0");
		button_clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editor.reset();
			}
		});
		button_freeze.setText("!");
		button_freeze.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editor.freezeToggle();
			}
		});
		button_refresh.setText("R");
		button_refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editor.Mode(EditMode.RENEW);
			}
		});
		view_bottom_left.addView(button_start, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		view_bottom_left.addView(button_up, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		view_bottom_left.addView(button_clear, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		view_bottom_left.addView(button_freeze, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		view_bottom_left.addView(button_refresh, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		viewControl.addView(view_bottom_left, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		LinearLayout view_bottom_right = new LinearLayout(this);
		view_bottom_right.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
		Button button_plus = new Button(this);
		button_plus.setText("[  +  ]");
		button_plus.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
		button_plus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GridFragment f = (GridFragment) getFragmentManager()
						.findFragmentByTag(VIEW_SELECT);
				if (f != null) {
					f.toggle();
				}
			}
		});
		view_bottom_left.addView(button_plus);
		Button button_finish = new Button(this);
		button_finish.setText("x");
		button_finish.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		view_bottom_right.addView(button_finish, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		viewControl.addView(view_bottom_right, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		rootview.addView(viewEditing, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		// cam = new CameraFragment(this);

		frameLayout.addView(rootview);
		frameLayout.addView(viewControl, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		frameLayout.setId(0x00001235);
		frameLayout.setTag("OVERLAY");

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		player = MediaPlayer.create(this, R.raw.tennisball);

		LinearLayout l = new LinearLayout(this);
		rootview.addView(l);
		l.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
		l.setId(0x00001234);
		new GridFragment().setContext(this).show(GridShow.HIDE);
		Log.i("TapChainView.state", "onCreate");

	}

	//2.Getters and setters
	@Override
	public boolean redraw(String str) {
		viewEditing.onDraw();
		return true;
	}
	
	public void postMQ(Runnable r) {
		mq.post(r);
	}

	//5.Local classes
	public enum GridShow {
		SHOW, HIDE, HALF
	}

	public static class GridFragment extends Fragment {
		String tag = VIEW_SELECT;
		GridShow show = GridShow.HIDE;
		int _width = LayoutParams.FILL_PARENT,
				_height = LayoutParams.FILL_PARENT;
		boolean autohide = false;
		TapChainView a = null;

		public GridFragment() {
			super();
		}

		public GridFragment setContext(TapChainView a) {
			Log.i("TapChain", "GridFragment#setContext called");
			this.a = a;
			return this;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle saved) {
			Log.i("TapChain", "GridFragment#onCreateView called");
			ActorSelector sel = new ActorSelector(a);
			ViewAdapter adapter = new ViewAdapter();
			for (int j = 0; j < a.editor.getFactory().getSize() + 1; j++)
				adapter.add(new ActorButton(a, j));
			sel.setAdapter(adapter);
			sel.setLayoutParams(new LinearLayout.LayoutParams(_width, _height));
			return sel;
		}

		public void setSize(int w, int h) {
			Log.i("TapChain", "GridFragment#setSize called");
			_width = w;
			_height = h;
			getView().setLayoutParams(
					new LinearLayout.LayoutParams(_width, _height));

		}

		public void show(GridShow _show) {
			Log.i("TapChain", "GridFragment#show called");
			show = _show;
			FragmentTransaction ft = a.getFragmentManager().beginTransaction();
			if (this != a.getFragmentManager().findFragmentByTag(tag)) {
				Log.w("TapChain", "GridFragment#show fragment replaced");
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.replace(0x00001234, this, tag);
			}
			switch (_show) {
			case SHOW:
				setSize(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				ft.show(this);
				break;
			case HALF:
				Pair<Integer, Integer> p1 = a.checkDisplayAndRotate();
				setSize(p1.first, p1.second);
				ft.show(this);
				break;
			case HIDE:
				ft.hide(this);
			}
			ft.commit();
		}

		public void toggle() {
			show((show == GridShow.HIDE) ? GridShow.HALF : GridShow.HIDE);
		}

		public void setAutohide() {
			autohide = !autohide;
		}

		public void kickAutohide() {
			if (autohide)
				show(GridShow.HIDE);
		}
	}

	public static class ActorSelector extends GridView {
		ActorSelector(final Activity act) {
			super(act);
			setBackgroundColor(0xaa000000);
			setColumnWidth(100);
			setVerticalSpacing(0);
			setHorizontalSpacing(0);
			setNumColumns(GridView.AUTO_FIT);
			setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int action = event.getAction();
					switch (action) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_POINTER_DOWN:
					default:
						GridFragment f = (GridFragment) act
								.getFragmentManager().findFragmentByTag(
										VIEW_SELECT);
						if (f != null)
							f.show(GridShow.HIDE);
					}
					return false;
				}
			});
		}

	}

	public static class ViewAdapter extends BaseAdapter {
		private ArrayList<View> array;

		public ViewAdapter() {
			array = new ArrayList<View>();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				return array.get(position);
			}
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			return array.size();
		}

		public boolean add(View a) {
			return array.add(a);
		}
	}

	static OverlayPopup p = null;

	public static class ActorButton extends PieceImage {
		ActorButton(final Context c, final int j) {
			super(c, j);
			if (p == null)
				p = new OverlayPopup(j);
			setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int action = event.getAction();
					Log.w("Action", String.format("action = %d", action));
					switch (action) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_POINTER_DOWN:
						getParent().requestDisallowInterceptTouchEvent(true);
						p.setView(c, j);
						GridFragment f = (GridFragment) ((Activity) c)
								.getFragmentManager().findFragmentByTag(
										VIEW_SELECT);
						if (f != null)
							f.kickAutohide();
						p.show((int) event.getRawX(), (int) event.getRawY());
						// LocalButton.this.clearFocus();
						break;
					case MotionEvent.ACTION_MOVE:
						// p.getContentView().dispatchTouchEvent(event);
						p.show((int) event.getRawX(), (int) event.getRawY());
						break;
					case MotionEvent.ACTION_POINTER_UP:
					case MotionEvent.ACTION_UP:
						((TapChainView) c).viewEditing.sendDownEvent(
								event.getRawX(), event.getRawY());
						((TapChainView) c).setCode(j);
						((TapChainView) c).viewEditing.sendUpEvent();
						p.dismiss();
					case MotionEvent.ACTION_CANCEL:
						break;
					}
					return true;
				}
			});

		}
	}

	public static class OverlayPopup extends PopupWindow {
		int halfw, halfh;
		View v = null;
		Context cxt = null;

		OverlayPopup(int j) {
			super();
			// setView(c, j);
			setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		}

		public void setView(Context c, int i) {
			v = new PieceImage(c, i);
			cxt = c;
			setContentView(v);
		}

		public void show(int x, int y) {
			if (!isShowing())
				showAtLocation(((Activity) cxt).findViewById(0x00001235),
						Gravity.NO_GRAVITY, x - v.getWidth() / 2,
						y - v.getHeight() / 2);
			else
				update(x - v.getWidth() / 2, y - v.getHeight() / 2, -1, -1);
		}
	}

	public static class PieceImage extends ImageView {
		PieceImage(Context c, final int j) {
			super(c);
			Factory f = ((TapChainView) c).editor.getFactory();
			AndroidView v = null;
			try {
				v = (AndroidView) f.createView(j).newInstance(null);
			} catch (ChainException e) {
				e.printStackTrace();
			}
			Drawable a = (v != null) ? v.getDrawable() : getResources()
					.getDrawable(R.drawable.cancel);
			setImageDrawable(a);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i("TapChainView.state", "onStop");
		if (sensorManager != null)
			sensorManager.unregisterListener(this);
		editor.onDownClear();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public Pair<Integer, Integer> checkDisplayAndRotate() {
		DisplayMetrics metrix = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrix);
		if (metrix.widthPixels > metrix.heightPixels)
			return new Pair<Integer, Integer>(metrix.widthPixels / 2,
					LayoutParams.FILL_PARENT);
		return new Pair<Integer, Integer>(LayoutParams.FILL_PARENT,
				metrix.heightPixels / 2);
	}

	public void setDisplayEditor(GridFragment f, boolean visible) {
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("TapChainView.state", "onResume");
		try {
			accelerometer = sensorManager.getSensorList(
					Sensor.TYPE_ACCELEROMETER).get(0);
			sensorManager.registerListener(this, accelerometer,
					SensorManager.SENSOR_DELAY_FASTEST);
			Log.i("DEBUG",
					String.format(
							"registered %d",
							sensorManager.getSensorList(
									Sensor.TYPE_ACCELEROMETER).size()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private float[] currentAccelerationValues = { 0.0f, 0.0f, 0.0f };

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	float targetValue = 0;

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		if (sensor == accelerometer) {
			float tmp = targetValue;
			currentAccelerationValues[0] = event.values[0];// -
															// currentOrientationValues[0];
			currentAccelerationValues[1] = event.values[1];// -
															// currentOrientationValues[1];
			currentAccelerationValues[2] = event.values[2];// -
															// currentOrientationValues[2];
			targetValue = Math.abs(currentAccelerationValues[0])
					+ Math.abs(currentAccelerationValues[1])
					+ Math.abs(currentAccelerationValues[2]);
			if (tmp > targetValue)
				return;
			// shake if absolute value of accelerometer is more than threshold.
			if (targetValue > 20.0f) {
				Float rtn = targetValue / 60f;
				if (rtn > 2f)
					rtn = 2f;
				if (rtn < 0.5f)
					rtn = 0.5f;
				editor.getManager().getChain().Shake(rtn);
			}
		}
	}

	public void Shake(int interval) {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(interval);
	}

	HashMap<Integer, IntentHandler> intentHandlers = new HashMap<Integer, IntentHandler>();

	public TapChainView addIntentHandler(int requestCode, IntentHandler h) {
		intentHandlers.put(requestCode, h);
		return this;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		if (intentHandlers.containsKey(requestCode))
			intentHandlers.get(requestCode).onIntent(resultCode, data);
		else
			setCode(data.getIntExtra("TEST", 0));
		return;
	}

	public void setVisibility() {
		if (viewControl.getVisibility() == View.VISIBLE)
			viewControl.setVisibility(View.INVISIBLE);
		else
			viewControl.setVisibility(View.VISIBLE);
	}

	public void setCode(int code) {
		if (code < editor.getFactory().getSize())
			editor.getFactory().getInstance(code);
	}

	public class WritingView extends TapChainSurfaceView implements
			GestureDetector.OnDoubleTapListener {
		TapChainView v = null;

		public WritingView(Context context, TapChainView v) {
			super(context);
			move(-100, -100);
			this.v = v;
		}

		@Override
		public void draw(Canvas canvas) {
			// if(DEBUG) {
			// Log.i("MAIN_DEBUG", str);
			// }
			canvas.setMatrix(matrix);
			editor.show(canvas);
		}

		void drawText(Canvas canvas, String str) {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);
			canvas.drawText(str, 100, 100, paint);
		}

		int index = 0;

		@Override
		public boolean onDown(MotionEvent e) {
			sendDownEvent(e.getX(), e.getY());
			return true;
		}

		public void sendDownEvent(float x, float y) {
			v.editor.onDown(getPosition(x, y));
		}

		public void sendUpEvent() {
			v.editor.onUp();
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return editor.onFling((int) velocityX, (int) velocityY);
		}

		@Override
		public void onLongPress(MotionEvent e) {
			editor.onLongPress();
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// Log.w("TouchPoint", String.format("move point %f %f", e2.getX(),
			// e2.getY()));
			editor.onScroll(getVector(-distanceX, -distanceY),
					getPosition(e2.getX(), e2.getY()));
			return false;
		}

		public WorldPoint getPosition(float x, float y) {
			float[] pos = new float[] { x, y };
			inverse.mapPoints(pos);
			return new ScreenPoint((int) pos[0], (int) pos[1])
					.getWorldPoint(this);
		}

		public WorldPoint getVector(float x, float y) {
			float[] pos = new float[] { x, y };
			inverse.mapVectors(pos);
			return new WorldPoint((int) pos[0], (int) pos[1]).setDif();
		}

		public ScreenPoint getScreenVector(float x, float y) {
			float[] pos = new float[] { x, y };
			matrix.mapVectors(pos);
			return new ScreenPoint((int) pos[0], (int) pos[1]);
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			editor.getManager().getChain().TouchOff();
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			((TapChainView) getContext()).setVisibility();
			editor.getManager().getChain().TouchOff();
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			editor.onSingleTapConfirmed();
			return true;
		}

		public boolean PressButton() {
			editor.Compile();
			editor.start();
			return true;
		}

		@Override
		public void move(int vx, int vy) {
			ScreenPoint v = getScreenVector(-vx, -vy);
			matrix.postTranslate(v.x, v.y);
			matrix.invert(inverse);
		}
	}

	public class ReadingView extends TapChainSurfaceView {
		ReadingView(Context context) {
			super(context);
		}

		@Override
		public void draw(Canvas canvas) {
		}
	}

	abstract class TapChainSurfaceView extends SurfaceView implements
			SurfaceHolder.Callback, GestureDetector.OnGestureListener,
			TapChainEdit.IWindow {
		GradientDrawable mGradient, mGradient2;
		private GestureDetector gdetect = new GestureDetector(this);
		Matrix matrix = new Matrix();
		Matrix inverse = new Matrix();
		Paint paint = new Paint(), paint_text = new Paint();
		WorldPoint window_size = new WorldPoint();

		public TapChainSurfaceView(Context context) {

			super(context);
			mGradient = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] {
					0xff303030, 0xff777777 });
			mGradient2 = new GradientDrawable(Orientation.RIGHT_LEFT,
					new int[] { 0xff303030, 0xff777777 });
			getHolder().addCallback(this);
			paint_text.setColor(0xff000000);
			paint_text.setTextSize(20);
			paint.setColor(0xff444444);
			// setFocusable(true);
			requestFocus();
		}

		@Override
		public WorldPoint getWindowPoint() {
			return window_size;
		}

		public void onDraw() {
			// Log.w("TapChainView", "writing view locked");
			Canvas canvas = getHolder().lockCanvas();
			if (canvas != null) {
				paintBackground(canvas);
				draw(canvas);
				canvas.drawText(
						"View = "
								+ Integer.toString(editor.getManager()
										.getChain().getViewNum()), 20, 20,
						paint_text);
				canvas.drawText(
						"Effect = "
								+ Integer.toString(editor.getManager()
										.getChain().getPieces().size()), 20,
						40, paint_text);
				canvas.drawText(
						"UserView = "
								+ Integer.toString(editor.getUserManager()
										.getChain().getViewNum()), 20, 60,
						paint_text);
				canvas.drawText(
						"UserEffect = "
								+ Integer.toString(editor.getUserManager()
										.getChain().getPieces().size()), 20,
						80, paint_text);
				getHolder().unlockCanvasAndPost(canvas);
			}
		}

		public abstract void draw(Canvas canvas);

		public void paintBackground(Canvas canvas) {
			mGradient.draw(canvas);
			mGradient2.draw(canvas);
			// canvas.drawRect(new Rect(0, 0, window_size.x, window_size.y),
			// paint);
			return;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			int xmax = getWidth(), ymax = getHeight();
			int xcenter = xmax / 2;
			mGradient.setBounds(new Rect(0, 0, xcenter, ymax));
			mGradient2.setBounds(new Rect(xcenter, 0, xmax, ymax));
			window_size.x = getWidth();
			window_size.y = getHeight();
			editor.kickDraw();
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			int xmax = getWidth(), ymax = getHeight();
			int xcenter = xmax / 2;
			mGradient.setBounds(new Rect(0, 0, xcenter, ymax));
			mGradient2.setBounds(new Rect(xcenter, 0, xmax, ymax));
			window_size.x = getWidth();
			window_size.y = getHeight();
			editor.kickDraw();
		}

		static final int NONE = 0;
		static final int ZOOM = 1;
		static final String TAG = "ACTION";
		int mode = NONE;
		float oldDist = 0f;
		Matrix savedMatrix = new Matrix();
		PointF mid = new PointF();

		private float spacing(MotionEvent event) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}

		private void midPoint(PointF point, MotionEvent event) {
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x / 2, y / 2);
		}

		public boolean onTouchEvent(MotionEvent ev) {
			gdetect.onTouchEvent(ev);
			int action = ev.getAction();
			switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				savedMatrix.set(matrix);
				oldDist = spacing(ev);
				Log.d(TAG, "oldDist=" + oldDist);
				midPoint(mid, ev);
				if (oldDist > 10f) {
					mode = ZOOM;
					Log.d(TAG, "mode=ZOOM");
					editor.onUp();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == ZOOM) {
					float newDist = spacing(ev);
					matrix.set(savedMatrix);
					// If you want to tweak font scaling, this is the place to
					// go.
					if (newDist > 10f) {
						float scale = newDist / oldDist;
						midPoint(mid, ev);
						Log.d(TAG, "scale=" + String.valueOf(scale));
						// oldDist = newDist;
						matrix.postScale(scale, scale, mid.x, mid.y);
					}
				}
				// put_points(ev);
				break;
			case MotionEvent.ACTION_UP:
				mode = NONE;
				// points.clear();
				// onDraw();
				editor.onUp();
				break;
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				matrix.invert(inverse);
				break;
			}
			return true;
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
		}

		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

		public void move(int vx, int vy) {
		}

	}

}