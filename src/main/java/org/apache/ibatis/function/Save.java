package org.apache.ibatis.function;

@FunctionalInterface
public interface Save<T> {

    int save(T datum);
}
