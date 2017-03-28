package utils;

import models.common.Country;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CountryUtils {

  private CountryUtils() {}

  /**
   * Utility to combine the final destination country and list of through countries, filtering out blanks and duplicates
   * @param finalDestinationCountry The final destination country
   * @param throughDestinationCountries The list of through countries
   * @return The combined list of both final and through destination countries, with any blanks or duplicates removed.
   * If the final destination country is blank, then the through destination countries are not included in the result.
   */
  public static List<String> getDestinationCountries(String finalDestinationCountry, List<String> throughDestinationCountries) {
    // List of unique countries from both finalDestinationCountry and throughDestinationCountries
    Set<String> seenCountries = new HashSet<>();

    // If there is no final destination, there should be no through destinations
    if (StringUtils.isNotBlank(finalDestinationCountry)) {
      seenCountries.add(finalDestinationCountry);

      // Filter out empty of blank countries (occurs when initialising the throughDestinationCountries dao)
      Stream<String> throughDestinationCountriesStream = throughDestinationCountries.stream()
          .filter(countryRef -> StringUtils.isNotBlank(countryRef) && seenCountries.add(countryRef));

      return Stream.concat(Stream.of(finalDestinationCountry), throughDestinationCountriesStream).collect(Collectors.toList());
    }
    else {
      return Collections.emptyList();
    }
  }

  /**
   * Returns filtered list of Country's, where we return Country's with a matching country ref.
   * @param allCountries Countries used to search for matched country refs
   * @param countryRefs Country refs
   * @return List country all matched Countries
   */
  public static List<Country> getFilteredCountries(List<Country> allCountries, List<String> countryRefs) {
    return getFilteredCountries(allCountries, countryRefs, false);
  }

  /**
   * Returns a filtered list of Country's. When negate is set to true, countryRefs will be removed from the
   * allCountries list. When false, only those countries which match a countryRef are returned.
   * @param allCountries Countries used to search for matched country refs
   * @param countryRefs Country refs
   * @param negate match
   * @return List of type Country, filtered as stated above.
   */
  public static List<Country> getFilteredCountries(List<Country> allCountries, List<String> countryRefs, boolean negate) {
    return countryRefs.stream()
        .flatMap(ref -> allCountries.stream().filter(c -> negate != c.getCountryRef().equals(ref)))
        .collect(Collectors.toList());
  }
}
