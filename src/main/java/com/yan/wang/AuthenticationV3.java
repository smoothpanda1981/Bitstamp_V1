package com.yan.wang;

import com.yan.wang.dao.AuthenticationPojo;
import com.yan.wang.dao.UserTransactionPojo;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * improved code
 */
public class AuthenticationV3 {
        public static void main(String[] args) {
            List<UserTransactionPojo> userTransactionPojoList = loadTransactionsFromDB();
            // Deposit


        }

        public static List<UserTransactionPojo> loadTransactionsFromDB() {
            List<UserTransactionPojo> userTransactionPojoList = new ArrayList<UserTransactionPojo>();
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con= DriverManager.getConnection("jdbc:mysql://localhost:3306/bitstamp?sessionVariables=default_storage_engine=InnoDB&serverTimezone=Europe/Berlin","root","Ouafahwafa79*");

                Statement stmt=con.createStatement();
                ResultSet rs=stmt.executeQuery("select * from Transactions");
                while(rs.next()) {
                    UserTransactionPojo userTransactionPojo = new UserTransactionPojo();
                    userTransactionPojo.setType(rs.getString(1));
                    userTransactionPojo.setDatetime(rs.getString(2));
                    userTransactionPojo.setAccount(rs.getString(3));
                    userTransactionPojo.setAmount(rs.getString(4));
                    userTransactionPojo.setValue(rs.getString(5));
                    userTransactionPojo.setRate(rs.getString(6));
                    userTransactionPojo.setFee(rs.getString(7));
                    userTransactionPojo.setSubtype(rs.getString(8));
                    userTransactionPojoList.add(userTransactionPojo);
                }
                System.out.println(userTransactionPojoList.size());
                con.close();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return userTransactionPojoList;
        }
}
