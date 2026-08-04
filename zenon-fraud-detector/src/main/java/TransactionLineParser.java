import java.math.BigDecimal;
import java.util.Optional;

public class TransactionLineParser {

    public Optional<Transaction> parse(String line) {
        try {
            String[] chunks = line.split(",");
            if (chunks.length < 11) {
                throw new IllegalArgumentException("Line with invalid format: " + line);
            }

            int step = Integer.parseInt(chunks[0]);
            TransactionType type = TransactionType.valueOf(chunks[1]);

            if (chunks[2] == null || chunks[2].isEmpty()) {
                throw new IllegalArgumentException("Amount is null or empty: " + chunks[2]);
            }

            BigDecimal amount = new BigDecimal(chunks[2]);
            TransactionCustomer origin = new TransactionCustomer(
                    chunks[3],
                    new BigDecimal(chunks[4]),
                    new BigDecimal(chunks[5])
            );
            TransactionCustomer recipient = new TransactionCustomer(
                    chunks[6],
                    new BigDecimal(chunks[7]),
                    new BigDecimal(chunks[8])
            );
            boolean isFraud = "1".equals(chunks[9]);
            boolean isFlaggedFraud = "1".equals(chunks[10]);

            return Optional.of(new Transaction(step, type, amount, origin, recipient, isFraud, isFlaggedFraud));
        } catch (Exception e) {
            System.err.println("Erro ao fazer o parse: " + line + " - " + e.getMessage());
            return Optional.empty();
        }
    }
}
