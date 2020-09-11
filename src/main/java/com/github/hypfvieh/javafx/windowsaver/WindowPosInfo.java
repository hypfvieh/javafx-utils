package com.github.hypfvieh.javafx.windowsaver;

import java.util.Objects;

/**
 * Property object containing window information like size and position.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class WindowPosInfo {
    private double  width;
    private double  minWidth;
    private double  maxWidth;
    private double  prefWidth;
    private double  height;
    private double  minHeight;
    private double  maxHeight;
    private double  prefHeight;
    private double  x;
    private double  y;
    private boolean maximized;
    private String  title;

    public double getPrefWidth() {
        return prefWidth;
    }

    public void setPrefWidth(double _prefWidth) {
        prefWidth = _prefWidth;
    }

    public double getPrefHeight() {
        return prefHeight;
    }

    public void setPrefHeight(double _prefHeight) {
        prefHeight = _prefHeight;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double _width) {
        width = _width;
    }

    public double getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(double _minWidth) {
        minWidth = _minWidth;
    }

    public double getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(double _maxWidth) {
        maxWidth = _maxWidth;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double _height) {
        height = _height;
    }

    public double getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(double _minHeight) {
        minHeight = _minHeight;
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(double _maxHeight) {
        maxHeight = _maxHeight;
    }

    public double getX() {
        return x;
    }

    public void setX(double _x) {
        x = _x;
    }

    public double getY() {
        return y;
    }

    public void setY(double _y) {
        y = _y;
    }

    public boolean isMaximized() {
        return maximized;
    }

    public void setMaximized(boolean _maximized) {
        maximized = _maximized;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String _title) {
        title = _title;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, maxHeight, maxWidth, maximized, minHeight, minWidth, prefHeight, prefWidth, title,
                width, x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WindowPosInfo other = (WindowPosInfo) obj;
        return Double.doubleToLongBits(height) == Double.doubleToLongBits(other.height)
                && Double.doubleToLongBits(maxHeight) == Double.doubleToLongBits(other.maxHeight)
                && Double.doubleToLongBits(maxWidth) == Double.doubleToLongBits(other.maxWidth)
                && maximized == other.maximized
                && Double.doubleToLongBits(minHeight) == Double.doubleToLongBits(other.minHeight)
                && Double.doubleToLongBits(minWidth) == Double.doubleToLongBits(other.minWidth)
                && Double.doubleToLongBits(prefHeight) == Double.doubleToLongBits(other.prefHeight)
                && Double.doubleToLongBits(prefWidth) == Double.doubleToLongBits(other.prefWidth)
                && Objects.equals(title, other.title)
                && Double.doubleToLongBits(width) == Double.doubleToLongBits(other.width)
                && Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)
                && Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
    }

    @Override
    public String toString() {
        return "WindowPosInfo [width=" + width + ", height=" + height + ", x=" + x + ", y=" + y + ", maximized="
                + maximized + "]";
    }

}
