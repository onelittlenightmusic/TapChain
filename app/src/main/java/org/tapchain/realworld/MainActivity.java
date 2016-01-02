package org.tapchain.realworld;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.tapchain.core.Actor;
import org.tapchain.core.LinkType;
import org.tapchain.editor.TapChainEditor;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CanvasFragment.create(this, R.id.fragment);
        GridFragment.create(this, R.id.fragment2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getGrid() != null)
            getGrid().show(GridShow.HALF);
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

    /**
     * Make a actor from actor tag
     * @param key
     * @param actorTag
     * @return generated actor
     */
    public Actor add(TapChainEditor.FACTORY_KEY key, String actorTag) {
        return getCanvas().add(key, actorTag);
    }

    /**
     * Make a actor from actor tag and set location
     * @param key
     * @param tag
     * @param x
     * @param y
     * @return
     */
    public Actor add(TapChainEditor.FACTORY_KEY key, String tag, float x, float y) {
        return getCanvas().add(key, tag, x, y);
    }

    /**
     * Make a actor from actor tag and set both location and motion
     * @param key
     * @param tag
     * @param x
     * @param y
     * @param dx
     * @param dy
     * @return
     */
    public Actor add(TapChainEditor.FACTORY_KEY key, String tag, float x, float y, float dx,
                     float dy) {
        return getCanvas().add(key, tag, x, y, dx, dy);
    }

    /**
     * Make a actor from actor id
     * @param key
     * @param id
     * @return
     */
    public Actor add(TapChainEditor.FACTORY_KEY key, int id) {
        return getCanvas().add(key, id);
    }

    /**
     * Make a actor from actor id and set location
     * @param key
     * @param code
     * @param x
     * @param y
     * @return
     */
    public Actor add(TapChainEditor.FACTORY_KEY key, int code, float x, float y) {
        return getCanvas().add(key, code, x, y);
    }

    /**
     * Make a actor from actor id and set both location and motion
     * @param key
     * @param code
     * @param x
     * @param y
     * @param dx
     * @param dy
     * @return
     */
    public Actor add(TapChainEditor.FACTORY_KEY key, int code, float x, float y, float dx,
                     float dy) {
        return getCanvas().add(key, code, x, y, dx, dy);
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


}
