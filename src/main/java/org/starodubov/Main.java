package org.starodubov;

import org.starodubov.vm.Vm;

public class Main {
    public static void main(String[] args) {
        var vm = new Vm();

        vm.exec("""
               
               42
               
               """);
    }
}