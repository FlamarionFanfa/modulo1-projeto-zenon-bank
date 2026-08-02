import java.sql.SQLException;
import java.util.Optional;

public class DBMain {
    static void main() throws SQLException {
        ConnectionFactory.getConnection();
        System.out.println("Banco de dados conectado!");

        TransactionSQLRepository repository = new TransactionSQLRepository();
        repository.findByOriginName("C1231006815");
       repository.findByOriginName("C1231006814");


        Optional<Transaction> transaction = repository.findByOriginName("C1231006815");

        transaction.ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Transaction not found")
        );





    }
}
