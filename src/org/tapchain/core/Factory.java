package org.tapchain.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.IPiece;


public class Factory<T extends IPiece> {
	ArrayList<IBlueprint> collect;
	HashMap<Class<? extends T>, ArrayList<T>> items = new 
	HashMap<Class<? extends T>, ArrayList<T>>();
	IManager<IPiece> manager = null;
	public Factory(IManager<IPiece> m/*TapChainEdit e*/) {
		collect = new ArrayList<IBlueprint>();
		manager = m;
//		addClassTest(test_cls.class);
	}
	public Factory<T> Register(IBlueprint root) {
		collect.add(root);
		return this;
	}
	@SuppressWarnings("unchecked")
	public T newInstance(int num, WorldPoint wp) {
		IBlueprint blueprint = collect.get(num);
		IManager<IPiece> usermaker = manager;//editor.getUserManager();
		IPiece rtn = null;
		if(blueprint == null)
			return null;
		try {
			rtn = blueprint.newInstance(usermaker);
//			rtn.setLogLevel(true);
			Blueprint _view = getView(num);
			if(_view != null)
				((ActorManager) usermaker).setPieceView(rtn, _view, wp);
			usermaker.save();
			return (T)rtn;
		} catch (ChainException e) {
			usermaker.error(e);
		}
		return (T)rtn;
	}
	public int getSize() {
		return collect.size();
	}
	public String getName(int n) {
		if(n >= getSize())
			return null;
		return collect.get(n).getName();
	}
	public Blueprint getView(int n) throws ChainException {
		if(n >= getSize()) {
//			Log.w("PieceFactory", "Size Over, "+String.valueOf(n));
			throw new ChainException(this, "TapChain#PieceBlueprint size over");
		} else if(collect.get(n) == null) {
//			Log.w("PieceFactory", "Class null, "+String.valueOf(n));
			throw new ChainException(this, "TapChain#PieceBlueprint no object");
		} else if(collect.get(n).getView() == null) {
//			Log.w("PieceFactory", "View null, "+String.valueOf(n));
			throw new ChainException(this, "TapChain#PieceBlueprint object has no view");
		}
		return (Blueprint) collect.get(n).getView();
	}
//	ArrayList<Class<?>> collect_test  = new ArrayList<Class<?>>();
//	public PieceFactory addClassTest(Class<?> _cls) {
//		collect_test.add(_cls);
//		return this;
//	}
//	public Object getInstanceTest(int num) {
//		Class<?> c = collect_test.get(num);
//		if(c == null)
//			return null;
//		try {
//			return c.newInstance();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	public static class test_cls {
//		test_cls() {
//		}
//		String getname() {
//			return "this is test_cls";
//		}
//	}
}
