@cvicenie07 @cena_dopravy @AC_CENA_04
Feature: Cena dopravy po uplatnení zľavy
  Kuriér je zdarma od 10000 centov za produkty po zľave.
  Pod touto hranicou stojí 490 centov. Osobný odber je zdarma.
  Pravidlo: docs/PRAVIDLA_A_SCENARE.md, AC_CENA_04.

  Rule: Hranica bezplatného kuriéra sa posudzuje po zľave

    @smoke
    Scenario: Hranica sa posudzuje po zľave
      Given používateľ má BAG-001 v množstve 4
      And používa kupón STUDENT10
      When si zvolí kuriérske doručenie
      Then doprava stojí 490 centov
      And celková suma je 9490 centov

    @hranica
    Scenario Outline: Cena dopravy pre príklad <priklad>
      Given používateľ má BAG-001 v množstve 4
      And kupón je "<kupon>"
      When si zvolí doručenie "<dorucenie>"
      Then doprava stojí <doprava> centov
      And celková suma je <celkom> centov

      # Prázdna bunka kupon znamená bez kupónu v kroku vznikne prázdny reťazec "".
      Examples:
        | priklad                      | kupon     | dorucenie | doprava | celkom |
        | presná hranica bez zľavy      |           | courier   | 0       | 10000  |
        | pokles pod hranicu po zľave   | STUDENT10 | courier   | 490     | 9490   |
        | osobný odber po zľave         | STUDENT10 | pickup    | 0       | 9000   |

    @pod_hranicou
    Scenario: Pod hranicou bez zľavy sa platí kuriér
      Given používateľ má BAG-001 v množstve 3
      And nepoužíva kupón
      When si zvolí kuriérske doručenie
      Then medzisúčet je 7500 centov
      And zľava je 0 centov
      And doprava stojí 490 centov
      And celková suma je 7990 centov
