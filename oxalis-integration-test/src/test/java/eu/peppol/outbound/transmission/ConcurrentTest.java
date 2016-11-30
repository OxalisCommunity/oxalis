/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.outbound.transmission;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author steinar
 *         Date: 22.11.2016
 *         Time: 21.06
 */
public class ConcurrentTest {

    @Test
    public void firstTest() {

        int TASK_COUNT = 10;
        final CountDownLatch endGate = new CountDownLatch(TASK_COUNT);

        List<Callable<Integer>> callableList = new ArrayList<>();
        for (int i = 0; i < TASK_COUNT; i++) {
            Callable<Integer> task = () -> {
                long random = (long) (Math.random() * TASK_COUNT * 1000L);
                try {
                    System.out.println("Task Being executed by " + Thread.currentThread().getName() + ", sleeping " + TimeUnit.SECONDS.convert(random, TimeUnit.MILLISECONDS) + "s");
                    Thread.sleep(random);
                    return Integer.valueOf((int) random);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }   finally {
                        endGate.countDown();
                }

            };

            callableList.add(task);
        }

        ExecutorService executorService = Executors.newWorkStealingPool();


        try {
            List<Future<Integer>> futureList = executorService.invokeAll(callableList);

            System.out.println("Waiting for tasks to complete");
            endGate.await();

            for (Future<Integer> future : futureList) {
                if (future.isDone()) {
                    try {
                        System.out.println("Result is " + future.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
    }

}