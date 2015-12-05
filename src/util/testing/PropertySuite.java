package util.testing;
/*
 * Copyright 2015 Inventive Designers nv.
 */



import java.util.Iterator;

import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import util.testing.filter.PropertyFilter;


/**
 * 
 * A test suite runner that allows tests to be selected dynamically based on properties. The
 * <code>@Suite.SuiteClasses</code> annotation must be used to list ALL test cases and suites.
 * 
 * The Runner will search for the property files 'test.filter' and 'local.test.filter'. The properties used for this
 * filtering are of the form 'package.testcase.test = included'. If no properties are provided all tests are executed.
 * If a value different from 'excluded' is specified, the test will be executed. It is possible to filter on different
 * levels:
 * 
 * * = excluded unit = included unit.TestCaseA = excluded unit.TestCaseB.testB = excluded
 * 
 * The above sequence results in only the test cases inside the unit package to be executed, excluding TestCaseA and
 * testB from TestCaseB. As indicated by the example above it is possible to specify glob regular expressions. Three
 * special characters are supported: '*' which matches anything for one subpart. '?' which matches one character. Both
 * '*' and '?' can occur in the beginning, middle and end of a name, and can occur multiple times.
 * 
 * For example:
 * 
 * <pre>
 * {@code
 * {@literal @}RunWith(PropertySuite.class)
 * {@literal @}Suite.SuiteClasses({ExampleTestCase.class})
 * class ExampleSuite
 * {
 * }
 * }
 * </pre>
 * 
 * @see org.junit.runners.Suite
 * @see org.junit.runners.Suite.SuiteClasses
 * 
 * @author cvesters
 *
 **/
public class PropertySuite extends Suite
{
	/**
	 * Constructor.
	 * 
	 * @param klass The class that should be run with this runner.
	 * @param builder A builder used to build new runners.
	 * 
	 **/
	public PropertySuite(final Class<?> klass, final RunnerBuilder builder) throws InitializationError
	{
		super(klass, builder);
		
		final Filter f = new PropertyFilter();
		final Iterator<Runner> it = getChildren().iterator();
		while (it.hasNext())
		{
			final Runner runner = it.next();
			try
			{
				f.apply(runner);
			}
			catch (final NoTestsRemainException e)
			{
				System.out.println("[WARNING] All tests of '" + runner.getDescription().getDisplayName() + "' were excluded from running!");
				it.remove();
			}
		}
	}
}