package org.starodubov.vm.utils;

import java.util.ArrayList;

public class DebugArrayList<T> extends ArrayList<T> {

    int dSize = 0;

    @Override
    public boolean add(T t) {
        dSize++;
        return super.add(t);
    }

    @Override
    public int size() {
        return dSize;
    }

    @Override
    public boolean isEmpty() {
        return dSize == 0;
    }

    @Override
    public T getLast() {
        return get(dSize - 1);
    }

    @Override
    public T getFirst() {
        return get(0);
    }

    @Override
    public T removeLast() {
        dSize--;
        return null;
    }
}
