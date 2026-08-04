import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TransactionSQLRepository implements TransactionRepository{
    private static final String INSERT_SQL = """
            insert into
            zenon_frauds.transactions
            (step, `type`, amount, nome_origin,
            old_balance_origin, new_balance_origin,
            name_recipient, old_balance_recipient,
            new_balance_recipient, is_fraud, is_flagged_fraud)
            values
            (?,?,?,?,?,?,?,?,?,?,?);
            """;

    @Override
    public void save(Transaction transaction) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)){
            bindTransaction(ps, transaction);
            ps.execute();
        }
    }

    public int[] saveBatch(List<Transaction> transactions) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                for (Transaction transaction : transactions) {
                    bindTransaction(ps, transaction);
                    ps.addBatch();
                }

                int[] result = ps.executeBatch();
                conn.commit();
                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void deleteAll() throws SQLException {
        String sql = "TRUNCATE TABLE zenon_frauds.transactions";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    public DatabaseStatistics calculateStatistics() throws SQLException {
        String sql = """
                SELECT
                    COUNT(*) AS total_transactions,
                    COALESCE(SUM(CASE WHEN is_fraud = 1 THEN 1 ELSE 0 END), 0) AS total_frauds,
                    COALESCE(SUM(amount), 0) AS total_amount
                FROM zenon_frauds.transactions
                """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return new DatabaseStatistics(0, 0, BigDecimal.ZERO);
            }

            return new DatabaseStatistics(
                    rs.getLong("total_transactions"),
                    rs.getLong("total_frauds"),
                    rs.getBigDecimal("total_amount")
            );
        }
    }

    @Override
    public Optional<Transaction> findByOriginName(String originName) throws SQLException {
        String sql = """
                SELECT step, `type`, amount, nome_origin,
           old_balance_origin, new_balance_origin,
           name_recipient, old_balance_recipient,
           new_balance_recipient, is_fraud, is_flagged_fraud
                FROM transactions
    WHERE nome_origin = ?
    ORDER BY step
    LIMIT 1
    """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1,originName);

            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    TransactionCustomer origin = new TransactionCustomer(
                            rs.getString("nome_origin"),
                            rs.getBigDecimal("old_balance_origin"),
                            rs.getBigDecimal("new_balance_origin")
                    );

                    TransactionCustomer recipient = new TransactionCustomer(
                            rs.getString("name_recipient"),
                            rs.getBigDecimal("old_balance_recipient"),
                            rs.getBigDecimal("new_balance_recipient")
                    );

                    Transaction transaction = new Transaction(
                            rs.getInt("step"),
                            TransactionType.valueOf(rs.getString("type")),
                            rs.getBigDecimal("amount"),
                            origin,
                            recipient,
                            rs.getBoolean("is_fraud"),
                            rs.getBoolean("is_flagged_fraud")
                    );

                    return Optional.of(transaction);
                } else {
                    System.out.println("Transaction not found to origin name: " + originName);
                }


            }



        }

        return Optional.empty();
    }

    private void bindTransaction(PreparedStatement ps, Transaction transaction) throws SQLException {
        ps.setInt(1, transaction.step());
        ps.setString(2, transaction.type().name());
        ps.setBigDecimal(3, transaction.amount());

        ps.setString(4, transaction.origin().name());
        ps.setBigDecimal(5, transaction.origin().oldBalance());
        ps.setBigDecimal(6, transaction.origin().newBalance());

        ps.setString(7, transaction.recipient().name());
        ps.setBigDecimal(8, transaction.recipient().oldBalance());
        ps.setBigDecimal(9, transaction.recipient().newBalance());

        ps.setBoolean(10, transaction.isFraud());
        ps.setBoolean(11, transaction.isFlaggedFraud());
    }

    public record DatabaseStatistics(long totalTransactions, long totalFrauds, BigDecimal totalAmount) {
    }
}
