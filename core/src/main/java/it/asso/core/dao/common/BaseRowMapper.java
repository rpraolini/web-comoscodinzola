package it.asso.core.dao.common;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;


	public abstract class BaseRowMapper<T extends Object> implements RowMapper<T> {
	  private Set<String> setAvailableColumns;
	  private ResultSet rs;
	  private final String prefix;
	  /**
	   * 
	   */
	  public BaseRowMapper() {
	    this.prefix = "";
	  }
	  /**
	   * @param prefix
	   */
	  public BaseRowMapper(String prefix) {
	    this.prefix = prefix;
	  }
	  /**
	   * @param rs
	   * @throws SQLException
	   */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void init(ResultSet rs1) throws SQLException {
	    this.rs = rs1;
	    this.setAvailableColumns = new HashSet();
	    ResultSetMetaData meta = rs1.getMetaData();
	    for (int i = 1, n = meta.getColumnCount() + 1; i < n; i++) {
	      this.setAvailableColumns.add(meta.getColumnName(i));
	    }
	  }
	  /*
	   * (non-Javadoc)
	   * 
	   * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	   */
	  
	  public T mapRow(ResultSet rs1, int rowNum) throws SQLException {
	    if (this.setAvailableColumns == null) {
	      init(rs1);
	    }
	    return mapRowImpl(rs1, rowNum);
	  }
	  /**
	   * @param rs
	   * @param rowNum
	   * @return
	   * @throws SQLException
	   */
	  public abstract T mapRowImpl(ResultSet rs1, int rowNum) throws SQLException;
	  /**
	   * @param sName
	   * @return
	   */
	  public boolean column(String sName) {
	    return (this.setAvailableColumns.contains(sName));
	  }
	  /**
	   * @param sName
	   * @return
	   * @throws SQLException
	   */
	  public Long getLong(String sName) throws SQLException {
	    if (column(this.prefix + sName)) {
	      return this.rs.getLong(this.prefix + sName);
	    }
	    return null;
	  }
	  /**
	   * @param sName
	   * @return
	   * @throws SQLException
	   */
	  public Double getDouble(String sName) throws SQLException {
	    if (column(this.prefix + sName)) {
	      return this.rs.getDouble(this.prefix + sName);
	    }
	    return null;
	  }
	  /**
	   * @param sName
	   * @return
	   * @throws SQLException
	   */
	  public Integer getInteger(String sName) throws SQLException {
	    if (column(this.prefix + sName)) {
	      return this.rs.getInt(this.prefix + sName);
	    }
	    return null;
	  }
	  /**
	   * @param sName
	   * @return
	   * @throws SQLException
	   */
	  public Date getDate(String sName) throws SQLException {
	    if (column(this.prefix + sName)) {
	      return this.rs.getDate(this.prefix + sName);
	    }
	    return null;
	  }
	  /**
	   * @param sName
	   * @return
	   * @throws SQLException
	   */
	  public Timestamp getTimestamp(String sName) throws SQLException {
	    if (column(this.prefix + sName)) {
	      return this.rs.getTimestamp(this.prefix + sName);
	    }
	    return null;
	}
	  
}
