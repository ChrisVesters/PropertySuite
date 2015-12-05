/*
 * Copyright 2015 Inventive Designers nv.
 */

package util.testing.filter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * 
 * A custom property that filters tests based on a set of properties. The filter reads the local filter file, processes
 * the properties and uses them for determining if a test should run.
 * 
 * @author cvesters
 */
public class PropertyFilter extends Filter
{
	/**
	 * A tree structure of the properties.
	 **/
	private final RuleNode fRootRule = new RuleNode();
	
	
	/**
	 * Constructor.
	 * @param properties The properties used to filter.
	 * 
	 **/
	public PropertyFilter()
	{
		readFilters();
	}
	
	
	/**
	 * Determines if a test should run based on properties.
	 * 
	 * @param description the {@link Description description} of the test to be run.
	 * @return <code>true</code> if the test should be run, false otherwise.
	 * 
	 **/
	@Override
	public boolean shouldRun(final Description description)
	{
		boolean shouldRun = false;
		for (final Description child : description.getChildren())
		{
			shouldRun = shouldRun || shouldRun(child);
		}
		
		if (description.isTest())
		{
			final String fullTestName = description.getClassName() + "." + description.getMethodName();
			return shouldRun || fRootRule.getBestMatchingValue(fullTestName);
		}
		
		return shouldRun;
	}
	
	
	/**
	 * @return The description of the filter.
	 **/
	@Override
	public String describe()
	{
		return "Filter out tests based on include and exclude properties.";
	}
	
	
	/**
	 * 
	 * Read the filter rules from the file, and process them.
	 * 
	 **/
	private void readFilters()
	{
		final String generalFilterFile = "testfilter.properties";
		final String localFilterFile = "local." + generalFilterFile;
		
		// Local properties override the general.
		if (Files.exists(Paths.get(localFilterFile)))
		{
			try
			{
				final Properties localProps = new Properties();
				localProps.load(new FileInputStream(localFilterFile));
				processProperties(localProps);
				return;
			}
			catch (final IOException e)
			{
				System.out.println("[ERROR] Failed to load '" + localFilterFile + "': " + e.getMessage());
				e.printStackTrace(System.out);
				System.out.println("[ERROR] Falling back to '" + generalFilterFile + "'.");
			}
		}
		
		if (Files.exists(Paths.get(generalFilterFile)))
		{
			try
			{
				final Properties props = new Properties();
				props.load(new FileInputStream(generalFilterFile));
				processProperties(props);
				return;
			}
			catch (final IOException e)
			{
				System.out.println("[ERROR] Failed to load '" + generalFilterFile + "': " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
	}
	
	
	/**
	 * 
	 * Process the properties by adding them to the tree structure.
	 * 
	 * @param props The properties to process.
	 * 
	 **/
	private void processProperties(final Properties props)
	{
		for (final String key : props.stringPropertyNames())
		{
			final String value = props.getProperty(key);
			fRootRule.addMatchRule(key, !"excluded".equalsIgnoreCase(value));
		}
	}
}