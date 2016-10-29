package com.csr.masterapp.scene.util;

import java.util.List;

/**
 * 项目名称：MasterApp v4
 * 类描述：
 * 创建人：11177
 * 创建时间：2016/8/8 17:25
 * 修改人：11177
 * 修改时间：2016/8/8 17:25
 * 修改备注：
 */
public class ScenesListModel {

    public List<SceneItemModel> conditions;
    public List<SceneItemModel> tasks;

    public ScenesListModel(List<SceneItemModel> conditions, List<SceneItemModel> tasks) {
        this.conditions = conditions;
        this.tasks = tasks;
    }
}
