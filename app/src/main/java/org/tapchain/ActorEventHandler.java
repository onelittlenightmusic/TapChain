package org.tapchain;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;

import org.tapchain.AndroidActor.AndroidImageView;
import org.tapchain.ColorLib.ColorCode;
import org.tapchain.core.Actor;
import org.tapchain.core.Actor.Controllable;
import org.tapchain.core.ActorPullException;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IActorBlueprint;
import org.tapchain.core.IActorConnectHandler;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IBlueprint;
import org.tapchain.core.IBlueprintFocusNotification;
import org.tapchain.core.IPoint;
import org.tapchain.core.LinkType;
import org.tapchain.core.PathType;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IActorEditor;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IAttachHandler;
import org.tapchain.editor.IEditor;
import org.tapchain.editor.IPathTap;
import org.tapchain.editor.TapChainEditor.InteractionType;
import org.tapchain.realworld.R;

public class ActorEventHandler implements IActorSharedHandler, IActorConnectHandler {
	Actor l;
	IEditor<Actor, ActorTap> edit;
	String _out = "_out", _in = "_in";
	Integer addSoundHammer = R.raw.button;
	Integer addSoundFail= R.raw.failbuzzer;
	LinkType spotActorlink = LinkType.PUSH;
	Resources res = null;
	Activity act = null;

	public ActorEventHandler(IEditor edit, Resources r, Activity activity) {
		this.edit = edit;
		act = activity;
		edit.editTap().add(l = new AndroidActor.AndroidAlert())
		/* .teacher(p) */.save();
		res = r;
	}

	@Override
	public Resources getResources() {
		return res;
	}

	@Override
	public void setSpot(LinkType al, IFocusable spot, ClassEnvelope clazz) {
		spotActorlink = al;
		edit.setNextPos(spot.getCenter());
		edit.changePaletteToConnectables(al.reverse(), clazz);
		spot.focus(al);

	};

	@Override
	public void resetSpot() {
		edit.resetNextPos();
	}

	@Override
	public void changeFocus(IActorTap v) {
		if(v.getActor() == getFocusControl().getTargetActor())
			return;
		IActorBlueprint b = v.getActor().getBlueprint();
		resetSpot();
		getFocusControl().clearAllFocusables();
		LinkType spotLatest = getFocusControl().getFocusedLinkType(), first = null;
		if(b instanceof IActorBlueprint) {
			getFocusControl().init(v);
			for(LinkType al: LinkType.values()) {
				ClassEnvelope clz = ((IActorBlueprint) b).getConnectClass(al);
				if(clz == null) {
					unsetLastPushed();
				} else {
					setLastPushed(al, v, clz);
						IFocusable spot = null;
						if(v.getActor().getLinkClassFromLib(al) != null) {
							if (al == LinkType.PUSH) {
								spot = new MyBeamTapStyle(getResources(), this, v, al, clz);
							} else if (al == LinkType.TO_CHILD) {
								spot = new MySpotOptionTapStyle(v, this, al, clz);
							} else {
								continue;
							}
						}
						getFocusControl().addFocusable(spot, al);
						if(first == null || spotLatest == al) {
							setSpot(al, spot, clz);
							getFocusControl().setTargetActor(v.getActor(), spot);
							first = al;
						}
				}
			}
			getFocusControl().save(edit);

		}

	}

	@Override
	public boolean onAttach(IActorTap t1, IActorTap t2, Actor a1, Actor a2, InteractionType type) {
		switch (type) {
		case TOUCH_TOP:
		case TOUCH_BOTTOM:
		case TOUCH_RIGHT:
		case TOUCH_LEFT:
			if (edit.connect(a1, LinkType.FROM_PARENT, a2)) {
				Log.w("Test", String.format("Parent %s to Child %s Succeeded",a1.getTag(), a2.getTag()));
				break;
			}
			if (edit.connect(a1, LinkType.TO_CHILD, a2)) {
				Log.w("Test", String.format("Child %s to Parent %s Succeeded",a2.getTag(), a1.getTag()));
				break;
			}
			return false;
		case CROSSING:
			if (a1 instanceof Controllable) {
				((Controllable) a1).interruptError();
			}
			return false;
		case INSIDE:
			boolean connect = false;
			if(t1 instanceof IAttachHandler) {
				connect = ((IAttachHandler)t1).onInside(edit, t2, a1, a2);
			} else {
					if(edit.connect(a1, LinkType.FROM_PARENT, a2)) {
						t2._valueGet().setOffset(t1, true);
						connect = true;
					}
			}
			if(!connect ) {
				t1.setCenter(new WorldPoint(0, 100).setDif());
				edit.editTap()
				.add(new AndroidActor.AndroidSound2(act, addSoundFail)).save();
			}

			return false;
		case OUTSIDE:
			if(a1.isConnectedTo(a2, PathType.FAMILY)) {
				edit.editTap().disconnect(a1, a2);
				t1._valueGet().unsetOffset(t2, true);
			}
			break;
		default:
			return false;
		}
		return true;
	}


	@Override
	public void onLockedScroll(IActorEditor edit, IActorTap tap, IPoint wp) {
	}

	@Override
	public void onAdd(Actor p, IActorTap v, IBlueprint b, IPoint pos) {
		try {
			edit.editTap()
			.add(new AndroidActor.AndroidSound2(act, addSoundHammer))
			.add(new AndroidActor.AndroidTTS(act, p.getTag()))
			.save();
		} catch (ChainException e) {
			e.printStackTrace();
		}

		combo(v, b);
	}


	@Override
	public void onPullLocked(IActorTap t, ActorPullException actorPullException) {
		l.offer(String.format("%s: \"%s\"", actorPullException.getLocation(),
				actorPullException.getErrorMessage()));
		AndroidImageView errorMark = new AndroidImageView(act, R.drawable.error);
		errorMark.setColorCode(ColorCode.RED)
		.setPercent(new WorldPoint(200f, 200f));
		errorMark._valueGet().setOffset(t, false);
		edit.editTap()
			.add(errorMark
			)
			._in()
			.add(new Actor.Reset(false)/*.setLogLevel(true)*/)
			.old(new Actor.Sleep(2000)/*.setLogLevel(true)*/)
			.save();

		//Create error balloon
		LinkType linkType = actorPullException.getLinkType();
		ClassEnvelope classEnvelopeInLink = actorPullException.getClassEnvelopeInLink();
		IActorTap balloon = createBalloon(t, linkType, classEnvelopeInLink);
		edit.editTap().add((Actor) balloon).save();
		t.setAccessoryTap(linkType, balloon);
		setLastPushed(linkType, t, classEnvelopeInLink);
	}

	@Override
	public void onPullUnlocked(IActorTap t, ActorPullException actorPullException) {

		LinkType linkType = actorPullException.getLinkType();
		if(linkType != null) {
			edit.editTap().remove((Actor) t.getAccessoryTap(linkType));
			t.unsetAccessoryTap(linkType);
			unsetLastPushed();

		}
	}

	IActorTap pushed = null;

	private IActorTap getLastPushed() {
		return pushed;
	}

	public IActorTap setLastPushed(LinkType ac, IActorTap target, ClassEnvelope classEnvelope) {
		edit.highlightConnectables(ac.reverse(), classEnvelope);

		if(pushed != null) {
			IActorTap last = pushed.getAccessoryTap(ac);
			if(last != null && last instanceof IBlueprintFocusNotification) {
				((IBlueprintFocusNotification)last).onFocus(null);
			}
		}
		pushed = target;
		if(target == null)
			return null;
		IActorTap lt = target.getAccessoryTap(ac);
//			ITap lt = t;
		if(lt != null && lt instanceof IBlueprintFocusNotification) {
			((IBlueprintFocusNotification)lt).onFocus(ac.getBooleanSet());
		}
		return target;
	}

	public void unsetLastPushed() {
		edit.unhighlightConnectables();
		pushed = null;
	}

	public void unsetLastPushed(IActorTap t) {
		if(t == pushed)
			unsetLastPushed();
	}

	@Override
	public void onPush(IActorTap t, LinkType linkType, Object obj) {
		setPushOutBalloon(t, linkType, obj);

	}

	public void setPushOutBalloon(IActorTap t, LinkType linkType, Object obj) {
			if(t.getActor().isConnectedTo(linkType)) {
				return;
			}
			IActorTap accessoryTap = t.getAccessoryTap(linkType);
			if(accessoryTap != null) {
				accessoryTap.setMyActorValue(obj);
				return;
			}

			ClassEnvelope classEnvelope = new ClassEnvelope(obj.getClass());
			IActorTap balloon = createBalloon(t, linkType, classEnvelope);
			edit.editTap().add((Actor) balloon).save();
			t.setAccessoryTap(linkType, balloon);
			setLastPushed(linkType, t, classEnvelope);
			Log.w("test", String.format("setPushOutBalloon ended %s", t.getTag()));
		}
		
		public void unsetPushOutBalloon(ActorTap t, LinkType linkType) {
			ActorTap accessoryTap = (ActorTap) t.getAccessoryTap(linkType);
			if(accessoryTap != null) {
				unsetLastPushed(t);
				t.unsetAccessoryTap(linkType);
				edit.editTap().remove(accessoryTap);
			}
		}
		
		public void combo(IActorTap t, IBlueprint b) {
            Actor actorNew = edit.toActor((ActorTap) t), aTarget = getFocusControl().getTargetActor();
            edit.connect(aTarget, getFocusControl().getFocusedLinkType(), actorNew);
		}

		
		public IActorTap createBalloon(IActorTap t, LinkType linkType, ClassEnvelope ce) {
			OptionTapStyle balloon = null;
			balloon = new BalloonTapStyle(t, linkType, ce, linkType.getOutOrIn()? _out : _in);
			balloon.setCenter(new WorldPoint(100f, 100f));
			balloon.setColorCode(ColorCode.GREEN);
			balloon._valueGet().setOffset(t, false);
			return balloon;
		}
		
		MyFocusControl focusControl = new MyFocusControl();

		@Override
		public MyFocusControl getFocusControl() {
			return focusControl;
		}


    @Override
    public void onConnect(IActorTap iActorTap, IPathTap iPathTap, IActorTap iActorTap2, LinkType linkType) {
        if(linkType == null) {
            Log.w("test", "LinkType is null");
            return;
        }
        if(iActorTap == null || iActorTap2 == null) {
            Log.w("test", "ActorTap is null");
            return;
        }
        if(iPathTap == null) {
            Log.w("test", "PathTap is null");
            return;
        }
        edit.editTap().remove((Actor) iActorTap.getAccessoryTap(linkType));
        iActorTap.unsetAccessoryTap(linkType);
        edit.editTap().remove((Actor) iActorTap2.getAccessoryTap(linkType.reverse()));
        iActorTap2.unsetAccessoryTap(linkType.reverse());
        Log.w("test", String.format("%s, %s, %s, %s", iActorTap.getTag(), iPathTap.getTag(), iActorTap2.getTag(), linkType.name()));
    }
}