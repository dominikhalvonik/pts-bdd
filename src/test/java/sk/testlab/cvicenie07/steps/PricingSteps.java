package sk.testlab.cvicenie07.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import sk.testlab.cvicenie07.domain.Delivery;
import sk.testlab.cvicenie07.support.ScenarioContext;

/** Tenké kroky: Gherkin dodá vstupy/očakávania, adaptéry vykonajú GUI alebo API operácie. */
public final class PricingSteps {
    private final ScenarioContext context;

    public PricingSteps(ScenarioContext context) { this.context = context; }

    @Given("používateľ má {word} v množstve {int}")
    public void product(String sku, int quantity) { context.addItem(sku, quantity); }

    @Given("používateľ má v košíku produkty:")
    public void products(DataTable table) {
        if (!table.row(0).equals(List.of("sku", "mnozstvo"))) {
            throw new IllegalArgumentException("DataTable musí mať hlavičku | sku | mnozstvo |.");
        }
        for (Map<String, String> row : table.asMaps()) {
            context.addItem(row.get("sku"), Integer.parseInt(row.get("mnozstvo")));
        }
    }

    @Given("používa kupón {word}")
    public void couponWord(String coupon) { context.coupon(coupon); }

    @Given("kupón je {string}")
    public void couponString(String coupon) { context.coupon(coupon); }

    @Given("nepoužíva kupón")
    public void noCoupon() { context.coupon(""); }

    @When("si zvolí kuriérske doručenie")
    public void courier() { context.calculate(Delivery.COURIER); }

    @When("si zvolí osobný odber")
    public void pickup() { context.calculate(Delivery.PICKUP); }

    @When("si zvolí doručenie {string}")
    public void delivery(String value) { context.calculate(Delivery.from(value)); }

    @Then("doprava stojí {int} centov")
    public void shipping(int expected) {
        compare("Cena dopravy", expected, context.actualQuote().shippingCents());
    }

    @Then("celková suma je {int} centov")
    public void total(int expected) {
        compare("Celková suma", expected, context.actualQuote().totalCents());
    }

    @Then("medzisúčet je {int} centov")
    public void subtotal(int expected) {
        compare("Medzisúčet", expected, context.actualQuote().subtotalCents());
    }

    @Then("zľava je {int} centov")
    public void discount(int expected) {
        compare("Zľava", expected, context.actualQuote().discountCents());
    }

    private void compare(String label, int expected, int actual) {
        Assertions.assertEquals(expected, actual,
            () -> label + " v centoch nesúhlasí s pravidlom v Gherkine.\n" + context.summary());
    }
}
