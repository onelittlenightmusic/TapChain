package org.tapchain.realworld;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

import org.tapchain.IntentHandler;
import org.tapchain.core.Actor;
import org.tapchain.core.ChainException;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.LinkType;
import org.tapchain.editor.TapChain;

/**
 *
 */
public class MainActivity extends Activity implements IIntentHandler {
    SparseArray<IntentHandler> intentHandlers = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CanvasFragment.create(this, R.id.canvas_fragment);
        GridFragment.create(this, R.id.grid_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getGrid() != null)
            getGrid().show(GridShowState.HALF);
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
        // as you specify a offerToFamily activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Make a actor from actor tag
     * @param key the key of factory to which actor will be added
     * @param tag actor tag
     * @return generated actor
     */
    public Actor add(TapChain.FACTORY_KEY key, String tag) {
        return getCanvas().add(key, tag);
    }

    /**
     * Make a actor from actor tag and set location
     * @param key the key of factory to which you are going to add actor
     * @param tag actor tag
     * @param x x of location where actor will be added
     * @param y y of location where actor will be added
     * @return added actor
     */
    public Actor add(TapChain.FACTORY_KEY key, String tag, float x, float y) {
        return getCanvas().add(key, tag, x, y);
    }

    /**
     * Make a actor from actor tag and set both location and motion
     * @param key key of factory to which actor will be added
     * @param tag actor tag
     * @param x x of location where actor will be added
     * @param y y of location where actor will be added
     * @param dx x of first velocity
     * @param dy y of first velocity
     * @return added actor
     */
    public Actor add(TapChain.FACTORY_KEY key, String tag, float x, float y, float dx,
                     float dy) {
        return getCanvas().add(key, tag, x, y, dx, dy);
    }

    /**
     * Make a actor from actor id
     * @param key key of factory to which actor will be added
     * @param id actor id
     * @return added actor
     */
    public Actor add(TapChain.FACTORY_KEY key, int id) {
        return getCanvas().add(key, id);
    }

    /**
     * Make a actor from actor id and set location
     * @param key key of factory to which actor will be added
     * @param id actor id
     * @param x x of location where actor will be added
     * @param y y of location where actor will be added
     * @return added actor
     */
    public Actor add(TapChain.FACTORY_KEY key, int id, float x, float y) {
        return getCanvas().add(key, id, x, y);
    }

    /**
     * Make a actor from actor id and set both location and motion
     * @param key
     * @param code
     * @param x x of location where actor will be added
     * @param y y of location where actor will be added
     * @param dx x of first velocity
     * @param dy y of first velocity
     * @return added actor
     */
    public Actor add(TapChain.FACTORY_KEY key, int code, float x, float y, float dx,
                     float dy) {
        return getCanvas().add(key, code, x, y, dx, dy);
    }

    /**
     * Make a actor from Blueprint
     * @param b
     * @return
     */
    public Actor add(IBlueprint<Actor> b) {
        return getCanvas().add(b, null, null);
    }

    public Actor add(IBlueprint<Actor> b, float x, float y) {
        return getCanvas().add(b, x, y);
    }

    public Actor add(IBlueprint<Actor> b, float rawX, float rawY, float velocityX, float velocityY) {
        return getCanvas().add(b, rawX, rawY, velocityX, velocityY);
    }

    /**
     * Connect an actor to another actor
     * @param a1 actor which link to another
     * @param type connection type as LinkType
     * @param a2 actor which is connected from a1
     */
    public void connect(Actor a1, LinkType type, Actor a2) {
        getTapChain().link(a1, type, a2);
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

    public TapChain getTapChain() {
        return getCanvas().getTapChain();
    }

    @Override
    public void addIntentHandler(int requestCode, IntentHandler h) {
        intentHandlers.put(requestCode, h);
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
            add(TapChain.FACTORY_KEY.ALL, data.getIntExtra("TEST", 0), 0f, 0f);
        return;
    }

}
