import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransactionListRepository implements TransactionRepository {

    private final List<Transaction> transactions;

    public TransactionListRepository(List<Transaction> transactions) {
        Objects.requireNonNull(transactions);
        this.transactions = transactions;
    }

    @Override
    public void save(Transaction transaction) throws SQLException {
        this.transactions.add(transaction);
    }

    @Override
    public Optional<Transaction> findByOriginName(String name) {
        return transactions.stream().filter(transaction -> transaction.origin().name().equals(name)).findFirst();
    }



}
