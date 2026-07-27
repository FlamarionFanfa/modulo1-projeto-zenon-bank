import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class TransactionReport {

    public record ReportTransaction(BigDecimal amount, boolean isFraud) {
    }

    public record Statistics(long totalTransactions, long totalFrauds, BigDecimal totalAmount) {
        public Statistics combine(Statistics other) {
            return new Statistics(
                    this.totalTransactions + other.totalTransactions,
                    this.totalFrauds + other.totalFrauds,
                    this.totalAmount.add(other.totalAmount)
            );
        }

        public Statistics accumulate(ReportTransaction rt) {
            return new Statistics(
                    totalTransactions + 1,
                    totalFrauds + (rt.isFraud() ? 1 : 0),
                    totalAmount.add(rt.amount())
            );
        }
    }

    public Statistics generateReport(String filename) {
        Path path = Path.of(filename);
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .skip(1)
                    .map(this::parseTransaction)
                    .flatMap(Optional::stream)
                    .reduce(
                            new Statistics(0, 0, BigDecimal.ZERO),
                            Statistics::accumulate,
                            Statistics::combine
                    );
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao ler o arquivo: " + filename, e);
        }
    }

    private Optional<ReportTransaction> parseTransaction(String line) {
        try {
            String[] chunks = line.split(",");
            if (chunks.length < 11) {
                throw new IllegalArgumentException("Lines with error of formatted " + line);
            }
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
