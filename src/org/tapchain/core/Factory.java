package org.tapchain.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.tapchain.core.Chain.ChainException;


public class Factory {
	ArrayList<IBlueprint> collect;
	HashMap<Class<? extends Actor>, ArrayList<Actor>> items = new 
	HashMap<Class<? extends Actor>, ArrayList<Actor>>();
	TapChainEdit editor = null;
	public Factory(TapChainEdit e) {
		collect = new ArrayList<IBlueprint>();
		editor = e;
//		addClassTest(test_cls.class);
	}
	public Factory Register(IBlueprint root) {
		collect.add(root);
		return this;
	}
	public Actor getInstance(int num) {
		IBlueprint pbp = collect.get(num);
		ActorManager usermaker = editor.getUserManager();
		Actor rtn = null;
		if(pbp == null)
			return null;
		try {
			rtn = pbp.newInstance(usermaker);
			rtn.setLogLevel(true);
			Blueprint _view = createView(num);
			if(_view != null)
				usermaker.setPieceView(rtn, _view);
			usermaker.save();
			return rtn;
		} catch (ChainException e) {
			usermaker.error(e);
		}
		return rtn;
	}
	public int getSize() {
		return collect.size();
	}
	public String getName(int n) {
		if(n >= getSize())
			return null;
		return collect.get(n).getName();
	}
	public Blueprint createView(int n) throws ChainException {
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
