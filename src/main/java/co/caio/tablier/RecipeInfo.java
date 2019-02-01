package co.caio.tablier;

import java.util.OptionalInt;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Style(visibility = ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface RecipeInfo {

  String name();

  String siteName();

  String crawlUrl();

  int numIngredients();

  OptionalInt calories();

  OptionalInt totalTime();

  class Builder extends ImmutableRecipeInfo.Builder {}
}
