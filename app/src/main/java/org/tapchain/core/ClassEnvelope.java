package org.tapchain.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class ClassEnvelope implements ParameterizedType, Comparable<ClassEnvelope> {
	ArrayList<Type> arguments = new ArrayList<Type>();
	Type original;
	Class<?> raw;

	public ClassEnvelope(List<Class<?>> clz) {
		if(!clz.isEmpty()) {
			Class<?> cls = clz.get(0);
			original = cls;
			raw = cls;
			arguments.addAll(clz);
			arguments.remove(0);
		}
	}
	
	public ClassEnvelope(Class<?> cls) {
		original = cls;
		raw = cls;
	}
	
	public ClassEnvelope(Object obj) {
		this(obj.getClass());
	}
	
	public ClassEnvelope(ParameterizedType pt, Map<Type, Type> convert) {
		original = pt;
		raw = (Class<?>) pt.getRawType();
		arguments = new ArrayList<Type>(ClassLib._getParameterizedType(pt));
		ClassLib.convertToOriginalType(arguments, convert);
	}

	public ClassEnvelope(Class<?> class1, ClassEnvelope parameter) {
		this(class1);
		arguments.addAll(parameter.getAllArguments());
	}
	
	@Override
	public Type[] getActualTypeArguments() {
		return arguments.toArray(new Type[]{});
	}

	@Override
	public Type getOwnerType() {
		return null;
	}

	@Override
	public Type getRawType() {
		return raw;
	}
	
	public void setArguments(Type arg) {
		arguments.add(arg);
	}
	
	public List<Class<?>> getAllArguments() {
		ArrayList<Class<?>> n = new ArrayList<Class<?>>();
		for(Type t : arguments)
			n.add((Class<?>)t);
		n.add(0, raw);
		return n;
	}
	
	public int argumentSize() {
		return arguments.size();
	}
	
	public String getClassString(Type type) {
		if(type instanceof Class)
			return ((Class)type).getSimpleName();
		return type.toString();
	}
	
	public String toString() {
		if(raw != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(getClassString(raw));
			sb.append("/");
			for(Type argument: arguments) {
				sb.append(getClassString(argument));
				sb.append(",");
			}
			return sb.toString();
		}
		if(original != null) {
			return getClassString(original);
		}
		return "";
	}
	
	@Override
	public int compareTo(ClassEnvelope arg0) {
		return ((Integer)this.hashCode()).compareTo(arg0.hashCode());
	}
	
	public boolean isAssignableFrom(ClassEnvelope ce0) {
		if(raw == null)
			return true;
		if(ce0 == null)
			return false;
		if(!raw.isAssignableFrom(ce0.raw)) {
			log("<Parent->Child> %s -> %s FALSE", toString(), ce0.toString());
			return false;
		}
		ArrayList<Type> parameters = new ArrayList<Type>(ce0.arguments);
		if(!ce0.raw.equals(raw)) {
				Map<Type, Type> rtn = ClassLib.getParameterizedTypeOld(ce0.raw, parameters, raw);
				if(rtn != null)
//					addLog(String.format("<> MAP %s", rtn.toString()));
				parameters = new ArrayList<Type>(rtn.values());
		}
//			return false;
		if(parameters.size() <= arguments.size()) {
			//It checks whether parent class uses erasure. If erasure is used, this method always returns true.
			//Otherwise this checks parameter inheritance.
			for(int i = 0; i < parameters.size(); i++) {
				Type parametergot = parameters.get(i);
				if(!(parametergot instanceof Class)) {
					log("<Parent->Child> %s -> %s FALSE", toString(), ce0.toString());
					return false;
					
				}
				if(parametergot.equals(Self.class))
					parametergot = ce0.raw;
				if(!getSubclass(i).isAssignableFrom((Class<?>)parametergot)) {
					log("<Parent->Child> %s -> %s FALSE", toString(), ce0.toString());
					return false;
				}
			}
		}
		log("<Parent->Child> %s -> %s TRUE", toString(), ce0.toString());
		return true;
	}
	

	public Class<?> getSubclass(int num) {
		if(argumentSize() > num)
			return (Class<?>) arguments.get(num);
		return null;
	}

	public String getSimpleName() {
		if(raw != null)
			return raw.getSimpleName();
		return "";
	}

	public Class getRawClass() {
		return raw;
	}

	public ClassEnvelope copy() {
		return new ClassEnvelope(this.raw, this);
	}
	
	public void log(String format, String... l) {
//		Log.w("ClassEnvelope", String.format(format, l));
	}
	

}