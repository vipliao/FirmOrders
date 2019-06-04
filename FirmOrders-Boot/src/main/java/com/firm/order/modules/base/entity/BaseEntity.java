package com.firm.order.modules.base.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

import com.firm.order.utils.BeanHelper;

import lombok.Data;


@MappedSuperclass
@Data
public class BaseEntity implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 36)
	protected String id;

	public Object getAttributeValue(String name) {
		return BeanHelper.getProperty(this, name);
	}

	public void setAttributeValue(String name, Object value) {
		BeanHelper.setProperty(this, name, value);
	}

}
