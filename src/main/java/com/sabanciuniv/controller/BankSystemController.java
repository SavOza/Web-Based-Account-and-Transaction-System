package com.sabanciuniv.controller;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sabanciuniv.model.Account;
import com.sabanciuniv.model.Transaction;
import com.sabanciuniv.repo.AccountRepository;
import com.sabanciuniv.repo.TransactionRepository;

@RestController
public class BankSystemController {

	@Autowired private AccountRepository accountRepository;
	@Autowired private TransactionRepository transactionRepository;
	
	interface Message{};
	
	private static final Logger logger = LoggerFactory.getLogger(BankSystemController.class);
	
	@PostConstruct
	private void init() {
		
		//delete all existing data and create test data upon initialization
		if(accountRepository.count() != 0)
		{
			accountRepository.deleteAll();
			transactionRepository.deleteAll();
		}
		
		Account acc1 = new Account("1111", "John");
		Account acc2 = new Account("2222", "Jack");
		
		accountRepository.insert(acc1);
		accountRepository.insert(acc2);
		
		Transaction trans1 = new Transaction("1111", "2222", 1500);
		Transaction trans2 = new Transaction("2222", "1111", 2500);
		
		transactionRepository.insert(trans1);
		transactionRepository.insert(trans2);
		
	}
	
	@PostMapping("account/save")
	public Message createAccount(@RequestBody Account account)
	{
		class MessageResponse implements Message
		{
			String message;
			Account data;
			
			MessageResponse(String message, Account data) {
				super();
				this.message = message;
				this.data = data;
			}

			public String getMessage() {
				return message;
			}

			public Account getData() {
				return data;
			}
		} 
		
		
		if(account.getOwner() == null || account.getId() == null)
		{
			return new MessageResponse("ERROR", null);
		}
		
		accountRepository.insert(account);
		return new MessageResponse("SUCCESS", account);
	} //Create Account
	
	
	@PostMapping("transaction/save")
	public Message createTransaction(@RequestBody Transaction transaction)
	{
		class MessageData
		{
			String id;
			Account from;
			Account to;
			LocalDateTime createDate;
			double amount;
			
			
			public String getId() {
				return id;
			}
			public Account getFrom() {
				return from;
			}
			public Account getTo() {
				return to;
			}
			public LocalDateTime getCreateDate() {
				return createDate;
			}
			public double getAmount() {
				return amount;
			}
			
			public MessageData(String id, Account from, Account to, LocalDateTime createDate, double amount) {
				super();
				this.id = id;
				this.from = from;
				this.to = to;
				this.createDate = createDate;
				this.amount = amount;
			}
		}
		
		class MessageResponse implements Message
		{
			String message;
			MessageData data;
			
			public MessageResponse(String message, MessageData data) {
				super();
				this.message = message;
				this.data = data;
			}

			public String getMessage() {
				return message;
			}

			public MessageData getData() {
				return data;
			}
		} 
		
		if(transaction.getFromId() == null || transaction.getToId() == null)
		{
			return new MessageResponse("ERROR:missing fields", null);
		}
		
		Optional<Account> tempFromAcc = accountRepository.findById(transaction.getFromId());
		Optional<Account> tempToAcc = accountRepository.findById(transaction.getToId());
		
		if(tempFromAcc.isEmpty() || tempToAcc.isEmpty())
		{
			return new MessageResponse("ERROR:account id", null);
		}
		
		Transaction tempTrans = transactionRepository.insert(transaction);
		
		return new MessageResponse("SUCCESS", new MessageData(tempTrans.getId(), tempFromAcc.get(), tempToAcc.get(), tempTrans.getCreateDate(), tempTrans.getAmount()));
	} //save transaction
	
	
	@GetMapping("account/{accountId}")
	public Message accountSummary(@PathVariable String accountId)
	{
		class transData
		{
			String id;
			Account from;
			Account to;
			LocalDateTime createDate;
			double amount;
			
			public String getId() {
				return id;
			}
			public Account getFrom() {
				return from;
			}
			public Account getTo() {
				return to;
			}
			public LocalDateTime getCreateDate() {
				return createDate;
			}
			public double getAmount() {
				return amount;
			}
			
			public transData(String id, Account from, Account to, LocalDateTime createDate, double amount) {
				super();
				this.id = id;
				this.from = from;
				this.to = to;
				this.createDate = createDate;
				this.amount = amount;
			}

		}
		
		class MessageData
		{
			String id;
			String owner;
			LocalDateTime createDate;
			List<transData> transactionsOut = new ArrayList<>();
			List<transData> transactionsIn = new ArrayList<>();
			double balance;
			
			public String getId() {
				return id;
			}
			public String getOwner() {
				return owner;
			}
			public LocalDateTime getCreateDate() {
				return createDate;
			}
			public List<transData> getTransactionsOut() {
				return transactionsOut;
			}
			public List<transData> getTransactionsIn() {
				return transactionsIn;
			}
			public double getBalance() {
				return balance;
			}

		}
		
		class MessageResponse implements Message
		{
			String message;
			MessageData data;
			
			public String getMessage() {
				return message;
			}
			
			public MessageData getData() {
				return data;
			}
			
			public MessageResponse(String message, MessageData data) {
				super();
				this.message = message;
				this.data = data;
			}
			
			
		}
		
		Optional<Account> tempAcc = accountRepository.findById(accountId);
		
		if(tempAcc.isEmpty())
		{	
			return new MessageResponse("ERROR:account doesnt exist!", null);
		}
		
		MessageData tempData = new MessageData();
		tempData.id = tempAcc.get().getId();
		tempData.owner = tempAcc.get().getOwner();
		tempData.createDate = tempAcc.get().getCreateDate();
		
		
		transactionRepository.findByFromId(accountId).forEach((n) -> tempData.transactionsOut.add(new transData(n.getId(), tempAcc.get(), accountRepository.findById(n.getToId()).get(), n.getCreateDate(), n.getAmount()))); 
		transactionRepository.findByToId(accountId).forEach((n) -> tempData.transactionsIn.add(new transData(n.getId(), accountRepository.findById(n.getFromId()).get(), tempAcc.get(), n.getCreateDate(), n.getAmount())));
		
		logger.info("made it here!");
		tempData.transactionsIn.forEach((n) -> tempData.balance += n.getAmount());
		tempData.transactionsOut.forEach((n) -> tempData.balance -= n.getAmount());
		
		return new MessageResponse("SUCCESS", tempData);
	} //accout summary
	
	@GetMapping("transaction/to/{accountId}")
	public Message incomingTransactions(@PathVariable String accountId)
	{
		class MessageData
		{
			String id;
			Account from;
			Account to;
			LocalDateTime createDate;
			double amount;
			
			public String getId() {
				return id;
			}
			public Account getFrom() {
				return from;
			}
			public Account getTo() {
				return to;
			}
			public LocalDateTime getCreateDate() {
				return createDate;
			}
			public double getAmount() {
				return amount;
			}
			
			public MessageData(String id, Account from, Account to, LocalDateTime createDate, double amount) {
				super();
				this.id = id;
				this.from = from;
				this.to = to;
				this.createDate = createDate;
				this.amount = amount;
			}
			
			
		}
		
		class MessageResponse implements Message
		{
			String message;
			List<MessageData> data = new ArrayList<>();
			
			public String getMessage() {
				return message;
			}
			public List<MessageData> getData() {
				return data;
			}
			
			public MessageResponse(String message, List<MessageData> data) {
				super();
				this.message = message;
				this.data = data;
			}
			
			
		}
		
		
		Optional<Account> tempAcc = accountRepository.findById(accountId);
		
		if(tempAcc.isEmpty())
		{	
			return new MessageResponse("ERROR:account doesnt exist!", null);
		}
		
		List<MessageData> tempDataList = new ArrayList<>();
		
		transactionRepository.findByToId(accountId).forEach((n) -> tempDataList.add(new MessageData(n.getId(), accountRepository.findById(n.getFromId()).get(), tempAcc.get(), n.getCreateDate(), n.getAmount())));
		
		return new MessageResponse("SUCCESS", tempDataList);
	}//get to transactions
	
	@GetMapping("transaction/from/{accountId}")
	public Message outgoingTransactions(@PathVariable String accountId)
	{
		class MessageData
		{
			String id;
			Account from;
			Account to;
			LocalDateTime createDate;
			double amount;
			
			public String getId() {
				return id;
			}
			public Account getFrom() {
				return from;
			}
			public Account getTo() {
				return to;
			}
			public LocalDateTime getCreateDate() {
				return createDate;
			}
			public double getAmount() {
				return amount;
			}
			
			public MessageData(String id, Account from, Account to, LocalDateTime createDate, double amount) {
				super();
				this.id = id;
				this.from = from;
				this.to = to;
				this.createDate = createDate;
				this.amount = amount;
			}
			
			
		}
		
		class MessageResponse implements Message
		{
			String message;
			List<MessageData> data = new ArrayList<>();
			
			public String getMessage() {
				return message;
			}
			public List<MessageData> getData() {
				return data;
			}
			
			public MessageResponse(String message, List<MessageData> data) {
				super();
				this.message = message;
				this.data = data;
			}
			
			
		}
		
		
		Optional<Account> tempAcc = accountRepository.findById(accountId);
		
		if(tempAcc.isEmpty())
		{	
			return new MessageResponse("ERROR:account doesnt exist!", null);
		}
		
		List<MessageData> tempDataList = new ArrayList<>();
		
		transactionRepository.findByFromId(accountId).forEach((n) -> tempDataList.add(new MessageData(n.getId(), tempAcc.get(), accountRepository.findById(n.getToId()).get(), n.getCreateDate(), n.getAmount())));
		
		return new MessageResponse("SUCCESS", tempDataList);
	} //get from transaction
}