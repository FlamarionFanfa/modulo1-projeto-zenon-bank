import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DBMain {
    static void main() throws SQLException, IOException {
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

        TransactionIngestor transactionIngestor = new TransactionIngestor();

        long startTimeList                ;
        long endTimeList;

        startTimeList = System.nanoTime();
        List<Transaction> transactions = transactionIngestor.readNew("data/PS_20174392719_1491204439457_log.csv");
        IO.println(transactions.size());
        endTimeList = System.nanoTime();
        System.out.println("Time to read file: " + (endTimeList - startTimeList));

        startTimeList = System.nanoTime();
        for (Transaction transaction1 : transactions) {
            repository.save(transaction1);
        }
        endTimeList = System.nanoTime();
        System.out.println("Time to save in DB: " + (endTimeList - startTimeList));


    }
}
