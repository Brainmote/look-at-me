package com.brainmote.lookatme.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTestSuite(DBTest.class);
		suite.addTestSuite(ChordMessageTest.class);
		return suite;
	}

}
