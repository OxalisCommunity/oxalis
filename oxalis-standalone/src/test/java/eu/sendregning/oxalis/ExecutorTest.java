/*
 * Copyright (c) 2010 - 2017 Norwegian Agency for Public Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.sendregning.oxalis;

import org.testng.annotations.Test;

import java.util.concurrent.*;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 09.01.2017
 *         Time: 15.40
 */
public class ExecutorTest {

    @Test
    public void executeSeveralTasksSomeOfWhichFails() throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        ExecutorCompletionService<Integer> ecs = new ExecutorCompletionService<Integer>(executorService);

        for (int i = 0; i < 50; i++) {

            final int n = i;

            Future<Integer> future = ecs.submit(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    System.out.println(Thread.currentThread().getName() + " executing no #" + n);
                    if (n > 0 && n % 10 == 0) {
                        throw new IllegalStateException("Odd things happening for no #" + n);
                    }
                    return n;
                }
            });
        }

        int completed = 0;
        for (int i = 0; i < 50; i++, completed++) {
            try {
                Integer integer = ecs.take().get();
                System.out.println(integer + " completed");
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            } catch (ExecutionException e) {
                System.err.println(e.getMessage());
            }
        }
        assertEquals(completed, 50);
    }
}
