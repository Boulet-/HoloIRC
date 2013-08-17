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

package com.fusionx.irc;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.fusionx.Utils;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.UserEventType;
import com.fusionx.irc.enums.UserLevel;
import com.fusionx.irc.handlerabstract.UserHandler;
import com.fusionx.irc.writers.UserWriter;
import com.fusionx.lightirc.R;
import com.fusionx.uiircinterface.MessageSender;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;

import de.scrum_master.util.UpdateableTreeSet;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Data
public class User implements UpdateableTreeSet.Updateable {
    protected String nick;
    protected String login;
    protected String host;
    protected String serverUrl;
    protected String realName;

    protected String buffer = "";

    @Getter(AccessLevel.NONE)
    protected String nickHTML;

    @Getter(AccessLevel.NONE)
    protected final UserChannelInterface userChannelInterface;
    protected final HashMap<Channel, UserLevel> userLevelMap = new HashMap<>();

    @Getter
    protected final UserWriter writer;

    public User(@NonNull final String nick,
                @NonNull final UserChannelInterface userChannelInterface) {
        this.nick = nick;
        this.writer = new UserWriter(userChannelInterface.getOutputStream(), this);
        this.userChannelInterface = userChannelInterface;

        nickHTML = "<font color=\"" + Utils.generateRandomColor(Utils
                .getUserColorOffset(userChannelInterface.getContext())) + "\">%1$s</font>";
    }

    public UpdateableTreeSet<Channel> getChannels() {
        return userChannelInterface.getAllChannelsInUser(this);
    }

    public String getColorfulNick() {
        return String.format(nickHTML, nick);
    }

    public String getPrettyNick(final String channel) {
        return getPrettyNick(userChannelInterface.getChannel(channel));
    }

    public String getPrettyNick(final Channel channel) {
        return String.format(nickHTML, getUserPrefix(channel) + nick);
    }

    protected String getUserPrefix(final Channel channel) {
        final UserLevel level = userLevelMap.get(channel);
        if (UserLevel.OP.equals(level)) {
            return "@";
        } else if (UserLevel.VOICE.equals(level)) {
            return "+";
        } else {
            return "";
        }
    }

    public void processWhoMode(final String rawMode, final Channel channel) {
        UserLevel mode = UserLevel.NONE;
        if (rawMode.contains("~")) {
            mode = UserLevel.OWNER;
        } else if (rawMode.contains("&")) {
            mode = UserLevel.SUPEROP;
        } else if (rawMode.contains("@")) {
            mode = UserLevel.OP;
        } else if (rawMode.contains("%")) {
            mode = UserLevel.HALFOP;
        } else if (rawMode.contains("+")) {
            mode = UserLevel.VOICE;
        }
        userLevelMap.put(channel, mode);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User && ((User) o).getNick().equals(nick);
    }

    public String processModeChange(final Context context, final String sendingNick,
                                    final Channel channel, final String mode) {
        boolean addingMode = false;
        for (char character : mode.toCharArray()) {
            switch (character) {
                case '+':
                    addingMode = true;
                    break;
                case '-':
                    addingMode = false;
                    break;
                case 'o':
                    if (addingMode) {
                        channel.getUsers().update(this, ImmutableList.of(channel, UserLevel.OP));
                        break;
                    }
                case 'v':
                    if (addingMode) {
                        channel.getUsers().update(this,
                                ImmutableList.of(channel, UserLevel.VOICE));
                        break;
                    }
                default:
                    if (!addingMode && (character == 'v' || character == 'o')) {
                        channel.getUsers().update(this,
                                ImmutableList.of(channel, UserLevel.NONE));
                    }
                    break;
            }
        }

        final String formattedSenderNick;
        final User sendingUser = userChannelInterface.getUserIfExists(sendingNick);
        if (sendingUser == null) {
            formattedSenderNick = sendingNick;
        } else {
            formattedSenderNick = sendingUser.getPrettyNick(channel);
        }

        return String.format(context.getString(R.string.parser_mode_changed), mode,
                getColorfulNick(), formattedSenderNick);
    }

    @Override
    public void update() {
        // used for nick changes
    }

    @Override
    public void update(Object newValue) {
        if (newValue instanceof ImmutableList) {
            // ArrayList = mode change
            ImmutableList list = (ImmutableList) newValue;
            if (list.get(0) instanceof Channel && list.get(1) instanceof UserLevel) {
                userLevelMap.put((Channel) list.get(0), (UserLevel) list.get(1));
            }
        }
    }

    public void registerHandler() {
        MessageSender.getSender(userChannelInterface.getServer().getTitle())
                .registerUserHandler(nick, userHandler);
    }

    void unregisterHandler() {
        MessageSender.getSender(userChannelInterface.getServer().getTitle())
                .unregisterUserHandler(nick);
    }

    private UserHandler userHandler = new UserHandler() {
        @Override
        public void handleMessage(Message msg) {
            final Bundle event = msg.getData();
                final UserEventType type = (UserEventType) event
                        .getSerializable(EventBundleKeys.eventType);
                final String message = event.getString(EventBundleKeys.message);
                switch (type) {
                    case Generic:
                        buffer += message + "\n";
                        break;
                }
        }
    };
}