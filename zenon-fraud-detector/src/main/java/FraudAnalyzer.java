import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FraudAnalyzer {
    private List<Transaction> transactions;

    public FraudAnalyzer(List<Transaction> transactions) {
        Objects.requireNonNull(transactions);
        this.transactions = transactions;
    }
    public  long countFrauds() {
        return transactions
                .stream()
                .filter(Transaction::isFraud)
                .count();
    }

    public List<Transaction> findHighValueFrauds(int limit) {
        return transactions
                .stream()
                .filter(Transaction::isFraud)
                .sorted(Comparator.comparing(Transaction::amount).reversed())
                .limit(limit)
                .toList();



    }

    public List<String> findTopSuspiciousClients(int limit) {
        return transactions
                .stream()
                .filter(Transaction::isFraud)
                .sorted(Comparator.comparing(Transaction::amount).reversed())
                .map(transactions ->  transactions.origin().name())
                .distinct()                
                .limit(limit)
                .toList();
                
    }

    public BigDecimal calculateTotalFraudLoss() {
        return transactions
                .stream()
                .filter(Transaction::isFraud)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    public Map<TransactionType, Long> countFraudsByType() {

        return transactions
                .stream()
                .filter(Transaction::isFraud)
                .collect(Collectors.groupingBy(Transaction::type, Collectors.counting()));

    }
}
