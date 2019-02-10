package co.caio.tablier;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import co.caio.tablier.model.ErrorInfo;
import co.caio.tablier.model.PageInfo;
import co.caio.tablier.model.RecipeInfo;
import co.caio.tablier.model.SearchFormInfo;
import co.caio.tablier.model.SearchResultsInfo;
import co.caio.tablier.model.SiteInfo;
import co.caio.tablier.view.Error;
import co.caio.tablier.view.Index;
import co.caio.tablier.view.Search;
import co.caio.tablier.view.ZeroResults;
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

  private static final Map<String, PageInfo> pageVariations;
  private static final Map<String, SearchFormInfo> searchFormVariations;
  private static final Map<String, SearchResultsInfo> searchResultsVariations;
  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    var pages = new HashMap<String, PageInfo>();
    var defaultPage = new PageInfo.Builder().title("Index").build();
    pages.put("", defaultPage);
    pages.put(
        "_unstable", new PageInfo.Builder().from(defaultPage).showUnstableWarning(true).build());

    pageVariations = Collections.unmodifiableMap(pages);

    var searchforms = new HashMap<String, SearchFormInfo>();
    var defaultSearchForm = new SearchFormInfo.Builder().build();
    searchforms.put("", defaultSearchForm);
    searchforms.put("_disabled", new SearchFormInfo.Builder().isDisabled(true).build());
    searchforms.put("_nofocus", new SearchFormInfo.Builder().isAutoFocus(false).build());
    searchforms.put(
        "_disablednofocus",
        new SearchFormInfo.Builder().isDisabled(true).isAutoFocus(false).build());

    searchFormVariations = Collections.unmodifiableMap(searchforms);

    var srs = new HashMap<String, SearchResultsInfo>();

    var srSinglePage =
        new SearchResultsInfo.Builder()
            .paginationStart(1)
            .paginationEnd(12)
            .numMatching(12)
            .addAllRecipes(samples(12))
            .build();
    srs.put("", srSinglePage);

    var srHasNext =
        new SearchResultsInfo.Builder()
            .paginationStart(1)
            .paginationEnd(20)
            .numMatching(21)
            .nextPageHref("/next")
            .addAllRecipes(samples(20))
            .build();
    srs.put("_next", srHasNext);

    var srHasPrevious =
        new SearchResultsInfo.Builder()
            .paginationStart(21)
            .paginationEnd(21)
            .numMatching(21)
            .previousPageHref("/previous")
            .addAllRecipes(samples(1))
            .build();
    srs.put("_prev", srHasPrevious);

    var srHasBoth =
        new SearchResultsInfo.Builder()
            .paginationStart(41)
            .paginationEnd(60)
            .numMatching(99)
            .nextPageHref("/next")
            .previousPageHref("/previous")
            .addAllRecipes(samples(20))
            .build();
    srs.put("_both", srHasBoth);

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
    var kcal =
        node.has("calories")
            ? OptionalInt.of(node.get("calories").intValue())
            : OptionalInt.empty();
    var time =
        node.has("calories")
            ? OptionalInt.of(node.get("calories").intValue())
            : OptionalInt.empty();

    return new RecipeInfo.Builder()
        .name(node.get("name").asText())
        .siteName(node.get("siteName").asText())
        .crawlUrl(node.get("crawlUrl").asText())
        .numIngredients(node.withArray("ingredients").size())
        .calories(readInt(node, "calories"))
        .totalTime(readInt(node, "totalTime"))
        .build();
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

  private static final SiteInfo siteInfo =
      new SiteInfo.Builder()
          .title("gula.recipes")
          .addNavigationItem("/", "gula.recipes", true)
          .addNavigationItem("/about", "About", false)
          .build();

  private static final ErrorInfo errorInfo =
      new ErrorInfo.Builder().title("Unknown Error").subtitle("Hue hue hue hue hue hue?").build();

  private static void generate() {
    pageVariations.forEach(
        (pagePrefix, page) ->
            searchFormVariations.forEach(
                (searchFormPrefix, searchForm) -> {
                  var indexName = String.format("index%s%s.html", pagePrefix, searchFormPrefix);
                  var zeroName =
                      String.format("zero_results%s%s.html", pagePrefix, searchFormPrefix);
                  var errorName = String.format("error%s%s.html", pagePrefix, searchFormPrefix);

                  writeResult(
                      indexName,
                      Index.template(siteInfo, page, searchForm)
                          .render(ArrayOfByteArraysOutput.FACTORY));

                  writeResult(
                      zeroName,
                      ZeroResults.template(siteInfo, page, searchForm)
                          .render(ArrayOfByteArraysOutput.FACTORY));

                  writeResult(
                      errorName,
                      Error.template(siteInfo, page, searchForm, errorInfo)
                          .render(ArrayOfByteArraysOutput.FACTORY));

                  searchResultsVariations.forEach(
                      (srPrefix, sr) -> {
                        var searchName =
                            String.format(
                                "search%s%s%s.html", pagePrefix, searchFormPrefix, srPrefix);

                        writeResult(
                            searchName,
                            Search.template(siteInfo, page, searchForm, sr)
                                .render(ArrayOfByteArraysOutput.FACTORY));
                      });
                }));
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
            key.pollEvents().stream()
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
