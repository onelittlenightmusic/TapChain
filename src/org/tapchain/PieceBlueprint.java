package org.tapchain;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import org.tapchain.AnimationChain.BasicPiece;
import org.tapchain.Chain.ChainException;
import org.tapchain.Chain.ChainPiece;
import org.tapchain.Chain.ChainPiece.PackType;

import android.util.Log;


public class PieceBlueprint {
	Class<? extends BasicPiece> cls = null;
	ArrayList<ConnectionBlueprint> connect = new ArrayList<ConnectionBlueprint>();
	ArrayList<PieceReservation> children = new ArrayList<PieceReservation>();
	PieceBlueprint view = null;
	PieceReservation This = null;
//	HashMap<Class<?>, Object> parent = null;
	Class<?> parent_type;
	Object parent_obj;
	protected PieceBlueprint() {
	}
	public PieceBlueprint(Class<? extends BasicPiece> _cls, BasicPiece... _args) {
		this();
		setClass(_cls);
		This = newReservation(_args).setParent(this);
	}
	protected PieceBlueprint setClass(Class<? extends BasicPiece> _cls) {
		cls = _cls;
		return this;
	}
	protected PieceBlueprint setLocalClass(Class<?> parent_type, Object parent_obj) {
//		parent = new HashMap<Class<?>, Object>();
//		parent.put(parent_type, parent_obj);
		this.parent_type = parent_type;
		this.parent_obj = parent_obj;
		return this;
	}
	public String getName() {
		return cls.getCanonicalName();
	}
	public PieceReservation newReservation(ChainPiece... args) {
		return new PieceReservation(this, args);
	}
	protected BasicPiece __newInstance(Class<?> types, Object args) throws IllegalAccessException, InstantiationException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
		if(args != null)
			return cls.getConstructor(types).newInstance(args);
		return cls.newInstance();
	}
	protected BasicPiece __newInstance() throws IllegalAccessException, InstantiationException {
		return cls.newInstance();
	}
	
	public BasicPiece newInstance(AnimationChainManager maker, PieceFactory factory) throws ChainException {
//		if(parent != null) {
//			Class<?>[] types = new Class<?>[]{};
//			parent.keySet().toArray(types);
//			return newInstance(maker, factory, types, parent.values().toArray());
		if(parent_obj != null) {
//			Log.w("test", String.format("%s, %s", parent_type.getName(), parent_obj));
			return newInstance(maker, factory, parent_type, parent_obj);
		}
		return newInstance(maker, factory, null, null);
	}
	public BasicPiece newInstance(AnimationChainManager maker, PieceFactory factory, Class<?> types, Object args) throws ChainException {
		BasicPiece rtn = null;
		ChainException ex = null;
		try {
			rtn = __newInstance(types, args);
			This.setInstance(rtn, maker, factory);
		__init_children(rtn, maker, factory);
		init_user(rtn, maker, factory);
		} catch (IllegalAccessException e) {
			ex = new ChainException(maker, "PieceFactory: cant create new instance");
		} catch (InstantiationException e) {
			ex = new ChainException(maker, "PieceFactory: cant create new instance");
		} catch (InterruptedException e) {
			ex = new ChainException(maker, "PieceFactory: cant create new instance");
		} catch (IllegalArgumentException e) {
			ex = new ChainException(maker, "PieceFactory: cant create new instance");
		} catch (SecurityException e) {
			ex = new ChainException(maker, "PieceFactory: cant create new instance");
		} catch (InvocationTargetException e) {
			ex = new ChainException(maker, "PieceFactory: cant create new instance");
		} catch (NoSuchMethodException e) {
			ex = new ChainException(maker, "PieceFactory: cant create new instance");
		}
		if(ex != null)
			if(maker != null)
				maker.Error(ex);
			else
				Log.w("PieceBlueprintError", "maker ==null and instantiation failed");
		if(maker != null) {
			maker.add(rtn);
		}
		return rtn;
	}
	public void init_user(BasicPiece newinstance, AnimationChainManager maker, PieceFactory factory) throws InterruptedException {
	}
	private BasicPiece __init_children(BasicPiece newinstance, AnimationChainManager maker, PieceFactory factory) throws IllegalAccessException, InstantiationException, ChainException, InterruptedException {
		for(PieceReservation local: children) {
			local.instantiate(maker, factory);
//			This.getInstance().addMember(local.getInstance());
		}
		for(ConnectionBlueprint cb: connect)
			cb.connect(maker);
		return newinstance;
	}
	public PieceReservation addLocal(PieceBlueprint bp, ChainPiece... args) {
		PieceReservation rtn = bp.newReservation(args).setParent(this);
		children.add(rtn);
		return rtn;
	}
	public PieceBlueprint append(ConnectionBlueprint conn) {
		connect.add(conn);
		return this;
	}
	public PieceBlueprint Append(PackType stack, PieceReservation target, PackType stack_target) {
		append(new ConnectionBlueprint(This, target, stack, stack_target));
		return this;
	}
	public PieceBlueprint refresh() {
		This.refresh();
		for(PieceReservation local: children)
			local.refresh();
		return this;
	}
	public PieceBlueprint setview(PieceBlueprint _view) {
		view = _view;
		return this;
	}
	
	public static class PieceReservation {
		PieceBlueprint cls = null, parent = null;
		BasicPiece instantiated = null;
		ChainPiece[] var = null;
		PieceReservation(PieceBlueprint _cls, ChainPiece... args) {
			cls = _cls;
			var = args;
		}
		PieceReservation setParent(PieceBlueprint _parent) {
			parent = _parent;
			return this;
		}
		public BasicPiece instantiate(AnimationChainManager maker, PieceFactory factory) throws IllegalAccessException, InstantiationException, ChainException, InterruptedException {
			return setInstance( cls.newInstance(maker, factory), maker, factory);
		}
		public synchronized BasicPiece setInstance(BasicPiece bp, AnimationChainManager maker, PieceFactory factory) throws ChainException {
			instantiated = bp;
			if(maker != null) {
				maker._return(instantiated);
				maker._mark();
				if(var != null)
					for(ChainPiece b: var) {
						Log.w("BlueprintInstance", "Args");
						if(b== null)
							continue;
					maker.args(b);
//					instantiated.addMember(b);
					maker._return();
				}
			}
			notifyAll();
			return instantiated;
		}
		public synchronized BasicPiece getInstance() throws InterruptedException {
			while(instantiated == null)
				wait();
			return instantiated;
		}
		public PieceReservation refresh() {
			instantiated = null;
			return this;
		}
		public PieceReservation Append(PackType stack, PieceReservation target, PackType stack_target) {
			parent.append(new ConnectionBlueprint(this, target, stack, stack_target));
			return this;
		}
	}
	
	public static class ConnectionBlueprint {
		PieceReservation Appender;
		PieceReservation Appendee;
		PackType appender_pack;
		PackType appendee_pack;
		PieceBlueprint view = null;
		public ConnectionBlueprint(PieceReservation pieceBlueprint,
				PieceReservation target, PackType stack, PackType stack_target) {
			Appender = pieceBlueprint;
			Appendee = target;
			appender_pack = stack;
			appendee_pack = stack_target;
		}
		public void connect(AnimationChainManager maker) throws ChainException, InterruptedException, IllegalAccessException, InstantiationException {
			maker.append(Appender.getInstance(), appender_pack, Appendee.getInstance(), appendee_pack);
			if(view != null)
				maker.add(view.newInstance(null, null));
/*			new Thread() {
				@Override
				public void run() {
					
				}
			};
*/		}
		public ConnectionBlueprint setview(PieceBlueprint _view) {
			view = _view;
			return this;
		}

	}
	
	public static class PieceBlueprintStatic extends PieceBlueprint {
		BasicPiece instance = null;
		public PieceBlueprintStatic(BasicPiece bp) {
			instance = bp;
		}
		@Override
		public BasicPiece newInstance(AnimationChainManager maker, PieceFactory factory) throws ChainException {
//			This.setInstance(instance);
			if(maker != null)
				maker.add(instance);
			return instance;
		}
	}
}
