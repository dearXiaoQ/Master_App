
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp;

/**
 * Defines an interface that Fragments can use to communicate with the Activity.
 *
 */
public interface SceneController {
    /**
     * 获取所有场景列表
     *
     * @return List of Scene objects
     */
    public void getScenes();
}

