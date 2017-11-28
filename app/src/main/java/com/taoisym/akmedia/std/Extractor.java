package com.taoisym.akmedia.std;

import java.util.Iterator;


public interface Extractor<T> {
    T apply(Iterator<T> in);
}

