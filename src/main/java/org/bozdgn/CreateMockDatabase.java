package org.bozdgn;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class CreateMockDatabase {
    static String username = "refectory";
    static String password = "";
    static String dbUrl = "jdbc:mysql://localhost/refectory";

    static void runMultipleStatements(
            Connection conn,
            String queryString
    ) throws SQLException {
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

    public static void main(String[] args) throws SQLException, IOException {
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
            runMultipleStatements(conn, queryDropAllTables);
            runMultipleStatements(conn, queryCreateTables);

            // NOTE(bora): Populate tables
            runMultipleStatements(conn, queryPopulateTables);
        }
    }


    private static final String queryDropAllTables = ""
        + "SET FOREIGN_KEY_CHECKS=0;\n"
        + "DROP TABLE IF EXISTS `users`;\n"
        + "DROP TABLE IF EXISTS `meal`;\n"
        + "DROP TABLE IF EXISTS `food`;\n"
        + "DROP TABLE IF EXISTS `reservations`;\n"
        + "DROP TABLE IF EXISTS `purchase`;\n"
        + "DROP TABLE IF EXISTS `has_food`;\n"
        + "DROP TABLE IF EXISTS `has_meal`;\n"
        + "SET FOREIGN_KEY_CHECKS=1;" ;

    private static final String queryCreateTables = ""
        + "CREATE TABLE `users` (\n"
        + "    `pid` int NOT NULL,\n"
        + "    `firstname` varchar(50) NOT NULL,\n"
        + "    `lastname` varchar(50) NOT NULL,\n"
        + "    `password` varchar(50) NOT NULL,\n"
        + "    PRIMARY KEY (`pid`)\n"
        + ")\n"
        + "COLLATE='utf8_turkish_ci'\n"
        + "ENGINE=InnoDB\n"
        + ";\n"
        + "\n"
        + "\n"
        + "CREATE TABLE `meal` (\n"
        + "    `mid` INT(10) NOT NULL AUTO_INCREMENT,\n"
        + "    `date` DATE NOT NULL,\n"
        + "    `repast` ENUM('B','L','D') NOT NULL,\n"
        + "    PRIMARY KEY (`mid`)\n"
        + ")\n"
        + "COLLATE='utf8_turkish_ci'\n"
        + "ENGINE=InnoDB\n"
        + "AUTO_INCREMENT=2\n"
        + ";\n"
        + "\n"
        + "\n"
        + "CREATE TABLE `food` (\n"
        + "    `fid` INT(10) NOT NULL AUTO_INCREMENT,\n"
        + "    `food_name` VARCHAR(50) NOT NULL,\n"
        + "    PRIMARY KEY (`fid`)\n"
        + ")\n"
        + "COLLATE='utf8_turkish_ci'\n"
        + "ENGINE=InnoDB\n"
        + "AUTO_INCREMENT=11\n"
        + ";\n"
        + "\n"
        + "\n"
        + "CREATE TABLE `reservations` (\n"
        + "    `pid` INT(10) NOT NULL,\n"
        + "    `mid` INT(10) NOT NULL,\n"
        + "    `refectory` ENUM('ieylul','yemre') NOT NULL,\n"
        + "    PRIMARY KEY (`pid`, `mid`),\n"
        + "    INDEX `FK_reservations_meal` (`mid`),\n"
        + "    CONSTRAINT `FK_reservations_meal` FOREIGN KEY (`mid`) REFERENCES `meal` (`mid`) ON UPDATE CASCADE ON DELETE RESTRICT,\n"
        + "    CONSTRAINT `FK_reservations_users` FOREIGN KEY (`pid`) REFERENCES `users` (`pid`) ON UPDATE CASCADE ON DELETE RESTRICT\n"
        + ")\n"
        + "COLLATE='utf8_turkish_ci'\n"
        + "ENGINE=InnoDB\n"
        + ";\n"
        + "\n"
        + "\n"
        + "CREATE TABLE `purchase` (\n"
        + "    `pid` INT(10) NOT NULL,\n"
        + "    `mid` INT(10) NOT NULL,\n"
        + "    `date` DATE NOT NULL,\n"
        + "    `repast` ENUM('B','L','D') NOT NULL,\n"
        + "    `refectory` ENUM('ieylul','yemre') NOT NULL,\n"
        + "    PRIMARY KEY (`pid`, `mid`, `date`),\n"
        + "    INDEX `FK_purchase_meal` (`mid`),\n"
        + "    CONSTRAINT `FK_purchase_meal` FOREIGN KEY (`mid`) REFERENCES `meal` (`mid`) ON UPDATE CASCADE ON DELETE RESTRICT,\n"
        + "    CONSTRAINT `FK_purchase_users` FOREIGN KEY (`pid`) REFERENCES `users` (`pid`) ON UPDATE CASCADE ON DELETE RESTRICT\n"
        + ")\n"
        + "COLLATE='utf8_turkish_ci'\n"
        + "ENGINE=InnoDB\n"
        + ";\n"
        + "\n"
        + "\n"
        + "CREATE TABLE `has_food` (\n"
        + "    `fid` INT(10) NOT NULL,\n"
        + "    `mid` INT(10) NOT NULL,\n"
        + "    PRIMARY KEY (`fid`, `mid`)\n"
        + ")\n"
        + "COLLATE='utf8_turkish_ci'\n"
        + "ENGINE=InnoDB\n"
        + ";\n"
        + "\n"
        + "\n"
        + "CREATE TABLE `has_meal` (\n"
        + "    `pid` INT(10) NOT NULL,\n"
        + "    `mid` INT(10) NOT NULL,\n"
        + "    `refectory` ENUM('ieylul','yemre') NOT NULL,\n"
        + "    PRIMARY KEY (`pid`, `mid`)\n"
        + ")\n"
        + "COLLATE='utf8_turkish_ci'\n"
        + "ENGINE=InnoDB\n"
        + ";" ;

    private static final String queryPopulateTables = ""
        + "INSERT INTO `food` (`fid`, `food_name`) VALUES\n"
        + "    (11, 'Patlıcan'),\n"
        + "    (12, 'Mercimek Çorbası'),\n"
        + "    (13, 'Pirinç Pilav'),\n"
        + "    (14, 'Puding'),\n"
        + "    (15, 'Poğaça'),\n"
        + "    (16, 'Haşlanmış Yumurta'),\n"
        + "    (17, 'Simit'),\n"
        + "    (18, 'Patates Yemeği'),\n"
        + "    (19, 'Karışık Kızartma'),\n"
        + "    (20, 'Orman Kebabı'),\n"
        + "    (21, 'Keşkül'),\n"
        + "    (22, 'Bulgur Pilavı'),\n"
        + "    (23, 'Yeşil Mercimek'),\n"
        + "    (24, 'Bulgur Pilavı');"
        + "\n"
        + "INSERT INTO `users` (`pid`, `firstname`, `lastname`, `password`) VALUES\n"
        + "    (10000101, 'Can', 'Simit', '1234'),\n"
        + "    (10000102, 'Ayşe', 'Fatma', '1234'),\n"
        + "    (10000103, 'Ahmet', 'Mehmet', '123');\n";
}
