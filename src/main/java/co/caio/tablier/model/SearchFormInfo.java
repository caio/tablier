package co.caio.tablier.model;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Style(visibility = ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface SearchFormInfo {

  @Value.Default
  default String title() {
    return "Search Recipes";
  }

  @Value.Default
  default String subtitle() {
    return "Over a million delicious recipes, zero ads.";
  }

  @Value.Default
  default String value() {
    return "";
  }

  @Value.Default
  default String buttonText() {
    return "Search";
  }

  @Value.Default
  default String placeholder() {
    return "Ingredients, diets, brands, etc";
  }

  @Value.Default
  default boolean isAutoFocus() {
    return true;
  }

  class Builder extends ImmutableSearchFormInfo.Builder {}
}
