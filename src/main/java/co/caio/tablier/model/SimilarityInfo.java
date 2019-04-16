package co.caio.tablier.model;

import java.util.List;
import org.immutables.value.Value;

@ImmutableStyle
@Value.Immutable
public interface SimilarityInfo {
  RecipeInfo recipe();

  List<RecipeInfo> similar();

  class Builder extends ImmutableSimilarityInfo.Builder {}
}
