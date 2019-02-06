package utils;

public class ListNameToFriendlyNameUtil {

    /**
     * Converts a sheet name (eg UK_MILITARY_LIST) to a friendly version (eg UK Military List)
     *
     * @param listName (eg UK_MILITARY_LIST)
     * @return the friendly name of the list (eg UK Military List)
     */
    public static String getFriendlyNameFromListName(String listName) {
        switch (listName) {
            case "UK_MILITARY_LIST":
                return "UK Military List";
            case "DUAL_USE_LIST":
                return "Dual-Use List";
        }
        return null;
    }

}
