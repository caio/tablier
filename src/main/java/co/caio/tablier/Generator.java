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
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.RockerRuntime;
import com.github.javafaker.Faker;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Generator {

  private static final Path outputDir = Path.of("src/");

  private static final Map<String, PageInfo> pageVariations;
  private static final Map<String, SearchFormInfo> searchFormVariations;
  private static final Map<String, SearchResultsInfo> searchResultsVariations;

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
            .addAllRecipes(generateRecipes(12))
            .build();
    srs.put("", srSinglePage);

    var srHasNext =
        new SearchResultsInfo.Builder()
            .paginationStart(1)
            .paginationEnd(20)
            .numMatching(21)
            .nextPageHref("/next")
            .addAllRecipes(generateRecipes(20))
            .build();
    srs.put("_next", srHasNext);

    var srHasPrevious =
        new SearchResultsInfo.Builder()
            .paginationStart(21)
            .paginationEnd(21)
            .numMatching(21)
            .previousPageHref("/previous")
            .addAllRecipes(generateRecipes(1))
            .build();
    srs.put("_prev", srHasPrevious);

    var srHasBoth =
        new SearchResultsInfo.Builder()
            .paginationStart(41)
            .paginationEnd(60)
            .numMatching(99)
            .nextPageHref("/next")
            .previousPageHref("/previous")
            .addAllRecipes(generateRecipes(20))
            .build();
    srs.put("_both", srHasBoth);

    searchResultsVariations = Collections.unmodifiableMap(srs);
  }

  private static List<RecipeInfo> generateRecipes(int wanted) {
    var result = new ArrayList<RecipeInfo>(wanted);

    var faker = new Faker();

    for (int i = 0; i < wanted; i++) {
      var recipe =
          new RecipeInfo.Builder()
              .name(faker.funnyName().name())
              .siteName(faker.company().name())
              .crawlUrl(faker.company().url())
              .numIngredients(faker.number().numberBetween(1, 15))
              .calories(faker.number().numberBetween(1, 1500))
              .totalTime(faker.number().numberBetween(1, 240))
              .build();

      result.add(recipe);
    }

    return result;
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
            key.pollEvents()
                .stream()
                .map(WatchEvent::context)
                .map(Object::toString)
                .filter(s -> s.endsWith(".html"))
                .collect(Collectors.toSet());

        System.out.println("Change detected: " + changes);
        generate();
        System.out.println("Regeneration complete!");
        key.reset();
      }
    }

    System.out.println("Done!");
  }
}
