package com.dformoso.fitcheck.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pools de hilos de la app.
 *
 * <p>Este es el sustituto de las coroutines en un proyecto Java: trabajo de disco y de red en
 * {@link #io()}, cálculo en {@link #computation()}, y el resultado se publica a la UI con
 * {@code LiveData.postValue()}, que ya salta al hilo principal por su cuenta.
 *
 * <p>Se inyecta como singleton con Hilt. No crees ejecutores sueltos por ahí.
 */
public final class AppExecutors {

    private final ExecutorService io;
    private final ExecutorService computation;

    public AppExecutors() {
        this(
                Executors.newFixedThreadPool(4, named("fitcheck-io")),
                Executors.newFixedThreadPool(
                        Math.max(2, Runtime.getRuntime().availableProcessors()),
                        named("fitcheck-cpu")));
    }

    /** Constructor para tests: permite inyectar ejecutores síncronos. */
    public AppExecutors(ExecutorService io, ExecutorService computation) {
        this.io = io;
        this.computation = computation;
    }

    /** Disco, base de datos y red. Bloqueante y con latencia. */
    public ExecutorService io() {
        return io;
    }

    /**
     * Cálculo intensivo: reproceso del filtro de Kalman sobre el historial completo,
     * generación de rutinas, integración de volumen. Nunca en el hilo de IO.
     */
    public ExecutorService computation() {
        return computation;
    }

    private static ThreadFactory named(String prefix) {
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + counter.getAndIncrement());
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            return thread;
        };
    }
}
