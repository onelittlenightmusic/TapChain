package org.tapchain.core;

import org.tapchain.core.Actor.Controllable;
import org.tapchain.core.Chain.ChainException;

public class VirtualActor extends Controllable implements IValue<String>, IValueLog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8815935158215335864L;
	static {
	}
	String realId;
	String url = "http://gbforonelittlenight.appspot.com/push";
	String value;
	
	public VirtualActor(Class<?> c) {
		super();
		setAutoStart();
		setAutoEnd();
//		setLogLevel(true);
//		once();
		this.realId = c.getSimpleName();
//		final String id = realId;
//		this.getInPack(PackType.OFFER).setUserPathListener(new IPathListener() {
//			@Override
//			public void OnPushed(Connector p, Object obj)
//					throws InterruptedException {
//				pushObject(id, obj);
//			}
//		});
//		Log.w("test", "VirtualActor constructor:"+ c.getSimpleName());
//		try {
//			c.getMethod("staticInit", null).invoke(null, null);
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			l.loadClass("AndroidActor.AndroidMail");
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		try {
//			Class.forName(c.getCanonicalName(), true, l);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
		// For Dynamic Class Loading
		try {
			c.newInstance();
		} catch (InstantiationException e) {
//			e.printStackTrace();
		} catch (IllegalAccessException e) {
//			e.printStackTrace();
		}
		__setAssociatedClasses(c);
	}
	
	public VirtualActor(IBlueprint b) {
		this(b.getBlueprintClass());
	}
	
	@Override
	public boolean actorRun(Actor a) throws ChainException, InterruptedException {
		pushObject(realId, pull());
		return true;
	}

	public void pushObject(final String realId, final Object obj) {
////		new AsyncTask<Object, Void, String>() {
////			@Override
////			protected String doInBackground(Object... arg0) {
//				HttpClient client = new DefaultHttpClient();
////				Uri.Builder builder = new Uri.Builder();
////				builder.scheme("http");
////				builder.encodedAuthority(url);
////				builder.path(path);
////				builder.appendQueryParameter("id", realId);
////				if(obj != null)
////					builder.appendQueryParameter("obj", obj.toString());
////				HttpGet httpUriReq = new HttpGet(builder.build().toString());
//				HttpGet httpUriReq = new HttpGet(String.format("%s?id=%s&type=%s&obj=%s", url, realId, obj.getClass().getSimpleName(), obj.toString()));
//				String rtn = "";
//				try {
////					HttpResponse res = client.execute(httpUriReq);
//					rtn = client.execute(httpUriReq, new ResponseHandler<String>() {
//				        @Override
//				        public String handleResponse(HttpResponse response)
//				                throws ClientProtocolException, IOException {
//				            switch (response.getStatusLine().getStatusCode()) {
//				            case HttpStatus.SC_OK:
//				                return EntityUtils.toString(response.getEntity(), "UTF-8");
//				            }
//				            return "";
//				        }
//					});
////					if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
////						String entity = EntityUtils.toString(res.getEntity(), "UTF-8");
//////						throw new InterruptedException(String.format("VirtualActor: %s", entity));
////					}
////					rtn = EntityUtils.toString(res.getEntity(), "UTF-8");
//				} catch (ClientProtocolException e) {
//					e.printStackTrace();
////					throw new InterruptedException("VirtualActor: ClientProtocolException");
//				} catch (IOException e) {
//					e.printStackTrace();
////					throw new InterruptedException("VirtualActor: IOException");
//				}
//				client.getConnectionManager().shutdown();
////				return rtn;
////			}
////		    @Override
////		    protected void onPostExecute(String rtn) {
//		    	_valueSet(rtn);
//		    	Object pushing = CodingLib.decode(rtn, obj.getClass().getSimpleName());
//		    	push(pushing);
////		    	push(rtn);
//		    	invalidate();
////		    }
////		}.execute();
	}
	
	@Override
	public boolean _valueSet(String value) {
		this.value = value;
		return true;
	}

	@Override
	public String _valueGet() {
		return value;
	}
	String dest = "";

	@Override
	public Object _valueLog() {
		return value;
	}


}
