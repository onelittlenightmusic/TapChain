package org.tapchain.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Factory<PIECE extends Piece> extends ArrayList<IBlueprint<PIECE>> {
	ValueChangeNotifier notif;
    HashMap<String, IBlueprint> tapViewMap = new HashMap<>();

	public Factory() {
		super();
	}

    public static <P extends Piece> Factory<P> copy(Factory<P> factory) {
        return new Factory<>(factory);
    }

    public Factory(Factory<PIECE> factory) {
        super(factory);
    }

	public synchronized Factory<PIECE> Register(IBlueprint root) {
		add(root);
        tapViewMap.put(root.getTag(), root.getView());
		return this;
	}
	
	public synchronized Factory<PIECE> Register(List<IBlueprint<PIECE>> root) {
		addAll(root);
		return this;
	}
	
	public List<IBlueprint<PIECE>> getList() {
		return this;
	}

    public IBlueprint getView(String tag) {
        return tapViewMap.get(tag);
    }

	public void clear() {
		super.clear();
	}

	public void setNotifier(ValueChangeNotifier not) {
		notif = not;
	}

	public void notifyView() {
		if (notif != null)
			notif.notifyChange();
	}

	public void invalidate() {
		if (notif != null)
			notif.invalidate();
	}

    public void save() {
        notifyView();
    }


	public int getSize() {
		return size();
	}

	public String getName(int n) {
		if (n >= getSize())
			return null;
		return get(n).getName();
	}

	public Blueprint getViewBlueprint(int n) throws ChainException {
		if (n >= getSize()) {
			throw new ChainException(this, "TapChain#PieceBlueprint size over");
		} else if (get(n) == null) {
			throw new ChainException(this, "TapChain#PieceBlueprint no object");
		} else if (get(n).getView() == null) {
			throw new ChainException(this,
					"TapChain#PieceBlueprint object has no view");
		}
		return (Blueprint) get(n).getView();
	}

	public void setBlueprints(List<IBlueprint<PIECE>> listBlueprint) {
		clear();
		Register(listBlueprint);
        save();
	}

	public interface ValueChangeNotifier {
		void notifyChange();
		void invalidate();
	}

	public IBlueprint search(String tag) {
//		for (IBlueprint b : collect)
		for (IBlueprint b : this)
			if (b.getTag().equals(tag))
				return b;
		return null;
	}


	public List<IBlueprint<PIECE>> getConnectables(LinkType ac,
			ClassEnvelope classEnvelope) {
		List<IBlueprint<PIECE>> bl = new ArrayList<>();
        if (classEnvelope == null) {
            return bl;
        }

        for (IBlueprint b : this) {
            if (!(b instanceof IActorBlueprint)) {
                continue;
            }
            ClassEnvelope clz = ((IActorBlueprint) b)
                    .getConnectClass(ac);
            if (clz == null) {
                continue;
            }
            switch(ac) {
                case PULL:
                case FROM_PARENT:
                    if(clz.isAssignableFrom(classEnvelope))
                        bl.add(b);
                    break;
                default:
                    if(classEnvelope.isAssignableFrom(clz))
                        bl.add(b);
                    break;
            }
//            if ((EnumSet.of(LinkType.PULL, LinkType.FROM_PARENT)
//                    .contains(ac) && clz
//                    .isAssignableFrom(classEnvelope))
//                    || (EnumSet.of(LinkType.PUSH,
//                            LinkType.TO_CHILD).contains(ac) && classEnvelope
//                            .isAssignableFrom(clz))) {
//                bl.add(b);
//            }
		}
		return bl;
	}

	public void log(String format, String ...l) {
	}

}
