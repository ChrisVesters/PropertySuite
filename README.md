<h1>Description</h1>
The PropertySuite is an extension to the default jUnit Suite runner.
The PropertySuite allows a user to filter the tests that are run. This is done by specifying a list of rules in the testfilter.properties or local.testfilter.properties file.
The local.testfilter.properties precedes over the testfilter.properties, if such a file exists, the testfilter.properties is ignored.

By default all tests run.
Rules specify the tests by their fully qualified name. Example:
com.util.TestCaseA.testa = excluded
com.util.TestCaseB.testb = included

Support for two wildcards is provided: '\*' and '?' which match any sequence of characters and exactly one character respectively.
It is however still required to write the full path of the test. Example:
*.*.TestCaseA.testa = excluded

It is possible to write general rules as well. Example:
com.util = excluded
com.util.TestCaseA = included

For a rule to match all subparts of the rule match with a subpart of the test in the same order.
If multiple rules match, the best matching rule is selected based on two simple rules:
* Iteravely the best matching subpart is selected.
* A subpart matches better than another one if it has more characters, that are not wildcards.
* The rule with the most subparts wins.

This means that in the example above, a test of TestCaseA will match 'com.util.TestCaseA', while a test of TestCaseB will match 'com.util'.
