package edu.uob.models;

import java.util.HashSet;
import java.util.Map;

public class Location extends GameEntity{

    Map<String, GameEntity> attributes;

    HashSet<String> paths;

    public Location(String id, String type, String description,Map<String, GameEntity> attributes) {
        super(id, type, description);
        this.attributes = attributes;
        this.paths = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void removeAttribute(String name) {
        if (this.attributes.containsKey(name)) {
            this.attributes.remove(name);
        }
    }

    public void setAttributes(Map<String, GameEntity> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String name,GameEntity attribute){
        attributes.put(name,attribute);
    }

    public Map<String, GameEntity> getAttributes() {
        return attributes;
    }

    public HashSet<String> getPaths() {
        return paths;
    }

    public void setPaths(HashSet<String> paths) {
        this.paths = paths;
    }

    public void addPath(String path) {
            paths.add(path);
        }
    public boolean pathExists(String path) {
        return paths.contains(path);
    }

    public void removePath(String path) {
        paths.remove(path);
    }

    public boolean hasAttribute(String attributeName) {
        return this.attributes.containsKey(attributeName);
    }



    @Override
    public String toString() {
        return "Location{" +
                "id='" + id + '\'' +
                ", description='" + getDescription() + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
