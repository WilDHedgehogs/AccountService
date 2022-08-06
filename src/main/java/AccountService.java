import AccountExceptions.NotEnoughMoneyException;
import AccountExceptions.UnknownAccountException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountService {

    private final Connection connection;
    private final String selectAmountQuery = "SELECT amount FROM ACCOUNTS WHERE id = ?";
    private final String updateAmountQuery = "UPDATE ACCOUNTS SET amount = ? WHERE id = ?";

    public AccountService(Connection connection) {
        this.connection = connection;
    }

    public Account withdraw(int accountId, int amount) throws NotEnoughMoneyException, UnknownAccountException {
        PreparedStatement statement = null;
        Account account = null;
        try {
            try {
                statement = connection.prepareStatement(selectAmountQuery);
                statement.setInt(1, accountId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int dbAmount = resultSet.getInt("amount");
                    int difference = dbAmount - amount;
                    if (difference < 0) {
                        throw new NotEnoughMoneyException("Недостаточно денег на счету " + accountId +
                                ". В наличии: " + dbAmount + " .Операция на: " + amount);
                    } else {
                        statement = connection.prepareStatement(updateAmountQuery);
                        statement.setInt(1, accountId);
                        statement.setInt(2, difference);
                        statement.executeUpdate();
                        account = new Account(accountId, difference);
                    }
                } else {
                    throw new UnknownAccountException("Не найден аккаунт " + accountId);
                }
            } finally {
                statement.close();
            }
        } catch (SQLException exp) {
            exp.printStackTrace();
        }
        return account;
    }

    public int balance(int accountId) throws UnknownAccountException {
        PreparedStatement statement = null;
        int balance = -1;
        try {
            try {
                statement = connection.prepareStatement(selectAmountQuery);
                statement.setInt(1, accountId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    balance = resultSet.getInt("amount");
                } else {
                    throw new UnknownAccountException("Не найден аккаунт " + accountId);
                }
            } finally {
                statement.close();
            }
        } catch (SQLException exp) {
            exp.printStackTrace();
        }

        return balance;
    }

    public void deposit(int accountId, int amount) throws UnknownAccountException {
        PreparedStatement statement = null;
        try {
            try {
                statement = connection.prepareStatement(selectAmountQuery);
                statement.setInt(1, accountId);
                ResultSet resultSet = statement.executeQuery();
                int dbAmount = 0;
                if (resultSet.next()) {
                    dbAmount += resultSet.getInt("amount");
                } else {
                    throw new UnknownAccountException("Не найден аккаунт " + accountId);
                }
                int sum = amount + dbAmount;
                statement = connection.prepareStatement(updateAmountQuery);
                statement.setInt(1, sum);
                statement.setInt(2, accountId);
                statement.executeUpdate();
                System.out.println("Деньги в количестве " + amount + " отправлены на счет " + accountId +
                        ". Итогое количество: " + sum);
            } finally {
                statement.close();
            }
        } catch (SQLException exp) {
            exp.printStackTrace();
        }
    }

    public void transfer(int from, int to, int amount) throws NotEnoughMoneyException, UnknownAccountException {
        PreparedStatement statement = null;
        try {
            try {
                statement = connection.prepareStatement(selectAmountQuery);
                statement.setInt(1, from);
                ResultSet resultSet = statement.executeQuery();
                int firstAccountAmount = 0;
                int secondAccountAmount = 0;
                if (resultSet.next()) {
                    firstAccountAmount += resultSet.getInt("amount");
                } else {
                    throw new UnknownAccountException("Не найден аккаунт " + from);
                }
                statement.setInt(1, to);
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    secondAccountAmount += resultSet.getInt("amount");
                } else {
                    throw new UnknownAccountException("Не найден аккаунт " + to);
                }

                int difference = firstAccountAmount - amount;
                if (difference < 0 ){
                    throw new NotEnoughMoneyException("Недостаточно денег на счету " + from +
                            ". В наличии: " + firstAccountAmount + " .Операция на: " + amount);
                }

                statement = connection.prepareStatement(updateAmountQuery);

                statement.setInt(1, firstAccountAmount - amount);
                statement.setInt(2, from);
                statement.executeUpdate();
                System.out.println("Деньги в количестве " + amount + " списаны со счета " + from +
                        ". Итогое количество: " + (firstAccountAmount - amount));

                statement.setInt(1, secondAccountAmount + amount);
                statement.setInt(2, to);
                statement.executeUpdate();
                System.out.println("Деньги в количестве " + amount + " отправлены на счет " + to +
                        ". Итогое количество: " + (secondAccountAmount + amount));
            } finally {
                statement.close();
            }
        } catch (SQLException exp) {
            exp.printStackTrace();
        }
    }

}