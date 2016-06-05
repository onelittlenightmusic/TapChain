package org.tapchain;

import android.app.Activity;

import org.tapchain.core.ActorManager;
import org.tapchain.core.Chain;
import org.tapchain.core.IPoint;
import org.tapchain.core.IRelease;
import org.tapchain.core.IValue;
import org.tapchain.core.IValueArray;
import org.tapchain.core.WorldPoint;
import org.tapchain.core.actors.ViewActor;
import org.tapchain.editor.ColorLib;
import org.tapchain.editor.IActorTapView;
import org.tapchain.editor.ITapChain;
import org.tapchain.game.MyFloat;
import org.tapchain.game.MySetPedalTapViewStyle;
import org.tapchain.realworld.R;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by hiro on 2016/01/06.
 */
public class EditInstance implements IRelease {
    ActorTapView setter, exit, restart;
    ViewActor setterText;
    IActorTapView t;
    static HashMap<Class<?>, ClassEditCreator> classEdits = new HashMap<>();

    public interface ClassEditCreator {
        ActorTapView createEditorTap(IActorTapView parent);
    }

    static void addEditCreator(Class<?> cls, ClassEditCreator cec) {
        classEdits.put(cls, cec);
    }

    static void removeEditCreator(Class<?> cls) {
        classEdits.remove(cls);
    }

    EditInstance(Activity act, IActorTapView _p, Object val2) {
        t = _p;
        if(val2 == null)
            return;
        if(classEdits.containsKey(val2.getClass())) {
            setter = classEdits.get(val2.getClass()).createEditorTap(_p);
        } else if (val2 instanceof String) {
            setterText = new AndroidActor.AndroidTextInput(act, (IValue) _p.getActor());
            setterText._get().setOffset(_p);
            return;
        } else if (val2 instanceof IValueArray) {
            setter = new MySetSquarePathTapViewStyle(_p, BitmapMaker.makeOrReuse(
                    "pathExt", R.drawable.widen, 200, 200));
//            setter.setCenter((IPoint)((IValueArray)val2)._valueGetLast());
        } else if (val2 instanceof IPoint) {
            setter = new MySetPointTapViewStyle(_p, BitmapMaker.makeOrReuse(
                    "pointExt", R.drawable.widen, 200, 200));
        } else if (val2 instanceof Integer) {
            setter = new MySetIntegerTapViewStyle(_p);
        } else if (val2 instanceof Float) {
            setter = new MySetFloatTapViewStyle(_p);
        } else if (val2 instanceof MyFloat) {
            setter = new MySetPedalTapViewStyle(_p);
        } else if (val2 instanceof Calendar) {
            setter = new MySetTimeTapViewStyle(_p, BitmapMaker.makeOrReuse(
                    "pointExt", R.drawable.widen, 200, 200));
        } else {
            return;
        }

        exit = new MyExitOptionTapViewStyle(_p, BitmapMaker.makeOrReuse(
                "exit", R.drawable.dust, 70, 70));
        exit.setCenter(new WorldPoint(180f, -180f));
        exit.setColorCode(ColorLib.ColorCode.RED);
        restart = new MyRestartOptionTapViewStyle(_p, BitmapMaker.makeOrReuse(
                "restart", R.drawable.reload, 70, 70));
        restart.setCenter(new WorldPoint(180f, 180f));
        restart.setColorCode(ColorLib.ColorCode.BLUE);
    }

    public boolean registerToManager(Chain root, ITapChain tapChain) {
        ActorManager manager = new ActorManager(root);
        if (setter != null) {
            manager.add(setter);
        } else {
            if (setterText != null)
                manager.add(setterText);
            else
                return false;
        }
        if (exit != null)
            manager.add(exit);
        if (restart != null)
            manager.add(restart);
        manager.save();
        tapChain.lockReleaseTap(this);
        return true;
    }

    public void clear() {
        ActorManager manager = new ActorManager(setter.getRootChain());
        if (setter != null) {
            manager.remove(setter);
            setter = null;
        }
        if (exit != null) {
            manager.remove(exit);
            exit = null;
        }
        if (restart != null) {
            manager.remove(restart);
            restart = null;
        }
        if (setterText != null) {
            manager.remove(setterText);
            setterText = null;
        }
    }

    public IActorTapView getTap() {
        return t;
    }

    @Override
    public boolean onRelease(ITapChain edit, IPoint pos) {
        boolean rtn = false;
        if (setter instanceof IRelease)
            rtn = ((IRelease) setter).onRelease(edit, pos);
        clear();
        return rtn;
    }
}
