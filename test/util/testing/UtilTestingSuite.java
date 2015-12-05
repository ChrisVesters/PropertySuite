/*
 * Copyright 2015 Inventive Designers nv.
 */

package util.testing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import util.testing.filter.RuleNodeTest;



/**
 * 
 * @author cvesters
 */
@RunWith(PropertySuite.class)
@Suite.SuiteClasses
({
	RuleNodeTest.class
})
public class UtilTestingSuite
{
}
