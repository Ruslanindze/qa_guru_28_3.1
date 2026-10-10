package qa.guru.helpers;

import com.codeborne.selenide.SelenideElement;

public class TableUiHelper {

    public static SelenideElement getCellByNames(SelenideElement table, String rowName, String columnName) {
        int idxRow = table.$$("tbody tr > td:first-child")
                .texts().indexOf(rowName);
        int idxColumn = table.$$("thead > tr th")
                .texts().indexOf(columnName);

        return table.$$("tbody tr").get(idxRow)
                .$$("td").get(idxColumn);
    }
}
