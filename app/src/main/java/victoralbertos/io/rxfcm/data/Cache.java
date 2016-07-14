package victoralbertos.io.rxfcm.data;

import java.util.ArrayList;
import java.util.List;

import victoralbertos.io.rxfcm.data.entities.Notification;

/**
 * Created by victor on 01/04/16.
 */
public enum Cache {
    Pool;

    private final List<Notification> issues = new ArrayList<>();
    private final List<Notification> supplies = new ArrayList<>();
    private final List<Notification> nestedSupplies = new ArrayList<>();

    public void addIssue(Notification issue) {
        issues.add(issue);
    }

    public void addSupply(Notification supply) {
        supplies.add(supply);
    }

    public void addNestedSupply(Notification supply) {
        nestedSupplies.add(supply);
    }

    public List<Notification> getIssues() {
        return issues;
    }

    public List<Notification> getSupplies() {
        return supplies;
    }

    public List<Notification> getNestedSupplies() {
        return nestedSupplies;
    }

}
