package qa.guru.helpers;

public class TextUiCleaner {

    /**
     * Отмывает строку от фронтовой грязи: неразрывных пробелов,
     * двойных пробелов и длинных тире.
     */
    public static String clean(String dirtyText) {
        if (dirtyText == null) {
            return "";
        }
        return dirtyText
                .replace(" ", " ")               // Заменяем неразрывный пробел (юникод) на обычный
                .replaceAll("[\\s\\u00A0]+", " ") // Схлопываем множественные пробелы в один
                .replaceAll("[–—]", "-")         // Переводим среднее и длинное тире в обычный дефис
                .trim();                         // Обрезаем пробелы по краям
    }
}
