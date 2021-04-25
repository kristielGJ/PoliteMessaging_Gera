package Database;

import java.sql.*;

/**
 * For querying:
 * 			DB_Connection conn = new DB_Connection();
 * 			ResultSet rs = conn.query("select * from ....");
 * 			DB_Connection.printQuery(rs);
 * For inserting:
 * 	  	    int output = conn.update("INSERT INTO ... (...,...)
 * 	  	    VALUES ('varName','varName2');");
 */

public class DB_Connection implements DB_Connectivity {
	private final String url = "";
	private final String user = "";
	private final String password = "";
	private final Connection conn;

	public String toString() { return "Connection " + url + " (" + user + ", " + password + ")"; }

	public DB_Connection() {
		conn = connect();
	}

	/**
	 * @return Connection, which can be queried/updated.
	 */
	public Connection connect() {
		Connection dbConn;
		try {
			Class.forName("org.sqlite.JDBC");
			dbConn = DriverManager.getConnection("jdbc:sqlite:PMDB.db");
			System.out.println("Opened database successfully");
			return dbConn;
		}catch (Exception e) {
			System.out.println("Connection failed..");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Forcefully closes the connection to a model.database.
	 * @return Boolean value dependant on whether the action was successful.
	 */
	public boolean closeConnection() {
		try {
			conn.close();
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Executes a query command to the model.database.
	 * @param sql The SQL statement to execute. The statement should query values in the table (i.e. SELECT)
	 * @return An iterable that contains the rows from a successful query.:
	 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/sql/ResultSet.html">ResultSet doc</a>
	 */
	public ResultSet query(String sql) {
		try {
			Statement st;
			st = conn.createStatement();
			return st.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Executes an update command to the database.
	 * @param sql The SQL statement to execute. The statement should update values in the table. (i.e. UPDATE)
	 * @return An integer depending on success. (1) for success and (-1) for unsuccessful.
	 */
	public int update(String sql) {
		int result = -1;
		try {
			Statement st;
			st = conn.createStatement();
			result = st.executeUpdate(sql);
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public int executeUpdateWithId(String sql, PreparedStatement st) {
		int result = -1;
		try {
			result = st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = st.getGeneratedKeys();
			if (rs.next()){
				result = rs.getInt(1);
			}
			rs.close();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public Connection getConn() {
		return this.conn;
	}

	/**
	 * Outputs the rows of a ResultSet in nicer form for testing purposes.
	 * @param rs Takes a ResultSet (obtained from querying).
	 */
	public static void printQuery(ResultSet rs) {
		try {
			ResultSetMetaData resultMeta = rs.getMetaData();
			int columns = resultMeta.getColumnCount();
			int currentEntry = 1;
			while (rs.next()) {
				String result = "[" + currentEntry + "] ";
				for (int i = 1; i <= columns; i++) {
					result += resultMeta.getColumnName(i) + ": " + rs.getString(i) + " ";
				}
				System.out.println(result);
				currentEntry++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		DB_Connection db_connection = new DB_Connection();
		db_connection.connect();
	}
}