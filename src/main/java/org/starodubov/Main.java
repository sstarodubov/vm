package org.starodubov;

import org.starodubov.vm.Vm;

import java.text.ParseException;

public class Main {
    public static void main(String[] args) throws ParseException {
        var vm = new Vm();
        vm.exec("""
                
                42
                
                """);

    }
}