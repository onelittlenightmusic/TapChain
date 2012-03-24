package org.tapchain;

import java.util.ArrayList;

import org.tapchain.AndroidPiece.AndroidView;
import org.tapchain.AnimationChain.*;
import org.tapchain.Chain.ChainException;
import org.tapchain.TapChainEditor.*;

import org.tapchain.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window.Callback;
import android.view.WindowManager;
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
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class TapChainView extends Activity implements SensorEventListener {
	static boolean DEBUG = true;
	
	static WritingView view_edit;
	GridFragment sel;
	static FrameLayout controlview = null;
    SensorManager sensorManager;
    MediaPlayer player = null;
  private Sensor accelerometer;//‰Á‘¬“x‚¹ƒ“ƒT[
	static FrameLayout rootview = null;
	Handler mq = new Handler();
	static FrameLayout frameLayout;
	TapChainEditor editor = null;
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Writing Window Initialization
		AndroidPiece.setActivity(this);
		view_edit = new WritingView(this, this);
		editor = new TapChainEditor(view_edit);
//		editor.InitWindow(view_edit);
//			editor.Create();
		TapChainEditor.setModelAction(new ModelActionCallback() {
			@Override
			public boolean end() {
				editor.kickDraw();
				return false;
			}
			@Override
			public boolean redraw(String str) {
				view_edit.onDraw();
				return true;
			}
		});
		
		rootview = new FrameLayout(this);
    frameLayout = new FrameLayout(this)
//    {
//    	public boolean onInterceptTouchEvent (MotionEvent event) {
//				int action = event.getAction();
//				switch (action) {
//				case MotionEvent.ACTION_MOVE:
//					p.getContentView().dispatchTouchEvent(event);
//					break;
//				}
//				return false;
//    	}
//    }
    ;
		setContentView(frameLayout);
		controlview = new FrameLayout(this);
		LinearLayout view_bottom_left = new LinearLayout(this);
		view_bottom_left.setGravity(Gravity.LEFT | Gravity.BOTTOM);
		Button button_start = new Button(this);
		Button button_up = new Button(this);
		Button button_clear = new Button(this);
		Button button_refresh = new Button(this);
		button_start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				view_edit.PressButton();
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
		button_refresh.setText("!");
		button_refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editor.Mode(EditMode.RENEW);
			}
		});
		view_bottom_left.addView(button_start,
				new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		view_bottom_left.addView(button_up,
				new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		view_bottom_left.addView(
				button_clear, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		view_bottom_left.addView(
				button_refresh, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		controlview.addView(
				view_bottom_left, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));		
		LinearLayout view_bottom_right = new LinearLayout(this);
		view_bottom_right.setId(0x12345678);
		view_bottom_right.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
		Button button_plus = new Button(this);
		button_plus.setText(" [+] ");
		button_plus.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
		button_plus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sel.show(GridShow.HALF);
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
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		controlview.addView(view_bottom_right, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
//		LinearLayout view_center_left = new LinearLayout(this);
//		LinearLayout view_center_right = new LinearLayout(this);
//		view_center_left.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
//		view_center_right.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
//		controlview.addView(view_center_left,
//				new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//		controlview.addView(view_center_right,
//				new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		//		LinearLayout viewReplay = new LinearLayout(this);
//		viewReplay.addView(new ReadingView(this),
//				new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//		viewReplay.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
//		rootview.addView(viewReplay, new LayoutParams(
//				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		rootview.addView(view_edit,
				new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		sel = new GridFragment(this);
		
//		rootview.addView(sel, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//    ViewAdapter adapter = new ViewAdapter();
//    for(int j=0; j<pf.getSize()+1; j++) {
//    	View v = new LocalButton(this,j);
//    	final int NUM = j;
//    	v.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
////					if(rootview.getDisplayedChild() ==2) {
//						setCode(NUM);
////						rootview.showPrevious();
////					}
//				}
//			});
//     	adapter.add(v);
//    }
//    sel.setAdapter(adapter);

		frameLayout.addView(rootview);
//		frameLayout.addView(linearLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		frameLayout.addView(controlview, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		frameLayout.setId(0x01234567);

//		rootview.showNext();

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		player = MediaPlayer.create(this, R.raw.tennisball);
		
		LinearLayout l = new LinearLayout(this);
		rootview.addView(l);
		l.setGravity(Gravity.BOTTOM|Gravity.RIGHT);
    l.setId(0x00001234);
//    rootview.setForegroundGravity(Gravity.BOTTOM|Gravity.RIGHT);
//    sel.setSize(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//    FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
//   if(null == getSupportFragmentManager().findFragmentByTag("SELECTB")) {
//    	ft2.add(0x00001234, sel, "SELECTB");
//      ft2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//      ft2.hide(sel);
//    	ft2.commit();
//    }
	}
	
//	public class OverlayFragment extends Fragment {
//		int piece_num = 0;
//		OverlayFragment(int num) {
//			super();
//			piece_num = num;
//		}
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
//			View v = new LocalButton(getActivity(), piece_num);
//			v.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//			return v;
//		}
//	}
	public enum GridShow { SHOW, HIDE, HALF }
	public static class GridFragment extends Fragment {
		int _width = LayoutParams.FILL_PARENT, _height = LayoutParams.FILL_PARENT;
		TapChainView a = null;
		public GridFragment(TapChainView a) {
			super();
			this.a = a;
		}
	  @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
			ViewSelector sel = a.new ViewSelector(getActivity());
	    ViewAdapter adapter = new ViewAdapter();
	    for(int j=0; j<a.editor.GetFactory().getSize()+1; j++)
	    	adapter.add(a.new LocalButton(j));
	    sel.setAdapter(adapter);
	    sel.setLayoutParams(new LayoutParams(_width, _height));
	    return sel;
	  }
	  public void setSize(int w, int h) {
	  	_width = w;
	  	_height = h;
	  }
	  public void show(GridShow _show) {
	    FragmentTransaction ft = a.getFragmentManager().beginTransaction();
	    if(null == a.getFragmentManager().findFragmentByTag("SELECTB")) {
	      ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//      ft.addToBackStack(null);
	    	ft.add(0x00001234, this, "SELECTB");
	    }
//	    else {
//	    	ft.replace(0x00001234, this, "SELECTB");
//	    }
	    switch(_show) {
    	case SHOW:
    		setSize(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//    		Pair<Integer, Integer> p = a.checkDisplayAndRotate();
//    		setSize(p.first, p.second);
//    		setSize(100,100);
	    	ft.show(this);
	    	break;
    	case HALF:
    		Pair<Integer, Integer> p1 = a.checkDisplayAndRotate();
//    		setSize(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    		setSize(p1.first, p1.second);
 	    	ft.show(this);
	    	break;
    	case HIDE:
//	    } else {
	    	ft.hide(this);
	    }
    	ft.commit();
	  		
	  }
	}
	
	public class ViewSelector extends GridView {
		ViewSelector(Activity act) {
        super(act);
        setBackgroundColor(0xff000000);
//        view_select_contents.setOrientation(LinearLayout.VERTICAL);
//        	setStretchMode(GridView.STRETCH_SPACING_UNIFORM);
        setColumnWidth(100);
        setVerticalSpacing(0);
        setHorizontalSpacing(0);
        setNumColumns(GridView.AUTO_FIT);
        setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
  			setOnTouchListener(new View.OnTouchListener() {
  				@Override
  				public boolean onTouch(View v, MotionEvent event) {
  					int action = event.getAction();
//  					Log.w("Action", String.format("action = %d", action));
  					switch (action) {
  					case MotionEvent.ACTION_DOWN:
  					case MotionEvent.ACTION_POINTER_DOWN:
  						default:
  							sel.show(GridShow.HIDE);
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
	public class LocalButton extends PieceImage {
		LocalButton(final int j) {
			super(j);
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
							p.setView(j);
							sel.show(GridShow.HIDE);
							p.show((int)event.getRawX(), (int)event.getRawY());
//							LocalButton.this.clearFocus();
							break;
						case MotionEvent.ACTION_MOVE:
//							p.getContentView().dispatchTouchEvent(event);
							p.show((int)event.getRawX(), (int)event.getRawY());
							break;
						case MotionEvent.ACTION_POINTER_UP:
						case MotionEvent.ACTION_UP:
							view_edit.sendDownEvent(event.getRawX(), event.getRawY());
							setCode(j);
				    	p.dismiss();
						case MotionEvent.ACTION_CANCEL:
							break;
						}
					return true;
				}
			});
			
		}
	}
	
	public class OverlayPopup extends PopupWindow {
		int halfw, halfh;
		View v = null;
		OverlayPopup (int j) {
			super();
			setView(j);
			setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
		public void setView(int i) {
			v = new PieceImage(i);
			setContentView(v);
		}
		public void show(int x, int y) {
			if(!isShowing())
				showAtLocation(frameLayout, Gravity.NO_GRAVITY,
					x-v.getWidth()/2, y-v.getHeight()/2
					);
			else
				update(
						x-v.getWidth()/2, y-v.getHeight()/2,
						-1, -1);
		}
	}
	public class PieceImage extends ImageView {
		PieceImage(final int j) {
			super(TapChainView.this);
//			setFocusable(false);
			PieceFactory f = editor.GetFactory();
			String name = String.format("%d",j);
			if(f.getName(j)!=null)
				name += f.getName(j);
			final AndroidView v = (AndroidView) f.getView(j, null);
			Drawable a = (v!=null)?v.getDrawable():getResources().getDrawable(R.drawable.withface1);
			setImageDrawable(a);
		}
	}
	
   @Override
    protected void onStop() {
     super.onStop();
	   if(sensorManager != null)
        sensorManager.unregisterListener(this);
    }
   @Override
	public void onConfigurationChanged(Configuration newConfig) {
  	 super.onConfigurationChanged(newConfig);
//  	 checkDisplayAndRotate();
   }
   
   public Pair<Integer, Integer> checkDisplayAndRotate() {
     DisplayMetrics metrix = new DisplayMetrics();
     getWindowManager().getDefaultDisplay().getMetrics(metrix);
     if(metrix.widthPixels > metrix.heightPixels)
    	 return new Pair<Integer, Integer>(metrix.widthPixels/2,LayoutParams.FILL_PARENT);
  	 return new Pair<Integer, Integer>(LayoutParams.FILL_PARENT, metrix.heightPixels/2);
//     setDisplayEditor("SELECT", metrix.widthPixels > metrix.heightPixels);
   }
   
   public void setDisplayEditor(GridFragment f, boolean visible) {
//  		FragmentManager fm = getSupportFragmentManager();
//   		FragmentTransaction ft = fm.beginTransaction();
//   		Fragment mFragment1 = fm.findFragmentByTag(tag);
//   		if (mFragment1 != null) {
//   			if(visible)
//   				ft.show(mFragment1);
//  			else
//  				ft.hide(mFragment1);
//   			ft.commit();
//   		}
//  	 f.show(visible);
 	 
   }
	@Override
	protected void onResume() {
	    super.onResume();
	  	checkDisplayAndRotate();
	    try {
	    accelerometer = sensorManager
	    	.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
	    sensorManager.registerListener(this, accelerometer,
	    	    SensorManager.SENSOR_DELAY_FASTEST);
	    Log.i("DEBUG", String.format("registered %d",sensorManager
	    	    .getSensorList(Sensor.TYPE_ACCELEROMETER).size()));
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
	}
    private float[] currentAccelerationValues = {0.0f, 0.0f, 0.0f};
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	float targetValue = 0;
	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		if(sensor == accelerometer) {
			float tmp = targetValue;
			//Getting a result from accelerometer.
            currentAccelerationValues[0] = event.values[0];// - currentOrientationValues[0];
            currentAccelerationValues[1] = event.values[1];// - currentOrientationValues[1];
            currentAccelerationValues[2] = event.values[2];// - currentOrientationValues[2];
            targetValue = 
                Math.abs(currentAccelerationValues[0]) + 
                Math.abs(currentAccelerationValues[1]) +
                Math.abs(currentAccelerationValues[2]);
            if(tmp > targetValue)
            	return;
            //shake if absolute value of accelerometer is more than threshold.
            if(targetValue > 20.0f) {
            	Float rtn = targetValue/60f;
            	if(rtn>2f) rtn=2f;
            	if(rtn<0.5f) rtn=0.5f;
    			editor.GetManager().Get().Shake(rtn);
//    			ChainEditor.kickDraw("Shake!?");
            }
       }
	}

	public void Shake(int interval) {
      Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
      vibrator.vibrate(interval);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case AndroidPiece.VOICE_REQUEST:
			if(resultCode != RESULT_OK)
				break;
	            // get the returned value
	            ArrayList<String> results =
	                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	            
	            // refer the value
	            StringBuffer buffer = new StringBuffer();
	            for (int i = 0; i < results.size(); i++) {
	                buffer.append(results.get(i));
	            }
	            AndroidPiece.recognized.push(buffer.toString());
	            // show the value
	            Toast.makeText(this, buffer.toString(),
	                    Toast.LENGTH_LONG).show();
	            break;
		case 0:
		default:
			setCode(data.getIntExtra("TEST", 0));
		}
		return;
	}
	public static void setVisibility() {
		if(controlview.getVisibility() == View.VISIBLE)
			controlview.setVisibility(View.INVISIBLE);
		else
			controlview.setVisibility(View.VISIBLE);
	}
	
	public void setCode(int code) {
		if(code < editor.GetFactory().getSize())
			editor.GetFactory().getInstance(code);
	}
	
	public class WritingView extends BasicSurfaceView implements
			GestureDetector.OnDoubleTapListener {
		TapChainView v = null;

		public WritingView(Context context, TapChainView v) {
			super(context);
			window_orient.x = -100;
			window_orient.y = -100;
			this.v = v;
		}
		
		@Override
		public void draw(Canvas canvas) {
//				if(DEBUG) {
//					Log.i("MAIN_DEBUG", str);
//				}
				canvas.setMatrix(matrix);
				editor.Show(canvas);
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

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return editor.onFling((int)velocityX, (int)velocityY);
		}

		@Override
		public void onLongPress(MotionEvent e) {
			editor.onLongPress();
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
//			Log.w("TouchPoint", String.format("move point %f %f", e2.getX(), e2.getY()));
			editor.onScroll(getVector(-distanceX, -distanceY), getPosition(e2.getX(), e2.getY()));
			return false;
		}
		
		public WorldPoint getPosition(float x, float y) {
			float[] pos = new float[] {x, y};
			inverse.mapPoints(pos);
			return new ScreenPoint((int)pos[0], (int)pos[1]).getWorldPoint(this);
		}
		
		public WorldPoint getVector(float x, float y) {
			float[] pos = new float[] {x, y};
			inverse.mapVectors(pos);
			return new WorldPoint((int)pos[0], (int)pos[1]);
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			editor.GetManager().Get().TouchOff();
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			TapChainView.setVisibility();
			editor.GetManager().Get().TouchOff();
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
//			if(ChainEditor.onSingleTapConfirmed()) {
//				((TapChainView)getContext()).setDisplayEditor("SELECT", false);
//			}
			return true;
		}

		public boolean PressButton() {
			editor.Compile();
			editor.Start();
			return true;
		}
/*		
		public boolean GoParent() {
			if(ChainEditor.isRoot()) {
				return false;
			}
			ChainEditor.goParent();
			return true;
		}
		*/
	}
	public class ReadingView extends BasicSurfaceView {
		ReadingView(Context context) {
			super(context);
		}

		@Override
		public void draw(Canvas canvas) {
		}
	}
	abstract class BasicSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback,
		GestureDetector.OnGestureListener,
		TapChainEditor.IWindow {
		GradientDrawable mGradient, mGradient2;
		private GestureDetector gdetect = new GestureDetector(this);
		Matrix matrix = new Matrix();
		Matrix inverse = new Matrix();
		Paint paint = new Paint(), paint_text = new Paint();
		public BasicSurfaceView(Context context) {
			
			super(context);
			mGradient = new GradientDrawable(
					Orientation.LEFT_RIGHT,
					new int[] { 0xff303030, 0xff777777 });
			mGradient2 = new GradientDrawable(
					Orientation.RIGHT_LEFT,
					new int[] { 0xff303030, 0xff777777 });
			getHolder().addCallback(this);
			paint_text.setColor(0xff000000);
			paint_text.setTextSize(20);
			paint.setColor(0xff444444);
	        setFocusable(true);
	        requestFocus();
		}

		public void onDraw() {
			Canvas canvas = getHolder().lockCanvas();
			if (canvas != null) {
				paintBackground(canvas);
				draw(canvas);
				canvas.drawText("View = "+Integer.toString(editor.GetManager().Get().getViewNum()), 20, 20, paint_text);
				canvas.drawText("Effect = "+Integer.toString(editor.GetManager().Get().aFunc.size()), 20, 40, paint_text);
				canvas.drawText("View = "+Integer.toString(editor.GetUserManager().Get().getViewNum()), 20, 20, paint_text);
				canvas.drawText("UserEffect = "+Integer.toString(editor.GetUserManager().Get().aFunc.size()), 20, 60, paint_text);
				getHolder().unlockCanvasAndPost(canvas);
			}
		}
		public abstract void draw(Canvas canvas);
		public void paintBackground(Canvas canvas) {
//			mGradient.draw(canvas);
//			mGradient2.draw(canvas);
			canvas.drawRect(new Rect(0, 0, window_size.x, window_size.y), paint);
			return;
		}
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			int xmax = getWidth(), ymax = getHeight();
			int xcenter = xmax/2;
			mGradient.setBounds(new Rect(0,0,xcenter,ymax));
			mGradient2.setBounds(new Rect(xcenter,0,xmax, ymax));
			window_size.x = getWidth();
			window_size.y = getHeight();
			editor.kickDraw();
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			int xmax = getWidth(), ymax = getHeight();
			int xcenter = xmax/2;
			mGradient.setBounds(new Rect(0,0,xcenter,ymax));
			mGradient2.setBounds(new Rect(xcenter,0,xmax, ymax));
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
                   Log.d(TAG, "mode=ZOOM" );
                   editor.onUp();
                }
                break;
			case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    float newDist = spacing(ev);
                    matrix.set(savedMatrix);
                    // If you want to tweak font scaling, this is the place to go.
                    if (newDist > 10f) {
                        float scale = newDist / oldDist;
                        midPoint(mid, ev);
                        Log.d(TAG, "scale="+String.valueOf(scale) );
//                        oldDist = newDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                   }
                }
				// put_points(ev);
				break;
			case MotionEvent.ACTION_UP:
				mode = NONE;
//				points.clear();
//				onDraw();
				editor.onUp();			
				break;
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				matrix.invert(inverse);
				// put_points(ev);
				// int index = (action & MotionEvent.ACTION_POINTER_ID_MASK) >>
				// MotionEvent.ACTION_POINTER_ID_SHIFT;
				// points.remove(ev.getPointerId(index));
				break;
			}
			return true;
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			

		}

		public boolean onDown(MotionEvent e) {
//			((View) this.getParent()).setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 250));
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
		
		public void move(int vx, int vy) {
			window_orient.x += vx;
			window_orient.y += vy;
		}

	}
}