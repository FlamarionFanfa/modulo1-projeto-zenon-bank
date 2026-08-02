import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportMain {
    public static void main(String[] args) {
        TransactionReport report = new TransactionReport();
        String dataFile = resolveDataFile("PS_20174392719_1491204439457_log.csv");
        TransactionReport.Statistics statistics = report.generateReport(dataFile);

        //var locale = Locale.of("en", "US$");
        var locale = Locale.of("en", "US");
        var integerFomatter = NumberFormat.getIntegerInstance(locale);
        var currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        //currencyFormatter.setCurrency(Currency.getInstance("USD"));

        ResourceBundle bundle = ResourceBundle.getBundle("report", locale);

        System.out.println(bundle.getString("label.total.transctions")+ integerFomatter.format(statistics.totalTransactions()));
        System.out.println(bundle.getString("label.total.frauds") + integerFomatter.format(statistics.totalFrauds()));
        System.out.println(bundle.getString("label.total.amout")+ currencyFormatter.format(statistics.totalAmount()));




    }

    private static String resolveDataFile(String fileName) {
        Path moduleRelative = Path.of("data", fileName);
        if (Files.exists(moduleRelative)) {
            return moduleRelative.toString();
        }

        Path repoRelative = Path.of("..", "data", fileName);
        if (Files.exists(repoRelative)) {
            return repoRelative.toString();
        }

        throw new IllegalArgumentException("Data file not found: " + fileName);
    }
}
