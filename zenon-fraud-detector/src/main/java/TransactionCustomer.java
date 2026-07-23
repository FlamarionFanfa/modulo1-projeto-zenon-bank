import java.math.BigDecimal;
import java.util.Objects;

public record TransactionCustomer(
        String name,
        BigDecimal oldBalance,
        BigDecimal newBalance
) {
public TransactionCustomer{
    Objects.requireNonNull(name);
    Objects.requireNonNull(oldBalance);
    Objects.requireNonNull(newBalance);

    if (name == null || name.isEmpty()) throw new IllegalArgumentException("Name is null or empty: " + name);
    //if (oldBalance.compareTo(newBalance) > 0) throw new IllegalArgumentException("Old balance must be less than or equal to new balance: ");
   // if (newBalance.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("New balance must be positive: " );
}

}