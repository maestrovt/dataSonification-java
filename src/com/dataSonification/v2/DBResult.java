package com.dataSonification.v2;

import java.sql.ResultSet;
import java.sql.Statement;



/**
 * Encapsulates a ResultSet and a Statement associated with a query.
 * 
 * @author Kimo Johnson
 * @see DBManager
 */
//public because it is returned by DBManager
public class DBResult {
	/**
	 * Reference to Statement object.  Must be closed after ResultSet is processed.
	 */
	public Statement st = null;
	/**
	 * The result of the query
	 */
	public ResultSet rs = null;
	
	
	/**
	 * Defines a DBResult.  DBResult instances are created by DBManager.
	 * @param s a Statement
	 * @param r a ResultSet
	 */
	public DBResult(Statement s, ResultSet r) {
		st = s;
		rs = r;
	}
}