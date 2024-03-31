package de.nauriculus.bonkraids.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MySQL {
	
	public static String HOST = "";
	public static String DATABASE = "";
	public static String USER = "";
	public static String PASSWORD = "";	

	public static Connection con;

	public MySQL(String host, String database, String user, String password) {
		HOST = host;
		DATABASE = database;
		USER = user;
		PASSWORD = password;
		connect();
	}

	public void connect() {
		try {
			con = DriverManager.getConnection("jdbc:mysql://" + HOST + ":3306/" + DATABASE + "?autoReconnect=true", USER, PASSWORD);
			System.out.println("MySQL connection was successful.");
		} catch (SQLException e) {	
			System.out.println("Error when trying to connect to MySQL " + e.getMessage());
		}
	}

	public void close() {
		try {
			if (con != null) {
				System.out.println("MySQL connection was closed!");
				con.close();
			}
		} catch (SQLException localSQLException) {
		}
	}

	public void update(String qry) {
		try {
			Statement st = con.createStatement();
			st.executeUpdate(qry);
			st.close();
		} catch (SQLException e) {
			connect();
			System.err.println(e);
		}
	}
	
	public void update(String qry, List<Object> params) {
		try {
			PreparedStatement st = (PreparedStatement) con.prepareStatement(qry);
			for (int i = 0; i < params.size(); i++) {
				st.setObject(i + 1, params.get(i));
			}
			st.executeUpdate();
			st.close();
		} catch (SQLException e) {
			connect();
			System.err.println(e);
		}
	}

	public ResultSet query(String qry) {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(qry);
		} catch (SQLException e) {
			connect();
			System.err.println(e);
		}
		return rs;
	}
}
