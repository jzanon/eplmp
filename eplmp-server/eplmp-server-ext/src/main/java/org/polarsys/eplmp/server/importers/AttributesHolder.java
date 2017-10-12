package org.polarsys.eplmp.server.importers;


import java.io.Serializable;

public abstract class AttributesHolder implements Serializable {
    public abstract void addAttribute(Attribute newAttribute);
}
