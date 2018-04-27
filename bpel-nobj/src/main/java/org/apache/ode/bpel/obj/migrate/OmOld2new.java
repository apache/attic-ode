package org.apache.ode.bpel.obj.migrate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ode.bpel.obj.ExtensibleImpl;
import org.apache.ode.bpel.obj.OProcess.OExtension;

/**
 * Migrate from old Omodel classes to new ones.
 * 
 * @author fangzhen
 * @see ObjectTraverser
 */
public class OmOld2new extends AbstractObjectVisitor {
    private static final Logger __log = LoggerFactory.getLogger(OmOld2new.class);

    private static Map<String, String> beanPkgMap = new HashMap<String, String>();
    static {
        beanPkgMap.put("org.apache.ode.bpel.o", "org.apache.ode.bpel.obj");
        beanPkgMap.put("org.apache.ode.bpel.elang.xpath10.o",
                "org.apache.ode.bpel.elang.xpath10.obj");
        beanPkgMap.put("org.apache.ode.bpel.elang.xpath20.o",
                "org.apache.ode.bpel.elang.xpath20.obj");
        beanPkgMap.put("org.apache.ode.bpel.elang.xquery10.o",
                "org.apache.ode.bpel.elang.xquery10.obj");
    }

    public Object visit(Object obj) {
        __log.debug("migrating object: " + obj.getClass() + "@" + System.identityHashCode(obj));
        Object n;
        /*
         * we use two category of visitXXX methods here. The first visitXXX(Object) return
         * corresponding new object instance without fulfilling its contents, which avoids
         * recursively call. And then assign the new object. then fill contents. other wise, on
         * cyclic reference case, the object re-visited but hasn't prepared yet. However, this
         * workaround assumes that the new object is mutable, which is true in our case.
         */
        if (isMap(obj)) {
            n = visitMap(obj);
        } else if (isCollection(obj)) {
            n = visitCollection(obj);
        } else if (isArray(obj)) {
            n = visitArray(obj);
        } else {
            n = visitPojo(obj);
        }
        rtab.assign(obj, n);

        if (isMap(obj)) {
            visitMap(obj, n);
        } else if (isCollection(obj)) {
            visitCollection(obj, n);
        } else if (isArray(obj)) {
            visitArray(obj, n);
        } else {
            visitPojo(obj, n);
        }
        return n;
    }

    @Override
    protected boolean isCollection(Object old) {
        return (old instanceof Collection);
    }

    private boolean isOmodelBean(Object old) {
        Class<?> cls = old.getClass();
        if (beanPkgMap.containsKey(cls.getPackage().getName())
                && !cls.getSimpleName().equals("Serializer")) {
            return true;
        }
        return false;
    }

    @Override
    public Object visitArray(Object old) {
        throw new UnsupportedOperationException("Create new Array is unsupported");
    }

    private void visitArray(Object obj, Object n) {
        throw new UnsupportedOperationException("We don't need the method here");
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public Object visitCollection(Object old) {
        Collection o = (Collection) old;
        try {
            Collection n = o.getClass().newInstance();
            return n;
        } catch (Exception e) {
            // should not get here
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void visitCollection(Object old, Object nu) {
        Collection o = (Collection) old;
        Collection n = (Collection) nu;
        for (Object obj : o) {
            n.add(traverse.traverseObject(obj));
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object visitMap(Object old) {
        Map o = (Map) old;
        try {
            Map n = o.getClass().newInstance();
            return n;
        } catch (Exception e) {
            // should not get here
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void visitMap(Object obj, Object nu) {
        Set<Entry> entries = ((Map) obj).entrySet();
        Map n = (Map) nu;
        for (Entry e : entries) {
            n.put(traverse.traverseObject(e.getKey()), traverse.traverseObject(e.getValue()));
        }
    }

    @Override
    public Object visitPojo(Object old) {
        if (!isOmodelBean(old)) {
            return old;
        } else {
            return initiateNew(old);
        }
    }

    private void visitPojo(Object old, Object n) {
        if (isOmodelBean(old)) {
            // @hahnml: We need some special handling for the migration of OAssign model
            // elements since the new OModel supports extension assign operations.
            if (old.getClass().getSimpleName().equals("OAssign")) {
                constructNewOAssign(old, n);
            } else {
                constructNewOm(old, n);
            }
        }
    }

    /**
     * construct new omodel instances from old ones. Assume <code>old</code> is an old OmodelBean
     * 
     * @param old
     * @return
     */
    private Object constructNewOm(Object old, Object tn) {
        assert tn instanceof ExtensibleImpl;
        ExtensibleImpl n = (ExtensibleImpl) tn;
        List<Field> fields = getAllFields(old.getClass());
        Map<String, Object> fieldMap = n.getFieldContainer();
        for (Field f : fields) {
            if ((f.getModifiers() & Modifier.STATIC) != 0) {
                continue; // skip static fields
            }
            f.setAccessible(true);
            try {
                String fname = f.getName();
                Object fvalue = f.get(old);
                if (fvalue != null) {
                    fieldMap.put(fname, traverse.traverseObject(fvalue));
                } else {
                    fieldMap.put(fname, null);
                }
            } catch (Exception e) {
                RuntimeException rte = new RuntimeException(
                        "Error when try to construct corresponding new Omodel class from old one:"
                                + old.getClass() + "; Failed on field:" + f.getName());
                rte.initCause(e);
                throw rte;
            }
        }

        // @hahnml: We need to add the new "declaredExtensions" and "mustUnderstandExtensions" fields to the process for
        // equality.
        if (old.getClass().getSimpleName().equals("OProcess")) {
            fieldMap.put("declaredExtensions", new HashSet<OExtension>());
            fieldMap.put("mustUnderstandExtensions", new HashSet<OExtension>());
        }

        n.setClassVersion(1);
        n.setOriginalVersion(0);
        return n;
    }

    private Object constructNewOAssign(Object old, Object tn) {
        assert tn instanceof ExtensibleImpl;
        ExtensibleImpl n = (ExtensibleImpl) tn;
        List<Field> fields = getAllFields(old.getClass());
        Map<String, Object> fieldMap = n.getFieldContainer();
        for (Field f : fields) {
            if ((f.getModifiers() & Modifier.STATIC) != 0) {
                continue; // skip static fields
            }
            f.setAccessible(true);
            try {
                String fname = f.getName();
                Object fvalue = f.get(old);

                if (fname.equals("copy")) {
                    fname = "operation";
                }

                if (fvalue != null) {
                    fieldMap.put(fname, traverse.traverseObject(fvalue));
                } else {
                    fieldMap.put(fname, null);
                }
            } catch (Exception e) {
                RuntimeException rte = new RuntimeException(
                        "Error when try to construct corresponding new Omodel class from old one:"
                                + old.getClass() + "; Failed on field:" + f.getName());
                rte.initCause(e);
                throw rte;
            }
        }

        n.setClassVersion(1);
        n.setOriginalVersion(0);
        return n;
    }

    private List<Field> getAllFields(Class cls) {
        return getAllFieldsRec(cls, new ArrayList<Field>());
    }

    private List<Field> getAllFieldsRec(Class cls, ArrayList<Field> fields) {
        Class par = cls.getSuperclass();
        if (par != null) {
            getAllFieldsRec(par, fields);
        }
        fields.addAll(Arrays.asList(cls.getDeclaredFields()));
        return fields;
    }

    private Object initiateNew(Object old) {
        String clsName = old.getClass().getName();
        String qcls = clsName.replace(".o.", ".obj.");
        try {
            Constructor cons = Class.forName(qcls).getConstructor();
            cons.setAccessible(true);
            return cons.newInstance();
        } catch (Exception e) {
            RuntimeException rte = new RuntimeException(
                    "Error when try to initiate corresponding new Omodel class of old one:"
                            + old.getClass());
            rte.initCause(e);
            throw rte;
        }
    }

    @Override
    public Object visitSet(Object obj) {
        throw new UnsupportedOperationException("We don't really need this operatiion here");
    }
}
