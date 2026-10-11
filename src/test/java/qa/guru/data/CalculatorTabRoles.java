package qa.guru.data;


import java.util.List;
import java.util.stream.Stream;

public enum CalculatorTabRoles {
    DEPOSIT("Вклад", "#calculator",
            List.of("Подробные условия", "Открыть вклад")),
    SCORE("Счёт", "[data-test-id='PremiumSavingsAccountCalculator']",
            List.of("Подробные условия", "Открыть счёт")),
    CREDIT("Кредит", "[data-test-id='PilAndPledge']",
            List.of("Получить деньги")),
    MORTGAGE("Ипотека", "[data-test-id='mortgage-calculator']",
            List.of("График платежей", "Заполнить заявку"));

    // Поля
    private final String tabText;
    private final String buttonArea;
    private final List<String> expectedButtonsText;

    // Конструктор
    CalculatorTabRoles(String tabText, String ccsButtonArea, List<String> buttonsText) {
        this.tabText = tabText;
        this.buttonArea = ccsButtonArea;
        this.expectedButtonsText = buttonsText;
    }

    // Геттеры
    public String getTabText() {
        return this.tabText;
    }

    public String getButtonArea() {
        return this.buttonArea;
    }

    public List<String> getExpectedButtonsText() {
        return this.expectedButtonsText;
    }

    // Возвращаем списком все вкладки
    public static List<String> getAllTabText() {
        return Stream.of(values()).map(CalculatorTabRoles::getTabText).toList();
    }

    // Переопределяем toString() для JUnit 5
    @Override
    public String toString() {
        return this.tabText; // Возвращаем первое поле
    }
}
