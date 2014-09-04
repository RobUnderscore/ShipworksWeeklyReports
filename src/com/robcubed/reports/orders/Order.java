package com.robcubed.reports.orders;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.joda.time.LocalDate;

public class Order {
	private int orderId;
	private BigDecimal orderTotal;
	private ArrayList<Shipment> shipments;
	private BigDecimal shippingCharged;
	private BigDecimal tax;
	private LocalDate firstShipment;
	private LocalDate orderDate;

	public Order() {
		shipments = new ArrayList<Shipment>();
		orderTotal = new BigDecimal(0);
		shippingCharged = new BigDecimal(0);
		tax = new BigDecimal(0);
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public ArrayList<Shipment> getShipments() {
		return shipments;
	}

	public void setShipments(ArrayList<Shipment> shipments) {
		this.shipments = shipments;
	}

	private ArrayList<Integer> getShipmentIds() {
		ArrayList<Integer> shipmentIds = new ArrayList<>();
		if (!shipments.isEmpty()) {
			for (Shipment shipment : shipments) {
				shipmentIds.add(shipment.getShipmentId());
			}
			return shipmentIds;
		} else {
			return shipmentIds;
		}
	}

	public void addShipment(Shipment shipment) {
		ArrayList<Integer> shipmentIds = getShipmentIds();
		if (!shipmentIds.contains(shipment.getShipmentId())) {
			shipments.add(shipment);
			if (firstShipment == null) {
				firstShipment = shipment.getShipDate();
			} else {
				if (shipment.getShipDate().isBefore(firstShipment)) {
					firstShipment = shipment.getShipDate();
				}
			}
		}
	}

	public BigDecimal getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(BigDecimal orderTotal) {
		this.orderTotal = orderTotal;
	}

	public BigDecimal getShippingCharged() {
		return shippingCharged;
	}

	public void setShippingCharged(BigDecimal shippingCharged) {
		this.shippingCharged = shippingCharged;
	}

	public void addShippingCharged(BigDecimal shipmentCharge) {
		// System.out.println(orderId + " current shipping charged: " +
		// shippingCharged + " and add " + shipmentCharge);
		shippingCharged = shippingCharged.add(shipmentCharge);
		// System.out.println("new shipping charged: " + shippingCharged);
	}

	public BigDecimal getTax() {
		return tax;
	}

	public void setTax(BigDecimal tax) {
		this.tax = tax;
	}

	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", orderTotal=" + orderTotal
				+ ", shipments=" + shipments + ", shippingCharged="
				+ shippingCharged + ", tax=" + tax + "]";
	}

	public LocalDate getFirstShipment() {
		return firstShipment;
	}

	public void setFirstShipment(LocalDate firstShipment) {
		this.firstShipment = firstShipment;
	}

	public LocalDate getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDate orderDate) {
		this.orderDate = orderDate;
	}

}
