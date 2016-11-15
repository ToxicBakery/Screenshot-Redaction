package com.ToxicBakery.app.screenshot_redaction.dictionary;

/**
 * Concrete implementation of a dictionary status.
 */
class DictionaryStatus implements IDictionaryStatus {

    private final NamedDictionary dictionary;
    private final boolean isEnabled;

    DictionaryStatus(NamedDictionary dictionary,
                     boolean isEnabled) {

        this.dictionary = dictionary;
        this.isEnabled = isEnabled;
    }

    @Override
    public IDictionary getDictionary() {
        return dictionary.getDictionary();
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public String getName() {
        return dictionary.getName();
    }

}
