package com.github.hypfvieh.javafx.fx.fonts;

public interface IWebFontCode {

	Character getCharacter();
	default String getCharacterAsString() {
		return getCharacter() != null ? getCharacter().toString() : null;
	}

	String getFontFamily();
}
