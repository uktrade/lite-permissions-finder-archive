package components.comparator;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RelatedEntriesComparatorTest {

  @Test
  public void sortControlCode1() {

    List<String> listToSort = Arrays.asList("ML3", "ML16", "ML12", "ML4", "1B001", "ML1b", "ML2", "ML1", "ML1a", "1A0a1",
        "PL9001");

    List<String> expected = Arrays.asList("1A0a1", "1B001", "ML1", "ML1a", "ML1b", "ML2", "ML3", "ML4", "ML12", "ML16",
        "PL9001");

    Collections.sort(listToSort, new RelatedEntriesComparator());
    Assert.assertEquals(expected, listToSort);
  }

  @Test
  public void sortControlCode2() {

    List<String> listToSort = Arrays.asList("ML2", "ML1a1");
    List<String> expected = Arrays.asList("ML1a1", "ML2");

    Collections.sort(listToSort, new RelatedEntriesComparator());
    Assert.assertEquals(expected, listToSort);
  }

  @Test
  public void sortControlCode3() {

    List<String> listToSort = Arrays.asList("PL9001", "ML1");
    List<String> expected = Arrays.asList("ML1", "PL9001");

    Collections.sort(listToSort, new RelatedEntriesComparator());
    Assert.assertEquals(expected, listToSort);
  }

}
