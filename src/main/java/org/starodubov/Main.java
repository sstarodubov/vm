package org.starodubov;

import org.starodubov.vm.Vm;
import org.starodubov.vm.value.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("no files are passed");
            return;
        }

        final var vm = new Vm();
        final String program = Files.readString(Path.of(args[0]));

        final Value result = vm.exec(program);

        System.out.println("Result: " + result);
    }
}