package com.csr.masterapp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.csr.masterapp.BaseFragment;
import com.csr.masterapp.DeviceController;
import com.csr.masterapp.LoginAndRegisterUI;
import com.csr.masterapp.MainActivity;
import com.csr.masterapp.R;
import com.csr.masterapp.base.MenuController;
import com.csr.masterapp.utils.CacheUtils;
import com.csr.masterapp.utils.UIUtils;

import java.util.List;

/**
 * 项目名称：MasterApp
 * 类描述：
 * 创建人：11177
 * 创建时间：2016/6/21 10:13
 * 修改人：11177
 * 修改时间：2016/6/21 10:13
 * 修改备注：
 */
public class MenuFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private int[] mMenuIcons;

    private MenuListAdapter mAdapter;

    private ListView mListView;

    private int mCurrentMenu;

    public static boolean NETWORK_ONLINE = true;

    private DeviceController mComtorller;

    private Resources mResources;

    private List<MenuController> mMenuControllers;

    @Override
    protected View initView() {
        mListView = new ListView(mActivity);
        try{
            mResources = mActivity.getResources();
            mComtorller = (DeviceController) getActivity();
        } catch (Exception e){e.printStackTrace();
            Log.i("MainActivity转型异常", "");
        }

        mListView.setBackgroundColor(Color.WHITE);
        mListView.setOnItemClickListener(this);
        View headerView = View.inflate(getActivity(), R.layout.view_avatar, null);
        TextView userNameView = (TextView) headerView.findViewById(R.id.username);
        if(CacheUtils.getString(getActivity(),"userName",null) != null){
            userNameView.setVisibility(View.VISIBLE);
            userNameView.setText(CacheUtils.getString(getActivity(),"userName",null));
        }
        mListView.addHeaderView(headerView);
        return mListView;
    }

    @Override
    protected void initData() {
        String[] mMenuData = UIUtils.getStringArray(R.array.menu_pics);
        int[] mMenuDatas = {R.drawable.qq, R.drawable.weixin, R.drawable.sina, R.drawable.toutiao,
                R.drawable.taobao, R.drawable.leshi, R.drawable.xiami, R.drawable.iqiyi, R.drawable.ic_lixian, R.drawable.debug};

        //加菜单数据
        setData(mMenuDatas);

    }

    public void setData( int[] icos) {

        this.mMenuIcons = icos;
        mCurrentMenu = 0;

        //初始化menu
        mAdapter = new MenuListAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(position == 0) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.cancellation_accounts);
                    builder.setPositiveButton(R.string.yes_exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CacheUtils.setInt(getActivity(), "userId", 0);
                            Intent loginIntent = new Intent(getActivity(), LoginAndRegisterUI.class);
                            String autoLoginUserName = CacheUtils.getString(getActivity(), "autoLoginUserName");
                            String autoLoginPassword = CacheUtils.getString(getActivity(), "autoLoginPassword");
                            if(autoLoginUserName != null && autoLoginPassword != null) {
                                loginIntent.putExtra("resultUserName", autoLoginUserName);
                                loginIntent.putExtra("resultPassword", autoLoginPassword);
                                CacheUtils.setString(getActivity(), "autoLoginUserName", null);
                                CacheUtils.setString(getActivity(), "autoLoginPassword", null);
                                startActivityForResult(loginIntent, Activity.RESULT_OK);
                            }else{
                                startActivity(new Intent(getActivity(), LoginAndRegisterUI.class));
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.no, null);
                    builder.create().show();
                } else if(position == 9) {
                    showDialog(0);
                } else if(position == 10) {
                    showDialog(1);
                }
            }
            private void showDialog(int tyep) { //type == 0 离线模式， type == 1 调试模式
                AlertDialog.Builder dialog;
                String messageStr = "";
                if (tyep == 0) {
                    if (NETWORK_ONLINE) {
                        messageStr = mResources.getString(R.string.open_off_line_model);
                    } else {
                        messageStr = mResources.getString(R.string.close_off_line_model);
                    }
                    dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle(R.string.prompt).setMessage(messageStr).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (NETWORK_ONLINE) {
                                NETWORK_ONLINE = false;
                            } else {
                                NETWORK_ONLINE = true;
                            }
                            //   mComtorller.debugModelCallBack();
                        }
                    }).setNegativeButton(R.string.no, null).create().show();
                } else {
                    if(MainActivity.DEBUG_MODLE){
                        messageStr = mResources.getString(R.string.close_debug_model);
                    }else  {
                        messageStr = mResources.getString(R.string.open_debug_model);
                    }
                    dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle(R.string.prompt).setMessage(messageStr).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(MainActivity.DEBUG_MODLE){MainActivity.DEBUG_MODLE = false;} else{MainActivity.DEBUG_MODLE = true;}
                        }
                    }).setNegativeButton(R.string.cancel, null).create().show();
                }
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(position == 0){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.cancellation_accounts);
            builder.setPositiveButton(R.string.yes_exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CacheUtils.setInt(getActivity(), "userId", 0);
                    Intent loginIntent = new Intent(getActivity(), LoginAndRegisterUI.class);
                    String autoLoginUserName = CacheUtils.getString(getActivity(), "autoLoginUserName");
                    String autoLoginPassword = CacheUtils.getString(getActivity(), "autoLoginPassword");
                    if(autoLoginUserName != null && autoLoginPassword != null) {
                        loginIntent.putExtra("resultUserName", autoLoginUserName);
                        loginIntent.putExtra("resultPassword", autoLoginPassword);
                        CacheUtils.setString(getActivity(), "autoLoginUserName", null);
                        CacheUtils.setString(getActivity(), "autoLoginPassword", null);
                        startActivityForResult(loginIntent, Activity.RESULT_OK);
                    }else{
                        startActivity(new Intent(getActivity(), LoginAndRegisterUI.class));
                    }
                }
            });
            builder.setNegativeButton(R.string.no, null);
            builder.create().show();
        }

        //实体改变
        MainActivity ui = (MainActivity) mActivity;

        //对应实体的内容要改变
        ContentFragment contentFragment = ui.getContentFragment();
        contentFragment.switchMenu(position);

        //设置当前选中的菜单
        mCurrentMenu = position;

        // ui刷新
        mAdapter.notifyDataSetChanged();
    }

    class MenuListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mMenuIcons != null) {
                return mMenuIcons.length;
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mMenuIcons != null) {
                return mMenuIcons[position];
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = View.inflate(mActivity, R.layout.item_menu, null);

            ImageView iv = (ImageView) convertView.findViewById(R.id.iv_ico);

            iv.setImageResource(mMenuIcons[position]);
            iv.setEnabled(mCurrentMenu == position);

            return convertView;
        }
    }


}
