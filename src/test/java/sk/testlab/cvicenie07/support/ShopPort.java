package sk.testlab.cvicenie07.support;

import java.util.List;
import java.util.Optional;
import sk.testlab.cvicenie07.domain.CartItem;
import sk.testlab.cvicenie07.domain.Delivery;
import sk.testlab.cvicenie07.domain.PriceQuote;

/** Spoločné správanie pre GUI aj API. Kroky Gherkinu nepotrebujú poznať transport. */
public interface ShopPort extends AutoCloseable {
    void start();
    PriceQuote calculate(List<CartItem> items, String coupon, Delivery delivery);
    Optional<byte[]> screenshot();
    String diagnostics();
    @Override void close();
}
