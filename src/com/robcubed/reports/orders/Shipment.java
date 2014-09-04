package com.robcubed.reports.orders;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class Shipment {
	private int shipmentId;
	private LocalDate shipDate;
	private BigDecimal shipCost;

	public Shipment() {
		shipCost = new BigDecimal(0);
	}

	public int getShipmentId() {
		return shipmentId;
	}

	public void setShipmentId(int shipmentId) {
		this.shipmentId = shipmentId;
	}

	public LocalDate getShipDate() {
		return shipDate;
	}

	public void setShipDate(LocalDate shipDate) {
		this.shipDate = shipDate;
	}

	public BigDecimal getShipCost() {
		return shipCost;
	}

	public void setShipCost(BigDecimal shipCost) {
		this.shipCost = shipCost;
	}

	@Override
	public String toString() {
		return shipmentId + " : " + shipDate + " at $" + shipCost;
	}

}
