package org.starodubov.vm;

import org.starodubov.vm.value.Value;

public class GlobalVar {
        public String name;
        public Value value;

        public GlobalVar(String name, Value value) {
                this.name = name;
                this.value = value;
        }


        @Override
        public String toString() {
                return "{GlobalVar[name=%s,value=%s]}".formatted(name, value);
        }
}
