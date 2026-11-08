package services;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for happy, sad, neutral, and threshold sentiment classification.
 *
 * @author Mihir Vipulbhai Panchal (ID: 40366948)
 */

public class SentimentAnalysisServiceImplTest {

    /** The SentimentAnalysisServiceImpl instance under test. */
    private final SentimentAnalysisServiceImpl service = new SentimentAnalysisServiceImpl();

    /**
     * Confirms positive words, emojis, and emoticons can produce a happy classification.
     *
     * @author Mihir Vipulbhai Panchal
     */
    @Test
    public void classifiesHappyWordsAndSymbols() 
    {
        assertEquals(":-)", service.analyze("I love this, it's wonderful 😊 :-)"));
        assertEquals(":-)", service.analyze("great 😀"));
        assertEquals(":-)", service.analyze("I am very happy and I love this! :-)"));
        assertEquals(":-)", service.analyze("I love this amazing update! 👍 :-)"));
        assertEquals(":-)", service.analyze("HAPPY, LOVE!"));
    }

    /**
     * Confirms negative words, emojis, and emoticons can produce a sad classification.
     *
     * @author Mihir Vipulbhai Panchal
     */
    @Test
    public void classifiesSadWordsAndSymbols() 
    {
        assertEquals(":-(", service.analyze("I feel sad and disappointed 😢 :("));
        assertEquals(":-(", service.analyze("terrible 💔"));
        assertEquals(":-(", service.analyze("This is terrible and I hate it :-("));
        assertEquals(":-(", service.analyze("This is awful and disappointing. :-("));
    }

    /**
     * Confirms negative words and negated positive words classify critical content as sad.
     *
     * @author Mihir Vipulbhai Panchal
     */
    @Test
    public void handlesNegativeWordsAndNegation() 
    {
        assertEquals(":-(", service.analyze("I personally think this is crap, and it isn't great."));
        assertEquals(":-)", service.analyze("This is not bad, actually good."));
    }

    /**
     * Prevents regression for the critical Mastodon post that was previously shown as neutral.
     *
     * @author Mihir Vipulbhai Panchal
     */
    @Test
    public void classifiesScreenshotPostAsSad() 
    {
        String content = "I've noticed that AI loves to write `static` java functions. Put them everywhere. " +
                "I personally think this is crap, and it highlights the training data of Java code out " +
                "there isn't great. You can't swap static classes. You can test your code in isolation " +
                "with them. They're useful in certain circumstances. I think what's happening here is " +
                "knowledge vs wisdom - AI knows how to write Java code, but clearly hasn't lived through it.";

        assertEquals(":-(", service.analyze(content));
    }

    /**
     * Confirms mixed signals and exact 70-percent ratios remain neutral.
     *
     * @author Mihir Vipulbhai Panchal
     */
    @Test
    public void classifiesMixedAndThresholdRatiosAsNeutral() 
    {
        assertEquals(":-|", service.analyze("happy happy sad"));
        assertEquals(":-|", service.analyze("happy happy happy happy happy happy happy sad sad sad"));
        assertEquals(":-|", service.analyze("sad sad sad sad sad sad sad happy happy happy"));
        assertEquals(":-|", service.analyze("ordinary post with no sentiment signals"));
        assertEquals(":-|", service.analyze("The Java release was today and this is bad."));
        assertEquals(":-|", service.analyze("The Java release was today and this is happy."));
        assertEquals(":-|", service.analyze("Visit https://example.com/happy and ask @great about Java."));
        assertEquals(":-|", service.analyze("Visit example.com/happy for Java release details."));
        assertEquals(":-|", service.analyze("#happy Java 25 was released today."));
        assertEquals(":-)", service.analyze("#happy #great"));
    }

    /**
     * Confirms null and blank inputs safely return neutral sentiment.
     *
     * @author Mihir Vipulbhai Panchal
     */
    @Test
    public void classifiesNullAndBlankContentAsNeutral() 
    {
        assertEquals(":-|", service.analyze(null));
        assertEquals(":-|", service.analyze("  "));
    }
}