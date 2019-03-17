package co.caio.tablier.model;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import org.immutables.value.Value;

@ImmutableStyle
@Value.Immutable
public interface RecipeInfo {

  String name();

  String siteName();

  String goUrl();

  String crawlUrl();

  String infoUrl();

  int numIngredients();

  OptionalInt calories();

  OptionalDouble fatContent();

  OptionalDouble carbohydrateContent();

  OptionalDouble proteinContent();

  OptionalInt prepTime();

  OptionalInt cookTime();

  OptionalInt totalTime();

  List<String> ingredients();

  class Builder extends ImmutableRecipeInfo.Builder {}
}
