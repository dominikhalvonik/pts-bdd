@cvicenie07 @referencny @AC_CENA_05
Feature: Referenčná cena košíka TestLab Shop
  Cena zodpovedá akceptačným kritériám používaným v predošlých cvičeniach.

  Scenario: Dve klávesnice a myš s kupónom a kuriérom
    Given používateľ má v košíku produkty:
      | sku    | mnozstvo |
      | KB-001 | 2        |
      | MS-001 | 1        |
    And používa kupón STUDENT10
    When si zvolí kuriérske doručenie
    Then medzisúčet je 8970 centov
    And zľava je 897 centov
    And doprava stojí 490 centov
    And celková suma je 8563 centov
