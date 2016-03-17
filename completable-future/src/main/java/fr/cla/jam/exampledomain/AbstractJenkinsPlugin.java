package fr.cla.jam.exampledomain;

public abstract class AbstractJenkinsPlugin {

    @Override public String toString() {
        return getClass().getName().replace("fr.cla.jam.", "");
    }

}
