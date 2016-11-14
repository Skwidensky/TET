package tet.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseMgr {
	private static final Logger sLog = LoggerFactory.getLogger(DatabaseMgr.class);
	public static DatabaseMgr sDbMgrInstance = null;
	public static ArrayList<String> sQueryResults = new ArrayList<>();

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/";

	// Database credentials
	static final String USER = "admin";
	static final String PASS = "password";

	public static DatabaseMgr getDbMgr() {
		if (sDbMgrInstance == null) {
			sDbMgrInstance = new DatabaseMgr();
		}
		return sDbMgrInstance;
	}

	public static void sqlCommand(boolean pIsQuery, String pColumn, String... pCommands) {
		Connection conn = null;
		Statement stmt = null;
		sQueryResults.clear();
		try {
			sLog.info("Establishing connection to database...");
			// register JDBC driver
			Class.forName(JDBC_DRIVER);
			// open a connection
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();
			stmt.executeUpdate(sSqlCmds.useDb("tet"));
			if (pIsQuery) {
				PreparedStatement Statement = conn.prepareStatement(pCommands[0]);
				ResultSet resultSet = Statement.executeQuery();
				if (pColumn.equals("getRows")) {
					ResultSetMetaData metadata = resultSet.getMetaData();
					int columnCount = metadata.getColumnCount();
					StringJoiner joiner = new StringJoiner(",");
					for (int i = 1; i <= columnCount; i++) {
						joiner.add(metadata.getColumnName(i));
					}
					sQueryResults.add(joiner.toString());
					String row = "";
					while (resultSet.next()) {
						for (int i = 1; i <= columnCount; i++) {
							row += resultSet.getString(i);
						}
						row += "\n";
					}
					sQueryResults.add(row);
				} else if (!pColumn.isEmpty()) {
					while (resultSet.next()) {
						sQueryResults.add(resultSet.getString(pColumn));
					}
				}
				// if a column's name isn't specified, return the table's name
				else {
					while (resultSet.next()) {
						sQueryResults.add(resultSet.getString(1));
					}
				}
			} else {
				for (int i = 0; i < pCommands.length; i++) {
					sLog.info("Executing SQL command " + pCommands[i]);
					stmt.executeUpdate(pCommands[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException se) {
				sLog.error("Couldn't close connection and statement resources", se);
			}
		} // end try
		sLog.info("Finishing database processes");
	}

	public static ArrayList<String> getQuery() {
		return sQueryResults;
	}
}