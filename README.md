# TestLab Shop · Cvičenie 07

## BDD, Gherkin a Cucumber

Kompletný Maven projekt k prezentácii **07_BDD_Gherkin_Cucumber.pptx**. Zrozumiteľné akceptačné scenáre prepája s Java kódom a so spustenou aplikáciou TestLab Shop 1.0.0. Predvolene používa Selenium. Prepínačom možno rovnaké scenáre vykonať cez REST Assured.

Hlavným pravidlom je **kuriér zdarma od 100 € po zľave**. Projekt obsahuje sedem spustiteľných príkladov v troch `.feature` súboroch: päť pre dopravu, jeden pre zaokrúhlenie zľavy a jeden pre referenčný košík. Súčasťou je `Scenario Outline`, `Examples`, vstupná `DataTable`, zdieľaný stav jedného scenára, hooks a reporty.

Testy vyžiadajú cenovú ponuku. Nevytvárajú objednávky a neodpočítavajú sklad. **Spustený TestLab Shop s PHP a MariaDB musí byť dostupný samostatne**; tento ZIP je testovací projekt.

## Rýchle spustenie vo Windows

1. Rozbaľte celý ZIP. Pracujte v priečinku, kde je `pom.xml` a `mvnw.cmd`.
2. Nainštalujte **JDK 21** a Chrome, Edge alebo Firefox. `java -version` má ukazovať Java 21.
3. Spustite už pripravený TestLab Shop. V bežnom prehliadači overte, že sa dá prihlásiť a vypočítať košík.
4. V `config/test.properties` nastavte adresu aplikácie a svoj testovací účet.
5. Spustite najprv jeden scenár:

```powershell
.\mvnw.cmd test "-Dcucumber.filter.tags=@smoke"
```

Celá sada cez webové GUI:

```powershell
.\mvnw.cmd clean test
```

Rovnaká sada cez API:

```powershell
.\mvnw.cmd clean test "-Dtest.layer=api"
```

**Maven netreba inštalovať ani pridávať do PATH.** Priložený Maven Wrapper stiahne svoju pripnutú distribúciu a závislosti pri prvom použití. Prvé spustenie preto potrebuje internet. Wrapper nenahrádza JDK. Pri prvom GUI behu môže Selenium Manager potrebovať prístup na internet aj na získanie ovládača prehliadača.

Na Linuxe/macOS použite `./mvnw` namiesto `.\mvnw.cmd`. Ak po rozbalení chýba právo spúšťania, vykonajte `chmod +x mvnw`.

## Spustenie v IntelliJ IDEA

1. **File → Open** a vyberte `pom.xml`; otvorte ho ako projekt.
2. Nastavte **Project SDK = JDK 21**. Aj **Maven → Runner → JRE** musí používať JDK 21.
3. V okne Maven použite **Reload All Maven Projects** a počkajte na stiahnutie závislostí.
4. Upravte `config/test.properties`.
5. Spustite `src/test/java/sk/testlab/cvicenie07/RunCucumberTest.java` cez zelenú šípku pri triede. Alternatívne v Maven okne spustite cieľ `test`.

Pracovný priečinok run konfigurácie musí byť koreň projektu s `pom.xml`. Pre API variant pridajte do **VM options** `-Dtest.layer=api`. Rovnaké VM options podporujú aj `-Dcucumber.filter.tags=@smoke`.

Plugin **Cucumber for Java** je užitočný na zvýraznenie `.feature` a navigáciu na kroky. Základné spustenie cez Maven alebo triedu `RunCucumberTest` od neho nezávisí. Pre prvé spustenie používajte práve túto triedu; Java metóda označená `@Given` alebo `@Then` nie je samostatný JUnit test.

## Konfigurácia

```properties
base.url=http://127.0.0.1:8080/
test.email=student@example.test
test.password=Student123!
test.layer=gui
browser=chrome
headless=false
timeout.seconds=30
driver.path=
browser.binary=
```

`base.url` je adresa koreňa webovej aplikácie, prípadne jej podpriečinka, napríklad `https://server.sk/testlab-shop/`. Nevkladajte `#/login`, `api/index.php` ani JDBC adresu. Podporované hodnoty `test.layer` sú `gui` a `api`; `browser` môže byť `chrome`, `edge` alebo `firefox`.

Poradie nastavení je **Java parameter `-D` → premenná prostredia → konfiguračný súbor**. Zodpovedajúce premenné: `BASE_URL`, `TEST_EMAIL`, `TEST_PASSWORD`, `TEST_LAYER`, `BROWSER`, `HEADLESS`, `TIMEOUT_SECONDS`, `DRIVER_PATH`, `BROWSER_BINARY`. Na lokálne cesty vo Windows používajte v properties súbore lomky `/`, napríklad `C:/tools/chromedriver.exe`.

Vlastné prihlasovacie údaje môžete držať v kópii `config/test.local.properties`, ktorú Git ignoruje. Vyberiete ju parametrom `"-Dtest.config=config/test.local.properties"`. Tento parameter vyberá celý súbor; lokálna kópia má preto obsahovať aj ostatné nastavenia z pôvodného súboru.

Pre skutočné heslo použite tento lokálny súbor alebo premennú `TEST_PASSWORD`. Heslo nedávajte do príkazu `-Dtest.password=...`, kde by zostalo v histórii terminálu, argumentoch procesu a pri zlyhaní aj v systémových properties Surefire XML reportu. API klient nevypisuje prihlasovacie telá ani bearer tokeny.

Pre cvičenie je vhodný vlastný účet v priestore `run-*`, ktorý vyučujúci vytvorí v pôvodnom PHP projekte príkazom `php bin/fixtures.php create`. Prihlasovacie údaje z jeho výstupu prepíšte do konfigurácie. Môžete použiť aj pridelený účet z predchádzajúceho cvičenia, ak má nezmenené ceny a dostatok zásob. Demo účet v príklade slúži na rýchle lokálne spustenie a zdieľa sklad s ďalšími demo používateľmi.

Každý scenár dostane vlastnú reláciu prehliadača a prázdny košík, alebo vlastný API token a vlastné vstupné dáta. Serverový pracovný priestor sa pritom automaticky nevytvára ani nemaže. GUI sa na konci zatvorí; API variant odhlási svoj token. Priame JDBC pripojenie, databázové heslo ani administrátorský `X-Test-Key` tento projekt nepotrebuje.

## Čo čítať a meniť

| Súbor alebo priečinok | Úloha |
|---|---|
| `src/test/resources/features/` | Spustiteľné pravidlá a očakávané výsledky |
| `src/test/java/sk/testlab/cvicenie07/RunCucumberTest.java` | Vstupný bod pre JUnit Platform |
| `src/test/java/sk/testlab/cvicenie07/` | Implementácie krokov, stav scenára, GUI/API vrstva a podpora |
| `config/test.properties` | Lokálne prostredie a účet |
| `docs/PRAVIDLA_A_SCENARE.md` | Väzba pravidiel na scenáre a vysvetlenie výsledkov |
| `docs/PRIEBEH_CVICENIA.md` | Postup pre vyučujúceho a študentov |
| `zadanie/` | Študentské zadanie a šablóna ďalšieho scenára |
| `docs/SPRAVNA_A_CHYBNA_VERZIA.md` | Overenie pomocou riadených chýb aplikácie |
| `docs/RIESENIE_PROBLEMOV.md` | Najčastejšie problémy so spustením |
| `docs/OVERENIE.md` | Rozsah overenia dodaného balíka |

Očakávané ceny sú uvedené priamo v Gherkin scenároch a vychádzajú zo špecifikácie. Test si ich nevypočítava z aktuálnej odpovede servera. Technické kroky v prehliadači a HTTP požiadavky zostávajú v Jave.

## Užitočné príkazy

```powershell
# Iba príklad z prezentácie.
.\mvnw.cmd test "-Dcucumber.filter.tags=@smoke"

# Tri príklady Scenario Outline.
.\mvnw.cmd test "-Dcucumber.filter.tags=@hranica"

# Zaokrúhľovanie cez API.
.\mvnw.cmd test "-Dtest.layer=api" "-Dcucumber.filter.tags=@zaokruhlenie"

# GUI bez viditeľného okna prehliadača.
.\mvnw.cmd test "-Dheadless=true"

# Overenie napojenia Gherkin krokov bez pripojenia k aplikácii.
.\mvnw.cmd test "-Dcucumber.execution.dry-run=true"
```

Dry run kontroluje objavenie scenárov a definície krokov. Nevykoná hooks ani akceptačné kontroly, preto nenahrádza živý test.

## Výsledky

Po behu otvorte **`target/cucumber-report.html`**. Report obsahuje názvy scenárov, kroky, chyby a pri dostupnom GUI aj snímku zlyhania. Strojovo čitateľné výstupy sú `target/cucumber-report.json`, `target/cucumber-report.xml` a `target/surefire-reports/`. Súbory diagnostiky zlyhaní sú pod `target/artifacts/`.

Nový beh prepíše spoločné Cucumber reporty; `clean` vymaže celý `target`. Pred porovnaním správnej a chybnej verzie si potrebné výstupy skopírujte mimo `target`.

Výsledok musí hodnotiť aj počet vykonaných scenárov. Predvolená sada obsahuje sedem príkladov pre jednu vybranú vrstvu. Úspešný dry run alebo prázdny výber značiek nie je dôkaz správnosti aplikácie.

## Nadväznosť na predošlé cvičenia

Selenium časť používa Page Objecty a explicitné čakanie z cvičenia 03. API variant nadväzuje na REST Assured z cvičenia 04. Pribúda spustiteľná špecifikácia a oddelenie business viet od konkrétneho prístupu k aplikácii. Verzie závislostí sú pripnuté v `pom.xml`; JUnit BOM drží Jupiter a Platform v jednej kompatibilnej sade.

| Súčasť | Pripnutá verzia |
|---|---|
| Java | 21 |
| Maven cez Wrapper | 3.9.9 |
| Cucumber-JVM | 7.22.2 |
| JUnit Jupiter / JUnit Platform | 5.12.2 / 1.12.2 |
| Selenium | 4.49.0 |
| REST Assured | 5.5.7 |
| Maven Surefire | 3.5.4 |

