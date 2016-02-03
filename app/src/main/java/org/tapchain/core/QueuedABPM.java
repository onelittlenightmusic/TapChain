package org.tapchain.core;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

import org.tapchain.core.Actor.IDesigner;
import org.tapchain.core.Actor.IEffector;

public class QueuedABPM<TYPE extends Actor> extends ActorBlueprintManager<TYPE> {
	ConcurrentLinkedQueue<QueueBlueprint> queued = new ConcurrentLinkedQueue<QueueBlueprint>();
	QueueBlueprint now;
	public QueuedABPM(Factory<TYPE> f) {
		super(f);
	}
	
	public void setNow(QueueBlueprint now) {
		this.now = now;
		queued.add(now);
	}
	
	public QueueBlueprint getNow() {
		return now;
	}

	@Override
	public BlueprintManager<TYPE> add(Class<? extends TYPE> _cls) {
		setNow(new QueueBlueprint(_cls));
		return this;
	}

	@Override
	public <VALUE, INPUT, OUTPUT> BlueprintManager<TYPE> add(
			IDesigner<VALUE, INPUT, OUTPUT> designer) {
		setNow(new QueueBlueprint(designer));
		return this;
	}

	@Override
	public <PARENT, EFFECT> BlueprintManager<TYPE> addEffector(
			IEffector<PARENT, EFFECT> effector) {
		setNow(new QueueBlueprint(effector));
		return this;
	}

	@Override
	public BlueprintManager<TYPE> arg(Object... objs) {
		if(now != null)
			now.arg(objs);
		return this;
	}

	@Override
	public BlueprintManager<TYPE> setViewArg(Object... objs) {
		if(now != null)
			now.setTapArg(objs);
		return this;
	}

	@Override
	public BlueprintManager<TYPE> newSession() {
		return new QueuedABPM(factory).setOuterInstanceForInner(outer);
	}

	@Override
	public BlueprintManager<TYPE> save() {
		final QueuedABPM<TYPE> abm = this;
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				abm.__save();
			}
		});
		return this;
	}
	
	private void __save() {
		for(QueueBlueprint qb = queued.poll(); qb != null; qb = queued.poll()) {
			Object cls = qb.getCls();
			Object[] args = qb.getArgs();
			String tag = qb.getTag();
			Object[] objs = qb.getTapArg();
			if(cls != null) {
				if(cls instanceof Class)
					super.add((Class<? extends TYPE>)cls);
				else if(cls instanceof IDesigner)
					super.add((IDesigner)cls);
				else if(cls instanceof IEffector)
					super.addEffector((IEffector)cls);
				else
					return;
			}
			if(args != null)
				super.arg(args);
			if(tag != null)
				super.setTag(tag);
			if(objs != null)
				super.setViewArg(objs);
			super.save();
		}
		return;
	}

	@Override
	public BlueprintManager<TYPE> setTag(String tag) {
		if(now != null)
			now.setTag(tag);
		return this;
	}
	
	public class QueueBlueprint {
		Object cls;
		Object[] args;
		String tag;
		Object[] objs;
		public QueueBlueprint() {
		}
		public QueueBlueprint(Class<? extends Actor> cls) {
			this.cls = cls;
		}
		public QueueBlueprint(IDesigner designer) {
			this.cls = designer;
		}
		public QueueBlueprint(IEffector effector) {
			this.cls = effector;
		}
		public QueueBlueprint arg(Object... args) {
			this.args = args;
			return this;
		}
		public QueueBlueprint setTag(String tag) {
			this.tag = tag;
			return this;
		}
		public QueueBlueprint setTapArg(Object... objs) {
			this.objs = objs;
			return this;
		}
		public Object getCls() {
			return cls;
		}
		public Object[] getArgs() {
			return args;
		}
		public String getTag() {
			return tag;
		}
		public Object[] getTapArg() {
			return objs;
		}
	}
}
