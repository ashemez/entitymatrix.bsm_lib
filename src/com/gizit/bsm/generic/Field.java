package com.gizit.bsm.generic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Field<T> {
    private T value;
    public T get() {
        return value;
    }
    public void set(T value) {
        this.value = value;
    }
}
