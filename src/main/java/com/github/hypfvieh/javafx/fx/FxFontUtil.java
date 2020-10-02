package com.github.hypfvieh.javafx.fx;

import java.io.InputStream;

import javafx.scene.control.Label;
import javafx.scene.text.Font;

import com.github.hypfvieh.javafx.fx.fonts.IWebFontCode;

/**
 * Utility to work with different (Web)fonts.
 *
 * @author hypfvieh
 * @since v1.0.0 - 2020-10-02
 */
public class FxFontUtil {

	private FxFontUtil() {

	}

	/**
	 * Load given font (ttf) from stream.
	 * This is only another way to load fonts for JavaFX applications.
	 * You can also use {@link Font#loadFont(InputStream, double)}.
	 * @param _fontInputStream font file as input stream
	 * @param _size font size
	 */
	public static void loadFont(InputStream _fontInputStream, double _size) {
		Font.loadFont(_fontInputStream, _size);
	}

	/**
	 * Creates a new {@link Label} with the given 'iconCode' (e.g. "\uf000") and the given font size.
	 * @param _iconCode utf icon code
	 * @param _size font size
	 * @return label
	 */
	public static Label createIconLabel(String _iconCode, double _size) {
		Label label = new Label(_iconCode);
    	label.setStyle("-fx-font-size: "+ _size + "px;");
    	return label;
	}

	/**
	 * Creates a new {@link Label} with the given 'icon' and the given font size.
	 * @param _iconCode utf icon code
	 * @param _size font size
	 * @return label
	 */
	public static Label createIconLabel(IWebFontCode _iconCode, double _size) {
    	Label label = createIconLabel(_iconCode.getCharacterAsString(), _size);
    	label.setStyle(label.getStyle() + "-fx-font-family: " + _iconCode.getFontFamily() + ";");
    	label.getStyle();
		return label;
	}
}
