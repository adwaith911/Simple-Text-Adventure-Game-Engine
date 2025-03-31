package edu.uob.models;

public class GameEntity
{
        protected String id;
        protected String type; // location, artefact, furniture, character
        protected String description;

       public GameEntity(String id, String type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;

    }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }


    @Override
        public String toString() {
            return "Entity{" +
                    "id='" + id + '\'' +
                    ", type='" + type + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
