package edu.uob.models;

import java.util.HashSet;
import java.util.Set;

/**
 * Class for storing the game actions fetched from actions.xml file
 */

public class GameAction {
    private Set<String> subjects;
    private Set<String> consumed;
    private Set<String> produced;
    private String narration;

    public GameAction() {
        this.subjects = new HashSet<>();
        this.consumed = new HashSet<>();
        this.produced = new HashSet<>();
        this.narration = "";
    }

    public void addAttributes(String type, String attributeName) throws Exception {
        switch (type) {
            case "subjects":
                this.subjects.add(attributeName);
                break;
            case "consumed":
                this.consumed.add(attributeName);
                break;
            case "produced":
                this.produced.add(attributeName);
                break;
            default:
                throw new Exception("unrecognized action attribute.\n");
        }
    }

    public void addNarration(String narration) {
        this.narration = narration;
    }

    public Set<String> getSubjects() {
        return subjects;
    }

    public Set<String> getConsumed() {
        return consumed;
    }

    public Set<String> getProduced() {
        return produced;
    }

    public String getNarration() {
        return narration;
    }

}