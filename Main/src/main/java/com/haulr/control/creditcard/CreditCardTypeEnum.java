package com.haulr.control.creditcard;

/**
 * @description     Credit Card Type
 * @author          Adrian
 */
public enum CreditCardTypeEnum {
    VISA("Visa"),
    MASTER_CARD("MasterCard"),
    AMERICAN_EXPRESS("American Express");

    public final String cartType;

    CreditCardTypeEnum(String cartType) {
        this.cartType = cartType;
    }

    public static String[] creditCardTypes() {
        String types[] = new String[values().length];
        int i = 0;
        for(CreditCardTypeEnum type : values()) {
            types[i] = type.cartType;
            i += 1;
        }
        return types;
    }
}
