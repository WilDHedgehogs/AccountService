import AccountExceptions.NotEnoughMoneyException;
import AccountExceptions.UnknownAccountException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConsoleApp {

    private final static String connectionUrl = "jdbc:h2:mem:test;" +
            "INIT=RUNSCRIPT FROM 'src/schema.sql'\\;" +
            "RUNSCRIPT FROM 'src/data.sql'";
    private static Connection connection;
    private static AccountService service;

    public static void main(String[] args) {

        BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));

        try {
            connection = DriverManager.getConnection(connectionUrl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        service = new AccountService(connection);

        boolean isOver = false;

        while (!isOver) {
            System.out.print("Введите команду: ");
            String line;
            try {
                line = inp.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String[] params = line.split(" ");
            String command = params[0];

            switch (command) {
                case "balance":
                    try {
                        int id = Integer.parseInt(params[1]);
                        int balance = service.balance(id);
                        System.out.println("Баланс пользователя с id " + id + " = " + balance);
                    } catch (UnknownAccountException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "withdraw":
                    try {
                        int id = Integer.parseInt(params[1]);
                        int amount = Integer.parseInt(params[2]);
                        Account account = service.withdraw(id, amount);
                        System.out.println("Пользователь с id " + account.getId() + " снял " + amount +
                                " .Текущий остаток " + account.getAmount());
                    } catch (NotEnoughMoneyException | UnknownAccountException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "deposit":
                    try {
                        int id = Integer.parseInt(params[1]);
                        int amount = Integer.parseInt(params[2]);
                        service.deposit(id, amount);
                    } catch (UnknownAccountException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "transfer":
                    try {
                        int from = Integer.parseInt(params[1]);
                        int to = Integer.parseInt(params[2]);
                        int amount = Integer.parseInt(params[3]);
                        service.transfer(from, to, amount);
                        System.out.println("Совершён перевод с аккаунта " + from + " на аккаут " + to +
                                " в размере " + amount);
                    } catch (NotEnoughMoneyException | UnknownAccountException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "exit":
                    isOver = true;
                    break;
                default:
                    System.out.println("""
                            Неизвестная команда
                            Доступлные команды:
                            balance [id]
                            withdraw [id] [amount]
                            deposit [id] [amount]
                            transfer [from] [to] [amount]
                            exit""");
                    break;
            }
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        service = null;

    }

}
