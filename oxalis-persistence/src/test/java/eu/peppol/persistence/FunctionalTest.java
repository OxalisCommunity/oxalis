/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

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

    interface StringOperation {
        String execute(String x);
    }
}