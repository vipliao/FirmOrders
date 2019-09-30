package com.firm.order.modules.task;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Async
public class LockOrdesTask {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Transactional
	@Scheduled(cron = "0 0 16 * * ?")
	public void work() {
		String sql = "update order_info set order_state=1 where to_days(deliver_date) = to_days(now())";
		jdbcTemplate.update(sql);

	}

}
