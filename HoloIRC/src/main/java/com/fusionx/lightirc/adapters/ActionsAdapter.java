package com.fusionx.lightirc.adapters;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.constants.FragmentType;
import com.fusionx.lightirc.util.UIUtils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ActionsAdapter extends ArrayAdapter<String> implements StickyListHeadersAdapter {

    private final LayoutInflater mInflater;

    private final int mServerItemCount;

    private final String[] mChannelArray;

    private final String[] mUserArray;

    private boolean mConnected = false;

    private FragmentType mFragmentType = FragmentType.Server;

    public ActionsAdapter(final Context context) {
        super(context, R.layout.default_listview_textview, new ArrayList<>(Arrays.asList
                (context.getResources().getStringArray(R.array.server_actions))));
        mInflater = LayoutInflater.from(context);
        mServerItemCount = super.getCount();
        mChannelArray = context.getResources().getStringArray(R.array.channel_actions);
        mUserArray = context.getResources().getStringArray(R.array.user_actions);
    }

    @Override
    public View getHeaderView(int i, View convertView, ViewGroup viewGroup) {
        final TextView otherHeader = (TextView) (convertView == null ? mInflater.inflate(R.layout
                .sliding_menu_header, null, false) : convertView);
        if (i == 0 && convertView == null) {
            otherHeader.setText(getContext().getString(R.string.server));
        } else if (i == mServerItemCount) {
            otherHeader.setText(mFragmentType == FragmentType.Channel ? getContext()
                    .getString(R.string.channel) : getContext().getString(R.string.user));
        }
        return otherHeader;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView row = (TextView) convertView;
        if (row == null) {
            row = (TextView) mInflater.inflate(R.layout.default_listview_textview, parent, false);
        }
        UIUtils.setRobotoLight(getContext(), row);
        if (position == 2) {
            row.setText(mConnected ? "Disconnect" : "Close");
        } else {
            row.setText(getItem(position));
        }

        if (!isEnabled(position)) {
            row.setTextColor(Color.GRAY);
        } else {
            row.setTextColor(UIUtils.getThemedTextColor(getContext()));
        }

        return row;
    }

    @Override
    public long getHeaderId(int i) {
        return i < mServerItemCount ? 0 : 1;
    }

    public void setFragmentType(final FragmentType fragmentType) {
        if (mFragmentType != fragmentType) {
            mFragmentType = fragmentType;
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public String getItem(int position) {
        if (position < mServerItemCount) {
            return super.getItem(position);
        } else if (mFragmentType == FragmentType.Channel) {
            return mChannelArray[getCount() - position - 1];
        } else if (mFragmentType == FragmentType.User) {
            return mUserArray[getCount() - position - 1];
        } else {
            return "";
        }
    }

    @Override
    public int getCount() {
        if (mFragmentType == FragmentType.Server) {
            return mServerItemCount;
        } else if (mFragmentType == FragmentType.Channel) {
            return mServerItemCount + mChannelArray.length;
        } else {
            return mServerItemCount + mUserArray.length;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return !((position == 0) || (position == 1)) || mConnected;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public void setConnected(boolean connected) {
        mConnected = connected;
    }
}