package eu.daiad.web.logging;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.logging.log4j.core.appender.db.jdbc.ConnectionSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SpringJdbcAppenderConnectionSource implements ConnectionSource {

	@Autowired
	@Qualifier("applicationDataSource")
	DataSource dataSource;

	@Override
	public Connection getConnection() throws SQLException {
		return this.dataSource.getConnection();
	}

}
