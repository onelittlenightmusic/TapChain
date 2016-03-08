package org.tapchain.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

public class ClassLib {
    final static HashMap<Class<?>, Map<Class<?>, ClassLibReturnInner>> inited2 = new HashMap<Class<?>, Map<Class<?>, ClassLibReturnInner>>();

    public static ClassLibReturn getParameterizedType(Class<?> cls,
                                                      Class<?> target) {
        ClassLibReturnInner rtn = getParameterizedTypeInner(cls, target);
        return rtn.getRtn();
    }

    public static ClassLibReturn getParameterizedTypeOld(Class<?> cls,
                                                         List<Type> newList, Class<?> target) {
        ClassLibReturnInner rtn2 = getParameterizedTypeInner(cls, target);
        ClassLibReturn convert = new ClassLibReturn();
        for (int i = 0; i < Math.min(rtn2.getArgs().size(), newList.size()); i++) {
            convert.put(rtn2.getArgs().get(i), newList.get(i));
        }
        log(String.format("converting %s", convert.toString()));
        convertToOriginalType2(rtn2, convert);
        log(String.format("encapsuling %s", convert.toString()));
        encapsuleParameterToEnvelope2(rtn2, convert);
        return rtn2.getRtn();
    }

    public static ClassLibReturnInner getParameterizedTypeInner(Class<?> cls,
                                                                Class<?> target) {
        log(String.format(">>>>>%s, target=%s", cls.getSimpleName(), target.getSimpleName()));
        ClassLibReturnInner rtn2;
        if (target != null && inited2.containsKey(target) && inited2.get(target).containsKey(cls)) {
            rtn2 = inited2.get(target).get(cls).copy();
        } else {
            TypeVariable<?>[] typeParams = cls.getTypeParameters();
            ClassArg args = new ClassArg(typeParams);
            if (cls == null || cls == target) {
                ClassLibReturnInner rtn1 = new ClassLibReturnInner();
                rtn1.setArgs(args);
                for (Type t1 : typeParams) {
                    rtn1.put(t1, t1);
                }
                log(String.format("<<<<<%s=%s", cls.getSimpleName(), rtn1.toString()));
                return rtn1;
            }
            Class<?> superClass = null;
            List<Type> newList = null;
            for (Type type : cls.getGenericInterfaces()) {
                log(String.format("{Interface:%s", type.toString()));
                Class<?> c = (type instanceof ParameterizedType) ? (Class<?>) ((ParameterizedType) type)
                        .getRawType() : (Class<?>) type;
                if (target == null || target.isAssignableFrom(c)) {
                    superClass = c;
                    newList = _getParameterizedType(type);
                    break;
                }
                log(String.format("}Interface:%s", type.toString()));
            }
            if (superClass == null) {
                Type type = cls.getGenericSuperclass();
                if (type != null && type != Object.class) {
                    log(String.format("{GenericSuper:%s", type.toString()));
                    Class<?> c = (type instanceof ParameterizedType) ? (Class<?>) ((ParameterizedType) type)
                            .getRawType() : (Class<?>) type;
                    if (target == null || target.isAssignableFrom(c)) {
                        superClass = c;
                        newList = _getParameterizedType(type);
                    }
                    log(String.format("}GenericSuper:%s", type.toString()));
                }
            }

            // THIS IS END
            if (superClass == null) {
                ClassLibReturnInner rtn1 = new ClassLibReturnInner();
                rtn1.setArgs(args);
                for (Type t1 : typeParams) {
                    rtn1.put(t1, t1);
                }
                log(String.format("<<<<<%s=%s", cls.getSimpleName(), rtn1.toString()));
                return rtn1;
            }
            rtn2 = getParameterizedTypeInner(superClass, target);
            ClassLibReturn convert = new ClassLibReturn();
            int i = 0;
            for (Type t1 : rtn2.getArgs()) {
                Type tparam = (i < newList.size()) ? newList.get(i++)
                        : Object.class;
                convert.put(t1, tparam);
            }
            rtn2.setArgs(args);
            log(String.format("converting %s", convert.toString()));
            convertToOriginalType2(rtn2, convert);
            log(String.format("encapsuling %s", convert.toString()));
            encapsuleParameterToEnvelope2(rtn2, convert);

            if (inited2.get(target) == null)
                inited2.put(target,
                        new ConcurrentHashMap<Class<?>, ClassLibReturnInner>());
            inited2.get(target).put(cls, rtn2.copy());
        }
        log(String.format("<<<<<%s=%s", cls.getSimpleName(), rtn2.toString()));
        return rtn2;
    }

    static final List<Type> zeroList = new ArrayList<Type>();

    static List<Type> _getParameterizedType(Type t) {
        if (t != null && t instanceof ParameterizedType) {
            List<Type> rtn = Arrays.asList(((ParameterizedType) t)
                    .getActualTypeArguments());
            StringBuilder sb = new StringBuilder();
            for (Type type : rtn) {
                sb.append(type.toString());
                sb.append("/");
            }
            log(String.format("New Args: %s", sb.toString()));
            return rtn;
        }
        return zeroList;
    }


    public static void convertToOriginalType(List<Type> res,
                                             Map<Type, Type> convert) {
        for (Entry<Type, Type> t3 : convert.entrySet()) {
            Type tkey = t3.getKey();
            while (res.contains(tkey)) {
                int index = res.indexOf(tkey);
                res.remove(index);
                log(String.format("REMOVE typeparameters:%s(%s)", tkey.toString(), tkey
                        .getClass().getSimpleName()));
                res.add(index, t3.getValue());
                log(String.format("ADD typeparameters:%s(%s)", t3.getValue().toString(), t3
                        .getValue().getClass().getSimpleName()));
            }
        }
    }

    public static void convertToOriginalType2(ClassLibReturnInner rtn2,
                                              Map<Type, Type> convert) {
        Map<Type, Type> rtnMap = rtn2.getRtn();
        for (Entry<Type, Type> rtnkv : rtnMap.entrySet()) {
            Type rtnvalue = rtnkv.getValue();
            if (convert.containsKey(rtnvalue)) {
                rtnMap.put(rtnkv.getKey(), convert.get(rtnvalue));
            }
        }
    }

    public static void encapsuleParameterToEnvelope2(ClassLibReturnInner rtn,
                                                     Map<Type, Type> convert) {
        Map<Type, Type> ptypes = new HashMap<Type, Type>();
        for (Entry<Type, Type> kv : rtn.getRtn().entrySet()) {
            Type rtnValue = kv.getValue();
            if (rtnValue instanceof ParameterizedType) {
                ptypes.put(kv.getKey(), rtnValue);
            }
        }
        for (Entry<Type, Type> kv : ptypes.entrySet()) {
            Type key = kv.getKey();
            Type t5 = new ClassEnvelope((ParameterizedType) kv.getValue(),
                    convert);
            rtn.getRtn().put(key, t5);
            log(String.format("ADD typeparameters:%s(%s)", t5.toString(), t5.getClass()
                    .getSimpleName()));
        }

    }

    public static class ClassLibReturn extends ConcurrentHashMap<Type, Type> {
        public ClassEnvelope searchByName(String name) {
            for (Entry<Type, Type> entry : entrySet())
                if (entry.getKey().toString().equals(name)) {
                    Type rtn = entry.getValue();
                    if (rtn instanceof Class<?>)
                        return new ClassEnvelope((Class<?>) rtn);
                    else if (rtn instanceof ClassEnvelope)
                        return (ClassEnvelope) rtn;
                }
            return null;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (Entry<Type, Type> entry : entrySet()) {
                builder.append("[");
                String keyName;
                Type key = entry.getKey();
                if (key instanceof Class<?>)
                    keyName = ((Class<?>) key).getSimpleName();
                else
                    keyName = key.toString();
                builder.append(keyName);
                builder.append(":");
                String valueName;
                Type value = entry.getValue();
                if (value instanceof Class<?>)
                    valueName = ((Class<?>) value).getSimpleName();
                else
                    valueName = value.toString();
                builder.append(valueName);
                builder.append("]");
            }
            return builder.toString();
        }

        public ClassLibReturn copy() {
            ClassLibReturn rtn = new ClassLibReturn();
            rtn.putAll(this);
            return rtn;
        }
    }

    public static class ClassArg extends ArrayList<Type> {
        public ClassArg() {
            super();
        }

        public ClassArg(Type... types) {
            this();
            this.addAll(Arrays.asList(types));
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Type t : this) {
                sb.append(t.toString());
                sb.append("/");
            }
            return sb.toString();
        }

        public ClassArg copy() {
            ClassArg rtn = new ClassArg();
            for (Type t : this) {
                if (t instanceof ClassEnvelope)
                    rtn.add(((ClassEnvelope) t).copy());
                else
                    rtn.add(t);
            }
            return rtn;
        }
    }

    public static class ClassLibReturnInner {
        private ClassLibReturn rtn;
        private ClassArg args;

        public ClassLibReturnInner() {
            rtn = new ClassLibReturn();
            args = new ClassArg();
        }

        public ClassLibReturnInner(ClassLibReturn rtn, ClassArg args) {
            this.setRtn(rtn);
            this.setArgs(args);
        }

        public ClassLibReturn getRtn() {
            return rtn;
        }

        public void setRtn(ClassLibReturn rtn) {
            this.rtn = rtn;
        }

        public ClassArg getArgs() {
            return args;
        }

        public void setArgs(ClassArg args) {
            this.args = args;
        }

        public void put(Type t, Type t2) {
            rtn.put(t, t2);
        }

        @Override
        public String toString() {
            return String.format("Args:%s, Rtn:%s", args.toString(),
                    rtn.toString());
        }

        public ClassLibReturnInner copy() {
            return new ClassLibReturnInner(rtn.copy(), args.copy());
        }
    }

    protected static void log(String logs) {
//        Log.w("ClassLib", logs);
    }

    private static final Map<Class<?>, Class<?>> PrimitiveWrapper
            = new HashMap<Class<?>, Class<?>>() {
        {
            put(boolean.class, Boolean.class);
            put(byte.class, Byte.class);
            put(char.class, Character.class);
            put(double.class, Double.class);
            put(float.class, Float.class);
            put(int.class, Integer.class);
            put(long.class, Long.class);
            put(short.class, Short.class);
            put(void.class, Void.class);
        }
    };

    public static boolean checkAssignability(Class<?> parent, Class<?> child) {
        return parent.isAssignableFrom(child)
                || (parent.isPrimitive() && PrimitiveWrapper.containsKey(parent)
                && PrimitiveWrapper.get(parent).isAssignableFrom(child)
        );
    }
}

