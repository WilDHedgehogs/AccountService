import AccountExceptions.NotEnoughMoneyException;
import AccountExceptions.UnknownAccountException;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.Random;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Tests {

    private final String connectionUrl = "jdbc:h2:mem:test;" +
            "INIT=RUNSCRIPT FROM 'src/schema.sql'\\;" +
            "RUNSCRIPT FROM 'src/data.sql'";
    private Connection connection;
    private AccountService service;

    @BeforeAll
    public void init() {
        try {
            connection = DriverManager.getConnection(connectionUrl);
            service = new AccountService(connection);
        } catch (SQLException exp) {
            exp.printStackTrace();
        }
    }

    @AfterAll
    public void release() {
        try {
            connection.close();
        } catch (SQLException exp) {
            exp.printStackTrace();
        }
    }

    @Test
    @Order(1)
    public void testConnection() {
        Assertions.assertNotNull(connection);
    }

    @Test
    @Order(2)
    public void testStatement() {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNTS");
//            while (resultSet.next()) {
//                System.out.println("Account: " + resultSet.getInt("id") +
//                        " Amount: " + resultSet.getInt("amount"));
//            }
            Assertions.assertTrue(resultSet.next());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(3)
    public void testBalance() {
        try {
            Integer balance = service.balance(new Random().nextInt(9));
            Assertions.assertNotNull(balance);
        } catch (UnknownAccountException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(4)
    public void testWithdraw() {
        int id = new Random().nextInt(9);
        try {
            int balance = service.balance(id);
            int newBalance = balance - id;
            Account account = service.withdraw(id, id);
            Assertions.assertEquals(newBalance, account.getAmount());
        } catch (UnknownAccountException | NotEnoughMoneyException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(5)
    public void testDeposit() {
        int id = new Random().nextInt(9);
        try {
            int balance = service.balance(id);
            int newBalance = balance + id;
            service.deposit(id, id);
            Assertions.assertEquals(newBalance, service.balance(id));
        } catch (UnknownAccountException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(5)
    public void testTransfer() {
        int firstId = -1;
        int secondId = -1;
        for (int i = 0; i < 10; i++) {
            firstId = new Random().nextInt(9);
            secondId = new Random().nextInt(9);

            if (firstId != secondId) {
                break;
            }
        }
        try {
            int firstBalance = service.balance(firstId);
            int secondBalance = service.balance(secondId);
            int amount = new Random().nextInt(firstBalance);
            int newFirstBalance = firstBalance - amount;
            int newSecondBalance = secondBalance + amount;

            service.transfer(firstId, secondId, amount);

            Assertions.assertEquals(newFirstBalance, service.balance(firstId));
            Assertions.assertEquals(newSecondBalance, service.balance(secondId));
        } catch (UnknownAccountException | NotEnoughMoneyException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(6)
    public void testIncorrectBalanceID() {
        int incorrectID = -1;
        try {
            service.balance(incorrectID);
            throw new AssertionError("Пользователь должен быть не найден");
        } catch (UnknownAccountException e) {
            if (!e.getMessage().contains("Не найден аккаунт " + incorrectID)) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    @Order(7)
    public void testExceedWithdraw() {
        try {
            int id = new Random().nextInt(9);
            int balance = service.balance(id);
            int exceedBalance = balance + id + 1; //+1 для нулевого id
            service.withdraw(id, exceedBalance);
            throw new AssertionError("Денег должно быть не достаточно для снятия");
        } catch (UnknownAccountException e) {
            throw new RuntimeException(e);
        } catch (NotEnoughMoneyException e) {
            if (!e.getMessage().contains("Недостаточно денег на счету")) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    @Order(8)
    public void testExceedTransfer() {
        int firstId = -1;
        int secondId = -1;
        for (int i = 0; i < 10; i++) {
            firstId = new Random().nextInt(9);
            secondId = new Random().nextInt(9);

            if (firstId != secondId) {
                break;
            }
        }
        try {
            int firstBalance = service.balance(firstId);
            int exceedAmount = firstBalance + firstId + 1; //+1 для нулевого id
            service.transfer(firstId, secondId, exceedAmount);
            throw new AssertionError("Денег должно быть не достаточно для перевода");
        } catch (UnknownAccountException e) {
            throw new RuntimeException(e);
        } catch (NotEnoughMoneyException e) {
            if (!e.getMessage().contains("Недостаточно денег на счету")) {
                throw new RuntimeException(e);
            }
        }
    }

}
