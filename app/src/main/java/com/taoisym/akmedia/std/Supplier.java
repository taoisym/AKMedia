package com.taoisym.akmedia.std;



public class Supplier<T> {
    T val;

    final public void set(T in) {
        synchronized (this) {
            if (val != in) {
                val = in;
                notifyAll();
            }

        }
    }

    final public T get() {
        if (val != null)
            return val;
        synchronized (this) {
            try {
                while (val == null) {
                    wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return val;
        }
    }
}

