package com.github.hypfvieh.javafx.fx.fonts;

public enum PaymentWebFont implements IWebFontCode {

    CLICKANDBUY      ('\ue61d'),
    WESTERNUNION     ('\ue61e'),
    BRAINTREE        ('\ue61f'),
    PAYSAFECARD      ('\ue620'),
    IDEAL            ('\ue621'),
    PAYPAL           ('\ue622'),
    SKRILL           ('\ue61b'),
    CB               ('\ue61c'),
    GITTIP           ('\ue618'),
    FLATTR           ('\ue61a'),
    RIPPLE           ('\ue616'),
    SOFORT           ('\ue617'),
    BITCOIN          ('\ue614'),
    BITCOIN_SIGN     ('\ue615'),
    DINERS           ('\ue609'),
    MASTERCARD       ('\ue602'),
    TRUST_E          ('\ue612'),
    AMAZON           ('\ue613'),
    JCB              ('\ue610'),
    GOOGLE_WALLET    ('\ue611'),
    STRIPE           ('\ue60d'),
    SQUARE           ('\ue60e'),
    OGONE            ('\ue60f'),
    VERISIGN         ('\ue60c'),
    DISCOVER         ('\ue60a'),
    AMERICAN_EXPRESS ('\ue607'),
    PAYPAL__CLASSIC  ('\ue604'),
    MAESTRO          ('\ue605'),
    VISA             ('\ue601'),
    VISA_ELECTRON    ('\ue606'),
    POSTEPAY         ('\ue608'),
    CARTASI          ('\ue60b'),
    UNIONPAY         ('\ue603'),
    EC               ('\ue600'),
    BANCONTACT       ('\ue622');

    private final Character character;

    private PaymentWebFont(Character _character) {
        character = _character;
    }

    @Override
    public Character getCharacter() {
        return character;
    }

    @Override
    public String getFontFamily() {
        return "payment-webfont";
    }

}

