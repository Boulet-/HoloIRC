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

package com.fusionx.lightirc.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fusionx.Utils;
import com.fusionx.lightirc.R;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class SelectionAdapter<T> extends TreeSetAdapter<T> {
    private final ArrayList<T> selectedItems = new ArrayList<>();

    public SelectionAdapter(final Context context, final SortedSet<T> objects) {
        super(context, R.layout.layout_text_list, (TreeSet<T>) objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) super.getView(position, convertView, parent);
        Utils.setTypeface(getContext(), view);

        if (selectedItems.contains(getItem(position))) {
            view.setBackgroundResource(android.R.color.holo_blue_light);
        } else {
            view.setBackgroundResource(R.drawable.selectable_background_cardbank);
        }

        return view;
    }

    /**
     * Adds an item to the selected items list
     *
     * @param position position of item in adapter to add to the list of selected items
     */
    public void addSelection(final int position) {
        selectedItems.add(getItem(position));
        notifyDataSetChanged();
    }

    /**
     * Removes an item from the selected items list
     *
     * @param position position of item in adapter to remove from the list of selected items
     */
    public void removeSelection(final int position) {
        selectedItems.remove(getItem(position));
        notifyDataSetChanged();
    }

    /**
     * Clears the selected items in the adapter
     */
    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Returns the list of selected items in the adapter
     *
     * @return returns a Set of the currently selected items
     */
    public ArrayList<T> getSelectedItems() {
        return selectedItems;
    }

    public TreeSet<T> getCopyOfItems() {
        return new TreeSet<>(mObjects);
    }

    public void setInternalSet(SortedSet<T> set) {
        mObjects = (TreeSet<T>) set;
        notifyDataSetChanged();
    }
}