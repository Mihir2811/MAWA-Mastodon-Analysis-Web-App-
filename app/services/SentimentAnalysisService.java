package services;

/**
 * Classifies post content as positive, negative, or neutral sentiment.
 *
 * @author Mihir Vipulbhai Panchal (ID: 40366948)
 */
public interface SentimentAnalysisService
{

    /**
     * Analyzes content using the application's happy and sad word and symbol lists.
     *
     * @param content readable post content
     * @return the corresponding display symbol: {@code :-)}, {@code :-(}, or {@code :-|}
     * @author Mihir Vipulbhai Panchal
     */
    String analyze(String content);
}