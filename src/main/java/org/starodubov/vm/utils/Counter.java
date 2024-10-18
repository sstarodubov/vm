package org.starodubov.vm.utils;

public class Counter {

    public Counter(int init) {
        value = init;
    }

    public Counter() {
        value = 0;
    }

    private int value;

    public void inc() {
        value++;
    }

    public void dec() {
       value--;
    }

    public int value() {
        return value;
    }
}
