package com.robcubed.reports;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import com.robcubed.reports.email.Email;
import com.robcubed.reports.orders.Order;
import com.robcubed.reports.orders.Shipment;
import com.robcubed.reports.orders.Store;
import com.robcubed.reports.orders.WeeklyStats;

public class ProcessOrders {

	public static void getStores(Connection conn) throws SQLException {
		// Gathers the store IDs/Names, sets them each in a Store object
		Statement state = conn.createStatement();
		String sql = "SELECT [StoreID],[StoreName] FROM [" + Main.getInstance()
				+ "].[dbo].[Store]";

		ResultSet result = state.executeQuery(sql);

		while (result.next()) {
			Store store = new Store();
			store.setStoreId(result.getInt("StoreID"));
			store.setStoreName(result.getString("StoreName"));
			if (store.getStoreId() != 22005) {
				Main.allOrders.add(store);
			}
		}
		result.close();
	}

	public static void getOrders(Connection conn) {
		// Says getOrders, but we're really building orders out of the related
		// *shipments* that fall into that date.
		// Since we don't bill until we ship, the first ship date is the actual
		// billable date.
		for (Store store : Main.allOrders) {
			Main.endDate = new LocalDate();
			Main.endDate = Main.endDate.withDayOfWeek(DateTimeConstants.MONDAY)
					.plusDays(-1);
			for (int i = 0; i < Main.getWeeks(); i++) {
				try {
					getShipments(store.getStoreId(), conn, Main.endDate);
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				Main.endDate = Main.endDate.plusDays(-7);
			}
		}
	}

	private static void getShipments(int storeId, Connection conn,
			LocalDate endDate) throws SQLException, ParseException {

		Statement state = conn.createStatement();

		LocalDate startDate = endDate.plusDays(-6);
		// System.out.println(startDate + " to " + endDate);

		String sql = "SELECT S.ShipmentID " + ",S.OrderID " + ",S.ShipDate "
				+ ",S.ShipmentCost " + ",O.OrderTotal " + ", O.OrderDate "
				+ ",O.OrderNumberComplete " + ",C.Type " + ",C.Amount "
				+ "FROM [" + Main.getInstance() + "].[dbo].[Shipment] S "
				+ "INNER JOIN [" + Main.getInstance() + "].[dbo].[Order] O "
				+ "ON S.OrderID = O.OrderID " + "LEFT OUTER JOIN ["
				+ Main.getInstance() + "].[dbo].[OrderCharge] C "
				+ "ON S.OrderID = C.OrderID "
				+ "WHERE S.Processed = 1 AND S.ShipDate >= '" + startDate
				+ "' AND S.ShipDate <= '" + endDate + "' " + "AND O.StoreID = "
				+ storeId + " AND S.Voided = 0";

		// System.out.println(sql);

		ResultSet result = state.executeQuery(sql);
		// System.out.println(storeId + " : ");

		String sqlDatePattern = "yyyy-MM-dd HH:mm:ss.SSS";
		int orders = 0;

		// This is actually getting the orders out of the shipment results.
		while (result.next()) {
			Order order = null;
			int storeLoc = 0;
			int currentOrder = -1;
			LocalDate orderDate = LocalDate.parse(
					result.getString("OrderDate"),
					DateTimeFormat.forPattern(sqlDatePattern));
			String shipDateString = result.getString("ShipDate");
			String type = result.getString("TYPE");
			BigDecimal orderTotal = result.getBigDecimal("OrderTotal");
			BigDecimal shipCost = result.getBigDecimal("ShipmentCost");
			BigDecimal amount = result.getBigDecimal("Amount");
			int orderId = result.getInt("OrderID");
			int shipmentId = result.getInt("ShipmentID");

			for (Store storeCheck : Main.allOrders) {
				if (storeCheck.getStoreId() == storeId) {
					storeLoc = Main.allOrders.indexOf(storeCheck);
				}
			}

			// Check to see if this is a shipment for an order we've already
			// processed this run.
			for (Order checkOrder : Main.allOrders.get(storeLoc).getOrders()) {
				if (checkOrder.getOrderId() == orderId) {
					currentOrder = Main.allOrders.get(storeLoc).getOrders()
							.indexOf(checkOrder);
					// System.out.println("Old order - " +
					// allOrders.get(storeLoc).getOrders().get(currentOrder).getOrderId());
				}
			}

			// So if this is a 'new' order, let's make it.
			if (currentOrder == -1) {
				order = new Order();
				order.setOrderId(orderId);
				order.setOrderDate(orderDate);
				Main.allOrders.get(storeLoc).addOrder(order);
				currentOrder = Main.allOrders.get(storeLoc).getOrders().size() - 1;
				// System.out.println("New order - " +
				// allOrders.get(storeLoc).getOrders().get(currentOrder).getOrderId()
				// + " " + orderTotalString);
				Main.allOrders.get(storeLoc).getOrders().get(currentOrder)
						.setOrderTotal(orderTotal);
				orders = orders + 1;
			}

			// Now we make a shipment, regardless of whether or not this is a
			// 'new' order
			Shipment shipment = new Shipment();
			shipment.setShipmentId(shipmentId);
			shipment.setShipDate(LocalDate.parse(shipDateString,
					DateTimeFormat.forPattern(sqlDatePattern)));
			shipment.setShipCost(shipCost);

			int currentShipment = -1;
			// Neeed to see if it's an already existing shipment.
			for (Shipment checkShipment : Main.allOrders.get(storeLoc)
					.getOrders().get(currentOrder).getShipments()) {
				if (checkShipment.getShipmentId() == shipmentId) {
					currentShipment = Main.allOrders.get(storeLoc).getOrders()
							.get(currentOrder).getShipments()
							.indexOf(checkShipment);
					// System.out.println("Found Shipment: " +
					// currentShipment +
					// " - " +
					// allOrders.get(storeLoc).getOrders().get(currentOrder).getShipments().get(currentShipment).getShipCost()
					// + " + " + shipCost);
					if (type != null) {
						if (type.equals("SHIPPING")) {
							Main.allOrders.get(storeLoc).addShippingCharged(
									currentOrder, amount);
						} else if (type.equals("TAX")) {
							Main.allOrders.get(storeLoc).addTax(currentOrder,
									amount);
						}
					}
				}
			}

			// So if it's a new one, we need to add a charge.
			if (currentShipment == -1) {
				Main.allOrders.get(storeLoc)
						.addShipment(currentOrder, shipment);// getOrders().get(currentOrder).addShipment(shipment);
				// System.out.println(allOrders.get(storeLoc).getOrders().get(currentOrder));
				if (type != null) {
					if (type.equals("SHIPPING")) {
						Main.allOrders.get(storeLoc).addShippingCharged(
								currentOrder, amount);
					}
				}
			}

			// System.out.println(shipment.toString());
			// System.out.println(allOrders.get(storeLoc).getStoreName() + " " +
			// order);
		}
		// System.out.println("Total orders for this set: " + orders);
		result.close();
	}

	public static void clearOlderShipments(Connection conn) {
		for (Store store : Main.allOrders) {
			/*
			 * Get orders placed PRIOR to the 'first date' and run a query for
			 * other shipping dates... and add extra shipping dates prior to
			 * earliest date. This is because we can't bill it if it was billed
			 * on an earlier date. This would lead to inaccurate revenue report.
			 */
			int storeIndex = Main.allOrders.indexOf(store);
			LocalDate startDate = Main.endDate.plusDays(-6);
			String sqlDatePattern = "yyyy-MM-dd HH:mm:ss.SSS";
			ArrayList<Integer> ordersToRemove = new ArrayList<>();
			for (Order order : store.getOrders()) {
				int orderIndex = Main.allOrders.get(storeIndex).getOrders()
						.indexOf(order);
				// System.out.println();
				if (order.getOrderDate().isBefore(startDate)) {
					// get all shipments for this order number
					// System.out.println("Earlier Order: "
					// + order.getOrderId() + " "
					// + order.getOrderDate());
					Statement stmt;
					try {
						stmt = conn.createStatement();
						String sql = "SELECT [ShipmentID],[OrderID],[ShipDate] FROM [ShipWorks].[dbo].[Shipment] WHERE OrderID = "
								+ order.getOrderId();
						ResultSet result = stmt.executeQuery(sql);
						while (result.next()) {
							LocalDate shipDate = LocalDate.parse(
									result.getString("ShipDate"),
									DateTimeFormat.forPattern(sqlDatePattern));
							if (shipDate.isBefore(startDate)) {
								// System.out.println("Earlier shipment here. "
								// + shipDate.toString());
								if (!ordersToRemove.contains(orderIndex)) {
									ordersToRemove.add(orderIndex);
								}
							}
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

			Collections.sort(ordersToRemove, Collections.reverseOrder());

			for (int index : ordersToRemove) {
				Main.allOrders.get(storeIndex).removeOrder(index);
				// System.out.println("Order removed!");
			}

		}
	}

	public static void finalCalculations() {
		// Actually calculating everything out.
		for (Store store : Main.allOrders) {
			ArrayList<WeeklyStats> allWeeklyStats = new ArrayList<>();
			Main.endDate = new LocalDate();
			Main.endDate = Main.endDate.withDayOfWeek(DateTimeConstants.MONDAY)
					.plusDays(-1);
			for (int i = 0; i < Main.getWeeks(); i++) {
				LocalDate startDate = Main.endDate.plusDays(-6);
				WeeklyStats weeklyStats = new WeeklyStats(store.getStoreName(),
						startDate, Main.endDate);
				ArrayList<Order> betweenDates = store.getBetweenDates(
						startDate, Main.endDate);
				// System.out.println();
				// System.out.println("===========================================================");
				// System.out.println(store.getStoreName() + " : " +
				// startDate.toString() + " to " + endDate.toString());
				BigDecimal total = new BigDecimal(0);
				BigDecimal tax = new BigDecimal(0);
				BigDecimal shippingCharges = new BigDecimal(0);
				BigDecimal shippingCharged = new BigDecimal(0);
				// System.out.println(store.getStoreId() + " : "
				// + store.getStoreName());
				int numShipments = 0;
				for (Order order : betweenDates) {
					// System.out.println("\t\t\t\t" + order.toString());
					// System.out.println(order.getOrderId() + " " +
					// order.getShipments().size());
					total = total.add(order.getOrderTotal());
					tax = tax.add(order.getTax());
					shippingCharged = shippingCharged.add(order
							.getShippingCharged());
					numShipments = numShipments + order.getShipments().size();
					for (Shipment shipment : order.getShipments()) {
						shippingCharges = shippingCharges.add(shipment
								.getShipCost());
						weeklyStats.addShippingPaid(shipment.getShipCost());
						weeklyStats.addShipment();
					}
					weeklyStats.addRevenue(order.getOrderTotal());
					weeklyStats.addShippingCharged(order.getShippingCharged());
					weeklyStats.addTax(order.getTax());
					weeklyStats.addOrder();
				}
				/*
				 * System.out.println("\t" + betweenDates.size() + " orders | "
				 * + numShipments + " shipments.");
				 * System.out.println("\t\t total: " + total);
				 * System.out.println("\t\t tax charged: " + tax);
				 * System.out.println("\t\t shippingCharges: " +
				 * shippingCharges); System.out.println("\t\t shippingCharged: "
				 * + shippingCharged);
				 */
				allWeeklyStats.add(weeklyStats);
				Main.endDate = Main.endDate.plusDays(-7);
			}
			// System.out.println(allWeeklyStats.toString());

			Email email = new Email(Main.getEmailRecipient(),
					Main.getEmailHostName(), Main.getSmtpPort(),
					Main.getEmailSender(), Main.getEmailLogin(),
					Main.getEmailPassword(), Main.isEmailSSL(),
					Main.isStartTLS());
			email.setContent(allWeeklyStats);
			email.sendEmail();
		}
	}

}
