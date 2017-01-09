package utils;

import models.common.Country;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CountryUtils {

  /**
   * Utility to combine the final destination country and list of through countries
   * @param finalDestinationCountry The final destination country
   * @param throughDestinationCountries The list of through countries
   * @return The combined list of both final and through destination countries, with any blanks removed. If the final
   * destination country is blank, then the through destination countries are not included in the result.
   */
  public static List<String> getDestinationCountries(String finalDestinationCountry, List<String> throughDestinationCountries) {
    List<String> destinationCountries = new ArrayList<>();

    // If there is no final destination, there should be no through destinations
    if (StringUtils.isNotBlank(finalDestinationCountry)) {
      destinationCountries.add(finalDestinationCountry);

      // Filter out empty of blank countries (occurs when initialising the throughDestinationCountries dao)
      destinationCountries.addAll(
          throughDestinationCountries.stream()
              .filter(countryRef -> StringUtils.isNotBlank(countryRef))
              .collect(Collectors.toList())
      );
    }

    return destinationCountries;
  }

  /**
   * Returns filtered list of Country's, where we find matched country ref
   * @param allCountries Countries used to search for matched country refs
   * @param countryRefs Country refs
   * @return List country all matched Countries
   */
  public static List<Country> getFilteredCountries(List<Country> allCountries, List<String> countryRefs) {
    return countryRefs.stream().flatMap(ref -> allCountries.stream().filter(c -> c.getCountryRef().equals(ref))).collect(Collectors.toList());
  }
}
