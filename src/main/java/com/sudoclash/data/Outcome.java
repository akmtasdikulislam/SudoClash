package com.sudoclash.data;

public enum Outcome {
    WIN("Win"), LOSS("Loss"), DRAW("Draw");

    private final String label;

    Outcome(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
