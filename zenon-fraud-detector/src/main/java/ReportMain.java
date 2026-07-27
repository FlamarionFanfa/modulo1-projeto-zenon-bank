import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportMain {
    public static void main(String[] args) {
        TransactionReport report = new TransactionReport();
        TransactionReport.Statistics statistics = report.generateReport("data/PS_20174392719_1491204439457_log.csv");

        //var locale = Locale.of("en", "US$");
        var locale = Locale.of("en", "US");
        var integerFomatter = NumberFormat.getIntegerInstance(locale);
        var currencyFormatter =  DecimalFormat.getCurrencyInstance(locale);
        //currencyFormatter.setCurrency(Currency.getInstance("USD"));

        ResourceBundle bundle = ResourceBundle.getBundle("report", locale);





        System.out.println(bundle.getString("label.total.transctions")+ integerFomatter.format(statistics.totalTransactions()));
        System.out.println(bundle.getString("label.total.frauds") + integerFomatter.format(statistics.totalFrauds()));
        System.out.println(bundle.getString("label.total.amout")+ currencyFormatter.format(statistics.totalAmount()));



    }
}
