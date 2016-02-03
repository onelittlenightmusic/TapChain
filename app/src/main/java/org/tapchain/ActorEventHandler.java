package org.tapchain;

import android.app.Activity;

import org.tapchain.core.Actor;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.IActorConnectHandler;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPoint;
import org.tapchain.core.LinkType;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IAttachHandler;
import org.tapchain.editor.IEditor;
import org.tapchain.editor.IPathTap;
import org.tapchain.editor.TapChainEditor.InteractionType;
import org.tapchain.realworld.R;

public class ActorEventHandler implements IActorSharedHandler, IActorConnectHandler {
    IEditor<Actor, ActorTap> edit;
    Integer addSoundHammer = R.raw.button;
    Integer addSoundFail = R.raw.failbuzzer;
    Activity act = null;

    public ActorEventHandler(IEditor edit, Activity activity) {
        this.edit = edit;
        act = activity;
    }

    @Override
    public boolean onAttach(IActorTap t1, IActorTap t2, Actor a1, Actor a2, InteractionType type) {
        if(!type.touching()) {
            return false;
        }
        if(a1 != null && a2 != null) {
            if (edit.connect(a1, LinkType.FROM_PARENT, a2)) {
//                Log.w("Test", String.format("Parent %s to Child %s Succeeded", a1.getTag(), a2.getTag()));
            } else if (edit.connect(a1, LinkType.TO_CHILD, a2)) {
//                Log.w("Test", String.format("Child %s to Parent %s Succeeded", a2.getTag(), a1.getTag()));
            }
        }
        if (t1 instanceof IAttachHandler) {
            if(((IAttachHandler) t1).onTouch(edit, t2, a1, a2)) {
                t1.setCenter(new WorldPoint(0, 100).setDif());
                edit.editTap()
                        .add(new AndroidActor.AndroidSound2(act, addSoundFail)).save();
            }
        }
        return true;
    }

    @Override
    public void onAdd(final Actor p, final IActorTap v, final IBlueprint b, IPoint pos) {
        try
        {
            edit.editTap()
                    .add(new AndroidActor.AndroidSound2(act, addSoundHammer))
                    .add(new AndroidActor.AndroidTTS(act, p.getTag()))
                    .save();
        } catch (ChainException e) {
            e.printStackTrace();
        }

//        combo(v, b);
    }


    @Override
    public void onConnect(IActorTap iActorTap, IPathTap iPathTap, IActorTap iActorTap2, LinkType linkType) {
        edit.shake(100);
    }
}