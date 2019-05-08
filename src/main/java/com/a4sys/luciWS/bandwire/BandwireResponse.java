package com.a4sys.luciWS.bandwire;

/***
 * 
 * @author Martin morones	
 *
 */
public class BandwireResponse {


	private String error;
	private Integer id;
	private String  token;
	private String task;
	private String next_payment;
	private Boolean result; 
	private Card card;
	private Payment payment;
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getNext_payment() {
		return next_payment;
	}

	public void setNext_payment(String next_payment) {
		this.next_payment = next_payment;
	}

	public Boolean getResult() {
		return result;
	}

	public void setResult(Boolean result) {
		this.result = result;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}
	
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}
	
	/***
	 * 
	 * @author martin Morones
	 *
	 */
	public class Card{
		private String token;
		private String id;
		
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}
	/***
	 * 
	 * @author Martin Morones
	 *
	 */
	public class Payment {
		
		
		
		/*"payment":{"id_transaction":"1459705",
		   "id":"trx.46JLGsUoiCuUivEPmM7tkTPs1HGp",
	        "auth_code":"041962",
	        "reference":"1459705-79532",
	        "description":"Recurring Payment",
	        "amount":0.12,
	        "next_payment":"2016-05-01"
	   }*/
		
	   private String id_transaction;
	   private String id;
	   private String auth_code;
	   private String reference;
	   private String description;
	   private String amount;
	   private String next_payment;
	   

	   public String getId_transaction() {
		   return id_transaction;
	   }
	   public void setId_transaction(String id_transaction) {
		   this.id_transaction = id_transaction;
	   }
	   public String getId() {
		   return id;
	   }
	   public void setId(String id) {
		   this.id = id;
	   }
	   public String getAuth_code() {
		   return auth_code;
	   }
	   public void setAuth_code(String auth_code) {
		   this.auth_code = auth_code;
	   }
	   public String getReference() {
		   return reference;
	   }
	   public void setReference(String reference) {
		   this.reference = reference;
	   }
	   public String getDescription() {
		   return description;
	   }
	   public void setDescription(String description) {
		   this.description = description;
	   }
	   public String getAmount() {
		   return amount;
	   }
	   public void setAmount(String amount) {
		   this.amount = amount;
	   }
	   public String getNext_payment() {
		   return next_payment;
	   }
	   public void setNext_payment(String next_payment) {
		   this.next_payment = next_payment;
	   }
		
		
	}

	

}
