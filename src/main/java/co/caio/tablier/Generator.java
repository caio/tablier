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
import co.caio.tablier.view.Static;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.RockerRuntime;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Generator {

  private static final Map<String, SiteInfo> siteVariations;
  private static final Map<String, SearchResultsInfo> searchResultsVariations;
  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    siteVariations =
        Map.of(
            "",
            new SiteInfo.Builder().title("Search").searchValue("banana").build(),
            "_unstable",
            new SiteInfo.Builder().title("Index").isUnstable(true).build(),
            "_nofocus",
            new SiteInfo.Builder().title("Title").searchIsAutoFocus(false).build());

    var filters =
        List.of(
            new FilterInfo.Builder()
                .name("Sort recipes by")
                .isRemovable(false)
                .addOption("Relevance", "#", true)
                .addOption("Fastest to cook", "#", 22)
                .addOption("Least ingredients", "#", 4)
                .addOption("Calories", "#", 4)
                .build(),
            new FilterInfo.Builder()
                .name("Restrict by diet")
                // XXX if a filter is active we dont show counts
                .showCounts(false)
                .addOption("Low carb", "#", 12)
                .addOption("Vegan", "#", 22)
                .addOption("Keto", "#", true)
                .addOption("Paleo", "#", 4)
                .build(),
            new FilterInfo.Builder()
                .name("Limit Ingredients")
                .showCounts(true)
                .addOption("Up to 5", "#", 1)
                .addOption("From 6 to 10", "#", 22)
                .addOption("More than 10", "#", 4)
                .build(),
            new FilterInfo.Builder()
                .name("Limit Total Time")
                .showCounts(true)
                .addOption("Up to 15 minutes", "#", 7)
                .addOption("From 15 to 30 minutes", "#", 29)
                .addOption("From 30 to 60 minutes", "#", 11)
                .addOption("One hour or more", "#", 0)
                .build(),
            new FilterInfo.Builder()
                .name("Limit Nutrition (per serving)")
                .showCounts(true)
                .addOption("Up to 200 kcal", "#", 0)
                .addOption("Up to 500 kcal", "#", 29)
                .addOption("Up to 10g of Fat", "#", 11)
                .addOption("Up to 30g of Carbs", "#", 23)
                .build());

    var sidebar = new SidebarInfo.Builder().addAllFilters(filters).build();

    var srs = new HashMap<String, SearchResultsInfo>();

    int numRecipes = 1_000_001;

    var srHasBoth =
        new SearchResultsInfo.Builder()
            .paginationStart(1)
            .paginationEnd(10)
            .numMatching(31409)
            .numRecipes(numRecipes)
            .previousPageHref("/previous")
            .nextPageHref("/next")
            .addAllRecipes(samples(10))
            .sidebar(sidebar)
            .build();
    srs.put("", srHasBoth);

    var srHasNext =
        new SearchResultsInfo.Builder()
            .paginationStart(1)
            .paginationEnd(20)
            .numMatching(21)
            .numRecipes(numRecipes)
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
            .numRecipes(numRecipes)
            .previousPageHref("/previous")
            .addAllRecipes(samples(1))
            .sidebar(sidebar)
            .build();
    srs.put("_prev", srHasPrevious);

    var srHasNone =
        new SearchResultsInfo.Builder()
            .paginationStart(41)
            .paginationEnd(60)
            .numMatching(99)
            .numRecipes(numRecipes)
            .numAppliedFilters(4)
            .addAllRecipes(samples(20))
            .sidebar(sidebar)
            .build();
    srs.put("_none", srHasNone);

    var srEmpty =
        new SearchResultsInfo.Builder()
            .paginationStart(0)
            .paginationEnd(0)
            .numMatching(0)
            .numRecipes(numRecipes)
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
            .crawlUrl(node.get("crawlUrl").asText())
            .similarUrl("/similar.html")
            .infoUrl("/recipe.html")
            .numIngredients(node.withArray("ingredients").size())
            .calories(readInt(node, "calories"))
            .proteinContent(readDouble(node, "proteinContent"))
            .carbohydrateContent(readDouble(node, "carbohydrateContent"))
            .fatContent(readDouble(node, "fatContent"))
            .prepTime(readInt(node, "prepTime"))
            .cookTime(readInt(node, "cookTime"))
            .totalTime(readInt(node, "totalTime"));

    node.withArray("ingredients").forEach(i -> builder.addIngredients(i.asText()));

    return builder.build();
  }

  private static OptionalDouble readDouble(JsonNode node, String key) {
    if (node.has(key)) {
      double value = node.get(key).doubleValue();
      if (value != -1) {
        return OptionalDouble.of(value);
      }
    }
    return OptionalDouble.empty();
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

  private static final Path outputDir = Path.of("src/");

  private static void writeResult(Path filename, ArrayOfByteArraysOutput result) {
    try (var os = new FileOutputStream(filename.toFile())) {
      for (byte[] array : result.getArrays()) {
        os.write(array);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void writeResult(String filename, ArrayOfByteArraysOutput result) {
    writeResult(outputDir.resolve(filename), result);
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
              Recipe.template(site, samples(7).get(6)).render(ArrayOfByteArraysOutput.FACTORY));

          searchResultsVariations.forEach(
              (srPrefix, sr) -> {
                var searchName = String.format("search%s%s.html", sitePrefix, srPrefix);

                writeResult(
                    searchName, Search.template(site, sr).render(ArrayOfByteArraysOutput.FACTORY));
              });
        });
  }

  private static final Path MARKDOWN_INPUT_PATH = Path.of("src/markdown/");
  private static final Path MARKDOWN_OUTPUT_PATH = Path.of("src/pages/"); // XXX weird
  private static final Path ROCKER_TEMPLATE_PATH = Path.of("src/main/java/co/caio/tablier/view");

  static class FrontMatterVisitor extends AbstractYamlFrontMatterVisitor {

    public String getTitle() {
      try {
        return getData().get("title").get(0);
      } catch (Exception any) {
        throw new RuntimeException("title front matter is required!", any);
      }
    }
  }

  private static void buildStatic() throws IOException {
    var options = new MutableDataSet();

    options.set(
        Parser.EXTENSIONS,
        List.of(
            YamlFrontMatterExtension.create(),
            TypographicExtension.create(),
            TocExtension.create()));

    options.set(HtmlRenderer.RENDER_HEADER_ID, true);
    options.set(TocExtension.LIST_CLASS, "toc");

    var parser = Parser.builder(options).build();
    var renderer = HtmlRenderer.builder(options).build();

    var markdownFiles =
        Files.list(MARKDOWN_INPUT_PATH)
            .filter(p -> p.toFile().getName().endsWith(".md"))
            .collect(Collectors.toList());

    for (var markdownFile : markdownFiles) {
      var node = parser.parse(Files.readString(markdownFile));

      var html = renderer.render(node);

      var visitor = new FrontMatterVisitor();
      visitor.visit(node);
      var title = visitor.getTitle();

      var outputFile =
          MARKDOWN_OUTPUT_PATH.resolve(
              markdownFile.getFileName().toString().replace(".md", ".html"));

      var siteInfo = new SiteInfo.Builder().title(title).searchIsAutoFocus(false).build();

      writeResult(
          outputFile, Static.template(siteInfo, html).render(ArrayOfByteArraysOutput.FACTORY));
    }
  }

  public static void main(String[] args) throws Exception {

    Files.createDirectories(MARKDOWN_OUTPUT_PATH);

    System.out.println("Generating all possible template variations");
    generate();

    System.out.println("Generating static pages");
    buildStatic();

    if (args.length > 0 && "watch".equals(args[0])) {

      var watchService = FileSystems.getDefault().newWatchService();

      RockerRuntime.getInstance().setReloading(true);

      System.out.println("Watching for markdown changes at " + MARKDOWN_INPUT_PATH);
      System.out.println("Watching for template changes at " + ROCKER_TEMPLATE_PATH);

      ROCKER_TEMPLATE_PATH.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
      MARKDOWN_INPUT_PATH.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

      WatchKey key;
      while ((key = watchService.take()) != null) {

        var changes = new HashSet<Path>();
        for (var event : key.pollEvents()) {
          // XXX NPE if I other ENTRY_* events are captured. May not be a Path too
          var ctx = event.context().toString();

          var changedPath =
              ctx.endsWith(".html")
                  ? ROCKER_TEMPLATE_PATH.resolve(ctx)
                  : MARKDOWN_INPUT_PATH.resolve(ctx);
          var changedFile = changedPath.toFile();

          if (changedFile.exists() && changedFile.length() > 0) {
            changes.add(changedPath);
          }
        }

        key.reset();

        if (changes.isEmpty()) {
          continue;
        }

        System.out.println("Change detected: " + changes);
        generate();
        buildStatic();
        System.out.println("Regeneration complete!");
      }
    }

    System.out.println("Done!");
  }
}
