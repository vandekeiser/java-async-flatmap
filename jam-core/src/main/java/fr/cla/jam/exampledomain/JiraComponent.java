package fr.cla.jam.exampledomain;

import static java.lang.String.format;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

public class JiraComponent {

    private final String name;

    public JiraComponent(String name) {
        this.name = requireNonNull(name);
    }

    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof JiraComponent)) return false;
        JiraComponent that = (JiraComponent) obj;
        return this.name.equals(that.name);
    }

    @Override public int hashCode() {
        return hash(name);
    }

    @Override public String toString() {
        return format("COMPONENT-%s", name);
    }

}
