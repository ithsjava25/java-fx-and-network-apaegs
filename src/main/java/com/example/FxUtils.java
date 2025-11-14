package com.example;

import javafx.application.Platform;

public class FxUtils {
    /**
     * Runs task on the FX thread if possible, otherwise inline (e.g., in tests/headless mode)
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
