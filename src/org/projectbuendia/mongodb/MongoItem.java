package org.projectbuendia.mongodb;

import com.mongodb.DB;
import com.mongodb.MongoException;

/**
 * @author Pim de Witte
 */
public interface MongoItem {
	boolean canExecute();
	boolean execute(DB db) throws MongoException;
}
