package com.robcubed.reports.orders;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.joda.time.LocalDate;

public class Store {
	private int storeId;
	private String storeName;
	private ArrayList<Order> orders;

	public Store() {
		orders = new ArrayList<Order>();
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public int getStoreId() {
		return storeId;
	}

	public void setStoreId(int storeId) {
		this.storeId = storeId;
	}

	public ArrayList<Order> getOrders() {
		return orders;
	}

	public void setOrders(ArrayList<Order> orders) {
		this.orders = orders;
	}

	public void addOrder(Order order) {
		orders.add(order);
	}
	
	public void removeOrder(int index) {
		orders.remove(index);
	}

	public void addShipment(int currentOrder, Shipment shipment) {
		getOrders().get(currentOrder).addShipment(shipment);
	}

	public void addShippingCharged(int currentOrder, BigDecimal amount) {
		getOrders().get(currentOrder).addShippingCharged(amount);
	}

	public void addTax(int currentOrder, BigDecimal amount) {
		getOrders().get(currentOrder).setTax(amount);
	}

	public ArrayList<Order> getBetweenDates(LocalDate start, LocalDate end) {
		ArrayList<Order> returnOrders = new ArrayList<>();
		for (Order order : orders) {
			if ((order.getFirstShipment().isBefore(end) || order
					.getFirstShipment().isEqual(end))
					&& (order.getFirstShipment().isAfter(start) || order
							.getFirstShipment().isEqual(start))) {
				returnOrders.add(order);
			}
		}
		return returnOrders;
	}
}
