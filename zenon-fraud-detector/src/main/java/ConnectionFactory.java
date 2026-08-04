import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private static final String URL =
            "jdbc:mysql://localhost:3306/zenon_frauds?rewriteBatchedStatements=true&cachePrepStmts=true";
    private static final String USER = "root";
    private static final String PASSWORD = "senha123";

    private ConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new SQLException("Erro ao conectar ao banco de dados", e);
        }

    }
}

