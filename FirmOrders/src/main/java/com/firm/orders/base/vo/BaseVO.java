package com.firm.orders.base.vo;

import java.io.Serializable;

public class BaseVO implements Serializable{
	   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	   
}
