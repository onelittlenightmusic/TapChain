package org.tapchain;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.core.ActorManager;
import org.tapchain.core.IActorSharedHandler;
import org.tapchain.core.IPath;
import org.tapchain.core.IPoint;
import org.tapchain.core.TapLib;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IPathTap;

import android.util.Log;

public class PathTap extends AndroidView implements IPathTap {
		IPath myPath = null;
		IPoint recent = null;
		ActorManager manager;

		public PathTap() {
			super();
		}

		@Override
		public int onTick(IPath p, Object obj) {
			return 1;
		}
		
		@Override
		public void onAdd(ActorManager maker) {
//			manager = maker;
			TapLib.setTap(this);
		}
		
		@Override
		public PathTap end() {
			super.end();
//			manager.remove(this);
			TapLib.removeTap(this);
			return this;
		}

		@Override
		public IPoint getRecentPoint() {
			return recent;
		}

		@Override
		public void setRecentPoint(IPoint newKey) {
			recent = newKey;
		}

		@Override
		public void setMyPath(IPath path) {
			myPath = path;
		}

		@Override
		public IPath getMyPath() {
			return myPath;
		}

		@Override
		public void unsetMyPath() {
			myPath = null;
		}

		@Override
		public boolean contains(IPoint p) {
			Log.w("test", String.format("PathStyle.contains(%.2f, %.2f)", p.x(), p.y()));
			return p.subNew(getCenter()).getAbs() < 50f;
		}

	@Override
	public void setEditor(ActorManager manager) {
		this.manager = manager;
	}

	@Override
	public IPoint getPoint(float beta) {
		return WorldPoint.zero();
	}

	@Override
		public boolean hasEventHandler() {
			return false;
		}

		@Override
		public IActorSharedHandler getSharedHandler() {
			return null;
		}
		
	public PathMover getPathMover() {
		return new PathMover(this, 0.01f);
	}

	public static class PathMover extends ValueEffector<IPoint> {
		float beta = 0f;
		float dif = 0.01f;
		PathTap pathTap;
		PathMover(PathTap pathTap, float dif) {
			super();
			this.pathTap = pathTap;
			this.dif = dif;
		}

		@Override
		public IPoint _valueGet() {
			// values is not initialized so that it cannot be assigned
			beta += dif;
			if(beta > 1.0f)
				beta = 0f;
			if(pathTap == null)
				return null;
			IPoint rtn = pathTap.getPoint(beta);
			if(rtn == null)
				return WorldPoint.zero();
			return rtn;
		}

	}
}