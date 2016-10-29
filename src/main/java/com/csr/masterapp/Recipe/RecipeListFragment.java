
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.Recipe;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.csr.masterapp.DeviceController;
import com.csr.masterapp.R;

/**
 * Fragment used to configure devices. Handles assigning devices to groups, get firmware version, remove a device or
 * group, rename a device or group and add a new group. Contains two side by side CheckedListFragment fragments.
 *
 */
public class RecipeListFragment extends Fragment {
    private static final String TAG = "DeviceListFragment";


    private View mRootView;

    private DeviceController mController;
    private RecipeCallback mCallback;
    private Fragment mCurrentFragment;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mController = (DeviceController) activity;

            mCallback   = (RecipeCallback) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeviceController or RecipeCallback callback interface.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.recipe_list_fragment, container, false);
        }

        mRootView.findViewById(R.id.rt1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager Manager = getActivity().getSupportFragmentManager();
                Manager.beginTransaction().addToBackStack(null).replace(R.id.main_container_content, new RecipeControlFragment()).commit();
            }
        });

        mRootView.findViewById(R.id.rt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager Manager = getActivity().getSupportFragmentManager();
                Manager.beginTransaction().addToBackStack(null).replace(R.id.main_container_content, new RecipeControlFragment2()).commit();
            }
        });

        mRootView.findViewById(R.id.rt1).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("RecipeCallback", "回调响应！");
                mCallback.onRecipeCallback(true);
                return false;
            }
        });

        mRootView.findViewById(R.id.rt2).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("RecipeCallback", "回调响应！");
                mCallback.onRecipeCallback(true);
                return false;
            }
        });


        //屏幕触摸时间
        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("RecipeCallback", "回调响应！");
                mCallback.onRecipeCallback(true);
                return false;
            }
        });

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    /**
     * 创建者：11468
     * 接口描述：主函数的的回调接口
     */
    public interface RecipeCallback{
        void onRecipeCallback(boolean isCallback);
    }


}
