package jade.ui;

import jade.util.datatype.ColoredChar;
import jade.util.datatype.Coordinate;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

/**
 * Implements a {@code Terminal} on a {@code JPanel}, which can then be embedded into any container
 * able to use a {@code JPanel}.
 */
public class TermPanel extends Terminal
{
    public static final int DEFAULT_COLS = 80;
    public static final int DEFAULT_ROWS = 43;
    public static final int DEFAULT_SIZE = 12;
    
    private static Map<String,Boolean> menus = new HashMap<String,Boolean>();
    
    private Screen screen;

    /**
     * Constructs a new {@code TermPanel} with the given dimensions. Note that the rows and columns
     * can be changed by resizing the underlying JPanel, but font size is fixed.
     * @param columns the default number of columns to display
     * @param rows the default number of rows to display
     * @param fontSize the size of each tile
     */
    public TermPanel(int columns, int rows, int fontSize)
    {
        this(new Screen(columns, rows, fontSize));
    }
    
    /**
     * Constructs a new {@code TermPanel} with the default dimensions. There will be 80 columns, 24
     * rows, and a font size of 12.
     */
    public TermPanel()
    {
        this(DEFAULT_COLS, DEFAULT_ROWS, DEFAULT_SIZE);
    }
    
    protected TermPanel(Screen screen)
    {
        this.screen = screen;
    }

    /**
     * Constructs and returns a new {@code TermPanel} with default dimensions, which is placed
     * inside a {@code JFrame}. The {@code TermPanel} will initially have 80 columns, 24 rows, and a
     * font size of 12.
     * @param title the title of the {@code JFrame}
     * @return a new {@code TermPanel} with default dimensions.
     */
    public static TermPanel getFramedTerminal(String title)
    {
        TermPanel term = new TermPanel();
        frameTermPanel(term, title);
        return term;
    }
    
    protected static void frameTermPanel(TermPanel term, String title)
    {
        //Create and set up the window.	
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // add components here
        frame.add(term.panel());
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }

    /**
     * Returns the underlying {@code JPanel} display of the {@code TermPanel}. This {@code JPanel}
     * can then be embedded in any other container like a normal {@code JPanel}.
     * @return the underlying {@code JPanel} display of the {@code TermPanel}
     */
    public JPanel panel()
    {
        return screen;
    }
    
    protected Screen screen()
    {
        return screen;
    }
    
    public void registerMenu() {
    	menus.put("menu", false);
    	menus.put("seeAll", false);
    	menus.put("nextLevel", false);
    	menus.put("hell", false);
    }
    
    public void setMenu (String menu, boolean visible) {
    	menus.put(menu,visible);
    }
    
    public Boolean getMenu (String menu) {
    	return menus.get(menu);
    }

    @Override
    public char getKey() throws InterruptedException
    {
        return screen.consumeKeyPress();
    }

    @Override
    public void refreshScreen()
    {
    	synchronized (screen) {
    		 screen.setBuffer(getBuffer());
    		 // screen.revalidate();
    	     screen.repaint();
		}
    }
    
    public void bufferFile(String path)
    {
    	this.bufferFile(path,DEFAULT_ROWS);
    }
    
    protected static class Screen extends JPanel implements KeyListener
    {
        private static final long serialVersionUID = 7219226976524388778L;

        private int tileWidth;
        private int tileHeight;
        private BlockingQueue<Character> inputBuffer;
        private Map<Coordinate, ColoredChar> screenBuffer;

        public Screen(int columns, int rows, int fontSize)
        {
            this(columns, rows, fontSize * 3 / 4, fontSize);
        }
        
        public Screen(int columns, int rows, int tileWidth, int tileHeight)
        {
            inputBuffer = new LinkedBlockingQueue<Character>();
            screenBuffer = new HashMap<Coordinate, ColoredChar>();

            addKeyListener(this);
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            // Sets the preferred size of this component.
            setPreferredSize(new Dimension(columns * tileWidth, rows * tileHeight));
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, tileHeight));
            
            setBackground(Color.BLACK);
            setFocusable(true);
            setLayout(new SpringLayout());
            getComponentCount();
        }

        protected int tileWidth()
        {
            return tileWidth;
        }
        
        protected int tileHeight()
        {
            return tileHeight;
        }
  
        //inherited from JPanel -> JComponent

        @Override
        protected void paintComponent(Graphics page)
        {
            super.paintComponent(page);
            synchronized(screenBuffer)
            {
                for(Coordinate coord : screenBuffer.keySet())
                {
                    ColoredChar ch = screenBuffer.get(coord);
                    int x = tileWidth * coord.x();
                    int y = tileHeight * coord.y();

                    page.setColor(ch.color());
                    page.drawString(ch.toString(), x, y);
                }
            }
        }

        public void setBuffer(Map<Coordinate, ColoredChar> buffer)
        {
            synchronized(screenBuffer)
            {
                screenBuffer.clear();
                screenBuffer.putAll(buffer);
            }
        }

        @Override
        public void keyPressed(KeyEvent event)
        {
            inputBuffer.offer(event.getKeyChar());
        }

        public char consumeKeyPress() throws InterruptedException
        {
            return inputBuffer.take();
        }

        @Override
        public void keyReleased(KeyEvent e)
        {}

        @Override
        public void keyTyped(KeyEvent e)
        {}
        
    }
}
