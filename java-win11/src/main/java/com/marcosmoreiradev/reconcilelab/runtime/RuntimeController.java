package com.marcosmoreiradev.reconcilelab.runtime;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.engine.SearchEngine;
import com.marcosmoreiradev.reconcilelab.engine.SearchStrategy;

import java.util.Objects;
import java.util.concurrent.*;

public final class RuntimeController implements AutoCloseable {

    private final Object lock = new Object();
    private final SearchEngine engine = new SearchEngine();

    private ProblemInstance problem;
    private ExecutionMode mode = ExecutionMode.BALANCED;
    private ExecutionState state = ExecutionState.READY;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(
                    Thread.ofPlatform()
                            .name("reconcilelab-engine")
                            .daemon(true)
                            .factory());

    private Future<?> worker;
    private volatile boolean stopRequested;

    public void loadProblem(ProblemInstance problem) {
        stopWorker();

        synchronized (lock) {
            this.problem = Objects.requireNonNull(problem);
            engine.reset(problem);
            stopRequested = false;
            state = ExecutionState.READY;
        }
    }

    public void setStrategy(SearchStrategy strategy) {
        synchronized (lock) {
            engine.setStrategy(strategy);
        }
    }

    public void setMode(ExecutionMode mode) {
        synchronized (lock) {
            this.mode = Objects.requireNonNull(mode);
        }
    }

    public boolean start() {
        synchronized (lock) {
            requireProblem();

            if (engine.isComplete()) {
                state = ExecutionState.COMPLETED;
                return false;
            }

            if (state == ExecutionState.RUNNING) {
                return true;
            }

            stopRequested = false;
            state = ExecutionState.RUNNING;

            if (worker == null || worker.isDone()) {
                worker = executor.submit(this::workerLoop);
            }

            return true;
        }
    }

    public void pause() {
        synchronized (lock) {
            if (state == ExecutionState.RUNNING) {
                state = ExecutionState.PAUSED;
            }
        }
    }

    public void reset() {
        stopWorker();

        synchronized (lock) {
            requireProblem();
            engine.reset(problem);
            stopRequested = false;
            state = ExecutionState.READY;
        }
    }

    public boolean stepOnce() {
        synchronized (lock) {
            requireProblem();

            if (state == ExecutionState.RUNNING || engine.isComplete()) {
                if (engine.isComplete()) {
                    state = ExecutionState.COMPLETED;
                }
                return false;
            }

            engine.step();
            state = engine.isComplete()
                    ? ExecutionState.COMPLETED
                    : ExecutionState.PAUSED;

            return true;
        }
    }

    public ExecutionState getState() {
        synchronized (lock) {
            return state;
        }
    }

    public ExecutionSnapshot getSnapshot() {
        synchronized (lock) {
            requireProblem();
            return engine.snapshot();
        }
    }

    private void workerLoop() {
        while (!stopRequested) {
            long sleep;

            synchronized (lock) {
                if (stopRequested) {
                    break;
                }

                if (state == ExecutionState.RUNNING) {
                    if (!engine.isComplete()) {
                        engine.runBatch(batchSizeUnsafe());
                    }

                    if (engine.isComplete()) {
                        state = ExecutionState.COMPLETED;
                    }
                }

                sleep = sleepMillisUnsafe();
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private long batchSizeUnsafe() {
        if (mode == ExecutionMode.STUDY) return 1;
        if (mode == ExecutionMode.MAXIMUM) return 10_000;

        int count = problem.itemCount();

        if (count <= 8) return 1;
        if (count <= 12) return 16;
        if (count <= 16) return 128;
        return 1_024;
    }

    private long sleepMillisUnsafe() {
        if (mode == ExecutionMode.STUDY) return 120;
        if (mode == ExecutionMode.MAXIMUM) return 1;

        int count = problem == null ? 0 : problem.itemCount();

        if (count <= 8) return 25;
        if (count <= 12) return 10;
        return 5;
    }

    private void stopWorker() {
        Future<?> current;

        synchronized (lock) {
            current = worker;
            if (current == null || current.isDone()) {
                worker = null;
                stopRequested = false;
                return;
            }

            stopRequested = true;
        }

        try {
            current.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException ignored) {
            current.cancel(true);
        }

        synchronized (lock) {
            if (worker == current) {
                worker = null;
            }

            stopRequested = false;

            if (problem != null && engine.isComplete()) {
                state = ExecutionState.COMPLETED;
            } else if (state == ExecutionState.RUNNING) {
                state = ExecutionState.PAUSED;
            }
        }
    }

    private void requireProblem() {
        if (problem == null) {
            throw new IllegalStateException("No hay un problema cargado.");
        }
    }

    @Override
    public void close() {
        stopWorker();
        executor.shutdownNow();
    }
}
