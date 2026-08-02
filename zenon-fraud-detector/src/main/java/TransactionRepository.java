import java.sql.SQLException;
import java.util.Optional;

public interface TransactionRepository {
    void save(Transaction transaction) throws SQLException;
    Optional<Transaction> findByOriginName(String name) throws SQLException;
}
