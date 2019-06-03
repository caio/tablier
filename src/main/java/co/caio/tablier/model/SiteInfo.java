package co.caio.tablier.model;

import java.util.Map;
import org.immutables.value.Value;

@ImmutableStyle
@Value.Immutable
public interface SiteInfo {

  String title();

  Map<String, String> extraSearchParams();

  @Value.Default
  default String description() {
    return "Search over a million recipes. Filter by ingredients, nutritional value, diet (Low-Carb, Vegetarian, Vegan, Keto, Paleo) and more!";
  }

  @Value.Default
  default boolean isUnstable() {
    return false;
  }

  @Value.Default
  default String searchValue() {
    return "";
  }

  @Value.Default
  default boolean searchIsAutoFocus() {
    return true;
  }

  class Builder extends ImmutableSiteInfo.Builder {}
}
