// Program: BasketBandit (Discord Bot)
// Programmer: Joshua Mark Hunt
// Version: 02/05/2018 - JDK 10.0.1

package basketbandit.core;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Database {

    private Connection connection;

    /**
     * Database constructor.
     */
    public Database() {
        try {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:˜/Dropbox/GitHub/BasketBandit-Java/database/dbBasketBandit");
            ds.setUser("admin");
            ds.setPassword("password");

            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection ("jdbc:h2:~/Dropbox/GitHub/BasketBandit-Java/database/dbBasketBandit;mode=mysql", "admin","password");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initial setup for the database. (Dev only)
     */
    public void setupDatabase() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                "CREATE TABLE `Settings` (" +
                "`id` INT(9) NOT NULL AUTO_INCREMENT,\n" +
                "`server` varchar(18) NOT NULL,\n" +
                "`modDev` BOOLEAN NOT NULL DEFAULT '0',\n" +
                "`modModeration` BOOLEAN NOT NULL DEFAULT '0',\n" +
                "`modMusic` BOOLEAN NOT NULL DEFAULT '0',\n" +
                "`modCustom` BOOLEAN NOT NULL DEFAULT '1',\n" +
                "`modUtility` BOOLEAN NOT NULL DEFAULT '1',\n" +
                "`modLogging` BOOLEAN NOT NULL DEFAULT '0',\n" +
                "`modMath` BOOLEAN NOT NULL DEFAULT '0',\n" +
                "`modFun` BOOLEAN NOT NULL DEFAULT '0',\n" +
                "`modRuneScape` BOOLEAN NOT NULL DEFAULT '0',\n" +
                "PRIMARY KEY (`id`,`server`)\n" +
                ");" +

                "CREATE TABLE `CustomCommands` (\n" +
                "`id` INT(9) NOT NULL AUTO_INCREMENT,\n" +
                "`server` varchar(18) NOT NULL,\n" +
                "`commandName` varchar(10) NOT NULL,\n" +
                "`commandContents` varchar(2000) NOT NULL,\n" +
                "`commandAuthor` varchar(18) NOT NULL, \n" +
                "PRIMARY KEY (`id`)\n" +
                ");\n" +

                "ALTER TABLE `CustomCommands` ADD CONSTRAINT `CustomCommands_fk0` FOREIGN KEY (`server`) REFERENCES `Settings`(`server`) ON DELETE CASCADE;"
            );
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new server to the database and initialises it's settings.
     * @param server the server to add.
     * @return if the add was successful.
     */
    public boolean addNewServer(String server) {
        try {
            Statement statementReturn = connection.createStatement();
            ResultSet resultSet = statementReturn.executeQuery("SELECT id FROM `Settings` WHERE server = " + server);

            if(!resultSet.next()) {
                Statement statementLogic = connection.createStatement();
                statementLogic.executeUpdate("INSERT INTO `Settings` (server) VALUES (" + server + ")");
                return true;
            } else {
                return false;
            }

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all of the server settings for a server.
     * @param server the server id.
     * @return the results of the query.
     */
    public ResultSet getModuleSettings(String server) {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery("SELECT * FROM `Settings` WHERE server = " + server);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks to see if a modules is active before parsing a command.
     * @param modName the name of the modules.
     * @return (boolean) if the modules is active or not.
     */
    boolean checkModuleSettings(String modName, String server) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT " + modName + " FROM `Settings` WHERE server = " + server);

            resultSet.next();
            return resultSet.getBoolean(1);

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Toggles a modules for a server, returns the new value.
     * @param modName the modules to toggle
     * @param server the server in which the modules is to be toggled
     * @return the new value of the setting
     */
    public boolean toggleModule(String modName, String server) {
        try {
            Statement statementLogic = connection.createStatement();
            statementLogic.executeUpdate("UPDATE `Settings` SET " + modName + " = NOT " + modName);

            Statement statementReturn = connection.createStatement();
            ResultSet resultSet = statementReturn.executeQuery("SELECT " + modName + " FROM `Settings` WHERE server = '" + server + "'");

            resultSet.next();
            return resultSet.getBoolean(1);

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a custom command to the database. (Custom commands must be unique.)
     * @param commandName the command name.
     * @param commandContents the contents of the command.
     * @param server the server the command is on.
     * @param commandAuthor the author of the command.
     * @return if the command was added successfully.
     */
    public boolean addCustomCommand(String commandName, String commandContents, String server, String commandAuthor) {
        try {
            Statement statementCheck = connection.createStatement();
            ResultSet resultSet = statementCheck.executeQuery("SELECT commandContents FROM `CustomCommands` WHERE commandName = '" + commandName + "' AND server = '" + server + "'");
            if(resultSet.next()) {
                return false;
            }

            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO `CustomCommands` (server, commandName, commandContents, commandAuthor) VALUES ('" + server + "', '" + commandName + "', '" + commandContents + "', '" + commandAuthor + "')");
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a custom command.
     * @param commandName name of the command.
     * @param server name of the server.
     * @return if the removal was successful.
     */
    public boolean removeCustomCommand(String commandName, String server) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM `CustomCommands` WHERE commandName = '" + commandName + "' AND server = '" + server + "'");
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the contents of a custom command.
     * @param commandName the command name.
     * @param server the server the command is on.
     * @return the command contents.
     */
    public String getCustomCommand(String commandName, String server) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT commandContents FROM `CustomCommands` WHERE commandName = '" + commandName + "' AND server = '" + server + "'");

            if(resultSet.next()) {
                return resultSet.getNString(1);
            } else {
                return "This command does not exist.";
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}