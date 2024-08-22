package reminder.discord.bot.java.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Represents a list of user id and name tuples as a string
// e.g. "1234:john#1235:doe"
public class ParticipantUserIdsString {
    // # and : are chosen as separators as they are not valid Discord username or nickname characters
    // Ref: https://discord.com/developers/docs/resources/user#usernames-and-nicknames 

    public static final String ELEMENT_SEPARATOR = "#";
    public static final String ATTR_SEPARATOR = ":";

    private List<String> userIds;
    private List<String> names;

    // userIdsAndNames list elements should be of format: "<userId>:<name>"
    public ParticipantUserIdsString(List<String> userIdsAndNames) {
        this.userIds = new ArrayList<>();
        this.names = new ArrayList<>();
        for (String userIdAndName : userIdsAndNames) {
            String[] userIdAndNameArr = userIdAndName.split(ATTR_SEPARATOR, 2);
            this.userIds.add(userIdAndNameArr[0]);
            if (userIdAndNameArr.length > 0) {
                this.names.add(userIdAndNameArr[1]);
            }
        }
    }

    // userIdsAndNames string should be of format: "<userId1>:<name1>#<userId2>:<name2>"
    public ParticipantUserIdsString(String userIdsAndNames) {
        this(Arrays.asList(userIdsAndNames.split(ELEMENT_SEPARATOR)));
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
