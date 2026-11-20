package com.github.cafeduke.bee.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

public final class Util
{
  public static final File DIR_RESOURCES = new File("resources");

  /**
   * Static file: Dictionary words (commony used only)
   */
  public static final File FILE_DICT = new File(DIR_RESOURCES, "dict.txt");

  /**
   * Static file: Popular dictionary words sorted by their popularity -- from most popular to least.
   */
  public static final File FILE_DICT_POPULAR = new File(DIR_RESOURCES, "dict-popular.txt");

  /**
   * Bee words extracted from {@link #FILE_DICT dictionary}
   */
  public static final File FILE_BEE_WORDS = new File(DIR_RESOURCES, "bee-words.txt");

  /**
   * The {@link #FILE_DICT_POPULAR} is filtered to have only bee words along with a popularity-score
   */
  public static final File FILE_BEE_WORDS_POPULAR_SCORE = new File(DIR_RESOURCES, "bee-words-popular-score.txt");

  /**
   * Score for each alphabet from a range of 1-10
   */
  public static final File FILE_ALPHA_SCORE = new File(DIR_RESOURCES, "alpha-score.txt");

  public static final Random RAND = getRand();

  private Util()
  {

  }

  public static Random getRand()
  {
    ZoneId zoneIST = ZoneId.of(ZoneId.SHORT_IDS.get("IST"));
    LocalDate date = LocalDate.now(zoneIST);
    long seed = date.getDayOfYear();
    return new Random(seed);
  }

  public static int rand(int n)
  {
    return (int) (RAND.nextDouble() * n);
  }

  public static void main(String arg[]) throws Exception
  {

    // Equivalent to Math.random(), but reproducible
    for (int i = 0; i < 25; ++i)
    {
      int value = (int) (RAND.nextDouble() * 1000);
      System.out.println(value);
    }
  }

  public static void genAlphaScoreFile() throws IOException
  {
    if (!FILE_BEE_WORDS_POPULAR_SCORE.exists())
      throw new FileNotFoundException("File %s not found".formatted(FILE_BEE_WORDS_POPULAR_SCORE.getAbsolutePath()));

    int score[] = new int[26];
    try (BufferedReader in = new BufferedReader(new FileReader(FILE_BEE_WORDS_POPULAR_SCORE)))
    {
      String line = null;
      while ((line = in.readLine()) != null)
      {
        line.split(":")[0]
          .chars()
          .forEach(x -> score[x - 'a']++);
      }
    }

    try (PrintWriter out = new PrintWriter(FILE_ALPHA_SCORE))
    {
      Arrays.stream(score)
        .forEach(x -> out.println(x));
    }

  }

  /**
   * Generate file having <word>:<score> by reading through a file where words are sorted by popularity {@link #FILE_DICT_POPULAR}
   *
   * Scoring:
   *  - The score is of the range 1-100.
   *  - Higher the score, more uncommon a word, thus indicates a better guess
   *  - If the word does not exist in the {@link #FILE_DICT_POPULAR} but is a valid word, present in {@link #FILE_DICT}, then this word receives maximum score.
   *
   * @throws IOException
   */
  public static void genPopularWordsScoreFile() throws IOException
  {
    try (BufferedReader in = new BufferedReader(new FileReader(FILE_DICT_POPULAR)); PrintWriter out = new PrintWriter(FILE_BEE_WORDS_POPULAR_SCORE))
    {
      String word = null;
      int count = 0;
      while ((word = in.readLine()) != null)
      {
        if (!isBeeWord(word))
          continue;
        out.println("%s:%d".formatted(word, (++count / 100) + 1));
      }
    }
  }

  /**
   * Generate {@link #FILE_BEE_WORDS} from {@link #FILE_DICT} by seclecting only {@link #isBeeWord(String) BeeWords}
   *
   * @see {@link #isBeeWord(String)}
   * @throws IOException
   */
  public static void genBeeWordsFile() throws IOException
  {
    try (BufferedReader in = new BufferedReader(new FileReader(FILE_DICT)); PrintWriter out = new PrintWriter(FILE_BEE_WORDS))
    {
      String word = null;
      while ((word = in.readLine()) != null)
      {
        if (isBeeWord(word))
          out.println(word);
      }
    }
  }

  /**
   * A given word is a BeeWord if it satisfies all the following conditions
   *  - Has length greater than 3
   *  - Is NOT made of up of more than 7 unique letters
   *
   * @param word World to be analysed
   * @return true if the word is a BeeWord, false otherwise
   */
  public static boolean isBeeWord(String word)
  {
    if (word.length() < 4)
      return false;

    Set<Character> setChar = new HashSet<>();
    for (char ch : word.toCharArray())
      setChar.add(ch);

    if (setChar.size() > 7)
      return false;

    return true;
  }

  public static Character[] toArray(String s)
  {
    return ArrayUtils.toObject(s.toCharArray());
  }

  public static Set<Character> toSet(String s)
  {
    HashSet<Character> set = new HashSet<>();
    Collections.addAll(set, toArray(s));
    return set;
  }

  public static char[] toPrimitive(Set<Character> set)
  {
    return ArrayUtils.toPrimitive(set.toArray(new Character[0]));
  }
}
