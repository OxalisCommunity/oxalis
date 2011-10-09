package eu.peppol.start.util;

import java.util.concurrent.*;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 8:54:05 AM
 */
public abstract class Daemon {

    private int antallIterasjoner = -1;
    protected String daemonId = getClass().getSimpleName();
    private Time cycleDelay = new Time(10, Time.MINUTES);
    private Time initialDelay = new Time(0, Time.MILLISECONDS);
    private ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    public Daemon() {
        init();
    }

    void doInThread(Callable<Void> callable) {
        try {
            Future<Void> future = threadPoolExecutor.submit(callable);

            try {
                future.get((long) 1, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw e;
            } catch (ExecutionException e) {
                handleException(e.getCause());
            } catch (Throwable e) {
                handleException(e);
            }

        } catch (TimeoutException e) {
            // ignore
        }
    }

    private static void handleException(Throwable cause) {
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        } else {
            throw new RuntimeException("Callable avbrutt", cause);
        }
    }

    protected abstract void init();

    protected abstract void run() throws Exception;

    protected void setAntallIterasjoner(int antallIterasjoner) {
        this.antallIterasjoner = antallIterasjoner;
    }

    public void setCycleDelay(Time cycleDelay) {
        this.cycleDelay = cycleDelay;
    }

    public void setDaemonId(String daemonId) {
        this.daemonId = daemonId;
    }

    public void setInitialDelay(Time initialDelay) {
        this.initialDelay = initialDelay;
    }

    void sleep(Time time) {
        sleep(time.getMilliseconds());
    }

    void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }

    public void start() {

        doInThread(new Callable<Void>() {
            public Void call() throws Exception {
                try {
                    sleep(initialDelay);
                    Log.info("Starting daemon " + daemonId);
                    int i = 0;

                    while (i++ != antallIterasjoner) {
                        run();
                        sleep(cycleDelay);
                    }
                } catch (Throwable t) {
                    Log.error("**** Problem in daemon " + daemonId, t);
                }

                return null;
            }
        });
    }

    public void stop() {
        threadPoolExecutor.shutdownNow();
    }
}

