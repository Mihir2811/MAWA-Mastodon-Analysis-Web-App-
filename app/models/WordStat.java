package models;

import java.util.Objects;

/**
 * Model representing a single unique word and its occurrence frequency in posts.
 *
 * @author Qiang Xue
 */
public class WordStat {

    /** The unique word string. */
    private final String word;
    /** The frequency count of the word. */
    private final long count;

    /**
     * Constructs a new WordStat entry.
     *
     * @param word  the unique word
     * @param count the frequency of occurrences
     * @author Qiang Xue
     */
    public WordStat(String word, long count) {
        this.word = word != null ? word : "";
        this.count = count;
    }

    /**
     * Returns the word.
     *
     * @return the word string
     * @author Qiang Xue
     */
    public String getWord() {
        return word;
    }

    /**
     * Returns the count/frequency.
     *
     * @return occurrence frequency
     * @author Qiang Xue
     */
    public long getCount() {
        return count;
    }

    /**
     * Compares this word statistic to another object for equality.
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is equal to the argument; {@code false} otherwise
     * @author Qiang Xue
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordStat wordStat = (WordStat) o;
        return count == wordStat.count && Objects.equals(word, wordStat.word);
    }

    /**
     * Returns the hash code value for this word statistic.
     *
     * @return a hash code value for this object
     * @author Qiang Xue
     */
    @Override
    public int hashCode() {
        return Objects.hash(word, count);
    }

    /**
     * Returns a string representation of this word statistic.
     *
     * @return a string representation of the word statistic
     * @author Qiang Xue
     */
    @Override
    public String toString() {
        return "WordStat{" +
                "word='" + word + '\'' +
                ", count=" + count +
                '}';
    }
}
