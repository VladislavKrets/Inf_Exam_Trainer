package com.trainer;

public class Answer {
    private String data;
    private boolean isTrue;

    public Answer(String data, boolean isTrue) {
        this.data = data;
        this.isTrue = isTrue;
    }

    public String getData() {
        return data;
    }

    public boolean isTrue() {
        return isTrue;
    }
}
