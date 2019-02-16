package com.firm.orders.base.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

import com.firm.orders.base.utils.BeanHelper;

@MappedSuperclass
public class BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5974565880746231375L;
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 36)
	protected String id;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		if (id != null && id == "") {
			this.id = null;
		} else {
			this.id = id;
		}

	}

	public Object getAttributeValue(String name) {
		return BeanHelper.getProperty(this, name);
	}

	public void setAttributeValue(String name, Object value) {
		BeanHelper.setProperty(this, name, value);
	}

}
