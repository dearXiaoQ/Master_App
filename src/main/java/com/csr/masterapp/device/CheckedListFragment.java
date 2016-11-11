
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.device;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;

import com.csr.masterapp.CheckedListItem;
import com.csr.masterapp.DevicesComparator;
import com.csr.masterapp.adapter.CheckedItemArrayAdapter;
import com.csr.masterapp.entities.Device;
import com.csr.masterapp.entities.SingleDevice;
import com.csr.mesh.ConfigModelApi;
import com.csr.mesh.DataModelApi;
import com.csr.mesh.FirmwareModelApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Fragment that shows a ListView of items with checkboxes. Used by GroupAssignFragment to display a list of groups
 * and devices side by side
 * 
 */
public class CheckedListFragment extends ListFragment implements CheckedItemArrayAdapter.ItemCheckedListener {
    private ArrayList<CheckedListItem> itemList = new ArrayList<CheckedListItem>();

    protected static final int MAX_DEVICE_NAME_LENGTH = 20;

    protected static final String EXTRA_MENU_RESOURCE = "MENURESOURCE";

    private ItemListener listener = null;
    private CheckedItemArrayAdapter adapter = null;
    private boolean mCheckBoxesVisible = false;
    private int mContextMenuResourceId = com.csr.masterapp.R.menu.config_popup_device;
    private boolean mClickEnabled;
    private boolean mContextMenuEnabled;

    /**
     * Implemented by the Activity that hosts this Fragment to receive events about items being selected or
     * checked/unchecked.
     */
    public interface ItemListener {
        public void onItemSelected(String fragmentTag, Device device);

        public void onItemCheckStatusChanged(String fragmentTag, int deviceId, boolean checked);

        public void itemRename(String fragmentTag, int deviceId, String name);

        public void itemRemove(String fragmentTag, int deviceId);

        public void itemInfo(String fragmentTag, int deviceId);
        
        public void itemVersion(String fragmentTag, int deviceId);

        public void itemGetData(String fragmentTag, int deviceId);
        
        public void itemRequestModels(String fragmentTag, int deviceId);
        
        /**
         * This is a workaround for a bug with Fragment context menus. Should call Fragment's handleContextMenu.
         * 
         * @param menuGroupId
         *            Group id of menu as specified in xml
         * @param position
         *            Position in list.
         * @param menuId
         *            Id of menu item selected.
         */
        public void onItemContextMenuClick(int menuGroupId, int position, int menuId);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parent = getParentFragment();
        if (parent == null) {
            throw new NullPointerException("onAttach: parent fragment was null.");
        }
        try {
            listener = (ItemListener) getParentFragment();
        }
        catch (ClassCastException e) {
            throw new ClassCastException(getParentFragment().toString() + " must implement ItemChangedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClickEnabled = true;
        mContextMenuEnabled = true;
        adapter = new CheckedItemArrayAdapter(this, com.csr.masterapp.R.layout.group_list_row, itemList);
        setListAdapter(adapter);
        Bundle args = getArguments();
        mContextMenuResourceId = args.getInt(EXTRA_MENU_RESOURCE, com.csr.masterapp.R.menu.config_popup_device);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (mContextMenuEnabled) {
            super.onCreateContextMenu(menu, v, menuInfo);
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
            int position = info.position;
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(mContextMenuResourceId, menu);
            
            Device device = adapter.getItem(position).getDevice();
            if(device instanceof SingleDevice){
	            SingleDevice dev = (SingleDevice)(device);
	            
	            // Set the menu_get_data visible only ifthe device support DataModel
	            menu.findItem(com.csr.masterapp.R.id.menu_get_data).setVisible(dev.isModelSupported(DataModelApi.MODEL_NUMBER));
	            // Set the menu_info visible only if the device support FirmwareModelApi
	            menu.findItem(com.csr.masterapp.R.id.menu_info).setVisible(dev.isModelSupported(FirmwareModelApi.MODEL_NUMBER));
	            // Set the menu_models_supported visible only if the device support ConfigModel
	            menu.findItem(com.csr.masterapp.R.id.menu_models_supported).setVisible(dev.isModelSupported(ConfigModelApi.MODEL_NUMBER));
	            // Set the menu_request_config visible only if the device doesn't support ConfigModel
	            menu.findItem(com.csr.masterapp.R.id.menu_request_config).setVisible(!dev.isModelSupported(ConfigModelApi.MODEL_NUMBER));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // This is a workaround to the problem with the Android SDK that means the onContextItemSelected is not
        // always called on the correct Fragment. Call back to the parent Fragment and tell it the menu group id that
        // was set in the menu xml. The parent Fragment will then call back into the correct instance of this Fragment.
        int itemId = item.getItemId();
        if (itemId == com.csr.masterapp.R.id.menu_rename || itemId == com.csr.masterapp.R.id.menu_delete ||
        	itemId == com.csr.masterapp.R.id.menu_info || itemId == com.csr.masterapp.R.id.menu_models_supported ||
        	itemId == com.csr.masterapp.R.id.menu_get_data || itemId == com.csr.masterapp.R.id.menu_request_config) {
            listener.onItemContextMenuClick(item.getGroupId(), ((AdapterContextMenuInfo) item.getMenuInfo()).position,
                    itemId);
            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }
    }

    /**
     * Part of workaround to context item selection (see onContextItemSelected).
     * 
     * @param position
     *            Position of the item within the ListView on which the menu was triggered.
     * @param menuItemId
     *            Uniquely identifies menu item.
     */
    public void handleContextMenu(int position, int menuItemId) {
        CheckedListItem deviceItem = itemList.get(position);
        switch (menuItemId) {
        case com.csr.masterapp.R.id.menu_rename:
            rename(deviceItem.getDevice());
            break;
        case com.csr.masterapp.R.id.menu_delete:
            listener.itemRemove(this.getTag(), deviceItem.getDevice().getDeviceId());
            break;
        case com.csr.masterapp.R.id.menu_info:
            listener.itemInfo(this.getTag(), deviceItem.getDevice().getDeviceId());
            break;
        case com.csr.masterapp.R.id.menu_models_supported:
            listener.itemVersion(this.getTag(), deviceItem.getDevice().getDeviceId());
            break;
        case com.csr.masterapp.R.id.menu_get_data:
        	listener.itemGetData(this.getTag(), deviceItem.getDevice().getDeviceId());
        	break;    
        case com.csr.masterapp.R.id.menu_request_config:
        	listener.itemRequestModels(this.getTag(), deviceItem.getDevice().getDeviceId());
        	break;    
        }
    }

    /**
     * Rename a group or a light.
     * 
     * @param device
     *            The device object to rename.
     */
    private void rename(final Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(com.csr.masterapp.R.string.enter_new_name));
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_DEVICE_NAME_LENGTH) });
        builder.setView(input);

        builder.setPositiveButton(getActivity().getString(com.csr.masterapp.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                listener.itemRename(getTag(), device.getDeviceId(), newName);
                device.setName(newName);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(getActivity().getString(com.csr.masterapp.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable text) {

                if (text.length() <= 0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
                else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No behaviour.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No behaviour.
            }
        });
    }

    /**
     * Event handler for when a list item is clicked.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (!mClickEnabled) {
            selectNone();
        }
        else if (mCheckBoxesVisible || getSelectedItemPosition() == CheckedItemArrayAdapter.NO_ITEM_SELECTED) {
            CheckedListItem item = itemList.get(position);
            adapter.setSelectedPosition(position);
            listener.onItemSelected(this.getTag(), item.getDevice());
        }
    }

    /**
     * Make sure no items are selected in the ListView.
     */
    public void selectNone() {
        adapter.setSelectedPosition(CheckedItemArrayAdapter.NO_ITEM_SELECTED);
    }

    /**
     * Event handler for when a checkbox in a list item is clicked.
     */
    @Override
    public void checkBoxClicked(boolean checked, int deviceId) {
        listener.onItemCheckStatusChanged(this.getTag(), deviceId, checked);
    }

    /**
     * Hide or show checkboxes in the ListView. When checkboxes are shown, list items don't receive click events.
     * 
     * @param visible
     *            True if checkboxes should be shown.
     */
    public void setCheckBoxesVisible(boolean visible) {
        mCheckBoxesVisible = visible;
        adapter.setCheckBoxesVisible(visible);
    }

    /**
     * Set state of an individual checkbox in the ListView.
     * 
     * @param position
     *            Index of item to set. This is the same as the id in an Item object.
     * @param checked
     *            Boolean checkbox state to set.
     * @param event
     * 			  True if event handler should be called.
     */
    public void setCheckBoxState(int position, boolean checked, boolean event) {
        setItemCheckBoxState(itemList.get(position), checked, event);
    }

    /**
     * Toggle check box state for an item in the ListView.
     * 
     * @param deviceId
     *            Device id of device to toggle.
     * @param checked
     *            True if checkbox should be checked.
     * @param event
     * 			  True if event handler should be called.            
     */
    public void setDeviceCheckBoxState(int deviceId, boolean checked, boolean event) {        
        setItemCheckBoxState(getItem(deviceId), checked, event);
    }
    
    private void setItemCheckBoxState(CheckedListItem item, boolean checked, boolean event) {
    	if (item != null) {
            if (item.isChecked() != checked) {
                item.setChecked(checked);
                adapter.notifyDataSetChanged();
                if (event) {
                	checkBoxClicked(item.isChecked(), item.getDevice().getDeviceId());
                }
            }
        }
    }

    /**
     * Get state of an individual checkboxe in the ListView.
     * 
     * @param position
     *            Index of item to set. This is the same as the id in an Item object.
     * @return the check box state
     */
    public boolean getCheckBoxState(int position) {
        CheckedListItem item = itemList.get(position);
        return item.isChecked();
    }

    /**
     * Set state of all checkboxes in the ListView.
     * Does not trigger checkbox click event.
     * 
     * @param checked
     *            Boolean checkbox state to set.
     */
    public void setAllCheckboxes(boolean checked) {
        for (CheckedListItem item : itemList) {
            item.setChecked(checked);
        }
        adapter.setSelectedPosition(CheckedItemArrayAdapter.NO_ITEM_SELECTED);
        adapter.notifyDataSetChanged();
    }

    /**
     * Sort items in the ListView alphabetically.
     */
    private void sortItems() {
        Collections.sort(itemList, new DevicesComparator());
    }

    /**
     * Add items for display in the ListView. Each item has a name and an id.
     * 
     *
     *            ArrayList of Device objects.
     */
    public void addItems(List<Device> devices) {
        for (Device device : devices) {
            if (device.getDeviceId() > 0) {
                itemList.add(new CheckedListItem(false, device));
            }
        }

        sortItems();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Add a device to the ListView.
     * 
     * @param device
     *            Device object to add.
     */
    public void addItem(Device device) {
        if (device.getDeviceId() > 0) {
            itemList.add(new CheckedListItem(false, device));
        }
        sortItems();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Clear all items in the ListView.
     */
    public void clearItems() {
        itemList.clear();
    }

    /**
     * Get the device currently selected in the ListView.
     * 
     * @return The Device object for the selected item.
     */
    public Device getSelectedDevice() {
        return adapter.getItem(adapter.getSelectedPosition()).getDevice();
    }

    @Override
    public int getSelectedItemPosition() {
        return adapter.getSelectedPosition();
    }

    /**
     * Get item by device id.
     * 
     * @param deviceId
     *            The device id of the item to get.
     * @return CheckedListItem object.
     */
    public CheckedListItem getItem(int deviceId) {
        CheckedListItem result = null;
        for (CheckedListItem item : itemList) {
            if (item.getDevice().getDeviceId() == deviceId) {
                result = item;
                break;
            }
        }
        return result;
    }

    /**
     * Retrieve a list of all devices shown in this checked list.
     * 
     * @return List of Device objects.
     */
    public List<Device> getDevices() {
        ArrayList<Device> result = new ArrayList<Device>();
        for (CheckedListItem item : itemList) {
            result.add(item.getDevice());
        }
        return result;
    }

    /**
     * Clear the selected item, so that the listView doesn't display any item as highlighted.
     */
    public void clearSelection() {
        adapter.setSelectedPosition(CheckedItemArrayAdapter.NO_ITEM_SELECTED);
    }

    /**
     * Enable or disable click handler on list items.
     * 
     * @param enabled
     *            True if click handler should be called when a list item is clicked.
     */
    public void setClickEnabled(boolean enabled) {
        this.mClickEnabled = enabled;
    }

    /**
     * Enable or disable context menu on list items.
     * 
     * @param enabled
     *            True if context menu should be enabled.
     */
    public void setContextMenuEnabled(boolean enabled) {
        this.mContextMenuEnabled = enabled;
    }
}
