package org.tapchain;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.core.Factory;
import org.tapchain.core.Factory.ValueChangeNotifier;
import org.tapchain.core.IPiece;
import org.tapchain.core.IntentHandler;
import org.tapchain.core.ScreenPoint;
import org.tapchain.core.TapChainEdit;
import org.tapchain.core.TapChainEdit.IWindow;
import org.tapchain.core.WorldPoint;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.TapChainEdit.*;

import org.tapchain.R;

import android.app.Activity;
//import android.app.Fragment;
//import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class TapChainView extends FragmentActivity 
		implements SensorEventListener {
	static final String VIEW_SELECT = "SELECT";
	static final String X = "LOCATIONX", Y = "LOCATIONY", V = "VIEWS";
	static final RectF rf = new RectF(0, 0, 100, 100);
	static final boolean DEBUG = true;

	private WritingView viewEditing;
	private ReadingView viewUser;
	FrameLayout viewControl = null;
	SensorManager sensorManager;
	private Sensor accelerometer;
	Handler mq = new Handler();
	SparseArray<IntentHandler> intentHandlers = new SparseArray<IntentHandler>();

	// 1.Initialization
	@Override
	public void onSaveInstanceState(Bundle out) {
		RectF r = new RectF(rf);
		getEditView().matrix.mapRect(r);
		out.putParcelable(X, r);
		out.putParcelableArray(V, getEditor().editorManager.getSystemPieces()
				.toArray(new Parcelable[0]));
	}

	/** Called when the activity is first created. */
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
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
		AndroidActor.setActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Writing Window Initialization
		viewEditing = new WritingView(this);
		viewUser = new ReadingView(this);
		if (savedInstanceState != null) {
			getEditView().matrix.setRectToRect(rf,
					(RectF) savedInstanceState.getParcelable(X),
					Matrix.ScaleToFit.FILL);
			getEditView().matrix.invert(getEditView().inverse);
			Parcelable[] p = savedInstanceState.getParcelableArray(V);
			int i = 0;
			for (ISystemPiece v : getEditor().editorManager.getSystemPieces()) {
				v.setCenter(((AndroidView) p[i++]).getCenter());
			}
		}

		FrameLayout rootview = new FrameLayout(this);
		FrameLayout root = new FrameLayout(this);
		setContentView(root);
		viewControl = new FrameLayout(this);
		RelativeLayout view_bottom_left = new RelativeLayout(this);
//		view_bottom_left.setGravity(Gravity.LEFT | Gravity.BOTTOM);
		addLeftButton(view_bottom_left, "0", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getEditor().reset();
			}
		});
		addButton(view_bottom_left, R.drawable.dust, true, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getEditor().Mode(EditMode.REMOVE);
			}
		});
		addButton(view_bottom_left, R.drawable.stop, true, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(getEditor().freezeToggle())
					((ImageView)v).setImageResource(R.drawable.stop);
				else
					((ImageView)v).setImageResource(R.drawable.start);
			}
		});
		addButton(view_bottom_left, R.drawable.reload, true, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getEditor().Mode(EditMode.RENEW);
			}
		});
		addButton(view_bottom_left, R.drawable.pullup, true, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean gridshow = false;
				GridFragment f = getGrid();
				if (f != null) {
					gridshow = f.toggle();
				}
				if(gridshow)
					((ImageView)v).setImageResource(R.drawable.pulldown);
				else
					((ImageView)v).setImageResource(R.drawable.pullup);
			}
		});
		addButton(view_bottom_left, R.drawable.magnet, true, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean magnet = false;
				magnet = ((TapChainAndroidEdit)getEditor()).magnetToggle();
				if(!magnet)
					((ImageView)v).setAlpha(100);
				else
					((ImageView)v).setAlpha(255);
			}
		});
		viewControl.addView(view_bottom_left, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//		LinearLayout view_bottom_right = new LinearLayout(this);
//		view_bottom_right.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
		addButton(view_bottom_left, R.drawable.no, false, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
//		viewControl.addView(view_bottom_right, new LayoutParams(
//				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		rootview.addView(viewUser, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		rootview.addView(getEditView(), new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		root.addView(rootview);
		root.addView(viewControl, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		root.setId(0x00001235);
		root.setTag("OVERLAY");

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		LinearLayout l = new LinearLayout(this);
		rootview.addView(l);
		l.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
		l.setId(0x00001234);
		new GridFragment().setContext(this).show(GridShow.HIDE);
		// Log.i("TapChainView.state", "onCreate");

	}

	int leftnum = 100, rightnum = 200;
	public Button addLeftButton(ViewGroup parent, String label,
			View.OnClickListener c) {
		Button rtn = addButton(parent, label, c);
		rtn.setId(leftnum);
		RelativeLayout.LayoutParams lo2 = (RelativeLayout.LayoutParams)rtn.getLayoutParams();
		lo2.addRule(RelativeLayout.RIGHT_OF, leftnum-1);
		lo2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		leftnum++;
		return rtn;
	}
	
	public Button addButton(ViewGroup parent, String label, View.OnClickListener c) {
		Button bt = new Button(this);
		bt.setOnClickListener(c);
		LayoutParams lo = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		parent.addView(bt, lo);
		bt.setText(label);
		return bt;
		
	}
	
	public ImageView addButton(ViewGroup parent, int resource, boolean left,
			View.OnClickListener c) {
		ImageView rtn = _addButton(parent, resource, c);
		rtn.setId(leftnum);
		RelativeLayout.LayoutParams lo2 = (RelativeLayout.LayoutParams)rtn.getLayoutParams();
		if(left) {
			if(leftnum == 100)
				lo2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			else
				lo2.addRule(RelativeLayout.RIGHT_OF, leftnum-1);
			leftnum++;
		} else {
			if(rightnum == 200)
				lo2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			else
				lo2.addRule(RelativeLayout.LEFT_OF, rightnum-1);
			rightnum++;
		}
		lo2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		return rtn;
	}

	public ImageView _addButton(ViewGroup parent, int resource,
			View.OnClickListener c) {
		ImageView bt = new ImageView(this);
		bt.setOnClickListener(c);
		LayoutParams lo = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		parent.addView(bt, lo);
		bt.setImageResource(resource);
		bt.setMinimumWidth(50);
		bt.setMinimumHeight(50);
		return bt;
	}

	// 2.Getters and setters
	public GridFragment getGrid() {
//		[APIv11]
//		GridFragment f = (GridFragment) getFragmentManager().findFragmentByTag(
//		VIEW_SELECT);
		GridFragment f = (GridFragment) getSupportFragmentManager().findFragmentByTag(
		VIEW_SELECT);
		return f;
	}

	public WritingView getEditView() {
		return viewEditing;
	}

	public void postMQ(Runnable r) {
		mq.post(r);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i("TapChainView.state", "onStop");
		if (sensorManager != null)
			sensorManager.unregisterListener(this);
		getEditor().onDownClear();
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

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("TapChainView.state", "onResume");
		try {
			accelerometer = sensorManager.getSensorList(
					Sensor.TYPE_ACCELEROMETER).get(0);
			sensorManager.registerListener(this, accelerometer,
					SensorManager.SENSOR_DELAY_FASTEST);
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
			currentAccelerationValues[1] = event.values[1];// -
			currentAccelerationValues[2] = event.values[2];// -
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
				getEditor().getSystemManager().getChain().Shake(rtn);
			}
		}
	}

	public void Shake(int interval) {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(interval);
	}


	public TapChainView addIntentHandler(int requestCode, IntentHandler h) {
		intentHandlers.put(requestCode, h);
		return this;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		if (intentHandlers.get(requestCode) != null)
			intentHandlers.get(requestCode).onIntent(resultCode, data);
		else
			add(getEditor().getFactory(), data.getIntExtra("TEST", 0), 0f, 0f);
		return;
	}

	public void setVisibility() {
		if (viewControl.getVisibility() == View.VISIBLE)
			viewControl.setVisibility(View.INVISIBLE);
		else
			viewControl.setVisibility(View.VISIBLE);
	}

	public void add(Factory<IPiece> f, int code, float x, float y) {
		getEditView().onAdd(f, code, x, y);
	}

	public void add(Factory<IPiece> f, int code, float x, float y, float dx, float dy) {
//		Log.w("test", "add(xy, dxy) called");
		getEditView().onAdd(f, code, x, y, dx, dy);
	}

	public void dummyAdd(Factory<IPiece> f, int num, float x, float y) {
		getEditView().onDummyAdd(f, num, x, y);
	}

	public void dummyMoveTo(float x, float y) {
		getEditView().onDummyMoveTo(x, y);
	}

	public void dummyRemove() {
		getEditor().onDummyRemove();
	}

	/**
	 * @param viewEditing
	 *            the viewEditing to set
	 */

	/**
	 * @return the editor
	 */
	public TapChainEdit getEditor() {
		return getEditView().getEditor();
	}

	// 5.Local classes
	public enum GridShow {
		SHOW, HIDE, HALF
	}

	public static class GridFragment extends Fragment {
		String tag = VIEW_SELECT;
		GridShow show = GridShow.HIDE;
		int _width = LayoutParams.FILL_PARENT,
				_height = LayoutParams.FILL_PARENT;
		boolean autohide = false;
		ImageView ShowingDisabled;
		TapChainView act = null;
		TabHost tabH;

		public GridFragment() {
			super();
		}

		public GridFragment setContext(TapChainView a) {
			// Log.i("TapChain", "GridFragment#setContext called");
			this.act = a;
			return this;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle saved) {
			// Log.i("TapChain", "GridFragment#onCreateView called");
			LinearLayout tabView;
			HorizontalScrollView scrollTitle;
			TabWidget tabWidget;
			FrameLayout tabContent;
			FrameLayout darkMask;

			tabH = new TabHost(act, null);

			tabView = new LinearLayout(act);
			tabView.setOrientation(LinearLayout.VERTICAL);
			tabH.addView(tabView);

			scrollTitle = new HorizontalScrollView(act);
			tabView.addView(scrollTitle);

			// the tabhost needs a tabwidget, that is a container for the
			// visible tabs
			tabWidget = new TabWidget(act);
			tabWidget.setId(android.R.id.tabs);
			tabWidget.setPadding(0, 10, 0, 0);
			scrollTitle.addView(tabWidget);
//			tabView.addView(tabWidget);

			// the tabhost needs a frame layout for the views associated with
			// each visible tab
			tabContent = new FrameLayout(act);
			tabContent.setId(android.R.id.tabcontent);
			tabView.addView(tabContent, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

			// setup must be called if the tabhost is programmatically created.
			tabH.setup();
			addTab(tabH, "TS1", "[ + ]", act.getEditor().getFactory(),
					0xaa000000, R.drawable.plus);
			addTab(tabH, "TS2", "[ V ]", act.getEditor().getRecentFactory(),
					0xaa220000, R.drawable.history);
			addTab(tabH, "TS3", "[ <=> ]", act.getEditor().getRelatives(),
					0xaa000022, R.drawable.relatives);
//			addTab(tabH, "down", "[  ]", null,
//					0xaa000000, R.drawable.pulldown);
			ImageView img = new ImageView(act);
			img.setImageDrawable(getResources().getDrawable(R.drawable.pulldown));
			tabWidget.addView(img);
			tabWidget.getChildAt(3).setOnClickListener(new OnClickListener() { 
				@Override
				public void onClick(View v) {
						GridFragment f = act.getGrid();
						if(f != null)
							f.toggle();
				  }
				});
//			ImageView img2 = new ImageView(act);
//			img2.setImageDrawable(getResources().getDrawable(R.drawable.pin));
//			tabWidget.addView(img2);
//			tabWidget.getChildAt(4).setOnClickListener(new OnClickListener() { 
//				@Override
//				public void onClick(View v) {
//						GridFragment f = act.getGrid();
//						if(f != null)
//							f.setAutohide();
//				  }
//				});
//
			darkMask = new FrameLayout(act);
			darkMask.addView(tabH);
			darkMask.setLayoutParams(new FrameLayout.LayoutParams(_width,
					_height));
			ShowingDisabled = new ImageView(act);
			ShowingDisabled.setBackgroundColor(0x80000000);
			darkMask.addView(ShowingDisabled);
			enable();
			return darkMask;
		}

		public void addTab(TabHost h, String _tag, String label,
				final Factory<IPiece> f, final int color, int resource) {
			TabSpec ts = h.newTabSpec(_tag);
			ts.setIndicator(""/*label*/, getResources().getDrawable(resource));
			ts.setContent(new TabHost.TabContentFactory() {
				public View createTabContent(String tag) {
					return new ActorSelector(act, f, color);
				}
			});
			// ts1.setContent(new Intent(this,Tab1.class));
			h.addTab(ts);
			return;

		}

//		public void invalidateTab() { 
			// adapter.add(new ActorButton(a, a.editor.getRecent(), a.editor.getRecent().getSize()-1)); //
//		   adapter.notifyDataSetChanged(); // tabH.postInvalidate(); }
//		}

		public void setSize(int w, int h) {
			_width = w;
			_height = h;
			getView().setLayoutParams(
					new LinearLayout.LayoutParams(_width, _height));

		}

		private boolean contains(int rx, int ry) {
			int[] l = new int[2];
			getView().getLocationOnScreen(l);
			int x = l[0];
			int y = l[1];
			int w = getView().getWidth();
			int h = getView().getHeight();

			if (rx < x || rx > x + w || ry < y || ry > y + h) {
				return false;
			}
			return true;
		}

		public void show(GridShow _show) {
			// Log.i("TapChain", "GridFragment#show called");
			show = _show;
//			[APIv11]
//			FragmentTransaction ft = act.getFragmentManager()
//					.beginTransaction();
//			if (this != act.getFragmentManager().findFragmentByTag(tag)) {
			FragmentTransaction ft = act.getSupportFragmentManager()
					.beginTransaction();
			if (this != act.getSupportFragmentManager().findFragmentByTag(tag)) {
//				Log.w("TapChain", "GridFragment#show fragment replaced");
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.replace(0x00001234, this, tag);
			}
			switch (_show) {
			case SHOW:
				setSize(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				ft.show(this);
				break;
			case HALF:
				Pair<Integer, Integer> p1 = act.checkDisplayAndRotate();
				setSize(p1.first, p1.second);
				ft.show(this);
				break;
			case HIDE:
				ft.hide(this);
			}
			ft.commit();
		}

		public boolean toggle() {
			show((show == GridShow.HIDE) ? GridShow.HALF : GridShow.HIDE);
			return show != GridShow.HIDE;
		}

		public void setAutohide() {
			autohide = !autohide;
		}

		public void kickAutohide() {
			if (autohide)
				show(GridShow.HIDE);
		}

		public void enable() {
			ShowingDisabled.setVisibility(View.INVISIBLE);
		}

		public void disable() {
			ShowingDisabled.setVisibility(View.VISIBLE);
		}
	}

	public static class ActorSelector extends GridView {
		ActorSelector(final Activity act, Factory<IPiece> f, int color) {
			super(act);
			setBackgroundColor(color);
			setColumnWidth(100);
			setVerticalSpacing(0);
			setHorizontalSpacing(0);
			setNumColumns(GridView.AUTO_FIT);
			if(f != null)
				setAdapter(new ViewAdapter(act, f));
		}

	}

	public static class ViewAdapter extends BaseAdapter {
		private Factory<IPiece> f;
		private TapChainView act;

		public ViewAdapter(Context c, Factory<IPiece> f) {
			act = (TapChainView) c;
			this.f = f;
			f.setNotifier(new ValueChangeNotifier() {
				@Override
				public void notifyView() {
					ViewAdapter.this.notifyDataSetChanged();
				}
			});
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Judging convertView is valid.
			// convertView always returns another existing View.
			if (convertView == null
					|| convertView.getTag() == null
					|| !convertView.getTag().equals(f.get(position).hashCode())) {
				convertView = new ActorButton(act, f, position);
			}
			convertView.setTag(f.get(position).hashCode());
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return f.get(position).hashCode();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			return f.getSize();
		}
	}

	static OverlayPopup p;

	public static class ActorButton extends PieceImage implements View.OnTouchListener, OnGestureListener {
//		float offsetx = 0f;
//		float offsety = 0f;
		final TapChainView act;
		final Factory<IPiece> factory;
		final int num;
		private GestureDetector touchDetector =
				new GestureDetector(this);
		ActorButton(Context c, Factory<IPiece> f, final int j) {
			super(c, f, j);
			act = (TapChainView) c;
			factory = f;
			num = j;
			setOnTouchListener(this);
		}
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(touchDetector.onTouchEvent(event))
					return true;
			int action = event.getAction();
			// Log.w("Action", String.format("action = %d", action));
			switch (action) {
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
				act.dummyRemove();
				GridFragment f1 = act.getGrid();
				f1.enable();
				if (f1 != null
						&& f1.contains((int) event.getRawX(),
								(int) event.getRawY())) {
					p.dismiss();
					break;
				}
				float x = event.getRawX();
				float y = event.getRawY();
				act.add(factory, num, x, y);
				p.dismiss();
			case MotionEvent.ACTION_CANCEL:
				break;
			}
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			act.dummyAdd(factory, num, e.getRawX(), e.getRawY());
			getParent().requestDisallowInterceptTouchEvent(true);
			if(p == null)
				p = new OverlayPopup(act);
			p.setPopupView(act, factory, num);
			GridFragment f0 = act.getGrid();
			if (f0 != null) {
				f0.disable();
				f0.kickAutohide();
			}
			p.show((int) e.getRawX(), (int) e.getRawY());
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			act.dummyMoveTo(e2.getRawX(),e2.getRawY());
			p.show((int) e2.getRawX(), (int) e2.getRawY());
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			act.dummyRemove();
			GridFragment f1 = act.getGrid();
			f1.enable();
			if (f1 != null
					&& f1.contains((int) e2.getRawX(),
							(int) e2.getRawY())) {
				p.dismiss();
				return true;
			}
			act.add(factory, num, e2.getRawX(), e2.getRawY(), velocityX, velocityY);
			p.dismiss();
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

	}

	public static class OverlayPopup extends PopupWindow {
		int halfw, halfh;
		View v = null;
		Context cxt = null;

		public OverlayPopup(Context c) {
			super(c);
			setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		}

		public void setPopupView(Context c, Factory<?> f, int i) {
			v = new PieceImage(c, f, i);
			cxt = c;
			setContentView(v);
			//The following line is to prevent PopupWindow from drawing odd background.
			setBackgroundDrawable(new BitmapDrawable());
		}

		public void show(int x, int y) {
			if (v == null)
				return;
			if (!isShowing())
				showAtLocation(((Activity) cxt).findViewById(0x00001235),
						Gravity.NO_GRAVITY, x - v.getWidth() / 2,
						y - v.getHeight() / 2);
			else
				update(x - v.getWidth() / 2, y - v.getHeight() / 2, -1, -1);
		}
	}

	public static class PieceImage extends ImageView {
		PieceImage(Context c, Factory<?> f, final int j) {
			super(c);
			AndroidView v = null;
			try {
				if (f == null)
					f = ((TapChainView) c).getEditor().getFactory();
				v = (AndroidView) f.getView(j).newInstance(null);
			} catch (ChainException e) {
				e.printStackTrace();
			}
			Drawable a = (v != null) 
					? v.getDrawable() 
					: getResources().getDrawable(R.drawable.cancel);
			setImageDrawable(a);
		}
	}

	public class WritingView extends TapChainSurfaceView implements
			GestureDetector.OnDoubleTapListener {
		private TapChainEdit editor;

		public WritingView(Context context) {
			super(context);
			move(-100, -100);
			editor = new TapChainAndroidEdit(this);
			editor.kickSystemDraw(null);
		}

		/**
		 * @return the editor
		 */
		public TapChainEdit getEditor() {
			return editor;
		}

		@Override
		public void draw(Canvas canvas) {
			viewUser.paintBackground(canvas);
			viewUser.draw(canvas);
			canvas.setMatrix(matrix);
			int w = 100, h = 100;
			WorldPoint lefttop = getPosition(0f, 0f);
			WorldPoint rightbottom = getPosition(canvas.getWidth(),
					canvas.getHeight());
			int startx = lefttop.x() - lefttop.x() % w - w, starty = lefttop
					.y() - lefttop.y() % h - h, endx = rightbottom.x(), endy = rightbottom
					.y();
			for (int i = startx; i < endx; i += w) {
				canvas.drawLine(i, starty, i, endy, paint);
			}
			for (int j = starty; j < endy; j += h) {
				canvas.drawLine(startx, j, endx, j, paint);
			}
			getEditor().show(canvas);
		}

		void drawText(Canvas canvas, String str) {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);
			canvas.drawText(str, 100, 100, paint);
		}

		int index = 0;

		public void onAdd(Factory<IPiece> f, int code, float x, float y) {
			getEditor().onAdd(f, code, getPosition(x, y));
		}
		public void onAdd(Factory<IPiece> f, int code, float x, float y, float dx, float dy) {
			ISystemPiece added = getEditor().onAdd(f, code, getPosition(x, y)).getValue();
			getEditor().capturePiece(added);
			getEditor().onFling((int)dx, (int)dy);
		}
		
		public void onDummyAdd(Factory<IPiece> f, int num, float x, float y) {
			getEditor().onDummyAdd(f, num, getPosition(x, y));
		}
		
		public void onDummyMoveTo(float x, float y) {
			getEditor().onDummyMoveTo(getPosition(x, y));
		}

		@Override
		public boolean onDown(MotionEvent e) {
			sendDownEvent(e.getX(), e.getY());
			return true;
		}

		public void sendDownEvent(float x, float y) {
			getEditor().onDown(getPosition(x, y));
		}

		public void sendUpEvent() {
			getEditor().onUp();
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return getEditor().onFling((int) velocityX, (int) velocityY);
		}

		@Override
		public void onLongPress(MotionEvent e) {
			getEditor().onLongPress();
			setMode(CAPTURED);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (mode == CAPTURED) {
				return onSecondTouch(getPosition(e2.getX(),e2.getY()));
			}
			getEditor().onScroll(getVector(-distanceX, -distanceY),
					getPosition(e2.getX(), e2.getY()));
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			getEditor().onShowPress();
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			getEditor().getSystemManager().getChain().TouchOff();
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			((TapChainView) getContext()).setVisibility();
			getEditor().getSystemManager().getChain().TouchOff();
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			getEditor().onSingleTapConfirmed();
			return true;
		}

		public boolean PressButton() {
			getEditor().Compile();
			getEditor().start();
			return true;
		}

		@Override
		public void move(int vx, int vy) {
			ScreenPoint v = getScreenVector(-vx, -vy);
			matrix.postTranslate(v.x, v.y);
			matrix.invert(inverse);
		}
		
		@Override
		public boolean onSecondTouch(final WorldPoint wp) {
			return getEditor().onSecondTouch(wp);
		}
		
//		@Override
//		public void onDraw() {
//			viewUser.onDraw();
//			super.onDraw();
//		}
	}

	public class ReadingView extends TapChainSurfaceView {
		GradientDrawable mGradient, mGradient2;
		ReadingView(Context context) {
			super(context);
			mGradient = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] {
			0xff303030, 0xff777777 });
			mGradient2 = new GradientDrawable(Orientation.RIGHT_LEFT,
			new int[] { 0xff303030, 0xff777777 });
		}

		@Override
		public void paintBackground(Canvas canvas) {
			mGradient.draw(canvas);
			mGradient2.draw(canvas);
			// canvas.drawRect(new Rect(0, 0, window_size.x, window_size.y),
			// paint);
			return;
		}
		@Override
		public void draw(Canvas canvas) {
			getEditor().userShow(canvas);
			canvas.drawText("Goal = "+TapChainGoalSystem.printState(), 20, 100, paint_text);
		}
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			int xmax = getWidth(), ymax = getHeight();
			int xcenter = xmax / 2;
			mGradient.setBounds(new Rect(0, 0, xcenter, ymax));
			mGradient2.setBounds(new Rect(xcenter, 0, xmax, ymax));
			super.surfaceChanged(holder, format, width, height);
		}
	}

	abstract class TapChainSurfaceView extends SurfaceView implements
			SurfaceHolder.Callback, GestureDetector.OnGestureListener,
			TapChainEdit.IWindow {
		private GestureDetector gdetect = new GestureDetector(this);
		Matrix matrix = new Matrix();
		Matrix inverse = new Matrix();
		Paint paint = new Paint(), paint_text = new Paint();
		WorldPoint window_size = new WorldPoint();

		public TapChainSurfaceView(Context context) {

			super(context);
			SurfaceHolder holder = getHolder();
			holder.setFormat(PixelFormat.TRANSPARENT);
			holder.addCallback(this);
			paint_text.setColor(0xff000000);
			paint_text.setTextSize(20);
			paint.setColor(0xff444444);
			// setFocusable(true);
			requestFocus();
		}

		@Override
		public WorldPoint getWindowSize() {
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
								+ Integer.toString(getEditor().getSystemManager()
										.getChain().getViewNum()), 20, 20,
						paint_text);
				canvas.drawText(
						"Effect = "
								+ Integer.toString(getEditor().getSystemManager()
										.getChain().getPieces().size()), 20,
						40, paint_text);
				canvas.drawText(
						"UserView = "
								+ Integer.toString(getEditor().getUserManager()
										.getChain().getViewNum()), 20, 60,
						paint_text);
				canvas.drawText(
						"UserEffect = "
								+ Integer.toString(getEditor().getUserManager()
										.getChain().getPieces().size()), 20,
						80, paint_text);
				getHolder().unlockCanvasAndPost(canvas);
			}
		}

		public abstract void draw(Canvas canvas);

		public void paintBackground(Canvas canvas) {
			// canvas.drawRect(new Rect(0, 0, window_size.x, window_size.y),
			// paint);
			return;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			window_size.x = getWidth();
			window_size.y = getHeight();
			getEditor().kickSystemDraw(null);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			int xmax = getWidth(), ymax = getHeight();
			int xcenter = xmax / 2;
//			mGradient.setBounds(new Rect(0, 0, xcenter, ymax));
//			mGradient2.setBounds(new Rect(xcenter, 0, xmax, ymax));
			window_size.x = getWidth();
			window_size.y = getHeight();
			getEditor().kickSystemDraw(null);
		}

		static final int NONE = 0;
		static final int ZOOM = 1;
		static final int CAPTURED = 2;
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
			if(gdetect.onTouchEvent(ev))
				return true;
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
					getEditor().onUp();
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
						// Log.d(TAG, "scale=" + String.valueOf(scale));
						// oldDist = newDist;
						matrix.postScale(scale, scale, mid.x, mid.y);
					}
				} else if (mode == CAPTURED) {
					return onSecondTouch(getPosition(ev.getX(),ev.getY()));
				}

				break;
			case MotionEvent.ACTION_UP:
				mode = NONE;
				getEditor().onUp();
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
		
		protected void setMode(int _mode) {
			mode = _mode;
		}
		
		public WorldPoint getPosition(float x, float y) {
			float[] pos = new float[] { x, y };
			inverse.mapPoints(pos);
			return new WorldPoint((int) pos[0], (int) pos[1]);
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

		public boolean onSecondTouch(final WorldPoint wp) {
			return false;
		}
	}

	public IWindow getActorWindow() {
		return getEditView();
	}

}