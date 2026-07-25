public class ReportMain {
    public static void main(String[] args) {
        TransactionReport report = new TransactionReport();
        TransactionReport.Statistics statistics = report.generateReport("data/PS_20174392719_1491204439457_log.csv");
        System.out.println("""
        Total of lines: %d 
        Total of frauds: %d
         Amount total frauds: %.2f
         """.formatted(statistics.totalTransactions(),statistics.totalFrauds(),statistics.totalAmount()));


    }
}
