package org.bozdgn;

import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.stream.Collectors;

public class CreateMockDatabase {
    static String username = "refectory";
    static String password = "";
    static String dbUrl = "jdbc:mysql://localhost/refectory";

    static String queryDropAllTables = "MockData_Nuke.sql";
    static String queryCreateTables = "MockData_CreateTables.sql";
    static String queryPopulateTables = "MockData_Populate.sql";

    static void runSQLBatch(Connection conn, String resourceLocator) throws SQLException{
        InputStream resourceStream = CreateMockDatabase.class.
                getClassLoader().getResourceAsStream(resourceLocator);
        if(resourceStream == null) {
            System.out.printf("Cannot read resource `%s`\n", resourceLocator);
            return;
        }

        BufferedReader r = new BufferedReader(new InputStreamReader(resourceStream));
        String queryString = r.lines().collect(Collectors.joining("\n"));

        for(String q: queryString.split(";")) {
            String query = q.trim();
            if(query.length() != 0) {
                System.out.printf("execute> %s;\n\n", query);
                try(Statement st = conn.createStatement()) {
                    st.execute(query);
                }
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // assert JDBC driver
        }
        catch(ClassNotFoundException e) {
            System.out.println("MySQL driver not found");
            return;
        }


        Properties dbInfo = new Properties();
        dbInfo.setProperty("user", username);
        dbInfo.setProperty("password", password);
        dbInfo.setProperty("serverTimezone", "Europe/Istanbul");
//        dbInfo.setProperty("useUnicode", "true");
//        dbInfo.setProperty("characterEncoding", "utf-8");
        dbInfo.setProperty("useSSL", "false");

        try(Connection conn = DriverManager.getConnection(dbUrl, dbInfo)) {

            // NOTE(bora): Reset database
            runSQLBatch(conn, queryDropAllTables);
            runSQLBatch(conn, queryCreateTables);

            // NOTE(bora): Populate tables
            runSQLBatch(conn, queryPopulateTables);
        }
    }
}
