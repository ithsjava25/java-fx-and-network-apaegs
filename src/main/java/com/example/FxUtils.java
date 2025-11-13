package com.example;

import javafx.application.Platform;

public class FxUtils {

    /**
     * Kör task på FX-tråden om möjligt, annars inline (t.ex. i tester/headless)
     */
    static void runOnFx(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) task.run();
            else Platform.runLater(task);
        } catch (IllegalStateException notInitialized) {
            task.run();
        }
    }
}
