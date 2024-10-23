package com.nicolauscg.reminder.discord.bot;

import javax.annotation.Nonnull;

// Contains logic to handle use of interaction id in a custom id for interactions 
public class CustomId {
    static final String SEPARATOR = ",";

    String label;
    String firstIntrId;

    public CustomId(@Nonnull String label, @Nonnull String firstIntrId) {
        this.label = label; 
        this.firstIntrId = firstIntrId; 
    }

    public CustomId(@Nonnull String customId) {
        String[] arr = customId.split(SEPARATOR, 2);
        if (arr.length > 0) {
            this.label = arr[0]; 
        }
        if (arr.length > 1) {
            this.firstIntrId = arr[1]; 
        }
    }

    public String toString() {
        return this.label + SEPARATOR + this.firstIntrId;
    }
}
