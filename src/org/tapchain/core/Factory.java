package org.tapchain.core;

import java.util.ArrayList;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.TapChainEdit.ISystemPiece;


public class Factory<T extends IPiece> {
	ArrayList<IBlueprint> collect;
	ValueChangeNotifier notif;
	public Factory() {
		collect = new ArrayList<IBlueprint>();
//		addClassTest(test_cls.class);
	}
	public Factory<T> Register(IBlueprint root) {
		collect.add(root);
		if(notif != null)
			notif.notifyView();
		return this;
	}
	public IBlueprint get(int n) {
		return collect.get(n);
	}
	public void clear() {
		collect.clear();
		if(notif != null)
			notif.notifyView();
	}
	public void setNotifier(ValueChangeNotifier not) {
		notif = not;
	}
	@SuppressWarnings("unchecked")
	public T newInstance(int num, IPoint iPoint, IManager<IPiece> manager) {
//		Log.w("test", String.format("Factory: new instance %d", num));
		IBlueprint blueprint = get(num);
		IPiece rtn = null;
		if(blueprint == null)
			return null;
		try {
			rtn = blueprint.newInstance(manager);
//			if(rtn instanceof Actor)
//				((Actor)rtn).setLogLevel(true);
			Blueprint _view = getView(num);
			if(_view != null)
				((ActorManager)manager).setPieceView(rtn, _view, iPoint);
			manager._save();
			return (T)rtn;
		} catch (ChainException e) {
			manager.error(e);
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
	public void relatives(IBlueprint b, Factory<T> rel) {
		rel.clear();
		rel.Register(b);
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
	public interface ValueChangeNotifier {
		public void notifyView();
	}
}
