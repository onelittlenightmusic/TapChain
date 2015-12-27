package org.tapchain.realworld;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import org.tapchain.PaletteSort;
import org.tapchain.TapChainAndroidEditor;
import org.tapchain.TapChainGoalTap;
import org.tapchain.core.Actor;
import org.tapchain.core.Factory;
import org.tapchain.core.IBlueprintInitialization;
import org.tapchain.core.IPoint;
import org.tapchain.core.LinkType;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.TapChainEditor;
import org.tapchain.game.ISensorView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        ISensorView {
    static final String X = "LOCATIONX", V = "VIEWS";
    static final RectF RF = new RectF(0, 0, 100, 100);
    public static int tapOffset = 10000;
    String CANVAS_TAG = "Canvas";
    private WritingView viewCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

//        FrameLayout rootview = new FrameLayout(this);
//        FrameLayout root = new FrameLayout(this);
//        viewControl = new FrameLayout(this);
//        RelativeLayout view_bottom_left = new RelativeLayout(this);
//        addButton(view_bottom_left, R.drawable.dust, true,
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        getEditor().reset();
//                    }
//                });
//        addButton(view_bottom_left, R.drawable.stop, true,
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (getEditor().freezeToggle())
//                            ((ImageView) v).setImageResource(R.drawable.stop);
//                        else
//                            ((ImageView) v).setImageResource(R.drawable.start);
//                    }
//                });
//        addButton(view_bottom_left, R.drawable.pullup, true,
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        boolean gridshow = false;
//                        GridFragment f = getGrid();
//                        if (f != null) {
//                            gridshow = f.toggle();
//                        }
//                        if (gridshow)
//                            ((ImageView) v)
//                                    .setImageResource(R.drawable.pulldown);
//                        else
//                            ((ImageView) v).setImageResource(R.drawable.pullup);
//                    }
//                });
//        addButton(view_bottom_left, R.drawable.config, true,
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        try {
//                            Log.w("JSON TEST", getEditor().edit().getChain().toJSON().toString());
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//        viewControl.addView(view_bottom_left, new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        addButton(view_bottom_left, R.drawable.no, false,
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        finish();
//                    }
//                });
////        if(viewCanvas == null) {
////            viewCanvas = new CanvasViewImpl2(this);
//////            Log.w("test", "onCreate", new Throwable());
////        }
////        rootview.addView(getCanvas(), new LayoutParams(
////                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
////        rootview.setId(0x00001236);
//        root.addView(rootview);
//        root.addView(viewControl, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT));
//        root.setId(0x00001235);
//        root.setTag("OVERLAY");
//
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//
//
//        LinearLayout l2 = new LinearLayout(this);
//        rootview.addView(l2);
//        l2.setId(0x00001236);
//        l2.setTag("Canvas");
//
//        LinearLayout l = new LinearLayout(this);
//        rootview.addView(l);
//        l.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
//        l.setId(0x00001234);

        Fragment fragment;
        CanvasFragment canvasFragment;
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fragment = fm.findFragmentByTag(CANVAS_TAG);
        if(fragment == null) {
            fragment = Fragment.instantiate(this, CanvasFragment.class.getName());
            Log.w("test", "onCreate", new Throwable());
            canvasFragment = (CanvasFragment)fragment;
            viewCanvas = canvasFragment.setContext(this).view;
            ft.replace(R.id.fragment, fragment, CANVAS_TAG);
        } else {
            ft.attach(fragment);
            canvasFragment = (CanvasFragment)fragment;
            viewCanvas = canvasFragment.setContext(this).view;
        }
        ft.commit();
        new GridFragment().setContext(this).show(GridShow.HIDE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getGrid() != null)
            getGrid().show(GridShow.HALF);
        Log.i("TapChainView.state", "onResume");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, String tag) {
        return viewCanvas.onAdd(key, tag);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, String tag, float x, float y) {
        return viewCanvas.onAdd(key, tag, x, y);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, String tag, float x, float y, float dx,
                     float dy) {
        return viewCanvas.onAdd(key, tag, x, y, dx, dy);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, int code) {
        return viewCanvas.onAdd(key, code);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, int code, float x, float y) {
        return viewCanvas.onAdd(key, code, x, y);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, int code, float x, float y, float dx,
                     float dy) {
        return viewCanvas.onAdd(key, code, x, y, dx, dy);
    }

    public void connect(Actor a1, LinkType type, Actor a2) {
        getEditor().connect(a1, type, a2);
    }

    // 2.Getters and setters
    public GridFragment getGrid() {
        GridFragment f = GridFragment.getGrid(this);
        return f;
    }

    public CanvasFragment getCanvas() {
        CanvasFragment f = CanvasFragment.getCanvas(this);
        return f;
    }


    public TapChainEditor getEditor() {
        return viewCanvas.getEditor();
    }

    @Override
    public void shake(int interval) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(interval);
    }

    public class CanvasViewImpl2 extends WritingView {
        Rect r = new Rect();
        private TapChainEditor editor;

        public CanvasViewImpl2(Context context) {
            super(context);
            paint.setColor(0xff303030);
            paint.setStyle(Paint.Style.FILL);
        }

        public void setEditor(TapChainEditor editor) {
            this.editor = editor;
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
            canvas.drawText("Goal = " + TapChainGoalTap.printState(), 20, 100,
                    paint_text);
            canvas.setMatrix(matrix);
            getEditor().show(canvas);
            getEditor().userShow(canvas);
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

        }

        public TapChainEditor getEditor() {
            return editor;
        }

        @Override
        public void shake(int interval) {
            MainActivity.this.shake(interval);
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

        boolean standby = false;

        @Override
        public boolean standbyRegistration(IActorTap selected, int x, int y) {
            GridFragment f1 = getGrid();
            if (f1 != null
                    && f1.contains(x, y)) {
                if(standby) {
                    return true;
                }
                Factory f = getEditor().getFactory(f1.getCurrentFactory());
                IBlueprintInitialization i = getEditor().standbyRegistration(f, selected);
                if (i != null) {
                    return true;
                }
            }
            return false;
        }

        public void resetRegistration() {
            standby = false;
        }
    }

    HashMap<String, ActorImageButton> buttons = new HashMap<String, ActorImageButton>();

    public static class ViewAdapter extends BaseAdapter implements Factory.ValueChangeNotifier {
        private Factory<Actor> f;
        private TapChainEditor.FACTORY_KEY key;
        private MainActivity act;
        private GridView _parent;

        public ViewAdapter(Context c, GridView parent, TapChainEditor.FACTORY_KEY k) {
            act = (MainActivity) c;
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


}
