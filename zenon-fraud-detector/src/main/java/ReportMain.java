import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class ReportMain {
    public static void main(String[] args) {
        TransactionReport report = new TransactionReport();
        TransactionReport.Statistics statistics = report.generateReport("data/PS_20174392719_1491204439457_log.csv");

        var locale = Locale.of("pt", "BR");
        var integerFomatter = NumberFormat.getIntegerInstance(locale);
        var currencyFormatter =  DecimalFormat.getCurrencyInstance(locale);
        currencyFormatter.setCurrency(Currency.getInstance("USD"));





        System.out.println("Total of lines: " + integerFomatter.format(statistics.totalTransactions()));
        System.out.println("Total of frauds: " + integerFomatter.format(statistics.totalFrauds()));
        System.out.println("Amount total frauds: " + currencyFormatter.format(statistics.totalAmount()));



    }
}
