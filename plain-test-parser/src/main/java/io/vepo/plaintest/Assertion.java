package io.vepo.plaintest;

public record Assertion<T> (String property, String verb, T value) {

}
