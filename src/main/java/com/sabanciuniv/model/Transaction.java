package com.sabanciuniv.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Transaction {

	@Id private String id;
	
	
	private String fromId;
	private String toId;
	private double amount;
	private LocalDateTime createDate;
	
	public Transaction(String fromId, String toId, double amount) {
		super();
		this.fromId = fromId;
		this.toId = toId;
		this.amount = amount;
		this.createDate = LocalDateTime.now();
	}
	
	public Transaction()
	{
		super();
		this.createDate = LocalDateTime.now();
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getId() {
		return id;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}
	
}
