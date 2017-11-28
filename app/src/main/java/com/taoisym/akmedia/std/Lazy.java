package com.taoisym.akmedia.std;


abstract public class Lazy<T> {
    T refid;

    protected abstract T refid();

    final public T get() {
        if (refid != null)
            return refid;
        synchronized (this) {
            if (refid != null)
                return refid;
            refid = refid();
            return refid;
        }
    }
}
