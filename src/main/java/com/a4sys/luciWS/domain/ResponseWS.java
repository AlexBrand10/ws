package com.a4sys.luciWS.domain;

import com.a4sys.core.dao.ALQueryResult;

public class ResponseWS {
	
	private String status;
	private String  errorMessage;
	private ALQueryResult  data = new ALQueryResult();
	
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public ALQueryResult getData() {
		return data;
	}
	public void setData(ALQueryResult data) {
		this.data = data;
	}


	@Override
	public String toString() {
		return "ResponseWS{" +
				"status='" + status + '\'' +
				", errorMessage='" + errorMessage + '\'' +
				", data=" + data +
				'}';
	}
}
