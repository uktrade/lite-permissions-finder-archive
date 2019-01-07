package components.cms.parser.enums;

public enum ListSheet {
    UK_MILITARY_LIST(1,"UK Military List"), // Column 2
    DUAL_USE_LIST(2,"Dual-Use List"); // Column 3

    private final int index;
    private final String friendlyText;

    ListSheet(int index, String friendlyText) {
        this.index = index;
        this.friendlyText = friendlyText;
    }

    public int getIndex() {
        return index;
    }

    public String getFriendlyText() {
        return friendlyText;
    }
}
