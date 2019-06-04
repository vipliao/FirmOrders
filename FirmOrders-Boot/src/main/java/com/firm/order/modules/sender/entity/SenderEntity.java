package com.firm.order.modules.sender.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.order.modules.base.entity.SuperEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="sender_info")
@Data
@EqualsAndHashCode(callSuper=true)
public class SenderEntity extends SuperEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name="sender_name")
	private String senderName;
	@Column(name="serder_phone")
	private String serderPhone;
	@Column(name="sender_addr")
	private String senderAddr;
	@Column(name="is_enabled")
	private int isEnabled;
	@Column(name="biz_range")
	private int bizRange;

}
