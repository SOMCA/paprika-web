package app.utils;

import java.text.*;
import java.util.*;
/**
 * 
 * This file have be taken of the example of spark-basic-structure:
 * https://github.com/tipsy/spark-basic-structure
 *
 */
public class MessageBundle {

    private ResourceBundle messages;

    public MessageBundle(String languageTag) {
        Locale locale = languageTag != null ? new Locale(languageTag) : Locale.ENGLISH;
        this.messages = ResourceBundle.getBundle("localization/messages", locale);
    }

    public String get(String message) {
        return messages.getString(message);
    }

    public final String get(final String key, final Object... args) {
        return MessageFormat.format(get(key), args);
    }

}
