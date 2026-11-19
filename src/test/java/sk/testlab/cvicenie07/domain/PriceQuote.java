package sk.testlab.cvicenie07.domain;

/** Skutočné hodnoty jedného výsledku. Neobsahuje výpočet očakávanej ceny. */
public record PriceQuote(int subtotalCents, int discountCents, int shippingCents, int totalCents) {
}
