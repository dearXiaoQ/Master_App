package com.csr.masterapp.utils;

/**
 * 项目名称：MasterApp
 * 类描述：全局常量
 * 创建人：11177
 * 创建时间：2016/6/22 11:00
 * 修改人：11177
 * 修改时间：2016/6/22 11:00
 * 修改备注：
 */
public interface Constans {
    String BASE_URL = "http://sever.gmri.com.cn:3000/";

    /**
     * 添加场景
     */
    String SCENE_ADD_URL = BASE_URL + "scene/addScene";

    /**
     * 删除场景
     */
    String SCENE_DELETE_URL = BASE_URL + "scene/deleteScene";

    /**
     * 编辑场景
     */
    String SCENE_EDIT_URL = BASE_URL + "scene/modifyScene";

    /**
     * dev设备基本信息
     */
    String DEVICE_BASE_INFO = BASE_URL + "dev/getDevBaseInfo";

    /**
     * dev设备详细信息
     */
    String DEVICE_DETAIL_INFO = BASE_URL + "dev/getDevDetailInfo";

    /**
     * 设备与masterApp绑定
     */
    String DEVICE_BIND_MASTERAPP = BASE_URL + "dev/binding";

    /**
     * 设备删除与masterApp解除绑定
     */
    String DEVICE_UNBIND_MASTERAPP = BASE_URL + "dev/unbinding";

    /**
     * 设备重新命名
     */
    String DEVICE_RENAME_MASTERAPP = BASE_URL + "dev/reName";
}
