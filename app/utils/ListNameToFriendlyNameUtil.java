package utils;

public class ListNameToFriendlyNameUtil {

    public static String GetFriendlyNameFromListName(String listName) {
        switch (listName) {
            case "UK_MILITARY_LIST":
                return "UK Military List";
            case "DUAL_USE_LIST":
                return "Dual-Use List";
        }
        return null;
    }

}
