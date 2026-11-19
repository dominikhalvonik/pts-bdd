package sk.testlab.cvicenie07.domain;

/** Vstup scenára. Ceny sem nepatria: poskytuje ich aplikácia. */
public record CartItem(String sku, int quantity) {
    public CartItem {
        if (sku == null || !sku.matches("[A-Z0-9-]{1,32}")) {
            throw new IllegalArgumentException("SKU musí mať tvar napríklad BAG-001.");
        }
        if (quantity < 1 || quantity > 20) {
            throw new IllegalArgumentException("Množstvo musí byť celé číslo od 1 do 20.");
        }
    }
}
