package components.comparator;

import java.util.Comparator;

public class RelatedEntriesComparator implements Comparator<String> {

  public int compare(String string1, String string2) {

    String[] arr1 = string1.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
    String[] arr2 = string2.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

    for (int i = 0; i < Math.min(arr1.length, arr2.length); i++) {

      if (isDigit(arr1[i]) && isDigit(arr2[i])) {
        if (arr1[i].equals(arr2[i])) {
          continue;
        }
        if (!arr1[i].equals(arr2[i])) {
          return Long.compare(Long.valueOf(arr1[i]), Long.valueOf(arr2[i]));
        }
      }

      if (isLetter(arr1[i]) && isLetter(arr2[i])) {
        if (arr1[i].equals(arr2[i])) {
          continue;
        }
        if (!arr1[i].equals(arr2[i])) {
          return arr1[i].compareToIgnoreCase(arr2[i]);
        }
      }
    }
    return string1.compareTo(string2);
  }

  private static boolean isDigit(String s) {
    String regex = "^[0-9].*";
    return s.matches(regex);
  }

  private static boolean isLetter(String s) {
    String regex = "^[a-zA-Z].*";
    return s.matches(regex);
  }

}