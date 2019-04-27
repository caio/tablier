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

  @Value.Derived
  default boolean hasDurationData() {
    // Only need to check totalTime by default since without it
    // the other *Time() metadata would sound weird
    return totalTime().isPresent();
  }

  @Value.Derived
  default boolean hasNutritionData() {
    return calories().isPresent()
        || fatContent().isPresent()
        || proteinContent().isPresent()
        || carbohydrateContent().isPresent();
  }

  List<SimilarInfo> similarRecipes();

  @ImmutableStyle
  @Value.Immutable
  abstract class SimilarInfo {
    @Value.Parameter
    public abstract String name();

    @Value.Parameter
    public abstract String infoUrl();

    public static SimilarInfo of(String name, String infoUrl) {
      return ImmutableSimilarInfo.of(name, infoUrl);
    }
  }

  class Builder extends ImmutableRecipeInfo.Builder {}
}
