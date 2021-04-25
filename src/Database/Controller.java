package Database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The main class which acts as a bridge between the database logic and the GUI.
 * Things such as the main DB connection instance, transactions between database and client
 * and schedulers (and their respective thread pools) are stored here.
 */

public class Controller {


	public Controller() { }
}