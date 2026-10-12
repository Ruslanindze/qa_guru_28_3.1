package qa.guru.test;

import com.codeborne.selenide.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.openqa.selenium.Keys;
import qa.guru.data.CalculatorTabRoles;
import qa.guru.helpers.TableUiHelper;

import java.util.Map;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class AlphaCalculatorTests {
    private static final String CSS_CALCULATOR = "[data-test-id='calculator-layout'] [role='tablist']";

    @BeforeAll
    static void setup() {
        Configuration.baseUrl = "https://alfabank.ru";
    }

    @ParameterizedTest(name = "Проверка состава калькулятора | {0}")
    @ValueSource(strings = {"Крупному бизнесу", "Малому бизнесу", "Для всех"})
    void shouldFullCompositionCalculator(String nameTab) {
        // Откроем главную страницу и раскрываем
        open("");
        WebDriverRunner.getWebDriver().manage().window().maximize();

        // Кликаем на вкладку внутри страницы
        $(byText(nameTab)).click();

        // Переместим фокус на калькулятор
        $(CSS_CALCULATOR).scrollTo();

        // Находим все табы и проверяем, что среди них есть наши 4 штуки (остальные игнорируются)
        $$("[role='tab']")
                .shouldHave(
                        CollectionCondition.containExactTextsCaseSensitive(CalculatorTabRoles.getAllTabText()));
    }

    @ParameterizedTest(name = "Проверка вкладок калькулятора | {0}")
    @EnumSource(CalculatorTabRoles.class)
    void shouldSuccessfullySwitchCalculatorTabs(CalculatorTabRoles tab) {
        // Откроем главную страницу и раскрываем
        open("");
        WebDriverRunner.getWebDriver().manage().window().maximize();

        // Переместим фокус на калькулятор
        $(CSS_CALCULATOR).scrollTo();

        // Клик на вкладку калькулятора
        $(CSS_CALCULATOR).$(byText(tab.getTabText())).click();

        // Область кнопок
        ElementsCollection buttonArea = $$("%s [class*='button__component']".formatted(tab.getButtonArea()));
        // Проверка, что такие кнопки существуют
        buttonArea.shouldHave(CollectionCondition.texts(tab.getExpectedButtonsText()));
        // Проверка, что каждый элемент кликабелен
        buttonArea.forEach(element -> element.shouldBe(Condition.interactable));
    }

    @ParameterizedTest(name = "Расчёт вклада: {0} на {1}")
    @CsvSource({
            "2500000, 3 месяца, 85000",
            "2500000, 6 месяцев, 165000",
            "2500000, 1 год, 325000"
    })
    void shouldSuccessfullyCalculationDeposit(int amount, String period, int income) {
        // Откроем главную страницу и раскрываем
        open("");
        WebDriverRunner.getWebDriver().manage().window().maximize();

        // Переместим фокус на калькулятор
        $(CSS_CALCULATOR).scrollTo();

        // Клик на вкладку калькулятора
        $(CSS_CALCULATOR).$(byText("Вклад")).click();

        // Вводим сумму
        var inputAmount = $("[aria-label='Сумма вклада']");
        inputAmount.click();
        inputAmount.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        inputAmount.sendKeys("%s".formatted(amount));
        // Вводим срок вклада
        $(byText(period)).closest("button").click();

        // Проверка, что без вывода процентов
        $(byText("Условия:")).closest("div")
                .$("input[type='checkbox']").shouldBe(Condition.selected);

        // Проверка дохода по вкладу
        SelenideElement tableRes = $(byText("Наше предложение")).closest("div").$("table");
        SelenideElement incomeEl = TableUiHelper.getCellByNames(tableRes, "Доход по вкладу", "Всем клиентам");
        int actualIncome = Integer.parseInt(incomeEl.getText().replaceAll("\\D", ""));
        assertThat(actualIncome)
                .as("Проверка дохода по вкладу") // Описание теста для отчетов
                .isEqualTo(income);

        // Проверяем ожидаемую сумму в конце вклада
        SelenideElement amountAtEndEl = TableUiHelper.getCellByNames(tableRes, "Сумма в конце срока", "Всем клиентам");
        int actualAmountAtEnd = Integer.parseInt(amountAtEndEl.getText().replaceAll("\\D", ""));
        assertThat(actualAmountAtEnd)
                .as("Проверка дохода по вкладу") // Описание теста для отчетов
                .isEqualTo(amount + income);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void shouldSuccessfullyCalculationAccount(String desc, Map<String, String> mapInput, Map<String, String> mapExpected) {
        // Откроем главную страницу и раскрываем
        open("");
        WebDriverRunner.getWebDriver().manage().window().maximize();

        // Переместим фокус на калькулятор и откроем Счёт
        $(CSS_CALCULATOR).scrollTo();
        $(CSS_CALCULATOR).$(byText("Счёт")).click();

        // Заполняем входные данные
        // Вводим сумму
        var inputAmount = $("[aria-label='Сумма счёта']");
        inputAmount.click();
        inputAmount.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        inputAmount.sendKeys(mapInput.get("Сумма счёта"));
        // Выберем вид счёта
        $(byText(mapInput.get("Вид счёта"))).closest("button").click();
        // Условия - категория клиента
        $$(byText(mapInput.get("Условия")))
                .findBy(Condition.visible)
                .closest("button")
                .click();
        // Траты в месяц
        $(byText(mapInput.get("Траты в месяц"))).closest("button").click();
        // По-умолчанию выставлен первый счёт
        $(byText("Мой первый счёт")).closest("label")
                .$("input[type='checkbox']").shouldBe(Condition.selected);

        // Достаём всю информацию с таблицы
        SelenideElement tableRes = $$(byText("Наше предложение"))
                .findBy(Condition.visible)
                .closest("div")
                .$("table");
        SelenideElement percentForPeriod_1_2 = TableUiHelper
                .getCellByNames(tableRes, "1-2 месяца", "Ставка, % годовых");
        SelenideElement percentForPeriod_from_3 = TableUiHelper
                .getCellByNames(tableRes, "с 3-го месяца", "Ставка, % годовых");
        SelenideElement incomeForPeriod_1_2 = TableUiHelper
                .getCellByNames(tableRes, "1-2 месяца", "Доход в месяц");
        SelenideElement incomeForPeriod_from_3 = TableUiHelper
                .getCellByNames(tableRes, "с 3-го месяца", "Доход в месяц");

        // Проверка итоговой таблицы
        // Проверка процентов по счёту
        assertThat(percentForPeriod_1_2.getText())
                .as("Проверка ставки на 1-2 месяц")
                .isEqualTo(mapExpected.get("1-2 месяца"));
        assertThat(percentForPeriod_from_3.getText())
                .as("Проверка ставки на с 3-го месяца")
                .isEqualTo(mapExpected.get("с 3-го месяца"));
        // Проверка доходов в месяц в период "1-2 месяца"
        double doublePercent_1_2 = Double
                .parseDouble(mapExpected.get("1-2 месяца")
                        .replace("%", "").replace(",", ".").trim()) / 100;
        int inputAmountAsInt = Integer.parseInt(mapInput.get("Сумма счёта").replaceAll("\\D", ""));
        int expectedIncome_1_2 = (int) (inputAmountAsInt * doublePercent_1_2 / 12);
        int actualIncome_1_2 = Integer.parseInt(incomeForPeriod_1_2.getText().replaceAll("\\D", ""));
        assertThat(actualIncome_1_2)
                .as("Проверка размера дохода в месяц за период <1-2 месяца>")
                .isEqualTo(expectedIncome_1_2);
        // Проверка доходов в месяц в период "с 3-го месяца"
        double doublePercent_3 = Double
                .parseDouble(mapExpected.get("с 3-го месяца")
                        .replace("%", "").replace(",", ".").trim()) / 100;
        int expectedIncome_3 = (int) (inputAmountAsInt * doublePercent_3 / 12);
        int actualIncome_3 = Integer.parseInt(incomeForPeriod_from_3.getText().replaceAll("\\D", ""));
        assertThat(actualIncome_3)
                .as("Проверка размера дохода в месяц за период <с 3-го месяца>")
                .isEqualTo(expectedIncome_3);
    }

    private static Stream<Arguments> shouldSuccessfullyCalculationAccount() {
        return Stream.of(
                Arguments.of(
                        "Расчёт счёта | мин.остаток для клиентов Alfa Only",
                        Map.of(
                                "Сумма счёта", "3000000",
                                "Вид счёта", "% на минимальный остаток",
                                "Условия", "Клиентам Alfa Only",
                                "Траты в месяц", "≥ 100 000 ₽"),
                        Map.of(
                                "1-2 месяца", "14%",
                                "с 3-го месяца", "12,5%"
                        )
                ),
                Arguments.of(
                        "Расчёт счёта | ежедн.остаток для клиентов Alfa Only",
                        Map.of(
                                "Сумма счёта", "6000000",
                                "Вид счёта", "% на ежедневный остаток",
                                "Условия", "Клиентам А-Клуба",
                                "Траты в месяц", "≥ 20 000 ₽"),
                        Map.of(
                                "1-2 месяца", "13%",
                                "с 3-го месяца", "12,5%"
                        )
                )
        );
    }

    @AfterEach
    void tearDown() {
        Selenide.closeWebDriver();
    }
}
