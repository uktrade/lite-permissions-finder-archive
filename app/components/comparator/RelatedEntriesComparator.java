package components.comparator;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RelatedEntriesComparator implements Comparator<String> {

  public int compare(String string1, String string2) {

    if (Character.isAlphabetic(string1.charAt(0)) && Character.isAlphabetic(string2.charAt(0))) {
      List<CharGroup> list = getCharGroup(string1, string2);

      if (list.get(0).group1.equals(list.get(1).group1)) {
        if (NumberUtils.isCreatable(list.get(0).group2) && NumberUtils.isCreatable(list.get(1).group2)) {
          return Long.compare(Long.valueOf(list.get(0).group2), Long.valueOf(list.get(1).group2));
        }
      }
      if (!list.get(0).group1.equals(list.get(1).group1)) {
        return string1.compareToIgnoreCase(string2);
      }
    }
    return string1.compareTo(string2);
  }

  public List<CharGroup> getCharGroup(String s1, String s2) {
    CharGroup cg1 = createCharGroup(s1);
    CharGroup cg2 = createCharGroup(s2);

    return Arrays.asList(cg1, cg2);
  }

  /**
   * example code ML1a1
   * group1 ML
   * group2 1a1
   */
  public CharGroup createCharGroup(String s) {
    String group1 = null;
    String group2 = null;
    char[] ch = s.toCharArray();
    for (int i = 0; i < ch.length; i++) {
      if (Character.isDigit(ch[i])) {
        group2 = s.substring(i);
        break;
      }
      group1 = group1 + ch[i];
    }
    return new CharGroup(group1, group2);
  }

  class CharGroup {

    private String group1;
    private String group2;

    public CharGroup(String group1, String group2) {
      this.group1 = group1;
      this.group2 = group2;
    }

    public String getGroup1() {
      return group1;
    }

    public String getGroup2() {
      return group2;
    }
  }

}