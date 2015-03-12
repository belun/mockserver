package org.mockserver.matchers;

import org.junit.Test;
import org.mockserver.Line;

import static org.junit.Assert.*;

/**
 * @author jamesdbloom
 */
public class JsonStringMatcherTest {

    @Test
    public void shouldMatchExactMatchingJson() {
        // given
        String matched = "" +
                "{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"id\": \"file\", " + Line.SEPARATOR +
                "        \"value\": \"File\", " + Line.SEPARATOR +
                "        \"popup\": {" + Line.SEPARATOR +
                "            \"menuitem\": [" + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"New\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CreateNewDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Open\", " + Line.SEPARATOR +
                "                    \"onclick\": \"OpenDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Close\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CloseDoc()\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            ]" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}";

        // then
        assertTrue(new JsonStringMatcher("{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"id\": \"file\", " + Line.SEPARATOR +
                "        \"value\": \"File\", " + Line.SEPARATOR +
                "        \"popup\": {" + Line.SEPARATOR +
                "            \"menuitem\": [" + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"New\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CreateNewDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Open\", " + Line.SEPARATOR +
                "                    \"onclick\": \"OpenDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Close\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CloseDoc()\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            ]" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}").matches(matched));
    }

    @Test
    public void shouldMatchMatchingSubJson() {
        // given
        String matched = "" +
                "{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"id\": \"file\", " + Line.SEPARATOR +
                "        \"value\": \"File\", " + Line.SEPARATOR +
                "        \"popup\": {" + Line.SEPARATOR +
                "            \"menuitem\": [" + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"New\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CreateNewDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Open\", " + Line.SEPARATOR +
                "                    \"onclick\": \"OpenDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Close\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CloseDoc()\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            ]" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}";

        // then
        assertTrue(new JsonStringMatcher("{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"id\": \"file\", " + Line.SEPARATOR +
                "        \"value\": \"File\"" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}").matches(matched));
        assertTrue(new JsonStringMatcher("{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"popup\": {" + Line.SEPARATOR +
                "            \"menuitem\": [" + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"New\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CreateNewDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Open\", " + Line.SEPARATOR +
                "                    \"onclick\": \"OpenDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Close\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CloseDoc()\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            ]" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}").matches(matched));
    }

    @Test
    public void shouldMatchMatchingSubJsonWithSomeSubJsonFields() {
        // given
        String matched = "" +
                "{" + Line.SEPARATOR +
                "    \"glossary\": {" + Line.SEPARATOR +
                "        \"title\": \"example glossary\", " + Line.SEPARATOR +
                "        \"GlossDiv\": {" + Line.SEPARATOR +
                "            \"title\": \"S\", " + Line.SEPARATOR +
                "            \"GlossList\": {" + Line.SEPARATOR +
                "                \"GlossEntry\": {" + Line.SEPARATOR +
                "                    \"ID\": \"SGML\", " + Line.SEPARATOR +
                "                    \"SortAs\": \"SGML\", " + Line.SEPARATOR +
                "                    \"GlossTerm\": \"Standard Generalized Markup Language\", " + Line.SEPARATOR +
                "                    \"Acronym\": \"SGML\", " + Line.SEPARATOR +
                "                    \"Abbrev\": \"ISO 8879:1986\", " + Line.SEPARATOR +
                "                    \"GlossDef\": {" + Line.SEPARATOR +
                "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\", " + Line.SEPARATOR +
                "                        \"GlossSeeAlso\": [" + Line.SEPARATOR +
                "                            \"GML\", " + Line.SEPARATOR +
                "                            \"XML\"" + Line.SEPARATOR +
                "                        ]" + Line.SEPARATOR +
                "                    }, " + Line.SEPARATOR +
                "                    \"GlossSee\": \"markup\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            }" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}";

        // then
        assertTrue(new JsonStringMatcher("{" + Line.SEPARATOR +
                "    \"glossary\": {" + Line.SEPARATOR +
                "        \"GlossDiv\": {" + Line.SEPARATOR +
                "            \"title\": \"S\", " + Line.SEPARATOR +
                "            \"GlossList\": {" + Line.SEPARATOR +
                "                \"GlossEntry\": {" + Line.SEPARATOR +
                "                    \"ID\": \"SGML\", " + Line.SEPARATOR +
                "                    \"Abbrev\": \"ISO 8879:1986\", " + Line.SEPARATOR +
                "                    \"GlossDef\": {" + Line.SEPARATOR +
                "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\"" + Line.SEPARATOR +
                "                    }, " + Line.SEPARATOR +
                "                    \"GlossSee\": \"markup\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            }" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}").matches(matched));
    }

    @Test
    public void shouldMatchMatchingSubJsonWithDifferentArrayOrder() {
        // given
        String matched = "" +
                "{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"id\": \"file\", " + Line.SEPARATOR +
                "        \"value\": \"File\", " + Line.SEPARATOR +
                "        \"popup\": {" + Line.SEPARATOR +
                "            \"menuitem\": [" + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"New\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CreateNewDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Open\", " + Line.SEPARATOR +
                "                    \"onclick\": \"OpenDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Close\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CloseDoc()\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            ]" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}";

        // then
        assertTrue(new JsonStringMatcher("{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"id\": \"file\", " + Line.SEPARATOR +
                "        \"value\": \"File\"" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}").matches(matched));
        assertTrue(new JsonStringMatcher("{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"popup\": {" + Line.SEPARATOR +
                "            \"menuitem\": [" + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"New\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CreateNewDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Close\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CloseDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Open\", " + Line.SEPARATOR +
                "                    \"onclick\": \"OpenDoc()\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            ]" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}").matches(matched));
    }

    @Test
    public void shouldNotMatchIllegalJson() {
        assertFalse(new JsonStringMatcher("illegal_json").matches("illegal_json"));
        assertFalse(new JsonStringMatcher("illegal_json").matches("some_other_illegal_json"));
    }

    @Test
    public void shouldNotMatchNullExpectation() {
        assertFalse(new JsonStringMatcher(null).matches("some_value"));
    }

    @Test
    public void shouldNotMatchEmptyExpectation() {
        assertFalse(new JsonStringMatcher("").matches("some_value"));
    }

    @Test
    public void shouldNotMatchNonMatchingJson() {
        // given
        String matched = "" +
                "{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"id\": \"file\", " + Line.SEPARATOR +
                "        \"value\": \"File\", " + Line.SEPARATOR +
                "        \"popup\": {" + Line.SEPARATOR +
                "            \"menuitem\": [" + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"New\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CreateNewDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Open\", " + Line.SEPARATOR +
                "                    \"onclick\": \"OpenDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Close\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CloseDoc()\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            ]" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}";

        // then
        assertFalse(new JsonStringMatcher("{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"id\": \"wrong_value\", " + Line.SEPARATOR +
                "        \"value\": \"File\", " + Line.SEPARATOR +
                "        \"popup\": {" + Line.SEPARATOR +
                "            \"menuitem\": [" + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"New\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CreateNewDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Open\", " + Line.SEPARATOR +
                "                    \"onclick\": \"OpenDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Close\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CloseDoc()\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            ]" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}").matches(matched));
    }

    @Test
    public void shouldNotMatchNonMatchingSubJson() {
        // given
        String matched = "" +
                "{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"id\": \"file\", " + Line.SEPARATOR +
                "        \"value\": \"File\", " + Line.SEPARATOR +
                "        \"popup\": {" + Line.SEPARATOR +
                "            \"menuitem\": [" + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"New\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CreateNewDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Open\", " + Line.SEPARATOR +
                "                    \"onclick\": \"OpenDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Close\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CloseDoc()\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            ]" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}";

        // then
        assertFalse(new JsonStringMatcher("{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"id\": \"file\", " + Line.SEPARATOR +
                "        \"value\": \"other_value\"" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}").matches(matched));
        assertFalse(new JsonStringMatcher("{" + Line.SEPARATOR +
                "    \"menu\": {" + Line.SEPARATOR +
                "        \"popup\": {" + Line.SEPARATOR +
                "            \"menuitem\": [" + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"New\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CreateNewDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Open\", " + Line.SEPARATOR +
                "                    \"onclick\": \"OpenDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Close\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CloseDoc()\"" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                {" + Line.SEPARATOR +
                "                    \"value\": \"Close\", " + Line.SEPARATOR +
                "                    \"onclick\": \"CloseDoc()\"" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "            ]" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}").matches(matched));
    }

    @Test
    public void shouldNotMatchNullTest() {
        assertFalse(new JsonStringMatcher("some_value").matches(null));
    }

    @Test
    public void shouldNotMatchEmptyTest() {
        assertFalse(new JsonStringMatcher("some_value").matches(""));
    }

    @Test
    public void showHaveCorrectEqualsBehaviour() {
        assertEquals(new JsonStringMatcher("some_value"), new JsonStringMatcher("some_value"));
    }
}
