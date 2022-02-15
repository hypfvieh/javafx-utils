package com.github.hypfvieh.javafx.factories;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * Copy of {@link SpinnerValueFactory.DoubleSpinnerValueFactory} but handles null values without throwing exceptions.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class NullSafeDoubleSpinnerValueFactory extends SpinnerValueFactory<Double> {

    /**
     * Constructs a new DoubleSpinnerValueFactory that sets the initial value
     * to be equal to the min value, and a default {@code amountToStepBy} of
     * one.
     *
     * @param _min The minimum allowed double value for the Spinner.
     * @param _max The maximum allowed double value for the Spinner.
     */
    public NullSafeDoubleSpinnerValueFactory(@NamedArg("min") double _min,
                                     @NamedArg("max") double _max) {
        this(_min, _max, _min);
    }

    /**
     * Constructs a new DoubleSpinnerValueFactory with a default
     * {@code amountToStepBy} of one.
     *
     * @param _min The minimum allowed double value for the Spinner.
     * @param _max The maximum allowed double value for the Spinner.
     * @param _initialValue The value of the Spinner when first instantiated, must
     *                     be within the bounds of the min and max arguments, or
     *                     else the min value will be used.
     */
    public NullSafeDoubleSpinnerValueFactory(@NamedArg("min") double _min,
                                     @NamedArg("max") double _max,
                                     @NamedArg("initialValue") double _initialValue) {
        this(_min, _max, _initialValue, 1);
    }

    /**
     * Constructs a new DoubleSpinnerValueFactory.
     *
     * @param _min The minimum allowed double value for the Spinner.
     * @param _max The maximum allowed double value for the Spinner.
     * @param _initialValue The value of the Spinner when first instantiated, must
     *                     be within the bounds of the min and max arguments, or
     *                     else the min value will be used.
     * @param _amountToStepBy The amount to increment or decrement by, per step.
     */
    public NullSafeDoubleSpinnerValueFactory(@NamedArg("min") double _min,
                                     @NamedArg("max") double _max,
                                     @NamedArg("initialValue") double _initialValue,
                                     @NamedArg("amountToStepBy") double _amountToStepBy) {
        setMin(_min);
        setMax(_max);
        setAmountToStepBy(_amountToStepBy);
        setConverter(new StringConverter<Double>() {
            private final DecimalFormat df = new DecimalFormat("#.##");

            @Override public String toString(Double _value) {
                // If the specified value is null, return a zero-length String
                if (_value == null) {
                    return "";
                }

                return df.format(_value);
            }

            @Override public Double fromString(String _value) {
                try {
                    // If the specified value is null or zero-length, return null
                    if (_value == null) {
                        return null;
                    }

                    _value = _value.trim();

                    if (_value.length() < 1) {
                        return null;
                    }

                    // Perform the requested parsing
                    return df.parse(_value).doubleValue();
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        valueProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == null) return;

            // when the value is set, we need to react to ensure it is a
            // valid value (and if not, blow up appropriately)
            if (newValue < getMin()) {
                setValue(getMin());
            } else if (newValue > getMax()) {
                setValue(getMax());
            }
        });
        setValue(_initialValue >= _min && _initialValue <= _max ? _initialValue : _min);
    }



    /***********************************************************************
     *                                                                     *
     * Properties                                                          *
     *                                                                     *
     **********************************************************************/

    // --- min
    private DoubleProperty min = new SimpleDoubleProperty(this, "min") {
        @Override protected void invalidated() {
            Double currentValue = NullSafeDoubleSpinnerValueFactory.this.getValue();
            if (currentValue == null) {
                return;
            }

            final double newMin = get();
            if (newMin > getMax()) {
                setMin(getMax());
                return;
            }

            if (currentValue < newMin) {
                NullSafeDoubleSpinnerValueFactory.this.setValue(newMin);
            }
        }
    };

    public final void setMin(double _value) {
        min.set(_value);
    }
    public final double getMin() {
        return min.get();
    }
    /**
     * Sets the minimum allowable value for this value factory
     * @return the minimum allowable value for this value factory
     */
    public final DoubleProperty minProperty() {
        return min;
    }

    // --- max
    private DoubleProperty max = new SimpleDoubleProperty(this, "max") {
        @Override protected void invalidated() {
            Double currentValue = NullSafeDoubleSpinnerValueFactory.this.getValue();
            if (currentValue == null) {
                return;
            }

            final double newMax = get();
            if (newMax < getMin()) {
                setMax(getMin());
                return;
            }

            if (currentValue > newMax) {
                NullSafeDoubleSpinnerValueFactory.this.setValue(newMax);
            }
        }
    };

    public final void setMax(double _value) {
        max.set(_value);
    }
    public final double getMax() {
        return max.get();
    }
    /**
     * Sets the maximum allowable value for this value factory
     * @return the maximum allowable value for this value factory
     */
    public final DoubleProperty maxProperty() {
        return max;
    }

    // --- amountToStepBy
    private DoubleProperty amountToStepBy = new SimpleDoubleProperty(this, "amountToStepBy");
    public final void setAmountToStepBy(double _value) {
        amountToStepBy.set(_value);
    }
    public final double getAmountToStepBy() {
        return amountToStepBy.get();
    }
    /**
     * Sets the amount to increment or decrement by, per step.
     * @return the amount to increment or decrement by, per step
     */
    public final DoubleProperty amountToStepByProperty() {
        return amountToStepBy;
    }



    /** {@inheritDoc} */
    @Override public void decrement(int _steps) {
        final BigDecimal currentValue = BigDecimal.valueOf(getValue());
        final BigDecimal minBigDecimal = BigDecimal.valueOf(getMin());
        final BigDecimal maxBigDecimal = BigDecimal.valueOf(getMax());
        final BigDecimal amountToStepByBigDecimal = BigDecimal.valueOf(getAmountToStepBy());
        BigDecimal newValue = currentValue.subtract(amountToStepByBigDecimal.multiply(BigDecimal.valueOf(_steps)));
        setValue(newValue.compareTo(minBigDecimal) >= 0 ? newValue.doubleValue() :
                (isWrapAround() ? wrapValue(newValue, minBigDecimal, maxBigDecimal).doubleValue() : getMin()));
    }

    /** {@inheritDoc} */
    @Override public void increment(int _steps) {
        final BigDecimal currentValue = BigDecimal.valueOf(getValue());
        final BigDecimal minBigDecimal = BigDecimal.valueOf(getMin());
        final BigDecimal maxBigDecimal = BigDecimal.valueOf(getMax());
        final BigDecimal amountToStepByBigDecimal = BigDecimal.valueOf(getAmountToStepBy());
        BigDecimal newValue = currentValue.add(amountToStepByBigDecimal.multiply(BigDecimal.valueOf(_steps)));
        setValue(newValue.compareTo(maxBigDecimal) <= 0 ? newValue.doubleValue() :
                (isWrapAround() ? wrapValue(newValue, minBigDecimal, maxBigDecimal).doubleValue() : getMax()));
    }

    static BigDecimal wrapValue(BigDecimal _value, BigDecimal _min, BigDecimal _max) {
        if (_max.doubleValue() == 0) {
            throw new RuntimeException();
        }

        // note that this wrap method differs from the others where we take the
        // difference - in this approach we wrap to the min or max - it feels better
        // to go from 1 to 0, rather than 1 to 0.05 (where max is 1 and step is 0.05).
        if (_value.compareTo(_min) < 0) {
            return _max;
        } else if (_value.compareTo(_max) > 0) {
            return _min;
        }
        return _value;
    }

}
