import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class MainClass {
    public static void main(String[] args) throws IOException {
        System.out.println("==== ZENON BANK - TALKING OVER THE WORLD WITH JAVA ====");

//        Transaction t1 =  new Transaction(1,TransactionType.PAYMENT,new BigDecimal("9839.64"),
//                new TransactionCustomer("C1231006815",new BigDecimal("170136.0"),new BigDecimal("160296.36")),
//                new TransactionCustomer("M1979787155",new BigDecimal("0.0"),new BigDecimal("0.0")),
//                false,false);
//
//        Transaction t2 =  new Transaction(743,TransactionType.CASH_OUT,new BigDecimal("850002.52"),
//                new TransactionCustomer("C1280323807",new BigDecimal("850002.52"),new BigDecimal("0.0")),
//                new TransactionCustomer("C873221189",new BigDecimal("6510099.11"),new BigDecimal("7360101.63")),
//                true,false);
        //IO.println(t1);
        //IO.println(t2);

        //===============================
//        TransactionIngestor transactionIngestor = new TransactionIngestor();
//        List<Transaction> transactions = transactionIngestor.readNew("data/PS_20174392719_1491204439457_log.csv");
//        transactions.stream().limit(10).forEach(IO::println);
        //===============================

        TransactionIngestor transactionBadData = new TransactionIngestor();
        List<Optional<Transaction>> transactions = transactionBadData.readNew("data/paysim_with_bad_data.csv");
        transactions.stream().limit(10).forEach(IO::println);
        System.out.println(transactions.size());

    }

}