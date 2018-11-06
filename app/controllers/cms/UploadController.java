package controllers.cms;

import actions.BasicAuthAction;
import com.google.inject.Inject;
import components.cms.loader.Loader;
import components.cms.parser.Parser;
import components.cms.parser.ParserResult;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import triage.cache.CachePopulationService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class UploadController extends Controller {
    private final Parser parser;
    private final Loader loader;
    private final CachePopulationService cachePopulationService;
    private final String FILE_NAME = "spreadsheet.xlsx";
    private final String RELATIVE_PATH = "app/assets/spreadsheet/";
    private final String ADDRESS = "https://github.com/uktrade/lite-export-control-list-spreadsheet/blob/master/spreadsheet.xlsx?raw=true";

    @Inject
    public UploadController(Parser parser, Loader loader, CachePopulationService cachePopulationService) {
        this.parser = parser;
        this.loader = loader;
        this.cachePopulationService = cachePopulationService;
    }

    @With(BasicAuthAction.class)
    public Result spreadsheetUpload() {
        File file;
        String tempAddress = ADDRESS;

        System.out.println("Attempting to download the spreadsheet...");

        try {
            URL url = new URL(tempAddress);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            Map<String, List<String>> header = http.getHeaderFields();
            while (isRedirected(header)) {
                tempAddress = header.get("Location").get(0);
                url = new URL(tempAddress);
                http = (HttpURLConnection) url.openConnection();
                header = http.getHeaderFields();
            }
            InputStream input = http.getInputStream();
            byte[] buffer = new byte[4096];
            int n;

            // Create file and the folders it needs
            file = new File(RELATIVE_PATH + FILE_NAME);
            file.getParentFile().mkdirs();

            OutputStream output = new FileOutputStream(file);
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
            }
            output.close();

            System.out.println("Download completed successfully: '" + System.getProperty("user.dir") + "/" + RELATIVE_PATH + FILE_NAME + "'");

            // Parse the downloaded file
            System.out.println("Attempting to parse downloaded file...");

            ParserResult parserResult = parser.parse(file);
            loader.load(parserResult);
            // Caching takes ages, means that this method never finishes!
            cachePopulationService.populateCache();

            System.out.println("File parsed successfully");

            return ok("File uploaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return internalServerError("File not uploaded");
    }

    private boolean isRedirected(Map<String, List<String>> header) {
        for (String hv : header.get(null)) {
            if (hv.contains(" 301 ") || hv.contains(" 302 ")) {
                return true;
            }
        }
        return false;
    }

}
