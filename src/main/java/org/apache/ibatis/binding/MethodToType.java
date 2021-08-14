package org.apache.ibatis.binding;

import org.apache.ibatis.function.*;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodToType {

    private static final Map<Class<?>, SqlCommandType> CLASS_MAP;

    static {
        CLASS_MAP = new HashMap<>();
        CLASS_MAP.put(Save.class, SqlCommandType.INSERT);
        CLASS_MAP.put(BatchSave.class, SqlCommandType.INSERT);
        CLASS_MAP.put(Merge.class, SqlCommandType.INSERT);
        CLASS_MAP.put(BatchMerge.class, SqlCommandType.INSERT);
        CLASS_MAP.put(DeleteById.class, SqlCommandType.DELETE);
        CLASS_MAP.put(DeleteByIds.class, SqlCommandType.DELETE);
        CLASS_MAP.put(DeleteAll.class, SqlCommandType.DELETE);
        CLASS_MAP.put(Update.class, SqlCommandType.UPDATE);
        CLASS_MAP.put(FindById.class, SqlCommandType.SELECT);
        CLASS_MAP.put(FindByIds.class, SqlCommandType.SELECT);
    }

    public static SqlCommandType get(Method method) {
        Class<?> cls = method.getDeclaringClass();
        return CLASS_MAP.get(cls);
    }
}
