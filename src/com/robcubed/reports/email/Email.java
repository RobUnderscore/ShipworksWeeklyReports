package com.robcubed.reports.email;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.joda.time.LocalDate;

import com.robcubed.reports.Main;
import com.robcubed.reports.orders.WeeklyStats;

public class Email {
	private String emailRecipient;
	private String emailHostName;
	private int smtpPort;
	private String emailSender;
	private String emailLogin;
	private String emailPassword;
	private boolean emailSSL;
	private boolean startTLS;
	private String htmlMessage;
	private String title;

	public Email(String emailRecipient, String emailHostName, int smtpPort,
			String emailSender, String emailLogin, String emailPassword,
			boolean emailSSL, boolean startTLS) {
		this.emailRecipient = emailRecipient;
		this.emailHostName = emailHostName;
		this.smtpPort = smtpPort;
		this.emailSender = emailSender;
		this.emailLogin = emailLogin;
		this.emailPassword = emailPassword;
		this.emailSSL = emailSSL;
		this.startTLS = startTLS;
	}

	public void sendEmail() {
		HtmlEmail email = new HtmlEmail();
		try {
			email.setHostName(emailHostName);
			email.setSmtpPort(smtpPort);
			email.setAuthenticator(new DefaultAuthenticator(emailLogin,
					emailPassword));
			email.setSSLOnConnect(emailSSL);
			email.setStartTLSEnabled(startTLS);
			email.setFrom(emailSender);
			email.setSubject(title);
			email.setHtmlMsg(htmlMessage);
			email.addTo(emailRecipient);
			email.send();
			System.out.println("Email sent: " + title);
		} catch (EmailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setContent(ArrayList<WeeklyStats> weeklyStats) {
		StringBuilder email = new StringBuilder();
		String storeName = weeklyStats.get(0).getStoreName();
		LocalDate startDate = weeklyStats.get(0).getStartDate();
		LocalDate endDate = weeklyStats.get(0).getEndDate();
		BigDecimal startingTotal = weeklyStats.get(0).getTotal();
		BigDecimal endingTotal = weeklyStats.get(0).getTotal();
		BigDecimal finalTotal = new BigDecimal(0);
		email.append("<h2>");
		email.append(weeklyStats.get(0).getStoreName());
		email.append("</h2>");
		email.append("<table border=1>");
		email.append("<tr><th>Date Range</th><th>Orders</th><th>Shipments</th><th>Revenue</th><th>Shipping Charged</th><th>Shipping Actual</th><th>Tax</th><th>Total</th></tr>");
		for (WeeklyStats stats : weeklyStats) {
			email.append("<tr>");
			if (stats.getStartDate().isBefore(startDate)) {
				startDate = stats.getStartDate();
				startingTotal = stats.getTotal();
			}
			if (stats.getEndDate().isAfter(endDate)) {
				endDate = stats.getEndDate();
				endingTotal = stats.getTotal();
			}
			email.append("<td>" + stats.getStartDate() + " - "
					+ stats.getEndDate() + "</td>");
			email.append("<td>" + stats.getOrdersShipped() + "</td>");
			email.append("<td>" + stats.getShipments() + "</td>");
			email.append("<td>"
					+ stats.getRevenue().setScale(2, RoundingMode.CEILING)
					+ "</td>");
			email.append("<td>"
					+ stats.getShippingCharged().setScale(2,
							RoundingMode.CEILING) + "</td>");
			email.append("<td>"
					+ stats.getShippingPaid().setScale(2, RoundingMode.CEILING)
					+ "</td>");
			email.append("<td>"
					+ stats.getTax().setScale(2, RoundingMode.CEILING)
					+ "</td>");
			email.append("<td>"
					+ stats.getTotal().setScale(2, RoundingMode.CEILING)
					+ "</td>");
			// email.append("<br>" + stats.getStartDate() + " - " +
			// stats.getEndDate() + " | " + stats.getShipments() + " | " +
			// stats.getOrdersShipped() + "</br>");
			email.append("</tr>");
			finalTotal = finalTotal.add(stats.getTotal());
		}
		email.append("</table>");
		
		BigDecimal average = finalTotal.divide(new BigDecimal(Main.getWeeks()));
		//System.out.println(average);

		BigDecimal difference = average.subtract(endingTotal);

		if (endingTotal.compareTo(average) == 0) {
			email.append("<h3>No change in revenue over this time period. Average week was " + average.abs().setScale(2, RoundingMode.CEILING) +"</h3>");
		} else if (endingTotal.compareTo(average) == 1) {
			email.append("<h3>Increased by <font color=\"green\">"
					+ difference.abs().setScale(2, RoundingMode.CEILING)
					+ "</font> over the average weekly of " + average.abs().setScale(2, RoundingMode.CEILING) + "</h3>");
		} else {
			email.append("<h3>Decreased by <font color=\"red\">"
					+ difference.abs().setScale(2, RoundingMode.CEILING)
					+ "</font> under the average weekly of " + average.abs().setScale(2, RoundingMode.CEILING) + "</h3>");
		}
		email.append("<h3>Total over " + Main.getWeeks() + " weeks : "
				+ finalTotal.setScale(2, RoundingMode.CEILING) + "</h3>");
		// show total for all values
		// show growth overall (revenue)
		this.title = storeName + " - " + startDate.toString() + " to "
				+ endDate.toString();
		this.htmlMessage = email.toString();
		//System.out.println(htmlMessage);
	}
}