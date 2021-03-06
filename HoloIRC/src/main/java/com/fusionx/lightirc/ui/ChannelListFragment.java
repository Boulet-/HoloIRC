package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.BaseCollectionAdapter;
import com.fusionx.lightirc.constants.PreferenceConstants;
import com.fusionx.lightirc.interfaces.ServerSettingsCallbacks;
import com.fusionx.lightirc.ui.dialogbuilder.ChannelNamePromptDialogBuilder;
import com.fusionx.lightirc.util.MultiSelectionUtils;
import com.fusionx.lightirc.util.SharedPreferencesUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.fusionx.lightirc.constants.PreferenceConstants.PREF_AUTOJOIN;

public class ChannelListFragment extends MultiChoiceListFragment<String> {

    private BaseCollectionAdapter<String> mAdapter;

    private ServerSettingsCallbacks mCallbacks;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (ServerSettingsCallbacks) activity;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences settings = getActivity().getSharedPreferences(mCallbacks.getFileName(),
                Context.MODE_PRIVATE);
        final Set<String> set = SharedPreferencesUtils.getStringSet(settings,
                PreferenceConstants.PREF_AUTOJOIN, new HashSet<String>());
        mAdapter = new BaseCollectionAdapter<>(getActivity(),
                R.layout.default_listview_textview, new TreeSet<>(set));

        setListAdapter(mAdapter);
        setHasOptionsMenu(true);
    }

    @Override
    protected void attachSelectionController() {
        mMultiSelectionController = MultiSelectionUtils.attachMultiSelectionController(
                getListView(), (ActionBarActivity) getActivity(), this, true);
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, Menu menu) {
        final MenuInflater inflate = mode.getMenuInflater();
        inflate.inflate(R.menu.activty_server_settings_cab, menu);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        final List<String> positions = getCheckedItems();

        switch (item.getItemId()) {
            case R.id.activity_server_settings_cab_edit:
                final String edited = mAdapter.getItem(0);
                final ChannelNamePromptDialogBuilder dialog = new ChannelNamePromptDialogBuilder
                        (getActivity(), edited) {
                    @Override
                    public void onOkClicked(final String input) {
                        mAdapter.remove(edited);
                        mAdapter.add(input);
                    }
                };
                dialog.show();

                mode.finish();
                return true;
            case R.id.activity_server_settings_cab_delete:
                for (String selected : positions) {
                    mAdapter.remove(selected);
                }
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position,
            long id, boolean checked) {
        int selectedItemCount = getCheckedItems().size();
        if (selectedItemCount != 0) {
            final String quantityString = getResources().getQuantityString(R.plurals
                    .channel_selection, selectedItemCount, selectedItemCount);
            mode.setTitle(quantityString);
            mode.getMenu().getItem(0).setVisible(selectedItemCount == 1);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflate) {
        inflate.inflate(R.menu.activity_server_settings_channellist_ab, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.activity_server_settings_ab_add:
                final ChannelNamePromptDialogBuilder dialog =
                        new ChannelNamePromptDialogBuilder(getActivity()) {
                            @Override
                            public void onOkClicked(final String input) {
                                mAdapter.add(input);
                            }
                        };
                dialog.show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onPause() {
        SharedPreferencesUtils.putStringSet(getActivity().getSharedPreferences(mCallbacks
                .getFileName(), Context.MODE_PRIVATE), PREF_AUTOJOIN, mAdapter.getSetOfItems());
        super.onPause();
    }

    @Override
    protected BaseCollectionAdapter<String> getRealAdapter() {
        return mAdapter;
    }
}