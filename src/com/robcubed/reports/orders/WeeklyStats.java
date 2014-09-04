package com.robcubed.reports.orders;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class WeeklyStats {
	private String storeName;
	private LocalDate startDate;
	private LocalDate endDate;
	private int ordersShipped;
	private int shipments;
	private BigDecimal revenue;
	private BigDecimal total;
	private BigDecimal shippingPaid;

	public BigDecimal getShippingPaid() {
		return shippingPaid;
	}

	public void setShippingPaid(BigDecimal shippingPaid) {
		this.shippingPaid = shippingPaid;
	}

	private BigDecimal shippingCharged;

	public BigDecimal getShippingCharged() {
		return shippingCharged;
	}

	public void setShippingCharged(BigDecimal shippingCharged) {
		this.shippingCharged = shippingCharged;
	}

	private BigDecimal tax;

	public WeeklyStats(String storeName, LocalDate startDate, LocalDate endDate) {
		this.storeName = storeName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.shipments = 0;
		this.revenue = new BigDecimal(0);
		this.shippingPaid = new BigDecimal(0);
		this.shippingCharged = new BigDecimal(0);
		this.total = new BigDecimal(0);
		this.tax = new BigDecimal(0);
		this.ordersShipped = 0;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public int getShipments() {
		return shipments;
	}

	public void setShipments(int shipments) {
		this.shipments = shipments;
	}

	public BigDecimal getRevenue() {
		return revenue;
	}

	public void setRevenue(BigDecimal revenue) {
		this.revenue = revenue;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public void addRevenue(BigDecimal revenue) {
		this.revenue = this.revenue.add(revenue);
		this.total = this.total.add(revenue);
	}

	public void addShippingPaid(BigDecimal shippingPaid) {
		this.shippingPaid = this.shippingPaid.add(shippingPaid);
		this.total = this.total.subtract(shippingPaid);
	}

	public void addShippingCharged(BigDecimal shippingCharged) {
		this.shippingCharged = this.shippingCharged.add(shippingCharged);
	}

	public void addTax(BigDecimal tax) {
		this.tax = this.tax.add(tax);
		this.total = this.total.subtract(tax);
	}

	public void addShipment() {
		this.shipments++;
	}

	@Override
	public String toString() {
		return "WeeklyStats [storeName=" + storeName + ", startDate="
				+ startDate + ", endDate=" + endDate + ", shipments="
				+ shipments + ", revenue=" + revenue + ", shippingPaid="
				+ shippingPaid + ", shippingCharged=" + shippingCharged
				+ ", tax=" + tax + ", total=" + total + "]\n"
				+ "Shipping Net : " + shippingCharged.subtract(shippingPaid);
	}

	public BigDecimal getTax() {
		return tax;
	}

	public void setTax(BigDecimal tax) {
		this.tax = tax;
	}

	public int getOrdersShipped() {
		return ordersShipped;
	}

	public void addOrder() {
		this.ordersShipped++;
	}
}
