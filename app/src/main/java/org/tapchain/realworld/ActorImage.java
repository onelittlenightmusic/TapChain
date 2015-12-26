package org.tapchain.realworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import org.tapchain.AndroidActor;
import org.tapchain.core.Actor;
import org.tapchain.core.Chain;
import org.tapchain.core.Factory;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IBlueprintFocusNotification;
import org.tapchain.core.IPoint;

/**
 * Created by hiro on 2015/12/26.
 */
public class ActorImage extends ImageView {
    AndroidActor.AndroidView v;
    IBlueprint b;
    Drawable a;

    ActorImage(Context c, Factory<Actor> f, final int j) {
        super(c);
        try {
            v = (AndroidActor.AndroidView) f.getViewBlueprint(j).newInstance(null);
            if (f.size() > j)
                b = f.get(j);
        } catch (Chain.ChainException e) {
            e.printStackTrace();
        }
        a = (v != null) ? v.getDrawable() : getResources()
                .getDrawable(R.drawable.no);
        setImageDrawable(a);
        if (v != null) {
            setTag(v.getTag());
        }
        setId(MainActivity.tapOffset + j);
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
