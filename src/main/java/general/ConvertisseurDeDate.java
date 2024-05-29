package general;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ConvertisseurDeDate {

    public static Date localDateTime2UtilDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static java.sql.Date localDate2SQLDate(LocalDateTime date) {
        return java.sql.Date.valueOf(date.format(DateTimeFormatter.ISO_DATE));
    }

    public static LocalDateTime utilDate2LocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDate sqlDate2LocalDate(java.sql.Date date) {
//        Instant instant = Instant.ofEpochMilli(date.getTime());
//        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        return date.toLocalDate();
    }

}
