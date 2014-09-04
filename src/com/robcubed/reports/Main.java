package com.robcubed.reports;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Properties;

import org.joda.time.LocalDate;

import com.robcubed.reports.orders.Store;

public class Main {
	static public LocalDate endDate;
	static public ArrayList<Store> allOrders;

	// Variables from the configuration file
	static private int weeksToGoBack;
	static private String userName;
	static private String password;
	static private String serverUrl;
	static private String database;
	static private String instance;
	static private String emailRecipient;
	static private String emailHostName;
	static private int smtpPort;
	static private String emailSender;
	static private String emailLogin;
	static private String emailPassword;
	static private boolean emailSSL;
	static private boolean startTLS;

	public static void main(String[] args) {
		Properties properties = new Properties();
		InputStream input = null;
		String configFile = args[0];

		try {
			input = new FileInputStream(configFile);

			properties.load(input);
			weeksToGoBack = Integer.parseInt(properties
					.getProperty("weeksToGoBack"));
			userName = properties.getProperty("userName");
			password = properties.getProperty("password");
			serverUrl = properties.getProperty("serverUrl");
			database = properties.getProperty("database");
			instance = properties.getProperty("instance");
			emailRecipient = properties.getProperty("emailRecipient");
			emailHostName = properties.getProperty("emailHostName");
			smtpPort = Integer.parseInt(properties.getProperty("smtpPort"));
			emailSender = properties.getProperty("emailSender");
			emailLogin = properties.getProperty("emailLogin");
			emailPassword = properties.getProperty("emailPassword");
			emailSSL = Boolean.parseBoolean(properties.getProperty("emailSSL"));
			startTLS = Boolean.parseBoolean(properties.getProperty("startTLS"));
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		String url = "jdbc:jtds:sqlserver://" + serverUrl + "/" + database
				+ ";instance=" + instance;
		allOrders = new ArrayList<>();

		endDate = new LocalDate();
		// System.out.println("End Date : " + endDate);
		Connection conn;

		try {
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
			conn = DriverManager.getConnection(url, userName, password);
			System.out.println("Connection successful");

			ProcessOrders.getStores(conn);
			ProcessOrders.getOrders(conn);
			ProcessOrders.clearOlderShipments(conn);
			ProcessOrders.finalCalculations();
		} catch (Exception e) {
			System.err.println("Cannot connect to database server");
			e.printStackTrace();
		}
	}
	
	public static String getInstance() {
		return instance;
	}

	public static int getWeeks() {
		return weeksToGoBack;
	}

	public static String getEmailRecipient() {
		return emailRecipient;
	}

	public static String getEmailHostName() {
		return emailHostName;
	}

	public static int getSmtpPort() {
		return smtpPort;
	}

	public static String getEmailSender() {
		return emailSender;
	}

	public static String getEmailLogin() {
		return emailLogin;
	}

	public static String getEmailPassword() {
		return emailPassword;
	}

	public static boolean isEmailSSL() {
		return emailSSL;
	}

	public static boolean isStartTLS() {
		return startTLS;
	}
}