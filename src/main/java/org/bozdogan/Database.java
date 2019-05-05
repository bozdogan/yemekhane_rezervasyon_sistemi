package org.bozdogan;

import java.sql.*;
import java.util.*;

public class Database implements AutoCloseable{

    private Properties connectionInfo;
    private Connection connection;

    private String serverLocation;
    public String getServerLocation(){ return serverLocation; }
    private String databaseName;
    public String getDatabaseName(){ return databaseName; }

    private PreparedStatement statement;

    public Database(String URL, String username, String password, String databaseName){
        this.connectionInfo = new Properties();
        connectionInfo.setProperty("user", username);
        connectionInfo.setProperty("password", password);
        connectionInfo.setProperty("serverTimezone", "Europe/Istanbul"); //@ Timezone issue

        this.serverLocation = URL;
        this.databaseName = databaseName;
    }

    /** Check if can connect to DB. */
    public boolean testConnection(){
        try{
            return Conn()!=null;
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    private Connection Conn() throws SQLException{
        // Creates a connection object when called the first time.
        if(connection==null){
            try{
                Class.forName("com.mysql.cj.jdbc.Driver"); // driver check

                this.connection = DriverManager.getConnection(
                        "jdbc:mysql://"+serverLocation+"/"+databaseName+"?useUnicode=true&characterEncoding=utf-8", connectionInfo);

            } catch(ClassNotFoundException e){
                System.out.println("Mysql driver is not present.");
                System.err.println("Error: "+e.getMessage());
            }
        }

        return connection;
    }

    //
    // DATABASE OPERATIONS //

    public Database prepare(String query) throws SQLException{
        if(statement!=null)
            statement.close();

        statement = Conn().prepareStatement(query);
        return this; // for method chaining
    }


    /** Just executes current statement.
     * @param values */
    public Database execute(Map<Integer, Object> values) throws SQLException{
        if(values!=null)
            embedValues(statement, values);

        return execute();
    }

    /** Just executes current statement as it is. */
    public Database execute() throws SQLException{
        statement.execute();
        return this; // for method chaining
    }

    public Map<String, Object> fetch(Map<Integer, Object> values) throws SQLException{

        if(values!=null)
            embedValues(statement, values);

        ResultSet rs = statement.executeQuery();
        ResultSetMetaData metadata = rs.getMetaData();
        int columns = metadata.getColumnCount();

        HashMap<String, Object> tuple = new HashMap<>();

        if(rs.next())
            for (int i=1; i<=columns; i++)
                tuple.put(metadata.getColumnName(i), rs.getObject(i));

        rs.close(); //@ TODO Is that necessary?

        return tuple;
    }

    public List<Map<String, Object>> fetchAll(Map<Integer, Object> values) throws SQLException{

        if(values!=null)
            embedValues(statement, values);

        ResultSet rs = statement.executeQuery();
        ResultSetMetaData metadata = rs.getMetaData();
        int columns = metadata.getColumnCount();

        List<Map<String, Object>> results = new LinkedList<>();

        while(rs.next()){
            HashMap<String, Object> tuple = new HashMap<>();
            for(int i=1; i<=columns; i++)
                tuple.put(metadata.getColumnName(i), rs.getObject(i));

            results.add(tuple);
        }

        rs.close(); //@ TODO Is that necessary?

        return results;
    }

    public int executeUpdate(Map<Integer, Object> values) throws SQLException{
        if(values!=null)
            embedValues(statement, values);

        return statement.executeUpdate();
    }

    private static void embedValues(PreparedStatement statement, Map<Integer, Object> values) throws SQLException{
        // This is a helper function for setting the variables of a prep statement
        // from a Map. ~It basically determined the type of data and calls the
        // appropriate method to set value.~ or just calls setObject method and we'll be OK.

        for(Map.Entry<Integer, Object> e: values.entrySet()){
            int index = e.getKey();
            Object value = e.getValue();

            statement.setObject(index, value);
        }
    }


    /** Close the connection if it's no longer needed. */
    @Override
    public void close() throws SQLException{
        connection.close();
        connection = null; // to make seamless reconnect possible. getConnection() will handle the rest.
    }

}
