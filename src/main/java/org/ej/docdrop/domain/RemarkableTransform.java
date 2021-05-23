package org.ej.docdrop.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class RemarkableTransform {
    private final int m11;
    private final int m12;
    private final int m13;
    private final int m21;
    private final int m22;
    private final int m23;
    private final int m31;
    private final int m32;
    private final int m33;

    @JsonCreator
    public RemarkableTransform(@JsonProperty("m11") int m11,
                               @JsonProperty("m12") int m12,
                               @JsonProperty("m13") int m13,
                               @JsonProperty("m21") int m21,
                               @JsonProperty("m22") int m22,
                               @JsonProperty("m23") int m23,
                               @JsonProperty("m31") int m31,
                               @JsonProperty("m32") int m32,
                               @JsonProperty("m33") int m33) {
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public int getM11() {
        return m11;
    }

    public int getM12() {
        return m12;
    }

    public int getM13() {
        return m13;
    }

    public int getM21() {
        return m21;
    }

    public int getM22() {
        return m22;
    }

    public int getM23() {
        return m23;
    }

    public int getM31() {
        return m31;
    }

    public int getM32() {
        return m32;
    }

    public int getM33() {
        return m33;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemarkableTransform that = (RemarkableTransform) o;
        return m11 == that.m11 && m12 == that.m12 && m13 == that.m13 && m21 == that.m21 && m22 == that.m22 && m23 == that.m23 && m31 == that.m31 && m32 == that.m32 && m33 == that.m33;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m11, m12, m13, m21, m22, m23, m31, m32, m33);
    }

    @Override
    public String toString() {
        return "RemarkableTransform{" +
                "m11=" + m11 +
                ", m12=" + m12 +
                ", m13=" + m13 +
                ", m21=" + m21 +
                ", m22=" + m22 +
                ", m23=" + m23 +
                ", m31=" + m31 +
                ", m32=" + m32 +
                ", m33=" + m33 +
                '}';
    }
}
