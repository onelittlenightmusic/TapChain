package org.tapchain.realworld;

import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import org.tapchain.core.Actor;
import org.tapchain.core.Factory;
import org.tapchain.core.LinkType;
import org.tapchain.editor.TapChainEditor;

public class MainActivity extends AppCompatActivity {
    static final String X = "LOCATIONX", V = "VIEWS";
    static final RectF RF = new RectF(0, 0, 100, 100);
    public static int tapOffset = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CanvasFragment.create(this);
        GridFragment.create(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getGrid() != null)
            getGrid().show(GridShow.HALF);
//        Log.i("TapChainView.state", "onResume");
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
        return getCanvas().onAdd(key, tag);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, String tag, float x, float y) {
        return getCanvas().onAdd(key, tag, x, y);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, String tag, float x, float y, float dx,
                     float dy) {
        return getCanvas().onAdd(key, tag, x, y, dx, dy);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, int code) {
        return getCanvas().onAdd(key, code);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, int code, float x, float y) {
        return getCanvas().onAdd(key, code, x, y);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, int code, float x, float y, float dx,
                     float dy) {
        return getCanvas().onAdd(key, code, x, y, dx, dy);
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

    public void finishThisFromOutside() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.finish();
            }
        });
    }

    public TapChainEditor getEditor() {
        return getCanvas().getEditor();
    }

    public static class ViewAdapter extends BaseAdapter implements Factory.ValueChangeNotifier {
        private Factory<Actor> f;
        private TapChainEditor.FACTORY_KEY key;
        private MainActivity act;
        private GridView _parent;

        public ViewAdapter(Context c, GridView parent, TapChainEditor.FACTORY_KEY k) {
            super();
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
