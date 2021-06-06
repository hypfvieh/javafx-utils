package com.github.hypfvieh.javafx.windows.interfaces;

import javafx.stage.Stage;

/**
 * Interface which marks the window as dimension and position being saved/restored on close/creation.
 *
 * @author hypfvieh
 * @since v11.0.1 - 2021-06-06
 */
public interface IStageControllerAware {

    /**
     * Retrieve the saved stage.
     *
     * @return Stage, maybe null
     */
    public Stage getControllerStage();

    /**
     * Set the stage.
     *
     * @param _controllerStage stage to set
     */
    public void setControllerStage(Stage _controllerStage);
}
