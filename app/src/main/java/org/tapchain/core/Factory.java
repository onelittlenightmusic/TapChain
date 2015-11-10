package org.tapchain.core;

import org.tapchain.core.Chain.ChainException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Factory<PIECE extends Piece> extends ArrayList<IBlueprint<PIECE>> {
	ValueChangeNotifier notif;

	public Factory() {
		super();
	}

	public Factory<PIECE> Register(IBlueprint root) {
//		collect.add(root);
		add(root);
		notifyView();
		return this;
	}
	
	public Factory<PIECE> Register(List<IBlueprint<PIECE>> root) {
		addAll(root);
		notifyView();
		return this;
	}
	
	public List<IBlueprint<PIECE>> getList() {
		return this;
	}

	public void clear() {
		super.clear();
		notifyView();
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

	public void setRelatives(LinkType ac, ClassEnvelope classEnvelope, Factory<PIECE> rel) {
		rel.clear();
		rel.Register(getConnectables(ac, classEnvelope));
	}

	public interface ValueChangeNotifier {
		public void notifyChange();

		public void invalidate();
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
		List<IBlueprint<PIECE>> bl = new ArrayList<IBlueprint<PIECE>>();
		for (IBlueprint b : this) {
			if (classEnvelope != null) {
				if (b instanceof IActorBlueprint) {
					ClassEnvelope clz = ((IActorBlueprint) b)
							.getConnectClass(ac);
					if (clz != null) {
						if ((EnumSet.of(LinkType.PULL, LinkType.FROM_PARENT)
								.contains(ac) && clz
								.isAssignableFrom(classEnvelope))
								|| (EnumSet.of(LinkType.PUSH,
										LinkType.TO_CHILD).contains(ac) && classEnvelope
										.isAssignableFrom(clz))) {
							bl.add(b);
						}
					}
				}
			}
		}
		return bl;
	}

	public void log(String format, String ...l) {
	}

}
