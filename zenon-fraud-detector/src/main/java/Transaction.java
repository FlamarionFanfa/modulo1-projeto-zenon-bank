import java.math.BigDecimal;
import java.util.Objects;

public record Transaction(
        int step,
        TransactionType type,
        BigDecimal amount,
        TransactionCustomer origin,
        TransactionCustomer recipient,
        boolean isFraud,
        boolean isFlaggedFraud
) {
    //constructor compact of records
    public Transaction{
        Objects.requireNonNull(type);
        Objects.requireNonNull(origin);
        Objects.requireNonNull(recipient);
        Objects.requireNonNull(amount);

              if (step < 0) throw new IllegalArgumentException("Step must be positive: " + step);
              if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Amount must be positive: " + amount);
                          }
}
