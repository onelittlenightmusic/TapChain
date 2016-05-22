package org.tapchain.realworld;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tapchain.AndroidTapChain;
import org.tapchain.core.Actor;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPoint;
import org.tapchain.editor.EditorReturn;
import org.tapchain.editor.TapChain;

/**
 * CanvasFragment controls creation of canvas view and canvas model.
 * CanvasFragment is set for retaining instance so that canvas model
 * is not recreated.
 * Created by hiro on 2015/12/26.
 */
public class CanvasFragment extends Fragment {
    CanvasViewImpl view;
//    Activity act;
    static String CANVAS = "Canvas";
    TapChain editor;

    public CanvasFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Activity act = getActivity();
        if (view == null) {
            view = new CanvasViewImpl(act);
            if(editor == null) {
                editor = new AndroidTapChain(view, act);

                try {
                    editor.editBlueprint()
                            .add((Class<? extends Actor>)Class.forName("org.tapchain.core.Generator$WordGenerator"), "A", false)
                            .view(getResources().getIdentifier("a" , "drawable", getActivity().getPackageName()))
                            .tag("Word")
                            //                .setLogLevel()
                            .save();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                editor.invalidate();
            }
            view.setTapChain(editor);
        }
        return this.view;
    }

    public static CanvasFragment getCanvas(Activity act) {
        return (CanvasFragment) act.getFragmentManager()
                .findFragmentByTag(CANVAS);
    }

    /**
     * Make a actor from actor tag
     * @param key the key of factory to which actor will be added
     * @param tag actor tag
     * @return generated actor
     */
    public Actor add(TapChain.FACTORY_KEY key, String tag) {
        return add(key, tag, null, null);
    }

    /**
     * Make a actor from actor tag and set location
     * @param key the key of factory to which you are going to add actor
     * @param tag actor tag
     * @param x x of location where actor will be added
     * @param y y of location where actor will be added
     * @return
     */
    public Actor add(TapChain.FACTORY_KEY key, String tag, float x, float y) {
        return add(key, tag, view.getPosition(x, y), null);
    }

    /**
     * Make a actor from actor tag and set both location and motion
     * @param key key of factory to which actor will be added
     * @param tag actor tag
     * @param x x of location where actor will be added
     * @param y y of location where actor will be added
     * @param vx x of first velocity
     * @param vy y of first velocity
     * @return
     */
    public Actor add(TapChain.FACTORY_KEY key, String tag, float x, float y, float vx, float vy) {
        return add(key, tag, view.getPosition(x, y), view.getVector(vx, vy));
    }


    public Actor add(TapChain.FACTORY_KEY key, String tag, IPoint pos, IPoint vec) {
        EditorReturn editorReturn = editor.addActorFromBlueprint(key, tag, pos);
        if (editorReturn == null)
            return null;
        //TODO: resolve duplication
        IPoint resultPoint = editorReturn.getTap()._get();
        if(!view.isInWindow(resultPoint.x(), resultPoint.y()) && pos != null)
            //Centering
            view._onFlingBackgroundTo(pos.x(), pos.y());

        if (vec == null)
            return editorReturn.getActor();
        view.onFling(editorReturn.getTap(), vec);
        return editorReturn.getActor();
    }

    /**
     * Make a actor from actor id
     * @param key key of factory to which actor will be added
     * @param id actor id
     * @return added actor
     */
    public Actor add(TapChain.FACTORY_KEY key, int id) {
        return add(key, id, null, null);
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
        return add(key, id, view.getPosition(x, y), null);
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
    public Actor add(TapChain.FACTORY_KEY key, int code, float x, float y, float dx, float dy) {
        return add(key, code, view.getPosition(x, y), view.getVector(dx, dy));
    }

    public Actor add(TapChain.FACTORY_KEY key, int code, IPoint pos, IPoint vec) {
        EditorReturn editorReturn = editor.addActorFromBlueprint(key, code, pos);
        if (editorReturn == null)
            return null;

        //TODO: DRY
        IPoint resultPoint = editorReturn.getTap()._get();
        if(!view.isInWindow(resultPoint.x(), resultPoint.y()) && pos != null)
            //Centering
            view._onFlingBackgroundTo(pos.x(), pos.y());

        if (vec == null)
            return editorReturn.getActor();
        view.onFling(editorReturn.getTap(), vec);
        return editorReturn.getActor();
    }

    public TapChain getEditor() {
        return editor;
    }

    /**
     * Create CanvasFragment into android layout.
     * @param mainActivity Activity instance
     * @param id layout object id into which CanvasFragment is inserted
     */
    public static void create(MainActivity mainActivity, int id) {
        FragmentFactory.create(mainActivity, CanvasFragment.class, id, CANVAS);
    }

    public Actor add(IBlueprint<Actor> b, float x, float y) {
        return add(b, view.getPosition(x, y), null);
    }

    public Actor add(IBlueprint<Actor> b, float x, float y, float vx, float vy) {
        return add(b, view.getPosition(x, y), view.getVector(vx, vy));
    }
    /**
     * Make a actor from actor id and set both location and motion
     * @param b
     * @param pos
     * @param vec
     * @return
     */
    public Actor add(IBlueprint<Actor> b, IPoint pos, IPoint vec) {
        EditorReturn editorReturn = editor.addActorFromBlueprint(b, pos);
        if (editorReturn == null)
            return null;

        //TODO: DRY
        IPoint resultPoint = editorReturn.getTap()._get();
        if(!view.isInWindow(resultPoint.x(), resultPoint.y()) && pos != null)
            //Centering
            view._onFlingBackgroundTo(pos.x(), pos.y());

        if (vec == null)
            return editorReturn.getActor();
        view.onFling(editorReturn.getTap(), vec);
        return editorReturn.getActor();
    }

}
