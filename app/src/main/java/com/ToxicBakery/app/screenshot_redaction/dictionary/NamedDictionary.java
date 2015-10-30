package com.ToxicBakery.app.screenshot_redaction.dictionary;

import android.content.Context;
import android.support.annotation.NonNull;

class NamedDictionary {

    private final String name;
    private final IDictionary dictionary;

    NamedDictionary(@NonNull Context context,
                    @NonNull IDictionary dictionary) {

        this.name = context.getString(dictionary.getName());
        this.dictionary = dictionary;
    }

    public IDictionary getDictionary() {
        return dictionary;
    }

    public String getName() {
        return name;
    }

}
