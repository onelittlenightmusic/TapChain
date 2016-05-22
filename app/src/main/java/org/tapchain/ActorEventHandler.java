package org.tapchain;

import android.app.Activity;

import org.tapchain.core.Actor;
import org.tapchain.core.ChainException;
import org.tapchain.core.IActorConnectHandler;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPoint;
import org.tapchain.core.LinkType;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.TapManager;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IAttachHandler;
import org.tapchain.editor.ITapChain;
import org.tapchain.editor.IPathTap;
import org.tapchain.editor.TapChain.InteractionType;
import org.tapchain.realworld.R;

import static org.tapchain.core.LinkType.*;

public class ActorEventHandler implements IActorSharedHandler, IActorConnectHandler {
    ITapChain<Actor, ActorTap> tapChain;
    Integer addSoundHammer = R.raw.button;
    Integer addSoundFail = R.raw.failbuzzer;
    Activity act = null;

    public ActorEventHandler(ITapChain tapChain, Activity activity) {
        this.tapChain = tapChain;
        act = activity;
    }

    @Override
    public boolean onAttach(IActorTap t1, IActorTap t2, Actor a1, Actor a2, InteractionType type) {
        if(!type.touching()) {
            //check whether actors are already connected
            LinkType linkType = tapChain.getLinkType(a1, a2);
            if(linkType != null) {
                switch (tapChain.getLinkType(a1, a2)) {
                    case FROM_PARENT:
                    case TO_CHILD:
                        tapChain.unlink(a1, a2);
                }
//                ;
            }
            return false;
        }
        if(a1 != null && a2 != null) {
            if (tapChain.link(a1, FROM_PARENT, a2)) {
            } else if (tapChain.link(a1, TO_CHILD, a2)) {
            }
        }
        if (t1 instanceof IAttachHandler) {
            if(((IAttachHandler) t1).onTouch(tapChain, t2, a1, a2)) {
                t1.setCenter(new WorldPoint(0, 100).setDif());
                new TapManager(tapChain).editTap()
                        .add(new AndroidActor.AndroidSound2(act, addSoundFail)).save();
            }
        }
        return true;
    }

    @Override
    public void onAdd(final Actor p, final IActorTap v, final IBlueprint b, IPoint pos) {
        try
        {
            new TapManager(tapChain).editTap()
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
        tapChain.shake(100);
    }
}