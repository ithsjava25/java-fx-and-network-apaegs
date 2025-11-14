package com.example;

import javafx.application.Platform;

/**
 * Utility for running code on the JavaFX Application Thread.
 */
public final class FxUtils {

    /**
     * Runs the given task on the FX thread.
     * <p>
     * If already on the FX thread, the task runs immediately.
     * Otherwise, it is scheduled with {@code Platform.runLater}.
     * If JavaFX is not initialized (e.g. in tests), the task runs inline.
     *
     * @param task the code to run (must not be null)
     */
    static void runOnFx(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("task cannot be null");
        }
        try {
            if (Platform.isFxApplicationThread()) task.run();
            else Platform.runLater(task);
        } catch (IllegalStateException notInitialized) {
            task.run();
        }
    }
}
