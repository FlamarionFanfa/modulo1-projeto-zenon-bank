import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class TransactionReport {

    public record ReportTransaction(BigDecimal amount, boolean isFraud) {
    }


    public record Statistics(long totalTransactions, long totalFrauds, BigDecimal totalAmount) {
    }


    public Statistics generateReport(String filename) {
        Path path = Path.of(filename);
        try (Stream<String> lines = Files.lines(path)) {

            return lines
                    .skip(1)
                    .map(this::parseTransaction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .reduce(
                            new Statistics(0, 0, BigDecimal.ZERO),
                            (Statistics acc, ReportTransaction rt) -> new Statistics(
                                    acc.totalTransactions + 1,
                                    acc.totalFrauds + (rt.isFraud ? 1 : 0),
                                    acc.totalAmount.add(rt.amount)), (s1, s2) -> s1);



        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo" + e);
        }


    }

    private Optional<ReportTransaction> parseTransaction(String line) {
        try {

            String[] chunks = line.split(",");
            if (chunks[2] == null || chunks[2].isEmpty())
                throw new IllegalArgumentException("Amount is null or empty: " + chunks[2]);
            BigDecimal amount = new BigDecimal(chunks[2]);
            boolean isFraud = "1".equals(chunks[9]);
            return Optional.of(new ReportTransaction(amount, isFraud));
        } catch (Exception e) {
            System.err.println("Erro ao ao fazer o parse: " + line + " - " + e.getMessage());
            //e.printStackTrace();

            return Optional.empty();


        }


    }
}


