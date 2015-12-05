/*
 * Copyright 2015 Inventive Designers nv.
 */

package util.testing.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;

import util.testing.PropertySuite;
import util.testing.filter.RuleNodeTest.TestCollidingExpressions;
import util.testing.filter.RuleNodeTest.TestGlobRegex;
import util.testing.filter.RuleNodeTest.TestHierarchicalExpressions;
import util.testing.filter.RuleNodeTest.TestInvalidExpressions;
import util.testing.filter.RuleNodeTest.TestSingleExpression;


/**
 * 
 * Test the RuleNode class.
 * 
 * 
 * @author cvesters
 */
@RunWith(PropertySuite.class)
@Suite.SuiteClasses
({
	TestSingleExpression.class,
	TestGlobRegex.class,
	TestHierarchicalExpressions.class,
	TestCollidingExpressions.class,
	TestInvalidExpressions.class
})
public class RuleNodeTest
{
	/**
	 * Tests that verify matching for a single expression.
	 **/
	@RunWith(Parameterized.class)
	public static class TestSingleExpression
	{
		@Rule
		public Timeout globalTimeout = new Timeout(10);
		
		
		@Parameters(name = "{0}")
		public static Collection<Object[]> data()
		{
			return Arrays.asList(new Object[][]
				{
					{"UnitTests"},
					{"unit.Tests"},
					{"unit.Tests.test"},
					{"unit.Tests.test[abc]"},
					{"unit.Tests.test{abs}"}
				});
		}
		
		// A set of different trees used to test the expression.
		private static final RuleNode fgEmptyTree = new RuleNode();
		private static final RuleNode fgExcludedTree = new RuleNode();
		private RuleNode fLiteralTree;
		
		// The expression under test.
		private final String fExpression;
		
		
		/**
		 * 
		 * Constructor
		 * 
		 * @param expression The expression to verify.
		 * 
		 **/
		public TestSingleExpression(final String expression)
		{
			fExpression = expression;
		}
		
		
		/**
		 * Set up the test environment.
		 **/
		@BeforeClass
		public static void setupOnce()
		{
			fgExcludedTree.addMatchRule("*", false);
		}
		
		
		/**
		 * Set up the test.
		 **/
		@Before
		public void setup()
		{
			fLiteralTree = new RuleNode();
			fLiteralTree.addMatchRule(fExpression, false);
		}
		
		
		/**
		 * Test the expression with an empty tree. This should always match the root element, and thus be true.
		 **/
		@Test
		public void testRootNode()
		{
			assertTrue("The default behaviour should be 'true'", fgEmptyTree.getBestMatchingValue(fExpression));
		}
		
		
		/**
		 * Test the expression with a tree that excludes all.
		 **/
		@Test
		public void testExcludeAll()
		{
			assertFalse("The expression should match '*' and be exluded.", fgExcludedTree.getBestMatchingValue(fExpression));
		}
		
		
		/**
		 * Test the expression with a tree that excludes the literal expression.
		 **/
		@Test
		public void testExludeliteralMatch()
		{
			assertFalse("The expression should match the literal expression and be exluded.", fLiteralTree.getBestMatchingValue(fExpression));
		}
	}
	
	
	/**
	 * Tests for globbing/wildcard matching.
	 **/
	public static class TestGlobRegex
	{
		@Rule
		public Timeout globalTimeout = new Timeout(10);
		
		private RuleNode fNode;
		private final String fTestCase = "TestA";
		private final String fExpression = "unit." + fTestCase;
		private final String fLongExpression = "com.id." + fExpression;
		
		
		/**
		 * Set up the test.
		 **/
		@Before
		public void setup()
		{
			fNode = new RuleNode();
		}
		
		
		/**
		 * Test using a '*' wildcard for a complete subpart.
		 **/
		@Test
		public void testWildcardPart()
		{
			fNode.addMatchRule("unit.*", false);
			assertFalse("The expression should match 'unit.*' and be excluded.", fNode.getBestMatchingValue(fExpression));
		}
		
		
		/**
		 * Test using a '*' wildcard as part of a subpart.
		 **/
		@Test
		public void testWildcardSubPart()
		{
			fNode.addMatchRule("unit.Test*", false);
			assertFalse("The expression should match 'unit.Test*' and be exluded.", fNode.getBestMatchingValue(fExpression));
		}
		
		
		/**
		 * Test using a '?' wildcard.
		 **/
		@Test
		public void testWildcardSingleCharacter()
		{
			fNode.addMatchRule("unit.Test?", false);
			assertFalse("The expression should match 'unit.Test?' and be exluded.", fNode.getBestMatchingValue(fExpression));
		}
		
		
		/**
		 * Test using a '*' wildcard that does not match the expression.
		 **/
		@Test
		public void testNonMatchingWildCard()
		{
			fNode.addMatchRule("unit.ABC*", false);
			assertTrue("The expression should not match 'unit.ABC*' and be included.", fNode.getBestMatchingValue(fExpression));
		}
		
		
		/**
		 * Test using a '*' wildcard followed by a non-matching part. The wildcard does match someting, but the rest
		 * does not.
		 **/
		@Test
		public void testSingleAsterixNotMatchesMultipleSubPart()
		{
			fNode.addMatchRule("*.TestA", false);
			assertTrue("The expression should not match '*.testA' and be included.", fNode.getBestMatchingValue(fLongExpression));
		}
		
		
		/**
		 * Test using a '*' wildcard followed by a matching part. The wildcard matches what should be matched by the
		 * following part.
		 **/
		@Test
		public void testSingleAsterixMustMatchSubPart()
		{
			fNode.addMatchRule("*.TestA", false);
			assertTrue("The expression should not match '*.TestA' and be included.", fNode.getBestMatchingValue(fTestCase));
		}
	}
	
	
	/**
	 * Test matching rules with complex hierarchy.
	 **/
	public static class TestHierarchicalExpressions
	{
		@Rule
		public Timeout globalTimeout = new Timeout(10);
		
		private static final RuleNode fgNode = new RuleNode();
		
		
		/**
		 * Set up test environment.
		 **/
		@BeforeClass
		public static void setupOnce()
		{
			fgNode.addMatchRule("*", false);
			fgNode.addMatchRule("unit", true);
			fgNode.addMatchRule("unit.Test*", false);
			fgNode.addMatchRule("unit.TestA", true);
			fgNode.addMatchRule("other.obsolete", true);
		}
		
		
		/**
		 * Test that the best sub-match is chosen if a complete match does not exist.
		 **/
		@Test
		public void testMatchSubPart()
		{
			assertTrue("The expression should match 'unit' and be included", fgNode.getBestMatchingValue("unit.OtherTest"));
		}
		
		
		/**
		 * Test that in absence of a better match the wildcard '*' matches.
		 **/
		@Test
		public void testMatchWildCard()
		{
			assertFalse("The expression should match 'unit.Test*' and be excluded", fgNode.getBestMatchingValue("unit.TestB"));
		}
		
		
		/**
		 * Test that a literal match is chosen over a wildcard match.
		 **/
		@Test
		public void testMatchLiteral()
		{
			assertTrue("The expression should match 'unit.TestA' and be included", fgNode.getBestMatchingValue("unit.TestA"));
		}
		
		
		/**
		 * Test that the wildcard is chosen if not better match exists.
		 **/
		@Test
		public void testMatchNoMatchingSubPart()
		{
			assertFalse("The expression should match '*' and be excluded", fgNode.getBestMatchingValue("other.Test"));
		}
	}
	
	
	/**
	 * Test the behaviour if multiple rules match the expression.
	 **/
	public static class TestCollidingExpressions
	{
		@Rule
		public Timeout globalTimeout = new Timeout(10);
		
		private RuleNode fNode;
		
		
		/**
		 * Set up the test.
		 **/
		@Before
		public void setup()
		{
			fNode = new RuleNode();
		}
		
		
		/**
		 * Two literal matches should be combined with a logical OR.
		 **/
		@Test
		public void testDuplicateExpressionTrueFalse()
		{
			fNode.addMatchRule("unit.TestA", true);
			fNode.addMatchRule("unit.TestA", false);
			assertTrue("The logical OR should be false.", fNode.getBestMatchingValue("unit.TestA"));
		}
		
		
		/**
		 * Two literal matches should be combined with a logical OR.
		 **/
		@Test
		public void testDuplicateExpressionTrueTrue()
		{
			fNode.addMatchRule("unit.TestA", true);
			fNode.addMatchRule("unit.TestA", true);
			assertTrue("The logical OR should be true.", fNode.getBestMatchingValue("unit.TestA"));
		}
		
		
		/**
		 * Two literal matches should be combined with a logical OR.
		 **/
		@Test
		public void testDuplicateExpressionFalseFalse()
		{
			fNode.addMatchRule("unit.TestA", false);
			fNode.addMatchRule("unit.TestA", false);
			assertFalse("The logical OR should be false.", fNode.getBestMatchingValue("unit.TestA"));
		}
		
		
		/**
		 * Two matches with wildcards, but the same score should be combined with a logical OR.
		 **/
		@Test
		public void testSameValueExpressionsTrueFalse()
		{
			fNode.addMatchRule("unit.Test*", true);
			fNode.addMatchRule("unit.Tes*A", false);
			assertTrue("The logical OR should be true.", fNode.getBestMatchingValue("unit.TestA"));
		}
		
		
		/**
		 * Two matches with wildcards, but the same score should be combined with a logical OR.
		 **/
		@Test
		public void testSameValueExpressionsTrueTrue()
		{
			fNode.addMatchRule("unit.Test*", true);
			fNode.addMatchRule("unit.Tes*A", true);
			assertTrue("The logical OR should be true.", fNode.getBestMatchingValue("unit.TestA"));
		}
		
		
		/**
		 * Two matches with wildcards, but the same score should be combined with a logical OR.
		 **/
		@Test
		public void testSameValueExpressionsFalseFalse()
		{
			fNode.addMatchRule("unit.Test*", false);
			fNode.addMatchRule("unit.Tes*A", false);
			assertFalse("The logical OR should be false.", fNode.getBestMatchingValue("unit.TestA"));
		}
	}
	
	
	/**
	 * Test the behaviour if an expression is invalid.
	 **/
	public static class TestInvalidExpressions
	{
		@Rule
		public Timeout globalTimeout = new Timeout(10);
		
		private RuleNode fNode;
		
		
		/**
		 * Set up the test.
		 **/
		@Before
		public void setup()
		{
			fNode = new RuleNode();
		}
		
		
		/**
		 * A null expression is not allowed.
		 **/
		@Test(expected = AssertionError.class)
		public void testAddNullExpression()
		{
			fNode.addMatchRule(null, false);
		}
		
		
		/**
		 * An empty expression is not allowed.
		 **/
		@Test(expected = AssertionError.class)
		public void testAddEmptyExpression()
		{
			fNode.addMatchRule("", false);
		}
		
		
		/**
		 * Invalid characters in the expression are not allowed.
		 **/
		@Test(expected = AssertionError.class)
		public void testAddInvalidCharacterExpression()
		{
			fNode.addMatchRule("Abc&", false);
		}
		
		
		/**
		 * A null expression is not allowed.
		 **/
		@Test(expected = AssertionError.class)
		public void testGetNullExpression()
		{
			fNode.getBestMatchingValue(null);
		}
		
		
		/**
		 * An empty expression is not allowed.
		 **/
		@Test(expected = AssertionError.class)
		public void testGetEmptyExpression()
		{
			fNode.getBestMatchingValue("");
		}
	}
}