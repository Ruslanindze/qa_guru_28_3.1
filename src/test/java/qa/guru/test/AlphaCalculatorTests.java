package qa.guru.test;

import com.codeborne.selenide.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.openqa.selenium.Keys;
import qa.guru.data.CalcTabRoles;
import qa.guru.helpers.TableUiHelper;

import java.util.stream.Stream;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selectors.withText;
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
                        CollectionCondition.containExactTextsCaseSensitive(CalcTabRoles.getAllTabText()));
    }

    @ParameterizedTest(name = "Проверка вкладок калькулятора | {0}")
    @EnumSource(CalcTabRoles.class)
    void shouldSuccessfullySwitchCalculatorTabs(CalcTabRoles tab) {
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
        $(withText(period)).closest("button").click();

        // Проверка, что без вывода процентов
        $(withText("Условия:")).closest("div")
                .$("input[type='checkbox']").shouldBe(Condition.selected);

        // Проверка дохода по вкладу
        SelenideElement tableRes = $(withText("Наше предложение")).closest("div").$("table");
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

    @ParameterizedTest
    @MethodSource
    void shouldSuccessfullyCalculationAccount(String st) {

    }

    private static Stream<Arguments> test() {
        return Stream.of(
                Arguments.of("")
        );
    }
}
