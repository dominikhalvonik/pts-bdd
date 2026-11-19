package sk.testlab.cvicenie07.domain;

public enum Delivery {
    COURIER("courier"), PICKUP("pickup");

    private final String apiValue;

    Delivery(String apiValue) { this.apiValue = apiValue; }
    public String apiValue() { return apiValue; }

    public static Delivery from(String value) {
        return switch (value) {
            case "courier" -> COURIER;
            case "pickup" -> PICKUP;
            default -> throw new IllegalArgumentException("Doručenie musí byť courier alebo pickup.");
        };
    }
}
