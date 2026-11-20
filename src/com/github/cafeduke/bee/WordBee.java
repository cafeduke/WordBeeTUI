package com.github.cafeduke.bee;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.github.cafeduke.bee.util.Util;

public class WordBee
{
  public static final int BEE_COMB_SIZE = 7;

  public static Properties properties = new Properties();

  public static final int MAX_SCORE_PER_WORD = 80;

  public static final int ALPHA_SCORE[] =
  {
    /*  a,  b  c  d  e   f  g  h  i   j  k  l   m  n   o   p  q  r   s   t   u, v  w  x  y  z */
    /**/10, 4, 8, 8, 10, 3, 5, 5, 10, 1, 2, 10, 5, 10, 10, 6, 1, 10, 10, 10, 6, 3, 2, 1, 3, 1
  };

  /**
   * Letters chosen for the bee puzzle
   */
  private Set<Character> setBeeLetter = new HashSet<>();

  /**
   * The letter picked as golden
   */
  private char gold = ' ';

  /**
   * A map of all possible BeeWords (with given BeeLetters and gold) to the corresponding score.
   * The score is based on the popularity of the word. A rare word gets a higher score.
   */
  private Map<String, Integer> mapWordScore = new HashMap<>();

  /**
   * Given all possible words in {@code mapWordScore}, the summation of all scores gives max score.
   */
  private int maxScore = 0;

  /**
   * Badges earned with score
   */
  public enum Badge
  {
    BEGINNER, GOOD_START, NICE, GOOD, VERY_GOOD, EXCELLENT, AMAZING, MARVELLOUS, EXCEPTIONAL, OUTSTANDING;

    public static Badge getBadge(int score, int maxScore)
    {
      int badgeSize = Badge.values().length;
      int chunkSize = maxScore / badgeSize;
      int index = score / chunkSize;
      index = (index < badgeSize) ? index : badgeSize - 1;
      return Badge.values()[index];
    }
  }

  public WordBee()
  {
  }

  /**
   * This distribution determines the probability of chosing a vowel count (number of vowels)
   * Eg: Probability of having two vowels = (Number of occurences of 2 in VOWEL_COUNT_DISTRIBUTION) / VOWEL_COUNT_DISTRIBUTION.length
   */
  public static final int VOWEL_COUNT_DISTRIBUTION[] =
  {
    1, 1,
    2, 2, 2, 2, 2, 2, 2, 2,
    3, 3, 3, 3,
    4, 4
  };

  private static final String NEW_LINE = System.getProperty("line.separator");

  public static void main(String[] args) throws Exception
  {
    WordBee wordBee = new WordBee();
    wordBee.init("ouasypg", 'a');
  }

  public void init() throws IOException
  {
    init(null, ' ');
  }

  public void init(String beeLetters, char gold) throws IOException
  {
    if (beeLetters != null && !beeLetters.isBlank())
    {
      this.setBeeLetter = Util.toSet(beeLetters);
      this.gold = gold;
    }
    genBeeLetters();
    pickGoldenLetter();
    genMatchingWords();
  }

  public void genMatchingWords() throws IOException
  {
    // Set having all words that can be created using setBeeLetter and gold -- MatchingBeeWords
    Set<String> setMatch = new HashSet<>();
    try (BufferedReader in = new BufferedReader(new FileReader(Util.FILE_BEE_WORDS)))
    {
      String word = null;
      while ((word = in.readLine()) != null)
      {
        if (!isBeeWord(word))
          continue;
        setMatch.add(word);
      }
    }

    // Map of all the BeeWords to their corresponding score
    maxScore = 0;
    int mapSize = 0;
    int setMatchSize = setMatch.size();
    try (BufferedReader in = new BufferedReader(new FileReader(Util.FILE_BEE_WORDS_POPULAR_SCORE)))
    {
      String word = null;
      int score = -1;
      while ((word = in.readLine()) != null && mapSize < setMatchSize)
      {
        String token[] = word.split(":");
        assert token.length == 2;
        word = token[0];
        score = Integer.valueOf(token[1]);
        if (setMatch.contains(word))
        {
          mapWordScore.put(word, score);
          maxScore += score;
          setMatch.remove(word);
          ++mapSize;
        }
      }
    }

    for (String word : setMatch)
    {
      mapWordScore.put(word, MAX_SCORE_PER_WORD);
      maxScore += MAX_SCORE_PER_WORD;
    }
  }

  public void pickGoldenLetter()
  {
    if (gold != ' ')
      return;

    char ch[] = Util.toPrimitive(setBeeLetter);
    int cumulative[] = new int[BEE_COMB_SIZE];
    int sum = 0;
    for (int i = 0; i < ch.length; ++i)
      cumulative[i] = sum = sum + ALPHA_SCORE[ch[i] - 'a'];

    // if insertion index is negative, -insertion-1 gives the index at which the element num can be inserted.
    // In our case it gives the index of the chosen golden letter
    int num = Util.rand(sum) + 1;
    int insertionIndex = Arrays.binarySearch(cumulative, num);
    insertionIndex = (insertionIndex < 0) ? -insertionIndex - 1 : insertionIndex;

    gold = ch[insertionIndex];
  }

  /**
   * @return Set of char that from the letters of Bee puzzle
   */
  public void genBeeLetters()
  {
    if (setBeeLetter.size() == BEE_COMB_SIZE)
      return;

    // Determine vowel and consoant count
    int vowelsCount = VOWEL_COUNT_DISTRIBUTION[Util.rand(VOWEL_COUNT_DISTRIBUTION.length)];
    int consonantsCount = BEE_COMB_SIZE - vowelsCount;

    // Choose vowels
    List<Character> listVowelDistribution = new ArrayList<>();
    Collections.addAll(listVowelDistribution, Util.toArray("aaaeeiioou"));
    for (int i = 0; i < vowelsCount; ++i)
    {
      Character ch = listVowelDistribution.remove(Util.rand(listVowelDistribution.size()));
      listVowelDistribution.removeAll(Set.of(ch));
      setBeeLetter.add(ch);
    }

    // Choose consonants
    List<Character> listConsonantDistribution = new ArrayList<>();
    Collections.addAll(listConsonantDistribution, Util.toArray("bbbbccccccccddddddddfffggggghhhhhjkkllllllllllmmmmmnnnnnnnnnnppppppqrrrrrrrrrrssssssssssttttttttttvvvwwxyyyz"));
    for (int i = 0; i < consonantsCount; ++i)
    {
      Character ch = listConsonantDistribution.remove(Util.rand(listConsonantDistribution.size()));
      listConsonantDistribution.removeAll(Set.of(ch));
      setBeeLetter.add(ch);
    }
  }

  public boolean isDictBeeWord(String word)
  {
    return mapWordScore.containsKey(word);
  }

  public boolean isBeeWord(String word)
  {
    if (word.indexOf(gold) < 0)
      return false;

    // if setBeeLetter is superset of setDictLetter --> setDictLetter has only letters present in setBeeLetter
    Set<Character> setDictLetter = Util.toSet(word);
    return setBeeLetter.containsAll(setDictLetter) ? true : false;
  }

  public int getScore(String word)
  {
    return mapWordScore.containsKey(word) ? mapWordScore.get(word) : 0;
  }

  public Badge getBadge(int score)
  {
    return Badge.getBadge(score, this.maxScore);
  }

  public String getTextComb()
  {
    final String COMB_INDENT = "                                                  ";

    char ch[] = new char[BEE_COMB_SIZE - 1];
    int index = 0;
    for (Character c : setBeeLetter)
      if (c != gold)
        ch[index++] = (char) (c - 32);

    StringBuilder builder = new StringBuilder();
    builder
      .append("%s         _____".formatted(COMB_INDENT))
      .append(NEW_LINE)
      .append("%s        /     \\".formatted(COMB_INDENT))
      .append(NEW_LINE)
      .append("%s  _____/   %c   \\_____ ".formatted(COMB_INDENT, ch[0]))
      .append(NEW_LINE)
      .append("%s /     \\       /     \\".formatted(COMB_INDENT))
      .append(NEW_LINE)
      .append("%s/   %c   \\_____/   %c   \\".formatted(COMB_INDENT, ch[1], ch[2]))
      .append(NEW_LINE)
      .append("%s\\       /     \\       /".formatted(COMB_INDENT))
      .append(NEW_LINE)
      .append("%s \\_____/   %c   \\_____/".formatted(COMB_INDENT, (char) (gold - 32)))
      .append(NEW_LINE)
      .append("%s /     \\       /     \\".formatted(COMB_INDENT))
      .append(NEW_LINE)
      .append("%s/   %c   \\_____/   %c   \\".formatted(COMB_INDENT, ch[3], ch[4]))
      .append(NEW_LINE)
      .append("%s\\       /     \\       /".formatted(COMB_INDENT))
      .append(NEW_LINE)
      .append("%s \\_____/   %c   \\_____/".formatted(COMB_INDENT, ch[5]))
      .append(NEW_LINE)
      .append("%s       \\       / ".formatted(COMB_INDENT))
      .append(NEW_LINE)
      .append("%s        \\_____/".formatted(COMB_INDENT))
      .append(NEW_LINE);
    return builder.toString();
  }
}
