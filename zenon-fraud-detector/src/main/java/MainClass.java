import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Map;


public class MainClass {
    public static void main(String[] args) throws IOException {
        System.out.println("==== ZENON BANK - TALKING OVER THE WORLD WITH JAVA ====");


        TransactionIngestor transactionBadData = new TransactionIngestor();

        List<Transaction> transactions = transactionBadData.readNew("data/PS_20174392719_1491204439457_log.csv");
        transactions.stream().limit(10).forEach(IO::println);
        System.out.println(transactions.size());

        //Usando o FraudAnalyzer para contar os frauds
        System.out.println("==== Frauds ====");
        FraudAnalyzer fraudAnalyzer = new FraudAnalyzer(transactions);
        long l = fraudAnalyzer.countFrauds();
        System.out.println("Frauds: " + l);

        //Using the FraudAnalyzer to count the and find High Value of amount Frauds with limit 3
        System.out.println("High Value Frauds: ");
        List<Transaction> findHigh = fraudAnalyzer.findHighValueFrauds(3);
        findHigh.stream().map(Transaction::amount).forEach(IO::println);

        //Using the FraudAnalyzer to find Top Suspicious Clients with limit 5
        System.out.println("Top Suspicious Clients: ");
        List<String> topSuspiciousClients = fraudAnalyzer.findTopSuspiciousClients(5);
        topSuspiciousClients.stream().forEach(IO::println);

        //sum of all transactions of clients suspicious
        BigDecimal total = transactions.stream().map(Transaction::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println(total);

        //Counting the frauds by type

        System.out.println("Frauds by Type: ");
        Map<TransactionType, Long> countFraudsByType = fraudAnalyzer.countFraudsByType();
        countFraudsByType.forEach((type, count) -> System.out.println(type + ": " + count));

        System.out.println("==== Finding a transaction by name ====");

        TransactionRepository transactionListRepository = new TransactionListRepository(transactions);
        TransactionRepository transactionMapRepository = new TransactionMapRepository(transactions);


        long startTimeList                ;
        long endTimeList;

        startTimeList = System.nanoTime();
        transactionListRepository.findByOriginName("C1868032458").ifPresentOrElse(IO::println, () -> System.out.println("Not found customer"));
        endTimeList = System.nanoTime();
        System.out.println("Time using List: " + (endTimeList - startTimeList));


        //Time to find a transaction by name using a map
        System.out.println("Time to find a transaction by name using a map: ");
        startTimeList = System.nanoTime();
        transactionMapRepository.findByOriginName("C1868032458").ifPresentOrElse(IO::println,()-> System.out.println("Not found customer"));
        endTimeList = System.nanoTime();
        System.out.println("Time using Map: " + (endTimeList - startTimeList));






    }


}

