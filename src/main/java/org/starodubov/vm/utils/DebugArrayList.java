package org.starodubov.vm.utils;

import java.util.ArrayList;

public class DebugArrayList<T> extends ArrayList<T> {

    int p = 0;

    @Override
    public boolean add(T t) {
        p++;
        return super.add(t);
    }

    @Override
    public int size() {
        return p;
    }

    @Override
    public boolean isEmpty() {
        return p == 0;
    }

    @Override
    public T getLast() {
        return get(p);
    }

    @Override
    public T getFirst() {
        return get(0);
    }

    @Override
    public T remove(int index) {
        final var ret = super.get(index);
        p--;
        return ret;
    }
}
