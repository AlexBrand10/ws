package com.a4sys.luciWS.bandwire;

/***
 * 
 * @author Martin Morones
 *
 */
public class NotificacionBanwire {

	private String paymentId;
	private String paymentAuthCode;
	private String paymentReference;
	private String paymentDescription;
	private String paymentAmount;
	private String errorCode;
	private String errorMessage;
	private String errorDetail;			
	private String status;
	private String date;
	private String id;
	private String idCard;
	private String authCode;
	private String lastDigits;
	private String token;
	private String amount;
	private String reference;
	private String concept;
	private String email;
	private String cardOwner;
	private String remainingPayments;
	private String nextPayment;
	private String tokenPlan;
	
	/***
	 * Constructor con Parametros
	 * @param paymentId
	 * @param paymentAuthCode
	 * @param paymentReference
	 * @param paymentDescription
	 * @param paymentAmount
	 * @param errorCode
	 * @param errorMessage
	 * @param errorDetail
	 * @param status
	 * @param date
	 * @param id
	 * @param idCard
	 * @param authCode
	 * @param lastDigits
	 * @param token
	 * @param amount
	 * @param reference
	 * @param concept
	 * @param email
	 * @param cardOwner
	 * @param remainingPayments
	 * @param nextPayment
	 * @param tokenPlan
	 */
	public NotificacionBanwire(String paymentId, String paymentAuthCode,
			String paymentReference, String paymentDescription,
			String paymentAmount, String errorCode, String errorMessage,
			String errorDetail, String status, String date, String id,
			String idCard, String authCode, String lastDigits, String token,
			String amount, String reference, String concept, String email,
			String cardOwner, String remainingPayments, String nextPayment,
			String tokenPlan) {
		super();
		this.paymentId = paymentId;
		this.paymentAuthCode = paymentAuthCode;
		this.paymentReference = paymentReference;
		this.paymentDescription = paymentDescription;
		this.paymentAmount = paymentAmount;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.errorDetail = errorDetail;
		this.status = status;
		this.date = date;
		this.id = id;
		this.idCard = idCard;
		this.authCode = authCode;
		this.lastDigits = lastDigits;
		this.token = token;
		this.amount = amount;
		this.reference = reference;
		this.concept = concept;
		this.email = email;
		this.cardOwner = cardOwner;
		this.remainingPayments = remainingPayments;
		this.nextPayment = nextPayment;
		this.tokenPlan = tokenPlan;
	}


	public String getPaymentId() {
		return paymentId;
	}


	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}


	public String getPaymentAuthCode() {
		return paymentAuthCode;
	}


	public void setPaymentAuthCode(String paymentAuthCode) {
		this.paymentAuthCode = paymentAuthCode;
	}


	public String getPaymentReference() {
		return paymentReference;
	}


	public void setPaymentReference(String paymentReference) {
		this.paymentReference = paymentReference;
	}


	public String getPaymentDescription() {
		return paymentDescription;
	}


	public void setPaymentDescription(String paymentDescription) {
		this.paymentDescription = paymentDescription;
	}


	public String getPaymentAmount() {
		return paymentAmount;
	}


	public void setPaymentAmount(String paymentAmount) {
		this.paymentAmount = paymentAmount;
	}


	public String getErrorCode() {
		return errorCode;
	}


	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}


	public String getErrorMessage() {
		return errorMessage;
	}


	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


	public String getErrorDetail() {
		return errorDetail;
	}


	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getDate() {
		return date;
	}


	public void setDate(String date) {
		this.date = date;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getIdCard() {
		return idCard;
	}


	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}


	public String getAuthCode() {
		return authCode;
	}


	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}


	public String getLastDigits() {
		return lastDigits;
	}


	public void setLastDigits(String lastDigits) {
		this.lastDigits = lastDigits;
	}


	public String getToken() {
		return token;
	}


	public void setToken(String token) {
		this.token = token;
	}


	public String getAmount() {
		return amount;
	}


	public void setAmount(String amount) {
		this.amount = amount;
	}


	public String getReference() {
		return reference;
	}


	public void setReference(String reference) {
		this.reference = reference;
	}


	public String getConcept() {
		return concept;
	}


	public void setConcept(String concept) {
		this.concept = concept;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getCardOwner() {
		return cardOwner;
	}


	public void setCardOwner(String cardOwner) {
		this.cardOwner = cardOwner;
	}


	public String getRemainingPayments() {
		return remainingPayments;
	}


	public void setRemainingPayments(String remainingPayments) {
		this.remainingPayments = remainingPayments;
	}


	public String getNextPayment() {
		return nextPayment;
	}


	public void setNextPayment(String nextPayment) {
		this.nextPayment = nextPayment;
	}


	public String getTokenPlan() {
		return tokenPlan;
	}


	public void setTokenPlan(String tokenPlan) {
		this.tokenPlan = tokenPlan;
	}

	
	
}
