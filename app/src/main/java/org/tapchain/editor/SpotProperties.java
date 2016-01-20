package org.tapchain.editor;

import android.graphics.Bitmap;

import org.tapchain.core.IPoint;

//		public class MyBeamTapStyle extends BeamTapStyle implements IRelease, IFocusable {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 1L;
//			ActorLink al = null;
//			ClassEnvelope clazz = null;
//			public MyBeamTapStyle(Resources r, IActorTap t, ActorLink al, ClassEnvelope clz) {
//				super(r, t, BitmapMaker.makeOrReuse("Beam", R.drawable.beam));
//				this.al = al;
//				this.clazz = clz;
//			}
//
//			@Override
//			public void onRelease(IPoint pos, IEdit editActor) {
//				super.onRelease(pos, editActor);
//				changeFocus(al, this, clazz);
//			}
//			
//			public void highlight(ActorLink al) {
//				getFocusControl().unfocusAll(this);
//				setColorCode(ColorLib.getLinkColor(al.reverse()));
//				getFocusControl().setSpotActorLink(al);
//			}
//			
//			public void unfocus() {
//				setColorCode(ColorCode.CLEAR);
//				getFocusControl().setSpotActorLink(null);
//			}
//		}
//		
		public class SpotProperties {
			private Bitmap bitmap;
			private IPoint margin;
			public SpotProperties(Bitmap bitmap, IPoint margin) {
				this.setBitmap(bitmap);
				this.setMargin(margin);
			}
			public Bitmap getBitmap() {
				return bitmap;
			}
			public void setBitmap(Bitmap bitmap) {
				this.bitmap = bitmap;
			}
			public IPoint getMargin() {
				return margin;
			}
			public void setMargin(IPoint margin) {
				this.margin = margin;
			}
		}