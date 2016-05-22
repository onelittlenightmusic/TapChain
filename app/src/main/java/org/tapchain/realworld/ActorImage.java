package org.tapchain.realworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.tapchain.AndroidActor;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorManager;
import org.tapchain.core.ChainException;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IBlueprintFocusNotification;
import org.tapchain.core.IPoint;

/**
 * Created by hiro on 2015/12/26.
 */
public class ActorImage extends ImageView {
    public static int tapOffset = 10000;
    AndroidActor.AndroidView v;
    IBlueprint<Actor> b;
    Drawable a;
//    static int i = 0;

    public ActorImage(Context context, AttributeSet attr) {
        super(context, attr);
    }

    ActorImage(Context c, IBlueprint<Actor> b) {
        super(c);
        init(b);
    }

    public void init(IBlueprint<Actor> b) {
        try {
                this.b = b;
                //__create dummy instance not to be added to chain
                // (this instance will be removed soon after)
                IBlueprint<Actor> btap =  b.getView();
                v = (AndroidActor.AndroidView)btap.newInstance();
                new ActorManager(btap.getRootChain()).remove(v);
//            }
        } catch (ChainException e) {
            e.printStackTrace();
        }
        a = (v != null) ? v.getDrawable() : getResources()
                .getDrawable(R.drawable.no);
        setImageDrawable(a);
//        if (v != null) {
            setTag(b.getTag());
//        }
//        setId(tapOffset + j);

    }

    public void registerToFactory() {
        if (v instanceof IBlueprintFocusNotification)
            if (b != null)
                b.setNotification(
                        (IBlueprintFocusNotification) v);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        IPoint size = v.getSize()._get();
        a.setBounds(0, 0, (int) size.x(), (int) size.y());
        a.draw(canvas);
    }

    protected AndroidActor.AndroidView getInnerAndroidView() {
        return v;
    }
}
