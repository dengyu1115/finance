package org.apache.ibatis.function;

@FunctionalInterface
public interface Merge<T> {

    int merge(T datum);
}
