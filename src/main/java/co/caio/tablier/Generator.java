package co.caio.tablier;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import co.caio.tablier.model.ErrorInfo;
import co.caio.tablier.model.FilterInfo;
import co.caio.tablier.model.RecipeInfo;
import co.caio.tablier.model.SearchResultsInfo;
import co.caio.tablier.model.SidebarInfo;
import co.caio.tablier.model.SiteInfo;
import co.caio.tablier.view.Error;
import co.caio.tablier.view.Index;
import co.caio.tablier.view.Recipe;
import co.caio.tablier.view.Search;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.RockerRuntime;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Generator {

  private static final Path outputDir = Path.of("src/");

  private static final Map<String, SiteInfo> siteVariations;
  private static final Map<String, SearchResultsInfo> searchResultsVariations;
  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    siteVariations =
        Map.of(
            "",
            new SiteInfo.Builder().title("Search").searchValue("trololo").build(),
            "_unstable",
            new SiteInfo.Builder().title("Index").isUnstable(true).build(),
            "_nofocus",
            new SiteInfo.Builder().title("Title").searchIsAutoFocus(false).build());

    var filters =
        List.of(
            new FilterInfo.Builder()
                .showCounts(false)
                .isRemovable(false)
                .name("Limit Ingredients")
                .addOption("Less than 5", "#", true)
                .addOption("6 to 10", "#", 22)
                .addOption("More than 10", "#", 4)
                .build(),
            new FilterInfo.Builder()
                .name("Limit Cook Time")
                .addOption("Up to 15 minutes", "#", 7)
                .addOption("15 to 30 minutes", "#", 29, true)
                .addOption("30 to 60 minutes", "#", 11)
                .addOption("One hour or longer", "#", 2)
                .build(),
            new FilterInfo.Builder()
                .name("Limit Nutrition (per serving)")
                .addOption("Up to 200 kcal", "#", 2)
                .addOption("Up to 500 kcal", "#", 29)
                .addOption("Up to 10g of Fat", "#", 11)
                .addOption("Up to 30g of Carbs", "#", 23)
                .build());

    var sidebar = new SidebarInfo.Builder().showCounts(true).addAllFilters(filters).build();

    var srs = new HashMap<String, SearchResultsInfo>();

    var srSinglePage =
        new SearchResultsInfo.Builder()
            .paginationStart(1)
            .paginationEnd(12)
            .numMatching(12)
            .addAllRecipes(samples(12))
            .sidebar(sidebar)
            .build();
    srs.put("", srSinglePage);

    var srHasNext =
        new SearchResultsInfo.Builder()
            .paginationStart(1)
            .paginationEnd(20)
            .numMatching(21)
            .numAppliedFilters(1)
            .nextPageHref("/next")
            .addAllRecipes(samples(20))
            .sidebar(sidebar)
            .build();
    srs.put("_next", srHasNext);

    var srHasPrevious =
        new SearchResultsInfo.Builder()
            .paginationStart(21)
            .paginationEnd(21)
            .numMatching(21)
            .previousPageHref("/previous")
            .addAllRecipes(samples(1))
            .sidebar(sidebar)
            .build();
    srs.put("_prev", srHasPrevious);

    var srHasBoth =
        new SearchResultsInfo.Builder()
            .paginationStart(41)
            .paginationEnd(60)
            .numMatching(99)
            .numAppliedFilters(4)
            .nextPageHref("/next")
            .previousPageHref("/previous")
            .addAllRecipes(samples(20))
            .sidebar(sidebar)
            .build();
    srs.put("_both", srHasBoth);

    var srEmpty =
        new SearchResultsInfo.Builder()
            .paginationStart(0)
            .paginationEnd(0)
            .numMatching(0)
            .numAppliedFilters(4)
            .sidebar(sidebar)
            .build();
    srs.put("_empty", srEmpty);

    searchResultsVariations = Collections.unmodifiableMap(srs);
  }

  private static List<RecipeInfo> samples(int wanted) {
    return lines(Path.of("src/sample_recipes.jsonlines"))
        .map(Generator::parse)
        .flatMap(Optional::stream)
        .limit(wanted)
        .map(Generator::buildRecipe)
        .collect(Collectors.toList());
  }

  private static Stream<String> lines(Path filename) {
    try {
      return Files.lines(filename);
    } catch (Exception ignored) {
      return Stream.empty();
    }
  }

  private static RecipeInfo buildRecipe(JsonNode node) {
    var builder =
        new RecipeInfo.Builder()
            .name(node.get("name").asText())
            .siteName(node.get("siteName").asText())
            .goUrl(node.get("crawlUrl").asText())
            .infoUrl(node.get("slug").asText())
            .numIngredients(node.withArray("ingredients").size())
            .calories(readInt(node, "calories"))
            .totalTime(readInt(node, "totalTime"));

    node.withArray("ingredients").forEach(i -> builder.addIngredients(i.asText()));
    node.withArray("instructions").forEach(i -> builder.addInstructions(i.asText()));

    return builder.build();
  }

  private static OptionalInt readInt(JsonNode node, String key) {
    if (node.has(key)) {
      int value = node.get(key).intValue();
      if (value != 0) {
        return OptionalInt.of(value);
      }
    }
    return OptionalInt.empty();
  }

  private static Optional<JsonNode> parse(String line) {
    try {
      return Optional.ofNullable(mapper.readTree(line));
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  private static void writeResult(String filename, ArrayOfByteArraysOutput result) {
    try (var os = new FileOutputStream(outputDir.resolve(filename).toFile())) {
      for (byte[] array : result.getArrays()) {
        os.write(array);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static final ErrorInfo errorInfo =
      new ErrorInfo.Builder().title("Unknown Error").subtitle("Hue hue hue hue hue hue?").build();

  private static void generate() {
    siteVariations.forEach(
        (sitePrefix, site) -> {
          var indexName = String.format("index%s.html", sitePrefix);
          var errorName = String.format("error%s.html", sitePrefix);

          var recipeName = String.format("recipe%s.html", sitePrefix);

          writeResult(indexName, Index.template(site).render(ArrayOfByteArraysOutput.FACTORY));

          writeResult(
              errorName, Error.template(site, errorInfo).render(ArrayOfByteArraysOutput.FACTORY));

          writeResult(
              recipeName,
              Recipe.template(site, samples(5).get(4)).render(ArrayOfByteArraysOutput.FACTORY));

          searchResultsVariations.forEach(
              (srPrefix, sr) -> {
                var searchName = String.format("search%s%s.html", sitePrefix, srPrefix);

                writeResult(
                    searchName, Search.template(site, sr).render(ArrayOfByteArraysOutput.FACTORY));
              });
        });
  }

  public static void main(String[] args) throws Exception {

    System.out.println("Generating all possible template variations");
    generate();

    if (args.length > 0 && "watch".equals(args[0])) {

      var watchService = FileSystems.getDefault().newWatchService();
      var templatePath = Path.of("src/main/java/co/caio/tablier/view");

      RockerRuntime.getInstance().setReloading(true);

      System.out.println("Watching for template changes at " + templatePath);

      templatePath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

      WatchKey key;
      while ((key = watchService.take()) != null) {

        var changes =
            key.pollEvents()
                .stream()
                .map(WatchEvent::context)
                .map(Object::toString)
                .filter(s -> s.endsWith(".html"))
                .collect(Collectors.toSet());
        key.reset();

        if (changes.isEmpty()) {
          continue;
        }

        System.out.println("Change detected: " + changes);
        generate();
        System.out.println("Regeneration complete!");
      }
    }

    System.out.println("Done!");
  }
}
