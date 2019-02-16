package com.firm.orders.order.task;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LockOrdesTask {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Transactional
	public void work() {
		String sql = "update order_info set order_state=1 where to_days(deliver_date) = to_days(now())";
		jdbcTemplate.update(sql);

	}

}
