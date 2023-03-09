package com.sabanciuniv.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sabanciuniv.model.Transaction;

public interface TransactionRepository extends MongoRepository<Transaction, String>{

	public List<Transaction> findByFromId(String fromId);
	public List<Transaction> findByToId(String toId);
}
