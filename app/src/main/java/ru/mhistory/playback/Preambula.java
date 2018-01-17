package ru.mhistory.playback;

import ru.mhistory.providers.SearchConf;

/**
 * Created by shcherbakov on 30.11.2017.
 */

public class Preambula {

    public static String get(float distance, float angle) {
        StringBuilder str = new StringBuilder();
        if (angle >= -45 && angle < 45) str.append("Прямо. ");
        else if (angle >= 45 && angle < 135) str.append("Слева. ");
        else if (angle >= -135 && angle < -45) str.append("Справа. ");
        else str.append("Позади. ");
        str.append(" ");
        if (distance < 100) {
            str.append(distanseToStr(0));
        } else if (distance < 1000) {
            int id=Math.round(distance / 100);
            str.append(distanseToStr(id));
        } else if (distance < 10000) {
            int id=Math.round(distance / 1000);
            str.append(distanseToStr(9+id));
        } else if (distance < 100000){
            int id=Math.round(distance / 5000);
            str.append(distanseToStr(17+id));
        }
        return str.toString();
    }
    public static String get(float distance, float angle,SearchConf conf){
        StringBuilder str = new StringBuilder();
        if (angle >= -1*conf.deltaAngleZona2 && angle < conf.deltaAngleZona2) str.append("Прямо. ");
        else if (angle >= conf.deltaAngleZona2 && angle < conf.deltaAngleZona3) str.append("Слева. ");
        else if (angle >= -1*conf.deltaAngleZona3 && angle < -1*conf.deltaAngleZona2) str.append("Справа. ");
        else str.append("Позади. ");
        str.append(" ");
        if (distance < 100) {
            str.append(distanseToStr(0));
        } else if (distance < 1000) {
            int id=Math.round(distance / 100);
            str.append(distanseToStr(id));
        } else if (distance < 10000) {
            int id=Math.round(distance / 1000);
            str.append(distanseToStr(9+id));
        } else if (distance < 100000){
            int id=Math.round(distance / 5000);
            str.append(distanseToStr(17+id));
        }
        return str.toString();
    }
    private static String distanseToStr(int distanceId) {
        String[] str = {"рядом с нами", "в ста метрах", "в двухстах метрах", "в трёхстах метрах", "в четырёхстах метрах",
                "в пятистах метрах", "в шестистах метрах", "в семистах метрах", "в восьмистах метрах", "в девятистах метрах",
                "в километре", "в двух километрах", "в трёх километрах", "в четырёх километрах", "в пяти километрах",
                "в шести километрах","в семи километрах", "в восьми километрах", "в девяти километрах", "в десяти километрах",
                "в пятнадцати километрах", "в двадцати километрах", "в двадцати пяти километрах", "в тридцати километрах","в тридцати пяти километрах",
                "в сорока километрах", "в сорока пяти километрах", "в пятидесяти километрах", "в пятидесяти пяти километрах",
                "в шестидесяти километрах", "в шестидесяти пяти километрах", "в семидесяти километрах", "в семидесяти пяти километрах",
                "в восьмидесяти километрах", "в восьмидесяти пяти километрах","в девяноста километрах", "в девяноста пяти километрах", " в ста километрах"};
        return str[distanceId];
    }
    public static String distanseToStrShort(int distanceId) {
        String[] str = {"рядом с нами", "100 м", "200 м", "300 м", "400 м",
                "500 м", "600м", "700", "в восьмистах метрах", "в девятистах метрах",
                "в километре", "в двух километрах", "в трёх километрах", "в четырёх километрах", "в пяти километрах",
                "в шести километрах","в семи километрах", "в восьми километрах", "в девяти километрах", "в десяти километрах",
                "в пятнадцати километрах", "в двадцати километрах", "в двадцати пяти километрах", "в тридцати километрах","в тридцати пяти километрах",
                "в сорока километрах", "в сорока пяти километрах", "в пятидесяти километрах", "в пятидесяти пяти километрах",
                "в шестидесяти километрах", "в шестидесяти пяти километрах", "в семидесяти километрах", "в семидесяти пяти километрах",
                "в восьмидесяти километрах", "в восьмидесяти пяти километрах","в девяноста километрах", "в девяноста пяти километрах", " в ста километрах"};
        return str[distanceId];
    }
}
