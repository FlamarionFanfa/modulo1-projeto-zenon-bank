import java.sql.SQLException;
import java.util.Optional;

public interface TransactionRepository {
    Optional<Transaction> findByOriginName(String name) throws SQLException;
}
