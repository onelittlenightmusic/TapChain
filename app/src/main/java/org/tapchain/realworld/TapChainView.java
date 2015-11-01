package org.tapchain.realworld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
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
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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

import org.json.JSONException;
import org.tapchain.AndroidActor;
import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.IAndroidIntentHandler;
import org.tapchain.IntentHandler;
import org.tapchain.PaletteSort;
import org.tapchain.TapChainAndroidEditor;
import org.tapchain.TapChainGoalTap;
import org.tapchain.core.Actor;
import org.tapchain.core.BlueprintInitialization;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Factory;
import org.tapchain.core.Factory.ValueChangeNotifier;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IBlueprintFocusNotification;
import org.tapchain.core.IBlueprintInitialization;
import org.tapchain.core.IPoint;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IWindow;
import org.tapchain.editor.TapChainEditor;
import org.tapchain.game.ISensorView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.sqrt;

public class TapChainView extends FragmentActivity implements
		SensorEventListener, ISensorView, IAndroidIntentHandler {
	static final String VIEW_SELECT = "SELECT";
	static final String X = "LOCATIONX", Y = "LOCATIONY", V = "VIEWS";
	static final RectF RF = new RectF(0, 0, 100, 100);
	static final boolean DEBUG = true;

	private WritingView viewCanvas;
	private UserView viewUser;
	FrameLayout viewControl = null;
	SensorManager sensorManager;
	private Sensor accelerometer;
//	Handler mq = new Handler();
	SparseArray<IntentHandler> intentHandlers = new SparseArray<IntentHandler>();
	static Activity now;
	public static int tapOffset = 10000;

	public static Activity getNow() {
		return now;
	}

	// 1.Initialization
	@Override
	public void onSaveInstanceState(Bundle out) {
		RectF r = new RectF(RF);
		getCanvas().matrix.mapRect(r);
		out.putParcelable(X, r);
		out.putParcelableArray(V,
				getEditor().getTaps().toArray(new Parcelable[0]));
	}

	
	/** Called when the activity is first created. */
	/*
	 * (non-Javadoc)
	 *
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		now = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		viewCanvas = new CanvasViewImpl2(this);
		viewUser = new UserView(this);
		if (savedInstanceState != null) {
			getCanvas().matrix.setRectToRect(RF,
					(RectF) savedInstanceState.getParcelable(X),
					Matrix.ScaleToFit.FILL);
			getCanvas().matrix.invert(getCanvas().inverse);
			Parcelable[] p = savedInstanceState.getParcelableArray(V);
			int i = 0;
			for (IActorTap v : getEditor().manager.getTaps()) {
				v.setCenter(((AndroidView) p[i++]).getCenter());
			}
		}

		FrameLayout rootview = new FrameLayout(this);
		FrameLayout root = new FrameLayout(this);
		setContentView(root);
		viewControl = new FrameLayout(this);
		RelativeLayout view_bottom_left = new RelativeLayout(this);
		addButton(view_bottom_left, R.drawable.dust, true,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						getEditor().reset();
					}
				});
		addButton(view_bottom_left, R.drawable.stop, true,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (getEditor().freezeToggle())
							((ImageView) v).setImageResource(R.drawable.stop);
						else
							((ImageView) v).setImageResource(R.drawable.start);
					}
				});
		addButton(view_bottom_left, R.drawable.magnet, true,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean magnet = false;
						magnet = ((TapChainAndroidEditor) getEditor())
								.magnetToggle();
						if (!magnet)
							((ImageView) v).setImageAlpha(100);
						else
							((ImageView) v).setImageAlpha(255);
					}
				});
		addButton(view_bottom_left, R.drawable.config, true,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						recoverFactory();
					}
				});
		addButton(view_bottom_left, R.drawable.pullup, true,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean gridshow = false;
						GridFragment f = getGrid();
						if (f != null) {
							gridshow = f.toggle();
						}
						if (gridshow)
							((ImageView) v)
									.setImageResource(R.drawable.pulldown);
						else
							((ImageView) v).setImageResource(R.drawable.pullup);
					}
				});
		addButton(view_bottom_left, R.drawable.dust, true,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						viewCanvas.deleteRegistration();
					}
				});
		addButton(view_bottom_left, R.drawable.config, true,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							Log.w("JSON TEST", getEditor().edit().getChain().toJSON().toString());
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
		viewControl.addView(view_bottom_left, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addButton(view_bottom_left, R.drawable.no, false,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});
		rootview.addView(viewUser, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		rootview.addView(getCanvas(), new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		root.addView(rootview);
		root.addView(viewControl, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
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
		RelativeLayout.LayoutParams lo2 = (RelativeLayout.LayoutParams) rtn
				.getLayoutParams();
		lo2.addRule(RelativeLayout.RIGHT_OF, leftnum - 1);
		lo2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		leftnum++;
		return rtn;
	}

	public Button addButton(ViewGroup parent, String label,
			View.OnClickListener c) {
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
		RelativeLayout.LayoutParams lo2 = (RelativeLayout.LayoutParams) rtn
				.getLayoutParams();
		if (left) {
			if (leftnum == 100)
				lo2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			else
				lo2.addRule(RelativeLayout.RIGHT_OF, leftnum - 1);
			leftnum++;
		} else {
			if (rightnum == 200)
				lo2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			else
				lo2.addRule(RelativeLayout.LEFT_OF, rightnum - 1);
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
		// [APIv11]
		GridFragment f = (GridFragment) getSupportFragmentManager()
				.findFragmentByTag(VIEW_SELECT);
		return f;
	}

	public WritingView getCanvas() {
		return viewCanvas;
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i("TapChainView.state", "onStop");
		if (sensorManager != null)
			sensorManager.unregisterListener(this);
		getEditor().onDownClear();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (getGrid() != null)
			getGrid().show();
	}

	public Pair<Integer, Integer> checkDisplayAndRotate() {
		DisplayMetrics metrix = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrix);
		if (metrix.widthPixels > metrix.heightPixels)
			return new Pair<Integer, Integer>(metrix.widthPixels*1/2,
					LayoutParams.MATCH_PARENT);
		return new Pair<Integer, Integer>(LayoutParams.MATCH_PARENT,
				metrix.heightPixels *1/2);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getGrid() != null)
			getGrid().show(GridShow.HALF);
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AndroidActor.onDestroy();
	}

	private float[] currentAccelerationValues = { 0.0f, 0.0f, 0.0f };

	@Override
	public void onSensorChanged(SensorEvent event) {

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	float targetValue = 0;


	WorldPoint gravity = new WorldPoint();
	
	@Override
	public IPoint getTilt() {
		return gravity.set(-currentAccelerationValues[0], currentAccelerationValues[1]);
	}

	@Override
	public void shake(int interval) {
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
            try {
                intentHandlers.get(requestCode).onIntent(resultCode, data);
            } catch (ChainException e) {
                e.printStackTrace();
            }
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
	
	public void add(Factory<Actor> f, int code) {
		getCanvas().onAdd(f, code);
	}

	public void add(Factory<Actor> f, int code, float x, float y) {
		getCanvas().onAdd(f, code, x, y);
	}

	public void add(Factory<Actor> f, int code, float x, float y, float dx,
			float dy) {
		// Log.w("test", "addFocusable(xy, dxy) called");
		getCanvas().onAdd(f, code, x, y, dx, dy);
	}

	public void dummyAdd(Factory<Actor> f, int num, float x, float y) {
		getCanvas().onDummyAdd(f, num, x, y);
	}

	public void dummyMoveTo(float x, float y) {
		getCanvas().onDummyMoveTo(x, y);
	}

	public void dummyRemove() {
		getCanvas().onDummyRemove();
	}

	/**
	 * @param viewCanvas
	 *            the viewCanvas to set
	 */

	/**
	 * @return the editor
	 */
	public TapChainEditor getEditor() {
		return getCanvas().getEditor();
	}

	public View getViewByTag(String tag) {
		return buttons.get(tag);
	}

	// 5.Local classes
	public enum GridShow {
		SHOW, HIDE, HALF
	}
	
	public void showPalette(PaletteSort sort) {
		if(getGrid() != null)
			getGrid().setCurrentFactory(sort.getNum());
	}

	public static class GridFragment extends Fragment {
		String tag = VIEW_SELECT;
		GridShow show = GridShow.HIDE;
		int _width = LayoutParams.MATCH_PARENT,
				_height = LayoutParams.MATCH_PARENT;
		boolean autohide = false;
		ImageView ShowingDisabled;
		TapChainView act = null;
		TabHost tabH;
		ArrayList<Factory<Actor>> factoryList = new ArrayList<Factory<Actor>>();

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

			// the tabhost needs a frame layout for the views associated with
			// each visible tab
			tabContent = new FrameLayout(act);
			tabContent.setId(android.R.id.tabcontent);
			tabView.addView(tabContent, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

			// setup must be called if the tabhost is programmatically created.
			tabH.setup();
			addTab(tabH, "TS1", "[ + ]", act.getEditor().getFactory(),
					0xaa000000, R.drawable.plus);
			addTab(tabH, "TS2", "[ V ]", act.getEditor().getRecentFactory(),
					0xaa220000, R.drawable.history);
			addTab(tabH, "TS3", "[ <=> ]", act.getEditor().getRelatives(),
					0xaa000022, R.drawable.relatives);
			ImageView img = new ImageView(act);
			img.setImageDrawable(getResources()
					.getDrawable(R.drawable.pulldown));
			tabWidget.addView(img);
			tabWidget.getChildAt(3).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					GridFragment f = act.getGrid();
					if (f != null)
						f.toggle();
				}
			});
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
				final Factory<Actor> f, final int color, int resource) {
			TabSpec ts = h.newTabSpec(_tag);
			ts.setIndicator(""/* label */, getResources().getDrawable(resource));
			ts.setContent(new TabHost.TabContentFactory() {
				public View createTabContent(String tag) {
					return new ActorSelector(act, f, color);
				}
			});
			// ts1.setContent(new Intent(this,Tab1.class));
			h.addTab(ts);
			factoryList.add(f);
			return;

		}


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
			show = _show;
			FragmentTransaction ft = act.getSupportFragmentManager()
					.beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			if (this != act.getSupportFragmentManager().findFragmentByTag(tag)) {
				ft.replace(0x00001234, this, tag);
			}
			switch (_show) {
			case SHOW:
				setSize(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
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

		public void show() {
			show(show);
		}

		public String getShowState() {
			return show.toString();
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
		
		public Factory<Actor> getCurrentFactory() {
			int tabNum = tabH.getCurrentTab();
			return factoryList.get(tabNum);
		}
		
		public void setCurrentFactory(int tabNum) {
			tabH.setCurrentTab(tabNum);
		}

	}

	public static class ActorSelector extends GridView {
		ActorSelector(final Activity act, Factory<Actor> f, int color) {
			super(act);
			setBackgroundColor(color);
			setColumnWidth(100);
			setVerticalSpacing(0);
			setHorizontalSpacing(0);
			setNumColumns(GridView.AUTO_FIT);
			if (f != null) {
				final ViewAdapter va = new ViewAdapter(act, f);
				setAdapter(va);
				f.setNotifier(new ValueChangeNotifier() {

					@Override
					public void notifyView() {
						va.notifyView();
					}

					@Override
					public void invalidate() {
						invalidateOwn();
					}
					
				});
			}
		}
		public void invalidateOwn() {
			((Activity) getContext()).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					invalidate();
				}
			});
		}

	}

	HashMap<String, ActorImageButton> buttons = new HashMap<String, ActorImageButton>();
	public static class ViewAdapter extends BaseAdapter {
		private Factory<Actor> f;
		private TapChainView act;

		public ViewAdapter(Context c, Factory<Actor> f) {
			act = (TapChainView) c;
			this.f = f;
		}
		
		public void notifyView() {
			act.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ViewAdapter.this.notifyDataSetChanged();
				}
			});
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null || convertView.getTag() == null
					|| !convertView.getTag().equals(f.get(position).getTag())) {
					ActorImageButton v = new ActorImageButton(act, f, position);
					((TapChainView)act).buttons.put((String)v.getTag(), v);
					convertView = v;
			}
			convertView.setId(300 + position);
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return 300+position;
		}

		@Override
		public Object getItem(int position) {
			return f.get(position).getTag();
		}

		@Override
		public int getCount() {
			return f.getSize();
		}
	}

	public static class ActorImageButton extends ActorImage implements
			View.OnTouchListener, OnGestureListener {
		OverlayPopup p;
		final TapChainView act;
		final Factory<Actor> factory;
		final int num;
		private GestureDetector touchDetector;

		ActorImageButton(Context c, Factory<Actor> f, final int j) {
			super(c, f, j);
			registerToFactory();
			act = (TapChainView) c;
			touchDetector = new GestureDetector(act, this);
			factory = f;
			num = j;
			setOnTouchListener(this);
		}

        private GridFragment returnPaletteAble() {
            act.dummyRemove();
            GridFragment f1 = act.getGrid();
            f1.enable();
            return f1;
        }

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (touchDetector.onTouchEvent(event))
				return true;
			int action = event.getAction();
			// Log.w("Action", String.format("action = %d", action));
			switch (action) {
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
                GridFragment f1 = returnPaletteAble();
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
			if (p == null)
				p = new OverlayPopup(act);
			p.setPopupView(factory, num);
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
			act.dummyMoveTo(e2.getRawX(), e2.getRawY());
			p.show((int) e2.getRawX(), (int) e2.getRawY());
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
            GridFragment f1 = returnPaletteAble();
			if (f1 != null
					&& f1.contains((int) e2.getRawX(), (int) e2.getRawY())) {
				p.dismiss();
				p = null;
				return true;
			}
			act.add(factory, num, e2.getRawX(), e2.getRawY(), velocityX,
					velocityY);
			p.dismiss();
			p = null;
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
            returnPaletteAble();
			act.add(factory, num);
			p.dismiss();
			return true;
		}

	}

	public static class OverlayPopup extends PopupWindow {
		int halfw, halfh;
		View v = null;
		Context cxt = null;
		float lowx = 0f, lowy = 0f;

		public OverlayPopup(Context c) {
			super(c);
			cxt = c;
			setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		}

		public void setPopupView(Factory<?> f, int i) {
			v = new ActorImage(cxt, f, i);
			setContentView(v);
			// The following line is to prevent PopupWindow from drawing odd
			// background.
			setBackgroundDrawable(new BitmapDrawable());
		}

		public void setPopupView(View v) {
			this.v = v;
			setContentView(v);
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
			lowx = x;
			lowy = y;
		}
	}

	public static class ActorImage extends ImageView {
		AndroidView v;
		IBlueprint b;
		Drawable a;
		ActorImage(Context c, Factory<?> f, final int j) {
			super(c);
			try {
				if (f == null)
					f = ((TapChainView) c).getEditor().getFactory();
				v = (AndroidView) f.getViewBlueprint(j).newInstance(null);
				b = f.get(j);
			} catch (ChainException e) {
				e.printStackTrace();
			}
			a = (v != null) ? v.getDrawable() : getResources()
					.getDrawable(R.drawable.cancel);
			setImageDrawable(a);
			if (v != null) {
				setTag(v.getTag());
			}
            setId(tapOffset+j);
		}

		public void registerToFactory() {
			if(v instanceof IBlueprintFocusNotification)
				b.setNotification(
						(IBlueprintFocusNotification) v);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			IPoint size = v.getSize()._valueGet();
			a.setBounds(0, 0, (int)size.x(), (int)size.y());
			a.draw(canvas);
		}
	}

    public class WritingViewImplLattice extends WritingView {
        private Bitmap b = BitmapFactory.decodeResource(
                TapChainView.this.getResources(), R.drawable.car);
        public WritingViewImplLattice(Context context) {
            super(context);
        }
        @Override
        public void myDraw(Canvas canvas) {
            viewUser.paintBackground(canvas);
            canvas.setMatrix(matrix);
            canvas.drawBitmap(b, 100f, 100f, paint);
            int w = 100, h = 100;
            IPoint lefttop = getPosition(0f, 0f);
            IPoint rightbottom = getPosition(canvas.getWidth(),
                    canvas.getHeight());
            float offsetx = w / 2, offsety = h / 2;
            float startx = lefttop.x() - lefttop.x() % w - w + offsetx;
            float starty = lefttop.y() - lefttop.y() % h - h + offsety;
            float endx = rightbottom.x() + offsetx;
            float endy = rightbottom.y() + offsety;
            for (float i = startx; i < endx; i += w) {
                canvas.drawLine(i, starty, i, endy, paint);
            }
            for (float j = starty; j < endy; j += h) {
                canvas.drawLine(startx, j, endx, j, paint);
            }
            getEditor().show(canvas);
            viewUser.myDraw(canvas);
        }
    }

    public class CanvasViewImpl2 extends WritingView {

        public CanvasViewImpl2(Context context) {
            super(context);
        }

        @Override
        public void myDraw(Canvas canvas) {
            viewUser.paintBackground(canvas);
            canvas.setMatrix(matrix);
            getEditor().show(canvas);
            getEditor().userShow(canvas);
        }
    }

	public abstract class WritingView extends TapChainSurfaceView {
		private int width = 100;
		private int height = 100;
		private int max_x = 500, max_y = 500;
		private TapChainEditor editor;

		public WritingView(Context context) {
			super(context);
			setSize(300, 300);
			move(-100, -100);
			editor = new TapChainAndroidEditor(this, getResources(), TapChainView.this);
			editor.kickTapDraw(null);
			// paint.setShadowLayer(10, 20, 20, 0x80000000);
			gdetect = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onDown(MotionEvent e) {
					sendDownEvent(e.getX(), e.getY());
					return true;
				}

				@Override
				public void onShowPress(MotionEvent e) {
					getEditor().onShowPress();
				}

				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					// getEditor().editTap().getChain().TouchOff();
					return false;
				}

				@Override
				public boolean onDoubleTap(MotionEvent e) {
					((TapChainView) getContext()).setVisibility();
					// getEditor().editTap().getChain().TouchOff();
					return false;
				}

				@Override
				public boolean onDoubleTapEvent(MotionEvent e) {
					return false;
				}

				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					getEditor().onSingleTapConfirmed();
					return false;
				}

				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2,
						float distanceX, float distanceY) {
					if (mode == CAPTURED) {
						return onSecondTouch(getPosition(e2.getX(), e2.getY()));
					}
					GridFragment f1 = getGrid();
					if (f1 != null
							&& f1.contains((int) e2.getRawX(),
									(int) e2.getRawY())) {
						standbyRegistration();
						return true;
					}
					getEditor().onScroll(getVector(-distanceX, -distanceY),
							getPosition(e2.getX(), e2.getY()));
					return false;
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


			});
		}

		/**
		 * @return the editor
		 */
		public TapChainEditor getEditor() {
			return editor;
		}
		
		private void setSize(int max_x, int max_y) {
			this.max_x = max_x;
			this.max_y = max_y;
		}


		void drawText(Canvas canvas, String str) {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);
			canvas.drawText(str, 100, 100, paint);
		}

		int index = 0;

		public void onAdd(Factory<Actor> f, int code) {
			getEditor().onAdd(f, code, null);
		}
		
		public void onAdd(Factory<Actor> f, int code, float x, float y) {
			getEditor().onAdd(f, code, getPosition(x, y)).getValue();
		}

		public void onAdd(Factory<Actor> f, int code, float x, float y,
				float dx, float dy) {
			IActorTap added = getEditor().onAdd(f, code, getPosition(x, y))
					.getValue();
			getEditor().captureTap(added);
			getEditor().onFling((int) dx, (int) dy);
		}

		public void onDummyAdd(Factory<Actor> f, int num, float x, float y) {
			if (f.getSize() > num) {
				try {
					getEditor().onDummyAdd(getEditor().createView(f, num, getEditor().editTap()),
							getPosition(x, y));
				} catch (ChainException e) {
					getEditor().editTap().error(e);
				}
			}
		}

		public void onDummyMoveTo(float x, float y) {
			getEditor().onDummyScroll(getPosition(x, y));
		}

		public void onDummyRemove() {
			getEditor().onDummyRemove();
		}

		public void sendDownEvent(float x, float y) {
			getEditor().onDown(getPosition(x, y));
		}

		public void sendUpEvent() {
			getEditor().onUp();
		}

		int initNum = 0;
		public void standbyRegistration() {
			GridFragment f1 = getGrid();
			if (f1 != null) {
				Factory f = f1.getCurrentFactory();
//				f.Register(f.get(0));
				IBlueprintInitialization i = getEditor().standbyRegistration(f);
				if(i != null) {
					try {
					    FileOutputStream fos = openFileOutput(String.format("SaveData%d.dat", initNum), MODE_MULTI_PROCESS);
						Log.w("test", String.format("num = %d, tag = %s", initNum, i.getTag()));
					    inclementInitNum();
					    ObjectOutputStream oos = new ObjectOutputStream(fos);
					    oos.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		public void deleteRegistration() {
			for(int i = 0; i < initNum; i++)
				if(!deleteFile(String.format("SaveData%d.dat", i)))
					break;
			initNum = 0;
		}
		
		public void inclementInitNum() {
			initNum++;
		}
		
		public void registerBlueprint(IBlueprintInitialization bi) {
			GridFragment f1 = getGrid();
			if (f1 != null) {
				Factory f = f1.getCurrentFactory();
				getEditor().registerBlueprint(f, bi);
			}
		}
		
		@Override
		public void move(float vx, float vy) {
			IPoint v = getScreenVector(-vx, -vy);
			matrix.postTranslate(v.x(), v.y());
			float[] rtn = new float[9];
			matrix.getValues(rtn);
			float x = rtn[Matrix.MTRANS_X], dx = 0f;
			float y = rtn[Matrix.MTRANS_Y], dy = 0f;
			boolean change = false;
			if(x < 0f) {
				dx = -x;
				change = true;
			} else if(x > 300f) {
				dx = 300f - x;
				change = true;
			}
			if(y < 0f) {
				dy = -y;
				change = true;
			} else if(y > 300f) {
				dy = 300f - y;
				change = true;
			}
			if(change)
				matrix.postTranslate(dx, dy);
			matrix.invert(inverse);
		}

		@Override
		public boolean onSecondTouch(final IPoint wp) {
			return getEditor().onLockedScroll(wp);
		}
	}

	public class UserView extends TapChainSurfaceView {
		GradientDrawable mGradient, mGradient2;
		Rect r = new Rect();

		UserView(Context context) {
			super(context);
			mGradient = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] {
					0xff303030, 0xff777777 });
			mGradient2 = new GradientDrawable(Orientation.RIGHT_LEFT,
					new int[] { 0xff303030, 0xff777777 });
			paint.setColor(0xff303030);
			paint.setStyle(Style.FILL);
		}

		@Override
		public void paintBackground(Canvas canvas) {
			 canvas.drawRect(r,
			 paint);
			return;
		}

		@Override
		public void myDraw(Canvas canvas) {
			getEditor().userShow(canvas);
			canvas.drawText("Goal = " + TapChainGoalTap.printState(), 20, 100,
					paint_text);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			int xmax = getWidth(), ymax = getHeight();
			int xcenter = xmax / 2;
			r.set(0, 0, xmax, ymax);
			mGradient.setBounds(new Rect(0, 0, xcenter, ymax));
			mGradient2.setBounds(new Rect(xcenter, 0, xmax, ymax));
			super.surfaceChanged(holder, format, width, height);
		}
	}

	abstract class TapChainSurfaceView
	// extends TextureView implements
			extends SurfaceView implements SurfaceHolder.Callback,
			IWindow
	{
		protected GestureDetector gdetect;
		Matrix matrix = new Matrix();
		Matrix inverse = new Matrix();
		Paint paint = new Paint(), paint_text = new Paint();
		WorldPoint window_size = new WorldPoint();
		String log = "";

		public TapChainSurfaceView(Context context) {
			super(context);
//			gdetect = new GestureDetector(context, this);
			SurfaceHolder holder = getHolder();
			holder.setFormat(PixelFormat.TRANSPARENT);
			holder.addCallback(this);
			paint_text.setColor(0xff000000);
			paint_text.setTextSize(20);
			paint.setColor(0xff444444);
			setFocusable(true);
			requestFocus();
		}

		@Override
		public WorldPoint getWindowSize() {
			return window_size;
		}

		public void onDraw() {
			TapChainView.this.runOnUiThread(new Runnable() {


				@Override
				public void run() {
				Canvas canvas = null;
				try {
					canvas = getHolder().lockCanvas();
					if (canvas != null) {
						paintBackground(canvas);
						myDraw(canvas);
						canvas.drawText(
								"View = "
										+ Integer.toString(getEditor()
												.editTap().getChain()
												.getViewNum()), 20, 20,
								paint_text);
						canvas.drawText(
								"Effect = "
										+ Integer.toString(getEditor()
												.editTap().getChain()
												.getPieces().size()), 20,
								40, paint_text);
						canvas.drawText(
								"UserView = "
										+ Integer.toString(getEditor()
												.edit()
												.getChain().getViewNum()),
								20, 60, paint_text);
						canvas.drawText(
								"UserEffect = "
										+ Integer.toString(getEditor()
										.edit()
										.getChain().getPieces()
										.size()), 20, 80,
								paint_text);
						canvas.drawText("Log = "+log, 20, 120, paint_text);
					}
				} finally {
					if (canvas != null)
						getHolder().unlockCanvasAndPost(canvas);
				}

				}

			});
		}

		public abstract void myDraw(Canvas canvas);
		
		public void log(String...strings) {
			StringBuilder buf = new StringBuilder();
			for(String s : strings)
				buf.append(s);
			log = buf.toString();
		}

		public void paintBackground(Canvas canvas) {
			return;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			window_size.x = getWidth();
			window_size.y = getHeight();
			getEditor().kickTapDraw(null);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			window_size.x = getWidth();
			window_size.y = getHeight();
			getEditor().kickTapDraw(null);
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
			return (float) sqrt(x * x + y * y);
		}

		private void midPoint(PointF point, MotionEvent event) {
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x / 2, y / 2);
		}

		public boolean onTouchEvent(MotionEvent ev) {
			if (gdetect.onTouchEvent(ev))
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
					getCanvas().sendUpEvent();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == ZOOM) {
					float newDist = spacing(ev);
					matrix.set(savedMatrix);
					if (newDist > 10f) {
						float scale = newDist / oldDist;
						midPoint(mid, ev);
						matrix.postScale(scale, scale, mid.x, mid.y);
					}
				} else if (mode == CAPTURED) {
					onSecondTouch(getPosition(ev.getX(), ev.getY()));
					break;
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


		public void move(float vx, float vy) {
		}

		protected void setMode(int _mode) {
			mode = _mode;
		}

		public IPoint getPosition(float x, float y) {
			return TapChainView.getPosition(x, y, inverse);
		}

		public IPoint getVector(float x, float y) {
			return TapChainView.getVector(x, y, inverse);
		}

		public IPoint getScreenVector(float x, float y) {
			return TapChainView.getVector(x, y, matrix);
		}

		public IPoint getScreenPosition(float x, float y) {
			return TapChainView.getPosition(x, y, matrix);
		}

		public boolean onSecondTouch(final IPoint iPoint) {
			return false;
		}
		@Override
		public void showPalette(final PaletteSort sort) {
			((Activity) getContext()).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(getGrid() != null)
						getGrid().setCurrentFactory(sort.getNum());
					}
			});
		}

	}

	public IWindow getActorWindow() {
		return getCanvas();
	}

	public static IPoint getPosition(float x, float y, Matrix matrix) {
		float[] pos = new float[] { x, y };
		matrix.mapPoints(pos);
		return new WorldPoint(pos[0], pos[1]);

	}

	public static IPoint getVector(float x, float y, Matrix matrix) {
		float[] pos = new float[] { x, y };
		matrix.mapVectors(pos);
		return new WorldPoint(pos[0], pos[1]).setDif();
	}

	public void recoverFactory() {
		try {
			for(int i = 0 ; true; i++) {
			    FileInputStream fis = openFileInput(String.format("SaveData%d.dat", i));
			    if(fis == null)
			    	break;
			    getCanvas().inclementInitNum();
			    ObjectInputStream ois = new ObjectInputStream(fis);
			    BlueprintInitialization data = (BlueprintInitialization) ois.readObject();
			    ois.close();
			    if(data != null) {
			    	if(data.getObject() != null)
			    		Log.w("test", String.format("recoverFactory tag = %s, obj = %s", data.getTag(), data.getObject().toString()));
					getCanvas().registerBlueprint(data);
			    } else {
			    	break;
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}