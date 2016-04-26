package fr.cla.jam.exampledomain;

import static java.lang.String.format;

public abstract class AbstractJenkinsPlugin {

    private final String apiDescription;

    public AbstractJenkinsPlugin(JiraApi api) {
        this.apiDescription = api.description();
    }

    @Override public String toString() {
        return format(
            "%s (API=%s)",
            getClass().getName().replace("fr.cla.jam.", ""),
            apiDescription
        );
    }

}
