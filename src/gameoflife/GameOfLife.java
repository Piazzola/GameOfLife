/**Game of Life 
 * <br>
 * @author <A HREF="mailto:mirko.piazzola@gmail.com"> Mirko Piazzola VR353055 </A>
 */
package gameoflife;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
/**Main class of the Game of Life*/
public class GameOfLife extends JFrame{
    /**The number of rows in the grid printed on screen.*/
    int row;
    /**The number of columns in the grid printed on screen.*/
    int col;
    /**The size, in pixels, of a cell*/
    final int dim=10;
    /**The matrix of the cells that compose the world of Game of Life*/
    Cell grid[][];
    /**The temporary matrix of the state of cells for the next generation*/
    boolean next[][];
    /**The color value of the dead cells*/
    final Color DEAD=Color.BLACK;
    /**The color value of the alive cells*/
    final Color ALIVE=Color.GREEN;
    /**The values of the dimension of the screen where Game of Life is running*/
    Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
    /**The state of the game that specifies if it has to proccess the next 
     * generation or not*/
    boolean play=false;
    /**The value of the last widged clicked*/
    int click=0;
    /**The slider component used to control the refresh speed of the game*/
    JSlider speedController;
    /**The array of threads that scan the world to generate the next 
     * generation*/
    RowUpdater thread[];
    /**The constructor of Game of Life that build the window with the 
     * controllers*/
    public GameOfLife(int threads){
        super();
        createWindow();
        printControllers();
        printGrid();
        if(threads<1)
            threads=1;
        thread=new RowUpdater[threads];
        evolve(threads);
    }
    /**This metod creates the fullscreen window*/
    private void createWindow() {
        //no title bar
        setUndecorated(true); 
        //fullscreen
        setSize(screenSize.width,screenSize.height);
        //whitout this I have a green flash clicking on the window's borders
        getContentPane().setLayout(null); 
        row=screenSize.height/dim; 
        col=(screenSize.width-80)/dim;
    }
    /**This metod print on the left side window the play/pause, widgets, 
     * close buttons and speed slider*/
    private void printControllers(){
        //print playpause button
        PlayPause pp=new PlayPause();
        pp.setBounds(0,0,80,80);
        getContentPane().add(pp);
        //create pattern buttons
        Pattern p[]=new Pattern[6];
        //print blinker widget button
        p[0]=new Pattern("blinker");
        p[0].setBounds(0,80,80,80);
        getContentPane().add(p[0]);
        //print toad widget button
        p[1]=new Pattern("toad");
        p[1].setBounds(0,160,80,80);
        getContentPane().add(p[1]);
        //print beacon widget button
        p[2]=new Pattern("beacon");
        p[2].setBounds(0,240,80,80);
        getContentPane().add(p[2]);
        //print pulsar widget button
        p[3]=new Pattern("pulsar");
        p[3].setBounds(0,320,80,80);
        getContentPane().add(p[3]);
        //print glider widget button
        p[4]=new Pattern("glider");
        p[4].setBounds(0,400,80,80);
        getContentPane().add(p[4]);
        //print lightweight spaceship widget button
        p[5]=new Pattern("lwss");
        p[5].setBounds(0,480,80,80);
        getContentPane().add(p[5]);
        //print close button
        Close cls=new Close();
        cls.setBounds(0,560,80,80);
        getContentPane().add(cls);
        //print speed slider
        speedController=new JSlider(JSlider.VERTICAL,1,15,1);
        speedController.setBackground(DEAD);
        speedController.setBounds(0, 640, 80, screenSize.height-640);
        getContentPane().add(speedController);
    }
    /**This metod print the world of cells*/
    private void printGrid() {
        grid=new Cell[row=screenSize.height/dim][col=(screenSize.width-80)/dim];
        next=new boolean[row][col];
        for(int r=0;r<row;r++){
            for(int c=0;c<col;c++){
                getContentPane().add(grid[r][c]=new Cell(r,c,80,dim));
            }
        }
        setVisible(true);
    }
    /**This, is the main, asks how many threads use to build generations and 
     * starts the Game of Life*/
    public static void main(String[] args) {
        String res;
        GameOfLife frame;
        try{
            res=JOptionPane.showInputDialog(null,"Digit how many threads will "
                    + "be used in this instance of the game of life:",
                    "Game Of Life",JOptionPane.QUESTION_MESSAGE);
            frame = new GameOfLife(Integer.parseInt(res));
        }catch(HeadlessException | NumberFormatException e){
            System.out.println(e.toString());
            frame = new GameOfLife(1);
        }
        frame.setVisible(true);
    }
    /**This metod starts threads when user click on play and updates the world*/
    private void evolve(int threads){
        while(true){
            //wait a moment
            try{
                Thread.sleep(1000/speedController.getValue());
            }catch(InterruptedException e){
                System.out.println(e.toString());
            }
            //if play was clicked start threads
            if(play){
                for(int t=0;t<threads;t++){
                    thread[t]=new RowUpdater(t,threads);
                    thread[t].start();
                }
                //wait for threads
                for(int t=0;t<threads;t++){
                    try {
                        thread[t].join();
                    } catch (InterruptedException ex) {
                        System.out.println(ex.toString());
                    }
                }
                //update world
                for(int r=0;r<row;r++){
                    for(int c=0;c<col;c++){
                        if(next[r][c])
                            grid[r][c].born();
                        else 
                            grid[r][c].die();
                    }
                }
                repaint();
            }
        }
    }
    /**Cell is the object on what the Game of Life is based. This class 
     * specifies the state of a cell and what happen if it being clicked.*/
    public class Cell extends JButton {
        /**The state of the cell.*/
        private boolean dead=true;
        /**The row where this cell is located*/
        private final int cr;
        /**The column where this cell is located*/
        private final int cc;
        /**The weight of the controllers*/
        private final int cb;
        /**The dimension of the cell*/
        private final int cd;
        /**The constructor set a new dead cell*/
        protected Cell(int r, int c, int b, int d) {
            //build cell button
            super();
            //store location and size
            cr=r;
            cc=c;
            cb=b;
            cd=d;
            //print cell button
            setBounds((c*d)+b, r*d, d, d);
            setBackground(DEAD); //born dead
            this.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent event) {
                    switch(click){
                        //no widget clicked: this cell will reborn dead forever
                        case 0:
                            grid[cr][cc].die();
                            grid[cr][cc]=new Cell(cr,cc,cb,cd){
                                @Override
                                public void born(){
                                    die();
                                }
                            };
                            break;
                        //blinker
                        case 1:
                            grid[cr][cc].born();
                            if((cr+1)<row)
                                grid[cr+1][cc].born();
                            if((cr+2)<row)
                                grid[cr+2][cc].born();
                            break;
                        //toad
                        case 2:
                            grid[cr][cc].born();
                            if((cr+1)<row){
                                grid[cr+1][cc].born();
                                if((cc-1)>0)
                                    grid[cr+1][cc-1].born();
                                if((cc+1)<col){
                                    grid[cr+1][cc+1].born();
                                    grid[cr][cc+1].born();
                                }
                                if((cc+2)<col)
                                    grid[cr][cc+2].born();
                            }else{
                                if((cc+1)<col){
                                    grid[cr][cc+1].born();
                                }
                                if((cc+2)<col)
                                    grid[cr][cc+2].born();
                            }
                            break;
                        //beacon
                        case 3:
                            grid[cr][cc].born();
                            if((cr+1)<row){
                                grid[cr+1][cc].born();
                                if((cc+1)<col){
                                    grid[cr+1][cc+1].born();
                                    grid[cr][cc+1].born();
                                    if((cc+2)<col && (cr+2)<row){
                                        grid[cr+2][cc+2].born();
                                        if((cr+3)<row){
                                            grid[cr+3][cc+2].born();
                                            if((cc+3)<col){
                                                grid[cr+3][cc+3].born();
                                                grid[cr+2][cc+3].born();
                                            }
                                        }else{
                                            if((cc+3)<col){
                                                grid[cr+2][cc+3].born();
                                            }
                                        }
                                    }
                                }
                            }else{
                                if((cc+1)<col)
                                    grid[cr][cc+1].born();
                            }
                            break;
                        //pulsar
                        case 4:
                            grid[cr][cc].born();
                            if((cr+5)<row)
                                grid[cr+5][cc].born();
                            if((cr+7)<row)
                                grid[cr+7][cc].born();
                            if((cr+12)<row)
                                grid[cr+12][cc].born();
                            if((cc-2)>=0){
                                if((cr+2)<row)
                                    grid[cr+2][cc-2].born(); 
                                if((cr+3)<row)
                                    grid[cr+3][cc-2].born();
                                if((cr+4)<row)
                                    grid[cr+4][cc-2].born();
                                if((cr+8)<row)
                                    grid[cr+8][cc-2].born();
                                if((cr+9)<row)
                                    grid[cr+9][cc-2].born();
                                if((cr+10)<row)
                                    grid[cr+10][cc-2].born();
                            }
                            if((cc+1)<col){
                                grid[cr][cc+1].born();
                                if((cr+5)<row)
                                    grid[cr+5][cc+1].born();
                                if((cr+7)<row)
                                    grid[cr+7][cc+1].born();
                                if((cr+12)<row)
                                    grid[cr+12][cc+1].born();     
                                if((cc+2)<col){
                                    grid[cr][cc+2].born();
                                    if((cr+5)<row)
                                        grid[cr+5][cc+2].born();
                                    if((cr+7)<row)
                                        grid[cr+7][cc+2].born();
                                    if((cr+12)<row)
                                        grid[cr+12][cc+2].born();
                                    if((cc+3)<col){
                                        if((cr+2)<row)
                                            grid[cr+2][cc+3].born(); 
                                        if((cr+3)<row)
                                            grid[cr+3][cc+3].born();
                                        if((cr+4)<row)
                                            grid[cr+4][cc+3].born();
                                        if((cr+8)<row)
                                            grid[cr+8][cc+3].born();
                                        if((cr+9)<row)
                                            grid[cr+9][cc+3].born();
                                        if((cr+10)<row)
                                            grid[cr+10][cc+3].born();
                                        if((cc+5)<col){
                                            if((cr+2)<row)
                                                grid[cr+2][cc+5].born(); 
                                            if((cr+3)<row)
                                                grid[cr+3][cc+5].born();
                                            if((cr+4)<row)
                                                grid[cr+4][cc+5].born();
                                            if((cr+8)<row)
                                                grid[cr+8][cc+5].born();
                                            if((cr+9)<row)
                                                grid[cr+9][cc+5].born();
                                            if((cr+10)<row)
                                                grid[cr+10][cc+5].born();
                                            if((cc+6)<col){
                                                grid[cr][cc+6].born();
                                                if((cr+5)<row)
                                                    grid[cr+5][cc+6].born();
                                                if((cr+7)<row)
                                                    grid[cr+7][cc+6].born();
                                                if((cr+12)<row)
                                                    grid[cr+12][cc+6].born();
                                                if((cc+7)<col){
                                                    grid[cr][cc+7].born();
                                                    if((cr+5)<row)
                                                        grid[cr+5][cc+7].born();
                                                    if((cr+7)<row)
                                                        grid[cr+7][cc+7].born();
                                                    if((cr+12)<row)
                                                        grid[cr+12][cc+7].born();
                                                    if((cc+8)<col){
                                                        grid[cr][cc+8].born();
                                                        if((cr+5)<row)
                                                            grid[cr+5][cc+8].born();
                                                        if((cr+7)<row)
                                                            grid[cr+7][cc+8].born();
                                                        if((cr+12)<row)
                                                            grid[cr+12][cc+8].born();
                                                        if((cc+10)<col){
                                                            if((cr+2)<row)
                                                                grid[cr+2][cc+10].born(); 
                                                            if((cr+3)<row)
                                                                grid[cr+3][cc+10].born();
                                                            if((cr+4)<row)
                                                                grid[cr+4][cc+10].born();
                                                            if((cr+8)<row)
                                                                grid[cr+8][cc+10].born();
                                                            if((cr+9)<row)
                                                                grid[cr+9][cc+10].born();
                                                            if((cr+10)<row)
                                                                grid[cr+10][cc+10].born();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        //glider
                        case 5:
                            grid[cr][cc].born();
                            if((cc+1)<col){
                                if((cr+1)<row){
                                    grid[cr+1][cc+1].born();
                                    if((cr+2)<row)
                                        grid[cr+2][cc+1].born();
                                }    
                                if((cc+2)<col){
                                    grid[cr][cc+2].born();
                                    if((cr+1)<row)
                                        grid[cr+1][cc+2].born();
                                }
                            }
                            break;
                        //lightweight spaceship
                        case 6:
                            grid[cr][cc].born();
                            if((cc+3)<col)
                                grid[cr][cc+3].born();
                            if((cr+1)<row){
                                if((cc+4)<col)
                                    grid[cr+1][cc+4].born();
                                if((cr+2)<row){
                                    grid[cr+2][cc].born();
                                    if((cc+4)<col)
                                        grid[cr+2][cc+4].born();
                                    if((cr+3)<row){
                                        if((cc+1)<col){
                                            grid[cr+3][cc+1].born();
                                            if((cc+2)<col){
                                                grid[cr+3][cc+2].born();
                                                if((cc+3)<col){
                                                    grid[cr+3][cc+3].born();
                                                    if((cc+4)<col)
                                                        grid[cr+3][cc+4].born();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    } 
                }
            });
        }
        /**This metod kill the cell, setting color and the state*/
        public void die(){
            setBackground(DEAD);
            dead=true;
        }
        /**This metod make the cell born, setting color and state*/
        public void born(){
            setBackground(ALIVE);
            dead=false;
        }
        /**This metod return true if the cell is dead*/
        public boolean isDead(){
            return dead;
        }
    }
    /**This class specifies the play/pause button's behavior.*/
    public class PlayPause extends JButton{
        /**The constructor creates the button*/
        protected PlayPause(){
            super(new ImageIcon("images/playPause.jpg"));
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    click=0;
                    play=play?false:true;
                }
            });
        }
        /**This metod return true if the play/pause's state is paused*/
        public boolean isPaused(){
            return !play;
        }
    }
    /**Pattern is the button used to set a configuration of alive cells.*/
    public class Pattern extends JButton{
        /**The constructor build the button with the appropriate image and 
         * behavior*/
        protected Pattern(String t){
            super(new ImageIcon("images/" + t + ".jpg"));
            switch(t){
                case "blinker":
                    this.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            click=1;
                        }
                    });
                    break;
                case "toad":
                    this.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            click=2;
                        }
                    });
                    break;
                case "beacon":
                    this.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            click=3;
                        }
                    });
                    break;
                case "pulsar":
                    this.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            click=4;
                        }
                    });
                    break;
                case "glider":
                    this.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            click=5;
                        }
                    });
                    break;
                case "lwss":
                    this.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            click=6;
                        }
                    });
            }
            
        } 
    }
    /**Close is the button used to terminate the game.*/
    public class Close extends JButton{
        /**The constructor build the button with the close image*/
        protected Close(){
            super(new ImageIcon("images/close.jpg"));
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    System.exit(0);
                }
            });
        }
    }
    /**RowUpdater is the class that specifies what the threads have to do.*/
    public class RowUpdater extends Thread {
        /**This variable takes account on which row operate.*/
        private int currentRow;
        /**This is the value of how many rows skip from a scan and its next.*/
        private final int offset;
        /**The constructor saves the first row to scan and the offset.*/
        protected RowUpdater(int start,int os){
            currentRow=start;
            offset=os;
        }
        @Override
        /**This metod scan every cell on a row and decides which cells will die
         * according to how many neighbours them have.*/
        public void run(){
            int lnb;
            while(currentRow<row){
                for(int c=0;c<col;c++){
                    lnb=livingNeighbours(currentRow,c);
                    if(!grid[currentRow][c].isDead() && (lnb>4 || lnb<3)) {
                        next[currentRow][c]=false;
                    }else{
                        if(grid[currentRow][c].isDead() && lnb==3){
                            next[currentRow][c]=true;
                        }else
                            next[currentRow][c]=!grid[currentRow][c].isDead();
                    }
                }
                currentRow+=offset; //go to next row
            }
        }
        /**This metod scan the neighbour cells of the cell located with the 
         * coordinates passed as parameters and return the number of the aliving
         * neighbour cells*/
        private int livingNeighbours(int r, int c){
            int lnb=0;
            if(!grid[r][c].isDead())
                lnb++;
            if(r>0){
                if(!grid[r-1][c].isDead())
                    lnb++;
                if(r<row-1){
                    if(!grid[r+1][c].isDead())
                        lnb++;
                    if(c>0){
                        if(!grid[r-1][c-1].isDead())
                            lnb++;
                        if(!grid[r][c-1].isDead())
                            lnb++;
                        if(!grid[r+1][c-1].isDead())
                            lnb++;
                    }
                    if(c<col-1){
                        if(!grid[r-1][c+1].isDead())
                            lnb++;
                        if(!grid[r][c+1].isDead())
                            lnb++;
                        if(!grid[r+1][c+1].isDead())
                            lnb++;
                    }
                }else{
                    if(c>0){
                        if(!grid[r-1][c-1].isDead())
                            lnb++;
                        if(!grid[r][c-1].isDead())
                            lnb++;
                    }
                    if(c<col-1){
                        if(!grid[r][c+1].isDead())
                            lnb++;
                        if(!grid[r-1][c+1].isDead())
                            lnb++;
                    }
                }
            }else{
                if(!grid[r+1][c].isDead())
                    lnb++;
                if(c>0){
                    if(!grid[r][c-1].isDead())
                        lnb++;
                    if(!grid[r+1][c-1].isDead())
                        lnb++;
                }
                if(c<col-1){
                    if(!grid[r][c+1].isDead())
                        lnb++;
                    if(!grid[r+1][c+1].isDead())
                        lnb++;
                }

            }
            return lnb;
        }
    }
}