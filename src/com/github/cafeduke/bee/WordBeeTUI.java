package com.github.cafeduke.bee;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.github.cafeduke.bee.WordBee.Badge;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

public class WordBeeTUI
{
  private final String COMB_INDENT = "                                                  ";

  private WordBee wordBee = null;

  private Set<String> setWord = new TreeSet<>();

  private ActionListBox wordListBox = null;

  private int score = 0;

  private Badge badge = Badge.BEGINNER;

  public WordBeeTUI(WordBee wordBee)
  {
    this.wordBee = wordBee;
  }

  public static void main(String arg[]) throws IOException
  {
    WordBee wordBee = new WordBee();
    // wordBee.init("ouasypg", 'a');
    wordBee.init();
    System.out.println(wordBee.isBeeWord("hello"));
    WordBeeTUI tui = new WordBeeTUI(wordBee);
    tui.render();
  }

  public void render() throws IOException
  {
    // Initialize Terminal and Screen
    DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory(System.out, System.in, StandardCharsets.UTF_8);
    Screen screen = terminalFactory.createScreen();
    screen.startScreen();

    // Create the GUI manager
    MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
    gui.setTheme(LanternaThemes.getRegisteredTheme("defrost"));
    gui.addListener((textGUI, keyStroke) -> {
      if (keyStroke.getKeyType() == KeyType.Escape)
      {
        new ArrayList<>(((WindowBasedTextGUI) textGUI).getWindows()).forEach(Window::close);
        return true;
      }
      return false;
    });

    // Create a full-screen window without borders so the split fills the terminal
    BasicWindow window = new BasicWindow("WordBee");
    window.setHints(List.of(Window.Hint.FULL_SCREEN, Window.Hint.EXPANDED, Window.Hint.CENTERED));

    // Calculate exact 80% / 20% column widths from current terminal size
    TerminalSize size = screen.getTerminalSize();
    int totalCols = size.getColumns();
    int totalRows = size.getRows();
    int leftWidth = (int) (totalCols * 0.80);
    int rightWidth = totalCols - leftWidth;

    // Create Panels
    Panel mainPanel = new Panel(new BorderLayout());
    Panel panelLeft = new Panel(new LinearLayout(Direction.VERTICAL));
    Panel panelRight = new Panel(new LinearLayout(Direction.VERTICAL));
    renderLeftPanel(panelLeft, leftWidth, totalRows);
    renderRightPanel(panelRight, rightWidth, totalRows);
    mainPanel.addComponent(panelLeft.withBorder(Borders.singleLine("HoneyComb")), BorderLayout.Location.LEFT);
    mainPanel.addComponent(panelRight.withBorder(Borders.singleLine("Words")), BorderLayout.Location.RIGHT);
    mainPanel.setTheme(new SimpleTheme(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));

    window.setComponent(mainPanel);
    gui.addWindowAndWait(window);
    screen.stopScreen();
  }

  private void renderLeftPanel(Panel panel, int colCount, int rowCount) throws IOException
  {
    panel.setPreferredSize(new TerminalSize(colCount, rowCount));

    Label labelASCII = new Label(wordBee.getTextComb());
    labelASCII.setForegroundColor(TextColor.ANSI.YELLOW);

    // Input and submit button
    Panel panelForm = new Panel(new LinearLayout(Direction.HORIZONTAL));
    TextBox inputWord = new TextBox(new TerminalSize(25, 1));
    Button buttonAdd = newFlatButton("Add");
    panelForm.addComponent(inputWord.withBorder(Borders.singleLineBevel("Word")));
    panelForm.addComponent(buttonAdd.withBorder(Borders.singleLineBevel()));

    // Error label
    Label errorLabel = new Label("");
    errorLabel.setForegroundColor(TextColor.ANSI.RED_BRIGHT);

    // Assemble Left Panel
    panel.addComponent(labelASCII);
    panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
    panel.addComponent(panelForm);
    panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
    panel.addComponent(errorLabel);

    // Listener
    buttonAdd.addListener(button -> {
      String word = inputWord.getText().trim().toLowerCase();

      if (word.isEmpty())
        errorLabel.setText("ERROR: Word empty");
      else if (!word.matches("[a-zA-Z]+"))
        errorLabel.setText("ERROR: Letters only (no spaces/numbers)");
      else if (setWord.contains(word))
        errorLabel.setText("ERROR: Already found");
      else if (!wordBee.isBeeWord(word))
        errorLabel.setText("ERROR: Not a BeeWord -- Only use letters in comb with mandatory gold letter");
      else if (!wordBee.isDictBeeWord(word))
        errorLabel.setText("ERROR: Not in dictionary");
      else
      {
        // Valid input: clear error, add word, refresh list, clear box
        errorLabel.setText("");
        refreshWordList(word);
        inputWord.setText("");
      }
    });
  }

  private void renderRightPanel(Panel panelRight, int colCount, int rowCount) throws IOException
  {
    panelRight.setPreferredSize(new TerminalSize(colCount, rowCount));

    // ActionListBox provides built-in vertical scrolling and keyboard navigation
    wordListBox = new ActionListBox(new TerminalSize(colCount - 2, rowCount - 2));
    refreshWordList(null);

    // Assemble Right Panel
    panelRight.addComponent(wordListBox);
  }

  private void refreshWordList(String word)
  {
    if (wordListBox == null)
      return;

    wordListBox.clearItems();
    if (word != null)
    {
      setWord.add(word);
      score += wordBee.getScore(word);
      badge = wordBee.getBadge(score);
    }

    wordListBox.addItem("%10s [%03d]".formatted(badge.name(), score), null);
    int count = 0;
    for (String currWord : setWord)
      wordListBox.addItem("%2d) %s".formatted(++count, currWord), null);
  }

  private static Button newFlatButton(String label)
  {
    Button btn = new Button(" " + label + " ")
      {
        @Override
        protected ButtonRenderer createDefaultRenderer()
        {
          return new Button.FlatButtonRenderer();
        }
      };
    btn.setTheme(SimpleTheme.makeTheme(true, TextColor.ANSI.BLUE, TextColor.ANSI.BLACK, TextColor.ANSI.BLACK, TextColor.ANSI.BLACK, TextColor.ANSI.YELLOW, TextColor.ANSI.BLACK, TextColor.ANSI.BLACK));
    return btn;
  }

}
