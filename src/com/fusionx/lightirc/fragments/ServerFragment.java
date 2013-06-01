/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.ServerChannelActivity;

public class ServerFragment extends IRCFragment implements OnKeyListener {
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_irc, container, false);

        setTitle(getArguments().getString("title"));

        String buffer = getArguments().getString("buffer");
        if (buffer != null) {
            writeToTextView(buffer, rootView);
        }

        EditText edittext = (EditText) rootView.findViewById(R.id.editText1);
        edittext.setOnKeyListener(this);

        return rootView;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        EditText editText = (EditText) view;

        if ((event.getKeyCode() == KeyEvent.FLAG_EDITOR_ACTION)
                && !editText.getText().toString().equals("\n")
                && !editText.getText().toString().isEmpty()) {

            ((ServerChannelActivity) getActivity())
                    .parser.serverMessageToParse(getTitle(), editText.getText().toString());

            editText.setText("");

            return true;
        }
        return false;
    }
}
