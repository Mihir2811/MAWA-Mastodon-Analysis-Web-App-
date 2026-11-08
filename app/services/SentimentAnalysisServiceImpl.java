package services;

import javax.inject.Singleton;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Classifies content from recognized happy and sad words, emojis, and emoticons. At least two
 * sentiment cues are required, and one polarity must account for strictly more than 70 percent.
 *
 * @author Mihir Vipulbhai Panchal (ID: 40366948)
 */

@Singleton
public class SentimentAnalysisServiceImpl implements SentimentAnalysisService 
{

    /** Symbol representing happy sentiment. */
    private static final String HAPPY = ":-)";
    /** Symbol representing sad sentiment. */
    private static final String SAD = ":-(";
    /** Symbol representing neutral sentiment. */
    private static final String NEUTRAL = ":-|";
    /** Minimum number of sentiment cues required to classify non-neutral sentiment. */
    private static final long MIN_SENTIMENT_MATCHES = 2;

    /** Pattern used to tokenize words and punctuation tokens from text. */
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{L}\\p{N}']+|[,;.!?\\r\\n\\u2014\\u2013]");

    /** Set of punctuation and newline characters that break clause boundaries. */
    private static final Set<String> CLAUSE_BREAKS = Set.of(",", ";", ".", "!", "?", "\r", "\n", "—", "–");

    /** Pattern matching HTTP/HTTPS and domain URLs to strip before sentiment analysis. */
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?i)(?:https?://|www\\.)\\S+|\\b(?:[a-z0-9-]+\\.)+[a-z]{2,}(?::\\d+)?(?:[/?#]\\S*)?");

    /** Pattern matching account mentions (e.g. \@user\@host) to strip before sentiment analysis. */
    private static final Pattern MENTION_PATTERN = Pattern.compile(
            "(?<![\\p{L}\\p{N}_])@[\\p{L}\\p{N}_]+(?:@[\\p{L}\\p{N}.-]+)?");

    /** Set of English negation words that invert sentiment polarity of nearby terms. */
    private static final Set<String> NEGATIONS = Set.of(
            "ain't", "aren't", "can't", "cannot", "couldn't", "didn't", "doesn't", "don't", "hardly",
            "isn't", "lack", "lacks", "lacking", "never", "neither", "no", "nor", "not", "shouldn't",
            "wasn't", "weren't", "without", "won't", "wouldn't"
    );

    /** Set of recognized positive words in English and Portuguese. */
    private static final Set<String> HAPPY_WORDS = Set.of(
            "adorable", "amazing", "amazed", "awesome", "beautiful", "beneficial", "brilliant", "calm",
            "cheerful", "delighted", "enjoy", "enjoyed", "enjoying", "excellent", "excited", "exciting",
            "fantastic", "favorite", "favourite", "fun", "glad", "good", "gorgeous", "grateful",
            "great", "happy", "helpful", "impressed", "incredible", "like", "liked", "love", "loved",
            "lovely", "loving",
            "marvelous", "nice", "outstanding", "perfect", "pleasant", "pleased", "recommend", "recommended",
            "relieved", "satisfied", "smile", "smiled", "smiling", "success", "successful", "thankful",
            "thrilled", "valuable", "win", "won", "wonderful", "yay", "yes",
            "ótima", "ótimo", "adoro", "adorei", "alegria", "alegre", "amei", "bom", "boa", "excelente",
            "felicidade", "feliz", "gostei", "incrível", "legal", "linda", "lindo", "maravilhosa",
            "maravilhoso", "perfeita", "perfeito", "recomendo", "satisfeita", "satisfeito"
    );

    /** Set of recognized negative words in English and Portuguese. */
    private static final Set<String> SAD_WORDS = Set.of(
            "abysmal", "annoyed", "annoying", "angry", "anguished", "anxious", "awful", "bad", "boring",
            "broken", "buggy", "crap", "crappy", "cry", "crying", "damaged", "depressed", "depressing",
            "disappoint", "disappointed", "disappointing", "disaster", "disgusted", "disgusting", "dreadful",
            "failure", "failed", "failing", "fear", "frustrated", "frustrating", "frustration", "gloomy",
            "hate", "hated", "hates", "hating", "heartbroken", "horrible", "hostile", "hurt",
            "inferior", "irritated", "irritating", "lousy", "miserable", "miss", "missed", "negative",
            "pain", "painful", "pathetic", "poor", "problematic", "regret", "regrettable", "sad", "sadness",
            "scared", "shame", "shameful", "sucks", "terrible", "tragic", "unhappy", "unpleasant", "upset",
            "useless", "weak", "worthless", "worst", "wrong", "aborrecida", "aborrecido", "chateada",
            "chateado", "decepcionada", "decepcionado", "desapontada", "desapontado", "desastre", "detesto",
            "frustrada", "frustrado", "frustrante", "horrível", "inútil", "lamentável", "odeia", "odeio",
            "odiei", "péssima", "péssimo", "raiva", "ruim", "triste", "tristeza", "terrível"
    );

    /** List of emoticons and emojis representing happy sentiment. */
    private static final List<String> HAPPY_SYMBOLS = List.of(
            ":-)", ":)", ":-d", ":d", ";-)", ";)", "(-:", "<3", "^_^", "xd", "😀", "😃", "😄", "😁",
            "😆", "😌", "😊", "🙂", "🙃", "😍", "🥰", "🤗", "😎", "🥳", "❤️", "💖", "👍", "👏", "💯",
            "🎉", "🎊", "🙌", "✨", "🌟", "🚀"
    );

    /** List of emoticons and emojis representing sad sentiment. */
    private static final List<String> SAD_SYMBOLS = List.of(
            ":-(", ":(", ":'-(", ":'(", ";-(", ";(", "):", ">:(", "t_t", ";_;", ":c", "🙁", "☹️",
            "😞", "😔", "😢", "😭", "😟", "😩", "😫", "😕", "😖", "😣", "😥", "😓", "😠", "😡", "🤬", "😿",
            "👎", "💔"
    );

    /**
     * Counts recognized sentiment signals using Java Streams and applies the 70 percent rule.
     *
     * @param content readable post content, possibly null
     * @return {@code :-)}, {@code :-(}, or {@code :-|}
     * @author Mihir Vipulbhai Panchal
     */
    @Override
    public String analyze(String content) 

    {
        if (content == null || content.isBlank()) 
        {
            return NEUTRAL;
        }

        String normalized = prepareContent(content);
        String[] words = TOKEN_PATTERN.matcher(normalized).results()
            .map(match -> match.group())
            .toArray(String[]::new);


        long happyCount = countWords(words, true) + countSymbols(normalized, HAPPY_SYMBOLS);
        long sadCount = countWords(words, false) + countSymbols(normalized, SAD_SYMBOLS);
        long totalCount = happyCount + sadCount;

        if (totalCount < MIN_SENTIMENT_MATCHES) 
        {
            return NEUTRAL;
        }

        if (happyCount * 10 > totalCount * 7) 
        {
            return HAPPY;
        }

        if (sadCount * 10 > totalCount * 7) 
        {
            return SAD;
        }

        return NEUTRAL;
    }

    /**
     * Removes URLs and account mentions before matching while retaining post text, hashtags, and symbols.
     *
     * @param content original readable post content
     * @return normalized text to analyze
     * @author Mihir Vipulbhai Panchal
     */
    private static String prepareContent(String content) {
        String withoutUrls = URL_PATTERN.matcher(content).replaceAll(" ");
        String withoutMentions = MENTION_PATTERN.matcher(withoutUrls).replaceAll(" ");
        return withoutMentions.toLowerCase(Locale.ROOT)
                .replace('\u2018', '\'')
                .replace('\u2019', '\'');
    }

    /**
     * Counts whole-word sentiment matches, reversing polarity when a nearby negation is present.
     *
     * @param words tokens from lower-case post content
     * @param happy whether to count positive rather than negative words
     * @return the number of matching words
     * @author Mihir Vipulbhai Panchal
     */
    private static long countWords(String[] words, boolean happy) 
    {
        return IntStream.range(0, words.length)
                .filter(index -> {
                    boolean isHappy = HAPPY_WORDS.contains(words[index]);
                    boolean isSad = SAD_WORDS.contains(words[index]);
                    if (!isHappy && !isSad) 
                    {
                        return false;
                    }

                    boolean negated = isNegated(words, index);
                    return happy ? (negated ? isSad : isHappy) : (negated ? isHappy : isSad);

                })
                .count();
    }

    /**
     * Checks whether a sentiment token is preceded by a negation within three words.
     *
     * @param words tokens from lower-case post content
     * @param index index of the sentiment token
     * @return true when a preceding token negates the sentiment
     * @author Mihir Vipulbhai Panchal
     */
    private static boolean isNegated(String[] words, int index) 
    {
        int firstPossibleNegation = Math.max(0, index - 3);
        for (int previous = index - 1; previous >= firstPossibleNegation; previous--) 
            {

            if (CLAUSE_BREAKS.contains(words[previous])
                    || HAPPY_WORDS.contains(words[previous])
                    || SAD_WORDS.contains(words[previous])) 
            {
                return false;
            }

            if (NEGATIONS.contains(words[previous])) 
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts each occurrence of the supplied emoticons and emojis.
     *
     * @param content post content
     * @param symbols sentiment symbols to count
     * @return the number of matching symbols
     * @author Mihir Vipulbhai Panchal
     */
    private static long countSymbols(String content, List<String> symbols) {
        return symbols.stream()
                .mapToLong(symbol -> countOccurrences(content, symbol))
                .sum();
    }

    /**
     * Counts non-overlapping occurrences of one symbol in the content.
     *
     * @param content post content
     * @param symbol symbol to find
     * @return the number of occurrences
     * @author Mihir Vipulbhai Panchal
     */
    private static long countOccurrences(String content, String symbol) {
        long occurrences = 0;
        boolean hasLetters = symbol.codePoints().anyMatch(Character::isLetter);
        String expression = Pattern.quote(symbol);

        if (hasLetters) 
        {
            expression = "(?<![\\p{L}\\p{N}_])" + expression + "(?![\\p{L}\\p{N}_])";
        }

        java.util.regex.Matcher matcher = Pattern.compile(expression).matcher(content);

        while (matcher.find()) 
        {
            occurrences++;
        }
        return occurrences;
    }
}