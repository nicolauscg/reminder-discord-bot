package reminder.discord.bot.java;

import javax.annotation.Nonnull;

// Contains logic to handle use of interaction id in a custom id for interactions 
public class CustomId {
    static final String SEPARATOR = ",";

    String label;
    String intrId;

    public CustomId(@Nonnull String label, @Nonnull String intrId) {
        this.label = label; 
        this.intrId = intrId; 
    }

    public CustomId(@Nonnull String customId) {
        String[] arr = customId.split(SEPARATOR, 2);
        if (arr.length > 0) {
            this.label = arr[0]; 
        }
        if (arr.length > 1) {
            this.intrId = arr[1]; 
        }
    }

    public String toString() {
        return this.label + SEPARATOR + this.intrId;
    }
}
