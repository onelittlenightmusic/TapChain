package org.tapchain.core;

import java.util.ArrayList;

public class ChainPrimitives {
	ChainPrimitives() {
	}
	
	public interface AxonN<T> extends IAxon<T> {
		public AxonN<T> resetOutN(int n);
//		public boolean pushN(int n, T x);
//		public T popN(int n);
		public IAxon<T> getN(int n);
	}
	
	public static class SyncQueueN<T> extends SyncQueue<T> implements AxonN<T> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int ncopy = 1, nmerge = 1;
		ArrayList<SyncQueue<T>> children = new ArrayList<SyncQueue<T>>();
		@Override
		public AxonN<T> resetOutN(int n) {
			children.clear();
			ncopy = n;
			for(int i = 0; i < n-1; i++) {
				children.add(new SyncQueue<T>());
			}
			return this;
		}
		@SuppressWarnings("unchecked")
		@Override
		public synchronized boolean sync_push(Object obj) throws InterruptedException {
			for(SyncQueue<T> q : children)
			q.sync_push((T)obj);
			super.sync_push((T)obj);
			return true;
		}
/*		public synchronized T popN(int n) {
			return children.get(n).pop();
		}
*/		public SyncQueue<T> getN(int n) {
			if(n == 0) {
				return this;
			}
			return children.get(n - 1);
		}
	}
/*
	public static class QueueMerge {
	}
*/
/*	
	public static class ModelCount extends ChainEditor {
		ModelCount(TouchEventCaller v, ConnectClosedView cfv, EventHandler va, final int last) {
			super(
			new Chain.Func("ChainPrimitives #5 Count") {
				int a = 0;
				public boolean func_impl(IO f) throws InterruptedException {
//					f.output(new Integer(a));
					a++;
					return a <= last;
				}
	
				@Override
				public boolean reset(IO f) {
					a = 0;
					return true;
				}
			}, null, null, v, cfv, va);
//			this.addOutClass(Integer.class);
		}
	}
*/
	/*
	public static class ModelHippo extends ChainEditor {
		ModelHippo(TouchEventCaller v, ConnectClosedView cfv, EventHandler va, final Class<?> clazz) {
			super(
				new Chain.Func("ChainPrimitives #4 Hippo") {	
					@Override
					public boolean func_impl(IO f) throws InterruptedException {
//						Object var1 = f.input(null);
//						if(var1 == null) 
//							return false;
//						f.output(var1);
						return true;
					}
					
				}, null, null, v, cfv, va);
//			addInClass(clazz);
//			addOutClass(clazz, Chain.IOType.HIPPO);
		}
	}
	*/
	/*
	public static class PseudoModelHippo extends ChainEditor {
		PseudoModelHippo(TouchEventCaller v, ConnectClosedView cfv, EventHandler va) {
			super(
					new Chain.Func("ChainPrimitives #3 Hippo") {
						@Override
						public boolean func_impl(IO f) throws InterruptedException {
//							Integer var1 = f.<Integer>input(0);
							Integer var1 = new Integer(1000);
//							if(var1 == null) 
//								return false;
//
//							f.output(var1);
							return false;
						}
					}, null, null, v, cfv, va);
//					this.addInClass(Integer.class)
//					this.addOutClass(Integer.class, Chain.IOType.HIPPO);
		}
	}
	*/
	/*
	public static class ModelIF extends ChainEditor {
		ModelIF(TouchEventCaller v, ConnectClosedView cfv, EventHandler va, float dv, Class<?> clazz) {
			super((Chain.Func)null, null, null, v, cfv, va);
			super.setChainFunc(new FuncIF(dv))
//				.addInClass(clazz)
//				.addInClass(clazz)
//				.addOutClass(clazz)
//				.setName("IF");
			;
		}
		class FuncIF extends Chain.Func {
			float division = 1f;
			public FuncIF(float dv) {
				super("ChainPrimitives #2 FuncIF");
				division = dv;
			}
			@Override
			public boolean func_impl(IO f) throws InterruptedException {
//				Object val1 = f.input(null);
//				Object val2 = f.input(1);
//				if(val1 == null || val2 == null) {
//					return false;
//				}
//				if(Math.random() < division) {
//					f.output(0,val1);
//				} else {
//					f.output(0,val2);
//				}
				return true;
			}
		}
	}
*/
	/*
	public static class ModelRoundrobin extends ChainEditor {
		ModelRoundrobin(TouchEventCaller v, ConnectClosedView cfv, EventHandler va, Class<?> clazz, int _variation) {
			super((Chain.Func)null, null, null, v, cfv, va);
			super.setChainFunc(new Chain.Func("ChainPrimitives #1 Round Robin") {
				int count = 0;
				int wraparound = 1;
				public Chain.Func set(int var) {
					wraparound = var;
					return this;
				}
				@Override
				public boolean func_impl(IO f) throws InterruptedException {
					Object[] val = new Object[wraparound];
//					for(int i = 0; i< wraparound; i++) {
//						val[i] = f.input(i);
//						if(val[i] == null)
//							return false;
//					}
//					f.output(0,val[count++%wraparound]);
					return true;
				}
			}.set(_variation));
			for(int i = 0; i < _variation; i++) {
//				super.addInClass(clazz);
			}
//			super.addOutClass(clazz)
//				.setName("RR");
		}
	}
*/
}
