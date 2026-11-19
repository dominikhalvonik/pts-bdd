@cvicenie07 @zaokruhlenie @AC_CENA_03
Feature: Zaokrúhlenie zľavy na celé centy
  Zľava STUDENT10 je 10 percent z medzisúčtu.
  Zaokrúhľuje sa jedenkrát na konci spôsobom half-up.

  Scenario: Pol centa sa pri výpočte zľavy zaokrúhli nahor
    Given používateľ má PAD-001 v množstve 1
    And používa kupón STUDENT10
    When si zvolí osobný odber
    Then medzisúčet je 995 centov
    And zľava je 100 centov
    And doprava stojí 0 centov
    And celková suma je 895 centov
