package com.sabanciuniv.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sabanciuniv.model.Account;

public interface AccountRepository extends MongoRepository<Account, String>{

	
}
