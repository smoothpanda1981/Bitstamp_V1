package com.yan.wang;

import com.yan.wang.dao.AuthenticationPojo;
import com.yan.wang.dao.UserTransactionPojo;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
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
            computeTotalDeposit(userTransactionPojoList);
            // Withdrawal
            computeTotalWithdrawal(userTransactionPojoList);
            // Sub Account
            computeSubAccountTotal(userTransactionPojoList);
            // Market
            computeBuyAndSellTotal(userTransactionPojoList);
        }

    public static void computeBuyAndSellTotal(List<UserTransactionPojo> userTransactionPojoList) {
        BigDecimal totalBuyAndSellUSD = new BigDecimal("0.00");
        BigDecimal totalBuyAndSellEUR = new BigDecimal("0.00");
        BigDecimal totalBuyAndSellUSDFee = new BigDecimal("0.00");
        BigDecimal totalBuyAndSellEURFee = new BigDecimal("0.00");

        for (UserTransactionPojo transactionPojo : userTransactionPojoList) {
            if (transactionPojo.getType().equals("Market")) {
                String[] buyAndSellValueTab = transactionPojo.getValue().split(" ");
                BigDecimal buyAndSellValue = new BigDecimal(buyAndSellValueTab[0]);

                String[] buyAndSellFeeTab;
                BigDecimal buyAndSellFee;
                if (transactionPojo.getFee() == null || transactionPojo.getFee().equals("")) {
                    buyAndSellFee = new BigDecimal("0.00");
                } else {
                    buyAndSellFeeTab = transactionPojo.getFee().split(" ");
                    buyAndSellFee = new BigDecimal(buyAndSellFeeTab[0]);
                }

                if (buyAndSellValueTab[1].equals("USD")) {
                    totalBuyAndSellUSDFee = totalBuyAndSellUSDFee.add(buyAndSellFee);

                    if (transactionPojo.getSubtype().equals("Buy")) {
                        totalBuyAndSellUSD = totalBuyAndSellUSD.subtract(buyAndSellValue);
                    } else if (transactionPojo.getSubtype().equals("Sell")) {
                        totalBuyAndSellUSD = totalBuyAndSellUSD.add(buyAndSellValue);
                    } else {
                        System.out.println("Weird, operation is not Buy or Sell.");
                    }
                } else if (buyAndSellValueTab[1].equals("EUR")) {
                    totalBuyAndSellEURFee = totalBuyAndSellEURFee.add(buyAndSellFee);

                    if (transactionPojo.getSubtype().equals("Buy")) {
                        totalBuyAndSellEUR = totalBuyAndSellEUR.subtract(buyAndSellValue);
                    } else if (transactionPojo.getSubtype().equals("Sell")) {
                        totalBuyAndSellEUR = totalBuyAndSellEUR.add(buyAndSellValue);
                    } else {
                        System.out.println("Weird, operation is not Buy or Sell.");
                    }
                } else {
                    System.out.println("Weird, currency is not USD or EUR.");
                }
            }
        }
        System.out.println("Total Buy and Sell USD = " + totalBuyAndSellUSD + " USD");
        System.out.println("Total Buy and Sell Fee USD = " + totalBuyAndSellUSDFee + " USD");
        System.out.println("Total Buy and Sell EUR = " + totalBuyAndSellEUR + " EUR");
        System.out.println("Total Buy and Sell Fee EUR = " + totalBuyAndSellEURFee + " EUR");
    }


    public static void computeSubAccountTotal(List<UserTransactionPojo> userTransactionPojoList) {
        BigDecimal totalSubAccountTransferUSD = new BigDecimal("0.00");
        BigDecimal totalSubAccountTransferEUR = new BigDecimal("0.00");
        BigDecimal totalSubAccountTransferBTC = new BigDecimal("0.00");

        for (UserTransactionPojo transactionPojo : userTransactionPojoList) {
            if (transactionPojo.getType().equals("Sub Account Transfer")) {
                String[] subAccountTransferAmountTab = transactionPojo.getAmount().split(" ");
                BigDecimal subAccountTransferAmount = new BigDecimal(subAccountTransferAmountTab[0]);

                if (subAccountTransferAmountTab[1].equals("USD")) {
                    if (transactionPojo.getValue().equals("Addition")) {
                        totalSubAccountTransferUSD = totalSubAccountTransferUSD.add(subAccountTransferAmount);
                    } else if (transactionPojo.getValue().equals("Subtraction")) {
                        totalSubAccountTransferUSD = totalSubAccountTransferUSD.subtract(subAccountTransferAmount);
                    } else {
                        System.out.println("Weird, Operation is not Addition or Subtraction.");
                    }
                } else if (subAccountTransferAmountTab[1].equals("EUR")) {
                    if (transactionPojo.getValue().equals("Addition")) {
                        totalSubAccountTransferEUR = totalSubAccountTransferEUR.add(subAccountTransferAmount);
                    } else if (transactionPojo.getValue().equals("Subtraction")) {
                        totalSubAccountTransferEUR = totalSubAccountTransferEUR.subtract(subAccountTransferAmount);
                    } else {
                        System.out.println("Weird, Operation is not Addition or Subtraction.");
                    }
                } else if (subAccountTransferAmountTab[1].equals("BTC")) {
                    if (transactionPojo.getValue().equals("Addition")) {
                        totalSubAccountTransferBTC = totalSubAccountTransferBTC.add(subAccountTransferAmount);
                    } else if (transactionPojo.getValue().equals("Subtraction")) {
                        totalSubAccountTransferBTC = totalSubAccountTransferBTC.subtract(subAccountTransferAmount);
                    } else {
                        System.out.println("Weird, Operation is not Addition or Subtraction.");
                    }
                } else {
                    System.out.println("Weird, currency is not USD or EUR.");
                }
            }
        }

        if (totalSubAccountTransferUSD.compareTo(BigDecimal.ZERO) != 0) {
            System.out.println("Total Sub Account Transfer USD = " + totalSubAccountTransferUSD + " USD");
        }
        if (totalSubAccountTransferEUR.compareTo(BigDecimal.ZERO) != 0) {
            System.out.println("Total Sub Account Transfer USD = " + totalSubAccountTransferEUR + " EUR");
        }
        if (totalSubAccountTransferBTC.compareTo(BigDecimal.ZERO) != 0) {
            System.out.println("Total Sub Account Transfer BTC = " + totalSubAccountTransferBTC + " BTC");
        }
    }

    public static void computeTotalWithdrawal(List<UserTransactionPojo> userTransactionPojoList) {
        BigDecimal totalWithdrawalUSD = new BigDecimal("0.00");
        BigDecimal totalWithdrawalEUR = new BigDecimal("0.00");
        BigDecimal totalWithdrawalUSDFee = new BigDecimal("0.00");
        BigDecimal totalWithdrawalEURFee = new BigDecimal("0.00");

        for (UserTransactionPojo transactionPojo : userTransactionPojoList) {
            if (transactionPojo.getType().equals("Withdrawal")) {
                String[] withdrawalAmountTab = transactionPojo.getAmount().split(" ");
                BigDecimal depositAmount = new BigDecimal(withdrawalAmountTab[0]);

                String[] withdarawalFeeTab = transactionPojo.getFee().split(" ");
                BigDecimal depositFee = new BigDecimal(withdarawalFeeTab[0]);

                if (withdrawalAmountTab[1].equals("USD")) {
                    totalWithdrawalUSD = totalWithdrawalUSD.add(depositAmount);
                    totalWithdrawalUSDFee = totalWithdrawalUSDFee.add(depositFee);
                } else if (withdrawalAmountTab[1].equals("EUR")) {
                    totalWithdrawalEUR = totalWithdrawalEUR.add(depositAmount);
                    totalWithdrawalEURFee = totalWithdrawalEURFee.add(depositFee);
                } else {
                    System.out.println("Weird, currency is not USD or EUR.");
                }
            }
        }
        System.out.println("Total Withdrawal USD = " + totalWithdrawalUSD + " USD");
        System.out.println("Total Withdrawal Fee USD = " + totalWithdrawalUSDFee + " USD");
        System.out.println("Total Withdrawal EUR = " + totalWithdrawalEUR + " EUR");
        System.out.println("Total Withdrawal Fee EUR = " + totalWithdrawalEURFee + " EUR");
    }

        public static void computeTotalDeposit(List<UserTransactionPojo> userTransactionPojoList) {
            BigDecimal totalDepositUSD = new BigDecimal("0.00");
            BigDecimal totalDepositEUR = new BigDecimal("0.00");
            for (UserTransactionPojo transactionPojo : userTransactionPojoList) {
                if (transactionPojo.getType().equals("Deposit")) {
                    String[] depositAmountTab = transactionPojo.getAmount().split(" ");
                    BigDecimal depositAmount = new BigDecimal(depositAmountTab[0]);
                    if (depositAmountTab[1].equals("USD")) {
                        totalDepositUSD = totalDepositUSD.add(depositAmount);
                    } else if (depositAmountTab[1].equals("EUR")) {
                        totalDepositEUR = totalDepositEUR.add(depositAmount);
                    } else {
                        System.out.println("Weird, currency is not USD or EUR.");
                    }
                }
            }
            System.out.println("Total Deposit USD = " + totalDepositUSD + " USD");
            System.out.println("Total Deposit EUR = " + totalDepositEUR + " EUR");
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
