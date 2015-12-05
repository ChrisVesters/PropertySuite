/*
 * Copyright 2015 Inventive Designers nv.
 */

package util.testing.filter;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.TreeMap;

/**
 * 
 * A tree consisting of nodes each expressing a part in an expression. By following the nodes the expression can be
 * matched and a result be achieved. The expressions are of the form: "x.y.z" which is split into 3 parts, using '.' as
 * separator. Each node can have a value 'true' or 'false'.
 * 
 * @author cvesters
 */
public class RuleNode
{
	/**
	 * A boolean indicating the value of the expression.
	 **/
	private Boolean fIncluded;
	
	/**
	 * A list of subparts. This are the children in the tree.
	 */
	private final TreeMap<String, RuleNode> fSubParts = new TreeMap<String, RuleNode>();
	
	
	/**
	 * 
	 * Constructor. Creates the root element of the tree.
	 * 
	 **/
	public RuleNode()
	{
		fIncluded = Boolean.TRUE;
	}
	
	
	/**
	 * 
	 * Constructor. Create a new node with a specified value.
	 * 
	 * @param included Whether the rule is included or excluding tests.
	 * 
	 **/
	private RuleNode(final Boolean included)
	{
		fIncluded = included;
	}
	
	
	/**
	 * 
	 * Add a rule to the tree. If a rule with the same expression already exists, the values will be merged with a
	 * logical or.
	 * 
	 * @param expression The expression of the rule.
	 * @param included A boolean specifying the value of the expression.
	 * 
	 **/
	public void addMatchRule(final String expression, final boolean included)
	{
		assert ((expression != null) && !expression.isEmpty());
		assert expression.matches("([a-zA-z0-9]|\\*|\\?|\\.|\\{|\\}|\\[|\\]|\\$)+") : "The test rule contains an invalid character.";
		
		final String[] parts = expression.split("\\.", 2);
		if (fSubParts.containsKey(parts[0]))
		{
			final RuleNode subPart = fSubParts.get(parts[0]);
			if (parts.length == 1)
			{
				subPart.fIncluded = (subPart.fIncluded != null) ? (subPart.fIncluded.booleanValue() || included) : included;
			}
			else
			{
				subPart.addMatchRule(parts[1], included);
			}
		}
		else
		{
			if (parts.length == 1)
			{
				final RuleNode newRule = new RuleNode(included);
				fSubParts.put(parts[0], newRule);
			}
			else
			{
				final RuleNode newRule = new RuleNode(null);
				fSubParts.put(parts[0], newRule);
				newRule.addMatchRule(parts[1], included);
			}
		}
	}
	
	
	/**
	 * 
	 * Get the best matching value for a certain test. If there is no valued set for this expression, true is returned.
	 * 
	 * @param expression The expression that identifies the test.
	 * @return True or false depending on the best matching rule.
	 * 
	 **/
	public boolean getBestMatchingValue(final String expression)
	{
		assert ((expression != null) && !expression.isEmpty());
		
		return findBestMatchingValue(expression).booleanValue();
	}
	
	
	/**
	 * 
	 * Get the best matching value for the expression.
	 * 
	 * @param expression The expression that identifies the test.
	 * @return True if the test should run, false if the test should not run, null if the expression did not match
	 * anything.
	 * 
	 **/
	private Boolean findBestMatchingValue(final String expression)
	{
		assert ((expression != null) && !expression.isEmpty());
		
		final String[] parts = expression.split("\\.", 2);
		int bestScore = Integer.MIN_VALUE;
		Boolean bestResult = null;
		
		for (final String key : fSubParts.keySet())
		{
			final String globKey = key.replace("[", "\\[").replace("]", "\\]").replace("{", "\\{").replace("}", "\\}");
			final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globKey);
			if (matcher.matches((new File(parts[0])).toPath()))
			{
				// Only determine the result if this is the best option so far.
				final int score = key.replaceAll("\\*|\\?", "").length();
				if (score < bestScore)
				{
					continue;
				}
				
				final Boolean result;
				if (parts.length == 1)
				{
					result = fSubParts.get(key).fIncluded;
				}
				else
				{
					result = fSubParts.get(key).findBestMatchingValue(parts[1]);
				}
				
				// Turns out there was no match after all.
				if (result == null)
				{
					continue;
				}
				// We already had a match with the same score. Take a logical OR.
				else if (score == bestScore)
				{
					bestResult = bestResult || result;
				}
				else
				// (score > bestScore)
				{
					bestScore = score;
					bestResult = result;
				}
			}
		}
		
		// If we did not find any matching child, we are the best match.
		return bestResult != null ? bestResult : fIncluded;
	}
}