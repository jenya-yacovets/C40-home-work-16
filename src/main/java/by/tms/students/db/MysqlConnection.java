package by.tms.students.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlConnection {
    private static String user = "root";
    private static String password = "rootroot";
    private static String db = "db1";

    private static String url = "jdbc:mysql://localhost:3306/" + db;

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        return connection;
    }
}
