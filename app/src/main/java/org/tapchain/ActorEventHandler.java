package org.tapchain;

import android.app.Activity;
import android.content.res.Resources;

import org.tapchain.core.Actor;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IActorConnectHandler;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IPoint;
import org.tapchain.core.LinkType;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorEditor;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IAttachHandler;
import org.tapchain.editor.IEditor;
import org.tapchain.editor.IFocusable;
import org.tapchain.editor.IPathTap;
import org.tapchain.editor.TapChainEditor.InteractionType;
import org.tapchain.realworld.R;

public class ActorEventHandler implements IActorSharedHandler, IActorConnectHandler {
    Actor l;
    IEditor<Actor, ActorTap> edit;
    Integer addSoundHammer = R.raw.button;
    Integer addSoundFail = R.raw.failbuzzer;
    Resources res = null;
    Activity act = null;

    public ActorEventHandler(IEditor edit, Resources r, Activity activity) {
        this.edit = edit;
        act = activity;
        res = r;
    }

    @Override
    public Resources getResources() {
        return res;
    }


    @Override
    public void createFocus(IActorTap v) {
        final Actor actor = v.getActor();
        ClassEnvelope firstClassEnvelope = null;
        LinkType spotLatest = getFocusControl().getLinkType(), first = null;
        IFocusable firstSpot = null;
        if (actor == null || actor == getFocusControl().getTargetActor()) {
            return;
        }
        getFocusControl().clearAllFocusables();

        for (LinkType al : LinkType.values()) {
            ClassEnvelope clz = actor.getLinkClassFromLib(al);
            if (clz == null) {
                continue;
            }

            //Create beam view
            IFocusable spot = null;
            switch (al) {
                case PUSH:
                    MyBeamTapStyle beam = new MyBeamTapStyle(getResources(), v, al, clz);
                    if (v instanceof MyTapStyle2)
                        beam.init(((MyTapStyle2) v).getOffsetVectorRawCopy());
                    spot = beam;
                    break;
                case TO_CHILD:
                    spot = new MySpotOptionTapStyle(v, al, clz);
                    break;
                default:
                    continue;
            }

            getFocusControl().addFocusable(spot, al);
            if (first == null/* spotLatest == al*/) {
                first = al;
                firstClassEnvelope = clz;
                firstSpot = spot;
            }
        }
        if(firstSpot == null) {
            edit.changeFocus(null, null, null);
            return;
        }
        getFocusControl().save(edit.editTap());
        getFocusControl().setTargetActor(actor);
        edit.changeFocus(first, firstSpot, firstClassEnvelope);
    }

    @Override
    public boolean onAttach(IActorTap t1, IActorTap t2, Actor a1, Actor a2, InteractionType type) {
//        switch (type) {
//            case NONE:
//                break;
//            case TOUCH_TOP:
//            case TOUCH_BOTTOM:
//            case TOUCH_RIGHT:
//            case TOUCH_LEFT:
//                if (edit.connect(a1, LinkType.FROM_PARENT, a2)) {
//                    Log.w("Test", String.format("Parent %s to Child %s Succeeded", a1.getTag(), a2.getTag()));
//                    break;
//                }
//                if (edit.connect(a1, LinkType.TO_CHILD, a2)) {
//                    Log.w("Test", String.format("Child %s to Parent %s Succeeded", a2.getTag(), a1.getTag()));
//                    break;
//                }
//                return false;
//            case CROSSING:
////                if (a1 instanceof Controllable) {
////                    ((Controllable) a1).interruptError();
////                }
//                return false;
//            case INSIDE:
//                boolean connect = false;
//                if (t1 instanceof IAttachHandler) {
//                    connect = ((IAttachHandler) t1).onTouch(edit, t2, a1, a2);
////                } else {
////                    if (edit.connect(a1, LinkType.FROM_PARENT, a2)) {
////                        t2._valueGet().setOffset(t1, true);
////                        connect = true;
////                    }
//                }
//                if (!connect) {
//                    t1.setCenter(new WorldPoint(0, 100).setDif());
//                    edit.editTap()
//                            .add(new AndroidActor.AndroidSound2(act, addSoundFail)).save();
//                }
//
//                return false;
//            case OUTSIDE:
//                if (a1.isConnectedTo(a2, PathType.FAMILY)) {
//                    edit.editTap().disconnect(a1, a2);
//                    t1._valueGet().unsetOffset(t2, true);
//                }
//                break;
//            default:
//                return false;
//        }
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
    public void onLockedScroll(IActorEditor edit, IActorTap tap, IPoint wp) {
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

        combo(v, b);
    }

    public void combo(IActorTap t, IBlueprint b) {
        Actor actorNew = edit.toActor((ActorTap) t), aTarget = getFocusControl().getTargetActor();
        edit.connect(aTarget, getFocusControl().getLinkType(), actorNew);
    }

    MyFocusControl focusControl = new MyFocusControl();

    @Override
    public MyFocusControl getFocusControl() {
        return focusControl;
    }

    @Override
    public void onConnect(IActorTap iActorTap, IPathTap iPathTap, IActorTap iActorTap2, LinkType linkType) {
        edit.shake(100);
    }
}