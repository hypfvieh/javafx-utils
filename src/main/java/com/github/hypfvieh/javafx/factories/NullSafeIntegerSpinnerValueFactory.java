package com.github.hypfvieh.javafx.factories;

import javafx.beans.NamedArg;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.converter.IntegerStringConverter;

/**
 * Copy of {@link SpinnerValueFactory.IntegerSpinnerValueFactory} but handles null values without throwing exceptions.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class NullSafeIntegerSpinnerValueFactory extends SpinnerValueFactory<Integer> {

    /***********************************************************************
     *                                                                     *
     * Constructors                                                        *
     *                                                                     *
     **********************************************************************/

    /**
     * Constructs a new IntegerSpinnerValueFactory that sets the initial value
     * to be equal to the min value, and a default {@code amountToStepBy} of one.
     *
     * @param _min The minimum allowed integer value for the Spinner.
     * @param _max The maximum allowed integer value for the Spinner.
     */
    public NullSafeIntegerSpinnerValueFactory(@NamedArg("min") int _min,
                                      @NamedArg("max") int _max) {
        this(_min, _max, _min);
    }

    /**
     * Constructs a new IntegerSpinnerValueFactory with a default
     * {@code amountToStepBy} of one.
     *
     * @param _min The minimum allowed integer value for the Spinner.
     * @param _max The maximum allowed integer value for the Spinner.
     * @param _initialValue The value of the Spinner when first instantiated, must
     *                     be within the bounds of the min and max arguments, or
     *                     else the min value will be used.
     */
    public NullSafeIntegerSpinnerValueFactory(@NamedArg("min") int _min,
                                      @NamedArg("max") int _max,
                                      @NamedArg("initialValue") int _initialValue) {
        this(_min, _max, _initialValue, 1);
    }

    /**
     * Constructs a new IntegerSpinnerValueFactory.
     *
     * @param _min The minimum allowed integer value for the Spinner.
     * @param _max The maximum allowed integer value for the Spinner.
     * @param _initialValue The value of the Spinner when first instantiated, must
     *                     be within the bounds of the min and max arguments, or
     *                     else the min value will be used.
     * @param _amountToStepBy The amount to increment or decrement by, per step.
     */
    public NullSafeIntegerSpinnerValueFactory(@NamedArg("min") int _min,
                                      @NamedArg("max") int _max,
                                      @NamedArg("initialValue") int _initialValue,
                                      @NamedArg("amountToStepBy") int _amountToStepBy) {
        setMin(_min);
        setMax(_max);
        setAmountToStepBy(_amountToStepBy);
        setConverter(new IntegerStringConverter());

        valueProperty().addListener((o, oldValue, newValue) -> {
            // when the value is set, we need to react to ensure it is a
            // valid value (and if not, blow up appropriately)
            if (newValue == null) {
                return;
            }
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
    private IntegerProperty min = new SimpleIntegerProperty(this, "min") {
        @Override protected void invalidated() {
            Integer currentValue = NullSafeIntegerSpinnerValueFactory.this.getValue();
            if (currentValue == null) {
                return;
            }

            int newMin = get();
            if (newMin > getMax()) {
                setMin(getMax());
                return;
            }

            if (currentValue < newMin) {
                NullSafeIntegerSpinnerValueFactory.this.setValue(newMin);
            }
        }
    };

    public final void setMin(int value) {
        min.set(value);
    }
    public final int getMin() {
        return min.get();
    }
    /**
     * Sets the minimum allowable value for this value factory
     * @return the minimum allowable value for this value factory
     */
    public final IntegerProperty minProperty() {
        return min;
    }

    // --- max
    private IntegerProperty max = new SimpleIntegerProperty(this, "max") {
        @Override protected void invalidated() {
            Integer currentValue = NullSafeIntegerSpinnerValueFactory.this.getValue();
            if (currentValue == null) {
                return;
            }

            int newMax = get();
            if (newMax < getMin()) {
                setMax(getMin());
                return;
            }

            if (currentValue > newMax) {
                NullSafeIntegerSpinnerValueFactory.this.setValue(newMax);
            }
        }
    };

    public final void setMax(int _value) {
        max.set(_value);
    }
    public final int getMax() {
        return max.get();
    }
    /**
     * Sets the maximum allowable value for this value factory
     * @return the maximum allowable value for this value factory
     */
    public final IntegerProperty maxProperty() {
        return max;
    }

    // --- amountToStepBy
    private IntegerProperty amountToStepBy = new SimpleIntegerProperty(this, "amountToStepBy");
    public final void setAmountToStepBy(int _value) {
        amountToStepBy.set(_value);
    }
    public final int getAmountToStepBy() {
        return amountToStepBy.get();
    }
    /**
     * Sets the amount to increment or decrement by, per step.
     * @return the amount to increment or decrement by, per step
     */
    public final IntegerProperty amountToStepByProperty() {
        return amountToStepBy;
    }



    /***********************************************************************
     *                                                                     *
     * Overridden methods                                                  *
     *                                                                     *
     **********************************************************************/

    /** {@inheritDoc} */
    @Override public void decrement(int _steps) {
        final int min = getMin();
        final int max = getMax();
        final int newIndex = getValue() - _steps * getAmountToStepBy();
        setValue(newIndex >= min ? newIndex : (isWrapAround() ? wrapValue(newIndex, min, max) + 1 : min));
    }

    /** {@inheritDoc} */
    @Override public void increment(int _steps) {
        final int min = getMin();
        final int max = getMax();
        final int currentValue = getValue() == null ? getMin() : getValue();
        final int newIndex = currentValue + _steps * getAmountToStepBy();
        setValue(newIndex <= max ? newIndex : (isWrapAround() ? wrapValue(newIndex, min, max) - 1 : max));
    }

    static int wrapValue(int _value, int _min, int _max) {
        if (_max == 0) {
            throw new RuntimeException();
        }

        int r = _value % _max;
        if (r > _min && _max < _min) {
            r = r + _max - _min;
        } else if (r < _min && _max > _min) {
            r = r + _max - _min;
        }
        return r;
    }

}
