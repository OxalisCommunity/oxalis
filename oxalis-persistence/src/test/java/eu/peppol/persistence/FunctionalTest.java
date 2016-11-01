package eu.peppol.persistence;

import org.testng.annotations.Test;

/**
 * @author steinar
 *         Date: 18.10.2016
 *         Time: 16.05
 */
public class FunctionalTest {


    @Test
    void t() {

        Workhorse w = new Workhorse("Hallo ");
        String t = "test";
        StringOperation operation = (st) -> { return st.replace('t','f'); };
        String result = doSomething(t, operation,w);
        System.out.println(result);
    }


    public String doSomething(String x, StringOperation operation, StringOperation op2) {

        return "XXXX" + operation.execute(x) + op2.execute(x);
    }

    static class Workhorse implements StringOperation{

        private final String s;

        public Workhorse(String s) {
            this.s = s;
        }


        @Override
        public String execute(String x) {
            return " is a  workhorse";
        }
    };

}

interface StringOperation {
    String execute(String x);
}
