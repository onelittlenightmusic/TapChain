package org.tapchain.realworld;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Point;
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
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Display;
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
import org.tapchain.core.LinkType;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.EditorReturn;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.ITap;
import org.tapchain.editor.IWindow;
import org.tapchain.editor.TapChainEditor;
import org.tapchain.editor.TapChainEditor.FACTORY_KEY;
import org.tapchain.game.ISensorView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.sqrt;

public class TapChainView extends Activity implements
        SensorEventListener, ISensorView {
    static final String VIEW_SELECT = "SELECT";
    static final String X = "LOCATIONX", V = "VIEWS";
    static final RectF RF = new RectF(0, 0, 100, 100);

    private WritingView viewCanvas;
    FrameLayout viewControl = null;
    SensorManager sensorManager;
    private Sensor accelerometer;
    SparseArray<IntentHandler> intentHandlers = new SparseArray<>();
    static Activity now;
    public static int tapOffset = 10000;
    private TapChainEditor editor;
    String CANVAS_TAG = "Canvas";

    public static Activity getNow() {
        return now;
    }

    // 1.Initialization
    @Override
    public void onSaveInstanceState(Bundle out) {
        RectF r = new RectF(RF);
        getCanvas().matrix.mapRect(r);
//        out.putParcelable(X, r);
//        out.putParcelableArray(V,
//                getEditor().getTaps().toArray(new Parcelable[0]));
    }


    /**
     * Called when the activity is first created.
     */
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
//        if(viewCanvas == null) {
//            viewCanvas = new CanvasViewImpl2(this);
////            Log.w("test", "onCreate", new Throwable());
//        }
//        rootview.addView(getCanvas(), new LayoutParams(
//                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//        rootview.setId(0x00001236);
        root.addView(rootview);
        root.addView(viewControl, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        root.setId(0x00001235);
        root.setTag("OVERLAY");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        LinearLayout l2 = new LinearLayout(this);
        rootview.addView(l2);
        l2.setId(0x00001236);

        LinearLayout l = new LinearLayout(this);
        rootview.addView(l);
        l.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        l.setId(0x00001234);

        CanvasFragment canvas;
//        if (savedInstanceState == null) {
        if(getFragmentManager().findFragmentByTag(CANVAS_TAG) == null) {
            canvas = new CanvasFragment();
            Log.w("test", "onCreate", new Throwable());
            viewCanvas = canvas.setContext(this).view;
            FragmentTransaction ft = getFragmentManager().beginTransaction();
//            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.replace(0x00001236, canvas, CANVAS_TAG);
//            view.setLayoutParams(
//                    new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//            ft.show(this);
            ft.commit();
        }
        new GridFragment().setContext(this).show(GridShow.HIDE);
    }

    int leftnum = 100, rightnum = 200;


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
        GridFragment f = (GridFragment) getFragmentManager()
                .findFragmentByTag(VIEW_SELECT);
        return f;
    }

    public WritingView getCanvas() {
        return viewCanvas;
    }

    @Override
    protected void onStop() {
        super.onStop();
//        getEditor().reset();
//        Log.i("TapChainView.state", "onStop");
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
    }

    Point size = new Point();
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
            return new Pair<>(metrix.widthPixels * 1 / 2,
                    LayoutParams.MATCH_PARENT);
        return new Pair<>(LayoutParams.MATCH_PARENT,
                metrix.heightPixels * 1 / 2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Display disp = getWindowManager().getDefaultDisplay();
        disp.getSize(size);
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

    private float[] currentAccelerationValues = {0.0f, 0.0f, 0.0f};

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

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
            add(FACTORY_KEY.ALL, data.getIntExtra("TEST", 0), 0f, 0f);
        return;
    }

    public void setVisibility() {
        if (viewControl.getVisibility() == View.VISIBLE)
            viewControl.setVisibility(View.INVISIBLE);
        else
            viewControl.setVisibility(View.VISIBLE);
    }

    public Actor add(FACTORY_KEY key, String tag) {
        return getCanvas().onAdd(key, tag);
    }

    public Actor add(FACTORY_KEY key, String tag, float x, float y) {
        return getCanvas().onAdd(key, tag, x, y);
    }

    public Actor add(FACTORY_KEY key, String tag, float x, float y, float dx,
                         float dy) {
        return getCanvas().onAdd(key, tag, x, y, dx, dy);
    }

    public Actor add(FACTORY_KEY key, int code) {
        return getCanvas().onAdd(key, code);
    }

    public Actor add(FACTORY_KEY key, int code, float x, float y) {
        return getCanvas().onAdd(key, code, x, y);
    }

    public Actor add(FACTORY_KEY key, int code, float x, float y, float dx,
                         float dy) {
        return getCanvas().onAdd(key, code, x, y, dx, dy);
    }

    public void connect(Actor a1, LinkType type, Actor a2) {
        getEditor().connect(a1, type, a2);
    }


    public void finishThisFromOutside() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TapChainView.this.finish();
            }
        });
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
        if (getGrid() != null)
            getGrid().setCurrentFactory(sort.getNum());
    }

    public static class CanvasFragment extends Fragment {
        CanvasViewImpl2 view;
        TapChainView act;
        String tag = "Canvas";
        public CanvasFragment() {
            super();
//            setRetainInstance(true);
        }

        public CanvasFragment setContext(TapChainView a) {
            this.act = a;
            view = act.new CanvasViewImpl2(act);
            return this;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if(view == null)
                view = act.new CanvasViewImpl2(act);
            return this.view;
        }

    }

    public class CanvasViewImpl2 extends WritingView {
        Rect r = new Rect();

        public CanvasViewImpl2(Context context) {
            super(context);
            paint.setColor(0xff303030);
            paint.setStyle(Style.FILL);
        }


        @Override
        public void paintBackground(Canvas canvas) {
            canvas.drawRect(r, paint);
            return;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            int xmax = getWidth(), ymax = getHeight();
            r.set(0, 0, xmax, ymax);
            super.surfaceChanged(holder, format, width, height);
        }

        @Override
        public void myDraw(Canvas canvas) {
//            getEditor().userShow(canvas);
            canvas.drawText("Goal = " + TapChainGoalTap.printState(), 20, 100,
                    paint_text);
            canvas.setMatrix(matrix);
            getEditor().show(canvas);
            getEditor().userShow(canvas);
        }
    }

    public abstract class WritingView extends TapChainSurfaceView {
        IActorTap selected;
        public WritingView(Context context) {
            super(context);
//            setSize(300, 300);
            move(-100, -100);
            editor = new TapChainAndroidEditor(this, getResources(), TapChainView.this);
            editor.kickTapDraw(null);
            gdetect = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    ((TapChainView) getContext()).setVisibility();
                    return false;
                }

                @Override
                public boolean onDoubleTapEvent(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    return getEditor().onSingleTapConfirmed(getPosition(e.getX(), e.getY()));
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
                        standbyRegistration(selected);
                        return true;
                    }
                    getEditor().onScroll(selected, getVector(-distanceX, -distanceY),
                            getPosition(e2.getX(), e2.getY()));
                    return false;
                }


                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
                    return getEditor().onFling(selected, getPosition(e2.getRawX(), e2.getRawY()), getVector(velocityX, velocityY));
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    getEditor().onLongPress(selected);
                    setMode(CAPTURED);
                }
            });
        }

        public boolean onTouchEvent(MotionEvent ev) {
            if (gdetect.onTouchEvent(ev))
                return true;
            int action = ev.getAction();
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    ITap selectedTap = getEditor().onDown(getPosition(ev.getX(), ev.getY()));
                    if(selectedTap instanceof IActorTap)
                        selected = (IActorTap)selectedTap;
                    else
                        selected = null;
                    resetRegistration();
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    savedMatrix.set(matrix);
                    oldDist = spacing(ev);
                    Log.d(TAG, "oldDist=" + oldDist);
                    midPoint(mid, ev);
                    if (oldDist > 10f) {
                        mode = ZOOM;
                        Log.d(TAG, "mode=ZOOM");
                        getEditor().releaseTap(selected, getPosition(ev.getX(), ev.getY()));
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
                    getEditor().releaseTap(selected, getPosition(ev.getX(), ev.getY()));
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    matrix.invert(inverse);
                    break;
            }
            return true;
        }

        /**
         * @return the editor
         */
        public TapChainEditor getEditor() {
            return editor;
        }


        public Actor onAdd(FACTORY_KEY key, String tag) {
            return onAdd(key, tag, null, null);
        }

        public Actor onAdd(FACTORY_KEY key, String tag, float x, float y) {
            return onAdd(key, tag, getPosition(x, y), null);
        }

        public Actor onAdd(FACTORY_KEY key, String tag, float x, float y, float vx, float vy) {
            return onAdd(key, tag, getPosition(x, y), getVector(vx, vy));
        }


        public Actor onAdd(FACTORY_KEY key, String tag, IPoint pos, IPoint vec) {
            EditorReturn editorReturn = getEditor().onAdd(key, tag, pos);
            if (editorReturn == null)
                return null;
            if(vec == null)
                return editorReturn.getActor();
            getEditor().onFling(editorReturn.getTap(), pos, vec);
            return editorReturn.getActor();
        }

        public Actor onAdd(FACTORY_KEY key, int code) {
            return onAdd(key, code, null, null);
        }

        public Actor onAdd(FACTORY_KEY key, int code, float x, float y) {
            return onAdd(key, code, getPosition(x, y), null);
        }

        public Actor onAdd(FACTORY_KEY key, int code, float x, float y, float vx, float vy) {
            return onAdd(key, code, getPosition(x, y), getVector(vx, vy));
        }

        public Actor onAdd(FACTORY_KEY key, int code, IPoint pos, IPoint vec) {
            EditorReturn editorReturn = getEditor().onAdd(key, code, pos);
            if (editorReturn == null)
                return null;
            if(vec == null)
                return editorReturn.getActor();
            getEditor().onFling(editorReturn.getTap(), pos, vec);
            return editorReturn.getActor();
        }

        int initNum = 0;
        boolean standby = false;
        public void standbyRegistration(IActorTap selected) {
            GridFragment f1 = getGrid();
            if (standby || f1 == null) {
                return;
            }
            Factory f = f1.getCurrentFactory();
            IBlueprintInitialization i = getEditor().standbyRegistration(f, selected);
            if (i == null) {
                return;
            }
            standby = true;
            try {
                FileOutputStream fos = openFileOutput(String.format("SaveData%d.dat", initNum), MODE_MULTI_PROCESS);
                inclementInitNum();
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void resetRegistration() {
            standby = false;
        }

        public void inclementInitNum() {
            initNum++;
        }


        @Override
        public boolean onSecondTouch(final IPoint wp) {
            return getEditor().onLockedScroll(selected, wp);
        }
    }

    abstract class TapChainSurfaceView
            // extends TextureView implements
            extends SurfaceView implements SurfaceHolder.Callback,
            IWindow, ISensorView {
        protected GestureDetector gdetect;
        Matrix matrix = new Matrix();
        Matrix inverse = new Matrix();
        Paint paint = new Paint(), paint_text = new Paint();
        WorldPoint window_size = new WorldPoint();
        String log = "";
        TextPaint mTextPaint = new TextPaint();
        StaticLayout mTextLayout;

        public TapChainSurfaceView(Context context) {
            super(context);
//			gdetect = new GestureDetector(context, this);
            SurfaceHolder holder = getHolder();
            holder.setFormat(PixelFormat.TRANSPARENT);
            holder.addCallback(this);
            paint_text.setColor(0xff000000);
            paint_text.setTextSize(20);
            paint.setColor(0xff444444);
            mTextPaint.setTextSize(20);
            mTextLayout = new StaticLayout("", mTextPaint, 500, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            setFocusable(true);
            requestFocus();
        }

        public void onDraw() {
            TapChainView.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Canvas canvas = null;
                    try {
                        canvas = getHolder().lockCanvas();
                        if (canvas == null) {
                            return;
                        }
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
//						canvas.drawText("Log = "+log, 20, 120, paint_text);
                        canvas.save();
                        canvas.translate(20, 120);
                        mTextLayout.draw(canvas);
                        canvas.restore();
                    } finally {
                        if (canvas != null)
                            getHolder().unlockCanvasAndPost(canvas);
                    }

                }

            });
        }

        public abstract void myDraw(Canvas canvas);

        StringBuilder buf = new StringBuilder();

        public void log(String... strings) {
            for (String s : strings) {
                buf.append(s);
            }
            buf.append("\n");
            if (buf.length() > 300)
                buf.delete(0, buf.length() - 300);
            log = buf.toString();
            mTextLayout = new StaticLayout(log, mTextPaint, 500, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
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

        float spacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) sqrt(x * x + y * y);
        }

        void midPoint(PointF point, MotionEvent event) {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }


        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }

        @Override
        public void move(float vx, float vy) {
            IPoint v = getScreenVector(-vx, -vy);
            matrix.postTranslate(v.x(), v.y());
            matrix.invert(inverse);
        }

        @Override
        public boolean isInWindow(float x, float y) {
            IPoint d = getScreenPosition(x, y);
            if (size.x < d.x() || 0 > d.x() || size.y < d.y() || 0 > d.y()) {
                return false;
            }
            return true;
        }

        @Override
        public IPoint getMiddlePoint() {
            return getPosition(size.x/2f, size.y/2f);
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
                    if (getGrid() != null)
                        getGrid().setCurrentFactory(sort.getNum());
                }
            });
        }

        @Override
        public IPoint getTilt() {
            return TapChainView.this.getTilt();
        }

        @Override
        public void shake(int interval) {
            TapChainView.this.shake(interval);
        }
    }

    public IWindow getActorWindow() {
        return getCanvas();
    }

    public static IPoint getPosition(float x, float y, Matrix matrix) {
        float[] pos = new float[]{x, y};
        matrix.mapPoints(pos);
        return new WorldPoint(pos[0], pos[1]);

    }

    public static IPoint getVector(float x, float y, Matrix matrix) {
        float[] pos = new float[]{x, y};
        matrix.mapVectors(pos);
        return new WorldPoint(pos[0], pos[1]).setDif();
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
        ArrayList<FACTORY_KEY> factoryList = new ArrayList<>();

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
            addTab(tabH, "TS1", "[ + ]", FACTORY_KEY.ALL,
                    0xaa000000, R.drawable.plus);
            addTab(tabH, "TS2", "[ V ]", FACTORY_KEY.LOG,
                    0xaa220000, R.drawable.history);
            addTab(tabH, "TS3", "[ <=> ]", FACTORY_KEY.RELATIVES,
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
                           final FACTORY_KEY key, final int color, int resource) {
            TabSpec ts = h.newTabSpec(_tag);
            ts.setIndicator(""/* label */, getResources().getDrawable(resource));
            ts.setContent(new TabHost.TabContentFactory() {
                public View createTabContent(String tag) {
                    return new ActorSelector(act, key, color);
                }
            });
            // ts1.setContent(new Intent(this,Tab1.class));
            h.addTab(ts);
            factoryList.add(key);
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
            FragmentTransaction ft = act.getFragmentManager()
                    .beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            if (this != act.getFragmentManager().findFragmentByTag(tag)) {
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
            return act.getEditor().getFactory(factoryList.get(tabNum));
        }

        public void setCurrentFactory(int tabNum) {
            tabH.setCurrentTab(tabNum);
        }

    }

    public static class ActorSelector extends GridView {
        ActorSelector(final Activity act, FACTORY_KEY key, int color) {
            super(act);
            setBackgroundColor(color);
            setColumnWidth(100);
            setVerticalSpacing(0);
            setHorizontalSpacing(0);
            setNumColumns(GridView.AUTO_FIT);
            setAdapter(new ViewAdapter(act, this, key));
        }
    }

    HashMap<String, ActorImageButton> buttons = new HashMap<String, ActorImageButton>();

    public static class ViewAdapter extends BaseAdapter implements ValueChangeNotifier {
        private Factory<Actor> f;
        private FACTORY_KEY key;
        private TapChainView act;
        private GridView _parent;

        public ViewAdapter(Context c, GridView parent, FACTORY_KEY k) {
            act = (TapChainView) c;
            key = k;
            _parent = parent;
            f = Factory.copy(act.getEditor().getFactory(key));
            act.getEditor().getFactory(key).setNotifier(this);
        }

        @Override
        public void notifyChange() {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    f = new Factory(act.getEditor().getFactory(key));
                    ViewAdapter.this.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void invalidate() {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    f = new Factory(act.getEditor().getFactory(key));
                    _parent.invalidate();
                }
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || convertView.getTag() == null
                    || !convertView.getTag().equals(f.get(position).getTag())) {
                ActorImageButton v = new ActorImageButton(act, f, key, position);
                act.buttons.put((String) v.getTag(), v);
                convertView = v;
            }
            convertView.setId(300 + position);
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return 300 + position;
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
        final FACTORY_KEY key;
        final int num;
        private GestureDetector touchDetector;

        ActorImageButton(Context c, Factory<Actor> f, FACTORY_KEY key, final int j) {
            super(c, f, j);
            registerToFactory();
            act = (TapChainView) c;
            touchDetector = new GestureDetector(act, this);
            factory = f;
            this.key = key;
            num = j;
            setOnTouchListener(this);
        }

        private GridFragment returnPaletteAble() {
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
                case MotionEvent.ACTION_CANCEL:
                    GridFragment f1 = returnPaletteAble();
                    if (f1 != null
                            && f1.contains((int) event.getRawX(),
                            (int) event.getRawY())) {
                        act.add(key, num);
                    } else {
                        float x = event.getRawX();
                        float y = event.getRawY();
                        act.add(key, num, x, y);
                    }
                    p.dismiss();
                    break;
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
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
                return true;
            }
            act.add(key, num, e2.getRawX(), e2.getRawY(), velocityX,
                    velocityY);
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
        View v = null;
        Context cxt = null;
        float lowx = 0f, lowy = 0f;

        public OverlayPopup(Context c) {
            super(c);
            cxt = c;
            setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
        }

        public void setPopupView(Factory<Actor> f, int i) {
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

        ActorImage(Context c, Factory<Actor> f, final int j) {
            super(c);
            try {
                v = (AndroidView) f.getViewBlueprint(j).newInstance(null);
                if (f.size() > j)
                    b = f.get(j);
            } catch (ChainException e) {
                e.printStackTrace();
            }
            a = (v != null) ? v.getDrawable() : getResources()
                    .getDrawable(R.drawable.no);
            setImageDrawable(a);
            if (v != null) {
                setTag(v.getTag());
            }
            setId(tapOffset + j);
        }

        public void registerToFactory() {
            if (v instanceof IBlueprintFocusNotification)
                if (b != null)
                    b.setNotification(
                            (IBlueprintFocusNotification) v);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            IPoint size = v.getSize()._valueGet();
            a.setBounds(0, 0, (int) size.x(), (int) size.y());
            a.draw(canvas);
        }
    }


}