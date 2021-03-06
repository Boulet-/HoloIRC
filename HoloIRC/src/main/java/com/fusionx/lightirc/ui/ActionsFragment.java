/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ActionsAdapter;
import com.fusionx.lightirc.constants.FragmentType;
import com.fusionx.lightirc.ui.dialogbuilder.ChannelNamePromptDialogBuilder;
import com.fusionx.lightirc.ui.dialogbuilder.NickPromptDialogBuilder;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.relay.Server;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ActionsFragment extends Fragment implements AdapterView.OnItemClickListener,
        SlidingMenu.OnOpenListener {

    private Callbacks mCallbacks;

    private FragmentType mFragmentType;

    private StickyListHeadersListView mListView;

    private ActionsAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallbacks = FragmentUtils.getParent(this, Callbacks.class);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView.setOnItemClickListener(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_action_listview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ActionsAdapter(getActivity());
        mListView = (StickyListHeadersListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        if (mFragmentType != null) {
            mAdapter.setFragmentType(mFragmentType);
            mFragmentType = null;
        }
        onOpen();
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i,
            final long l) {
        switch (i) {
            case 0:
                channelNameDialog();
                break;
            case 1:
                nickChangeDialog();
                break;
            case 2:
                mCallbacks.disconnectFromServer();
                return;
            case 3:
                ActionsPagerFragment fragment = (ActionsPagerFragment) getParentFragment();
                fragment.switchToIgnoreFragment();
                return;
            case 4:
                mCallbacks.closeOrPartCurrentTab();
                break;
        }
        mCallbacks.closeAllSlidingMenus();
    }

    private void nickChangeDialog() {
        final NickPromptDialogBuilder nickDialog = new NickPromptDialogBuilder(getActivity(),
                mCallbacks.getNick()) {
            @Override
            public void onOkClicked(final String input) {
                mCallbacks.getServer().getServerCallBus().sendNickChange(input);
            }
        };
        nickDialog.show();
    }

    private void channelNameDialog() {
        final ChannelNamePromptDialogBuilder builder = new ChannelNamePromptDialogBuilder
                (getActivity()) {
            @Override
            public void onOkClicked(final String input) {
                mCallbacks.getServer().getServerCallBus().sendJoin(input);
            }
        };
        builder.show();
    }

    @Override
    public void onOpen() {
        if (mCallbacks.isConnectedToServer() != mAdapter.isConnected()) {
            mAdapter.setConnected(mCallbacks.isConnectedToServer());
            mAdapter.notifyDataSetChanged();
        }
    }

    public void updateConnectionStatus(final boolean isConnected) {
        mAdapter.setConnected(isConnected);
        mAdapter.notifyDataSetChanged();
    }

    public void onTabChanged(final FragmentType selectedType) {
        if (mAdapter == null) {
            mFragmentType = selectedType;
        } else {
            mAdapter.setFragmentType(selectedType);
        }
    }

    public interface Callbacks {

        public String getNick();

        public void closeOrPartCurrentTab();

        public boolean isConnectedToServer();

        public Server getServer();

        public void closeAllSlidingMenus();

        public void disconnectFromServer();
    }
}