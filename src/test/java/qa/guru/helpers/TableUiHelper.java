package qa.guru.helpers;

import com.codeborne.selenide.SelenideElement;

import java.util.List;

public class TableUiHelper {

    public static SelenideElement getCellByNames(SelenideElement table, String rowName, String columnName) {
        List<String> rowsText = table.$$("tbody tr > td:first-child").texts();
        List<String> colsText = table.$$("thead > tr th").texts();

        int idxRow = -1;
        int idxColumn = -1;

        // Ищем индекс строки, очищая фронтовую грязь прямо в цикле
        for (int i = 0; i < rowsText.size(); i++) {
            String cleanFrontRow = TextUiCleaner.clean(rowsText.get(i));

            if (cleanFrontRow.equals(rowName)) {
                idxRow = i;
                break;
            }
        }

        // Ищем индекс колонки, очищая грязь из шапки таблицы
        for (int j = 0; j < colsText.size(); j++) {
            String cleanFrontCol = TextUiCleaner.clean(colsText.get(j));

            if (cleanFrontCol.equals(columnName)) {
                idxColumn = j;
                break;
            }
        }

        // Если что-то не нашли — кидаем понятную ошибку
        if (idxRow == -1) throw new AssertionError("Строка '" + rowName + "' не найдена после очистки фронта!");
        if (idxColumn == -1) throw new AssertionError("Колонка '" + columnName + "' не найдена после очистки фронта!");

        // Возвращаем точную ячейку из нужной строки
        return table.$$("tbody tr").get(idxRow).$$("td").get(idxColumn);
    }
}
