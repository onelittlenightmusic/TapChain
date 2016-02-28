package org.tapchain;

import android.util.Log;

import org.tapchain.AndroidActor.AndroidView;
import org.tapchain.core.ActorManager;
import org.tapchain.core.ChainException;
import org.tapchain.core.Effector;
import org.tapchain.core.IPath;
import org.tapchain.core.IPoint;
import org.tapchain.core.Packet;
import org.tapchain.core.TapLib;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IPathTap;

public class PathTap extends AndroidView implements IPathTap {
		IPath myPath = null;
		IPoint recent = null;
		ActorManager manager;

		public PathTap() {
			super();
		}

		@Override
		public int onTick(IPath p, Packet obj) {
			return 1;
		}
		
		@Override
		public void ctrlStart() throws InterruptedException, ChainException {
            super.ctrlStart();
			TapLib.setTap(this);
		}
		
		@Override
		public void  ctrlStop() {
			TapLib.removeTap(this);
            super.ctrlStop();
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

	public static class PathMover extends Effector.ValueEffector<IPoint> {
		float beta = 0f;
		float dif = 0.01f;
		PathTap pathTap;
		PathMover(PathTap pathTap, float dif, int duration) {
			super();
			this.pathTap = pathTap;
			this.dif = dif;
            setDuration(duration);
		}

		@Override
		public IPoint _get() {
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