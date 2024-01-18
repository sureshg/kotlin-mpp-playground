package dev.suresh;

public record Lang(String name) {
}


record Model(String name, float temp, int tokens) {

    public static ModelBuilder builder() {
        return new ModelBuilder();
    }

    void main() {

        var b = Model.builder().name("test").build();

    }
}

class ModelBuilder {
    private String name;
    private float temp = 0.1f;

    private int tokens = 100;

    public ModelBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ModelBuilder temp(float temp) {
        this.temp = temp;
        return this;
    }

    public ModelBuilder tokens(int tokens) {
        this.tokens = tokens;
        return this;
    }

    public Model build() {
        return new Model(name, temp, tokens);
    }
}