package eu.peppol.statistics;

/*
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * An example that performs GETs from multiple threads.
 *
 */
public class ClientMultiThreadedExecution {

    public static void main(String[] args) throws Exception {
        // Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
        cm.setMaxTotal(100);

        final HttpClient httpclient = new DefaultHttpClient(cm);
        try {
            // create an array of URIs to perform GETs on
            final String[] urisToGet = {
                    "https://aksesspunkt.sendregning.no/oxalis/accessPointService?wsdl",
                    "https://start-ap.alfa1lab.com:443/accessPointService?wsdl",
                    "https://ap.kpmg.no/transport-start-server/accessPointService?wsdl",
                    "https://peppol.basware.com/btpep/accessPointService?wsdl"
            };

            ExecutorService executor = Executors.newFixedThreadPool(2);

            List<GetWsdlTask> getWsdlTasks = new ArrayList<GetWsdlTask>();
            for (int i = 0; i < urisToGet.length; i++) {
                getWsdlTasks.add(new GetWsdlTask(httpclient, urisToGet[i]));
            }

            List<Future<String>> futures = executor.invokeAll(getWsdlTasks, 5, TimeUnit.SECONDS);

            Iterator<GetWsdlTask> getWsdlTaskIterator = getWsdlTasks.iterator();

            for (Future<String> future : futures) {
                if (future.isDone()) {
                    String result = future.get();
//                    System.out.println(result);
                }
            }

            executor.shutdownNow();
        } catch (Exception e) {
            throw new IllegalStateException("ERROR:  " + e, e);
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }


    }

    static class GetWsdlTask implements Callable<String> {

        private final HttpClient httpClient;
        private final String url;

        GetWsdlTask(HttpClient httpClient, String url) {
            this.httpClient = httpClient;
            this.url = url;
        }

        @Override
        public String call() throws Exception {
            String result = null;

            HttpGet httpGet = new HttpGet(url);
            try {

                System.out.println(url + " - executing the get ...");
                HttpResponse response = httpClient.execute(httpGet, new BasicHttpContext());

                System.out.println(url + " - get executed");
                // get the response body as an array of bytes
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    result = EntityUtils.toString(entity);
                }
            } catch (SSLPeerUnverifiedException e) {
                System.err.println("SSL cetificate problem for " + url  + "; " +e.getMessage());
                e.printStackTrace(System.err);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to retrieve data from " + url, e);
            }
            return result;
        }
    }

    /**
     * A thread that performs a GET.
     */
    static class GetThread extends Thread {

        private final HttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpget;
        private final int id;

        public GetThread(HttpClient httpClient, HttpGet httpget, int id) {
            this.httpClient = httpClient;
            this.context = new BasicHttpContext();
            this.httpget = httpget;
            this.id = id;
        }

        /**
         * Executes the GetMethod and prints some status information.
         */
        @Override
        public void run() {

            System.out.println(id + " - about to get something from " + httpget.getURI());

            try {

                // execute the method
                HttpResponse response = httpClient.execute(httpget, context);

                System.out.println(id + " - get executed");
                // get the response body as an array of bytes
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    byte[] bytes = EntityUtils.toByteArray(entity);
                    System.out.println(id + " - " + bytes.length + " bytes read");
                }

            } catch (Exception e) {
                httpget.abort();
                System.out.println(id + " - error: " + e);
            }
        }

    }

}