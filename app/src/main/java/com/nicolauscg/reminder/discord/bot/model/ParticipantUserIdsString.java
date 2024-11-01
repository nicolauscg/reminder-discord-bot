package com.nicolauscg.reminder.discord.bot.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.dv8tion.jda.api.entities.User;

// Represents a list of user id and name tuples as a string
// e.g. "1234:john#1235:doe"
public class ParticipantUserIdsString {
    // # and : are chosen as separators as they are not valid Discord username or nickname characters
    // Ref: https://discord.com/developers/docs/resources/user#usernames-and-nicknames 

    public static final String ELEMENT_SEPARATOR = "#";
    public static final String ATTR_SEPARATOR = ":";

    private List<String> userIds;
    private List<String> names;

    public ParticipantUserIdsString(List<String> userIds, List<String> userNames) {
        this.userIds = userIds;
        this.names = userNames;
    }
    
    public ParticipantUserIdsString(List<User> users) {
        this(
            users.stream().map(user -> user.getId()).toList(),
            users.stream().map(user -> user.getEffectiveName()).toList()
        );
    }

    // userIdsAndNames string should be of format: "<userId1>:<name1>#<userId2>:<name2>"
    public static ParticipantUserIdsString fromString(String userIdsAndNames) {
        List<String> userIdAndNameList = Arrays.asList(userIdsAndNames.split(ELEMENT_SEPARATOR));
        return new ParticipantUserIdsString(
            userIdAndNameList.stream().map(str -> str.split(ATTR_SEPARATOR)[0]).toList(),
            userIdAndNameList.stream().map(str -> {
                String[] arr = str.split(ATTR_SEPARATOR);
                if (arr.length > 1) {
                    return arr[1];
                }
                return arr[0];
            }).toList()
        );
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public List<String> getNames() {
        return names;
    }

    @Override
    public String toString() {
        return IntStream.range(0, Math.min(this.userIds.size(), this.names.size()))
                .mapToObj(i -> this.userIds.get(i) + ATTR_SEPARATOR + this.names.get(i))
                .collect(Collectors.joining(ELEMENT_SEPARATOR));
    }
}
