package co.caio.tablier.model;

import java.util.List;
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

  OptionalInt totalTime();

  List<String> ingredients();

  List<String> instructions();

  class Builder extends ImmutableRecipeInfo.Builder {}
}
