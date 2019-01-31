package co.caio.tablier;

import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import views.Index;

public class Main {

  public static void main(String[] args) throws IOException {

    Path outputDir = Path.of("src/");

    var os = new FileOutputStream(outputDir.resolve("index.html").toFile());

    var result =
        Index.template(new SiteInfo(), new PageInfo(), new SearchInfo())
            .render(ArrayOfByteArraysOutput.FACTORY);

    for (byte[] array : result.getArrays()) {
      os.write(array);
    }

    os.close();
  }
}
