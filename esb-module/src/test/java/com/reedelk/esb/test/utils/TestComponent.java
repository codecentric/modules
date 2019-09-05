package com.reedelk.esb.test.utils;

import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Context;
import com.reedelk.runtime.api.message.Message;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TestComponent implements ProcessorSync {

    private String stringProperty;
    private long longProperty;
    private Long longObjectProperty;
    private int intProperty;
    private Integer intObjectProperty;
    private double doubleProperty;
    private Double doubleObjectProperty;
    private float floatProperty;
    private Float floatObjectProperty;
    private boolean booleanProperty;
    private Boolean booleanObjectProperty;
    private Number numberProperty;
    private BigDecimal bigDecimalProperty;
    private BigInteger bigIntegerProperty;

    @Override
    public Message apply(Message input, Context context) {
        throw new UnsupportedOperationException("Test Only ProcessorSync");
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public long getLongProperty() {
        return longProperty;
    }

    public void setLongProperty(long longProperty) {
        this.longProperty = longProperty;
    }

    public Long getLongObjectProperty() {
        return longObjectProperty;
    }

    public void setLongObjectProperty(Long longObjectProperty) {
        this.longObjectProperty = longObjectProperty;
    }

    public int getIntProperty() {
        return intProperty;
    }

    public void setIntProperty(int intProperty) {
        this.intProperty = intProperty;
    }

    public Integer getIntObjectProperty() {
        return intObjectProperty;
    }

    public void setIntObjectProperty(Integer intObjectProperty) {
        this.intObjectProperty = intObjectProperty;
    }

    public double getDoubleProperty() {
        return doubleProperty;
    }

    public void setDoubleProperty(double doubleProperty) {
        this.doubleProperty = doubleProperty;
    }

    public Double getDoubleObjectProperty() {
        return doubleObjectProperty;
    }

    public void setDoubleObjectProperty(Double doubleObjectProperty) {
        this.doubleObjectProperty = doubleObjectProperty;
    }

    public float getFloatProperty() {
        return floatProperty;
    }

    public void setFloatProperty(float floatProperty) {
        this.floatProperty = floatProperty;
    }

    public Float getFloatObjectProperty() {
        return floatObjectProperty;
    }

    public void setFloatObjectProperty(Float floatObjectProperty) {
        this.floatObjectProperty = floatObjectProperty;
    }

    public boolean isBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    public Boolean getBooleanObjectProperty() {
        return booleanObjectProperty;
    }

    public void setBooleanObjectProperty(Boolean booleanObjectProperty) {
        this.booleanObjectProperty = booleanObjectProperty;
    }

    public Number getNumberProperty() {
        return numberProperty;
    }

    public void setNumberProperty(Number numberProperty) {
        this.numberProperty = numberProperty;
    }

    public BigDecimal getBigDecimalProperty() {
        return bigDecimalProperty;
    }

    public void setBigDecimalProperty(BigDecimal bigDecimalProperty) {
        this.bigDecimalProperty = bigDecimalProperty;
    }

    public BigInteger getBigIntegerProperty() {
        return bigIntegerProperty;
    }

    public void setBigIntegerProperty(BigInteger bigIntegerProperty) {
        this.bigIntegerProperty = bigIntegerProperty;
    }

}
