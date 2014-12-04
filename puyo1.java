import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

class IPanel extends JPanel
{

    Color bgcolor;

    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x,y,width,height);
    }
    public Color getBackground()
    {
        return bgcolor;
    }
    public void setBackground(Color color)
    {
        super.setBackground(color);
        bgcolor = color;
    }
}

class puyo1
{
    static JFrame myframe;
    static JLabel scoreLabel;
    static JLabel gameover;
    static JPanel mypanels[][];
    static int puyomatrix[][];
    static Color colorList[];
    static boolean dropFlag;
    static int rensa = 0;
    static long score = 0;
    static int tempscore = 0;
    static int rensacount = 0;
    static int level = 1;
    static int puyoX = 3, puyoY = 1;
    static int puyoX2 = 3, puyoY2 = 2;
    static int color1 = 0, color2 = 0;
    static int rotate = 0;
    static boolean lock = false;
    static boolean lock2 = false;
    static JPanel nextpanel[];
    static int nextColor[];
    static JLabel playerlist[];

    static int myID = 0;
    static int temp_player = 0;

    static InetAddress server = null;

    static final int MODELEN = 2;
    static final int PLAYERLEN = 4;
    static final int DATALEN = 70;
    static final int SERVERPORT = 2424;
    static final int CLIENTPORT = 2323;
    static byte[] buf = new byte[DATALEN];
    static byte[] scoresBin = new byte[64];
    static DatagramPacket packet = new DatagramPacket(buf, buf.length);
    static DatagramSocket socketUp = null;
    static DatagramSocket socketDown = null;

    static boolean firstPlacing = true;
    static boolean gameIsOver = false;
    static int fallCount = 0;

    public static void main(String args[])
    {

        int i,x,y;

        final int puyoSize = 32;

        // init interface

        myframe =  new JFrame();
        myframe.setLayout(null); // does not use layout manager
        myframe.setSize(400,600); // window size : width = 400, height = 600

        myframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mypanels = new JPanel[13][8];
        puyomatrix = new int[13][8];
        colorList = new Color[5];

        nextpanel = new IPanel[2];
        nextColor = new int[2];
        nextColor[0] = (int)(Math.random()*3)+2;
        nextColor[1] = (int)(Math.random()*3)+2;


        colorList[0] = Color.gray;
        colorList[1] = Color.white;
        colorList[2] = Color.yellow;
        colorList[3] = Color.red;
        colorList[4] = Color.green;

        for( x = 0; x < 8; x++ )
        {
            for( y = 0; y < 13; y++ )
            {
                JPanel p = new IPanel();
                mypanels[y][x] = p;
                myframe.add(p);
                p.setBounds(30+puyoSize*x,40+puyoSize*y,puyoSize,puyoSize);
                setpuyo(x,y,0); // no puyo is here
            }
        }
        for( x = 0; x < 8; x++ )
        {
            setpuyo(x,12, 1); // ground is here
        }
        for( y = 0; y < 12; y++ )
        {
            setpuyo(0,y,1); // wall is here
            setpuyo(7,y,1); // wall is here
        }

        for(y = 0; y < 2; y++)
        {
            JPanel p = new IPanel();
            nextpanel[y] = p;
            myframe.add(p);
            p.setBounds(30 + puyoSize*8 + 10,
                    40 + puyoSize*y,
                    puyoSize,
                    puyoSize);
            nextpanel[y].setBackground(colorList[nextColor[y]]);
        }

        JPanel scorepanel = new JPanel();
        scorepanel.setBounds(40,470,600,40);

        scoreLabel = new JLabel();
        scorepanel.setBounds(0,470,60,40);
        scoreLabel.setText("Score: 0");

        gameover = new JLabel();
        myframe.add(gameover);
        gameover.setBounds(130, 0, 150, 20);


        scorepanel.add(scoreLabel);

        scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        scorepanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        scoreLabel.setVisible(true);

        myframe.add(scorepanel);

        // end init interface

        // setup key listeners

        KeyListener listener = new KeyListener() {

            @Override

            public void keyPressed(KeyEvent event) {

                if (event.getKeyCode() == KeyEvent.VK_UP) rotate();

                else if (event.getKeyCode() == KeyEvent.VK_LEFT) moveLeft();

                else if (event.getKeyCode() == KeyEvent.VK_RIGHT) moveRight();

                else if (event.getKeyCode() == KeyEvent.VK_DOWN) dropFlag = true;

            }

            @Override

            public void keyReleased(KeyEvent event) {

                if (event.getKeyCode() == KeyEvent.VK_DOWN) dropFlag = false;

            }

            @Override

            public void keyTyped(KeyEvent event) {

            }

        };

        myframe.addKeyListener(listener);

        // end setup key listeners

        // init network

        if (args.length == 1) try { server = InetAddress.getByName(args[0]);
        } catch(Exception e) { }

        else try {
            server = InetAddress.getByName("10.70.241.53");
        } catch (UnknownHostException e) { };

        try { socketUp = new DatagramSocket(SERVERPORT);
            socketDown = new DatagramSocket(CLIENTPORT);
        } catch (IOException e) { }

        System.out.println(server);

        buf[0] = 1;
        buf[1] = 1;

        for ( int n = 2; n < 6 ; n++ ) buf[n] = 0;

        try { socketUp.send(new DatagramPacket(buf, buf.length, server, SERVERPORT));
        } catch (IOException e) { }

        try { socketDown.receive(packet);
        } catch (IOException e) { }

        System.out.println("Got ID " + parseID(buf, 2, 5));

        myID = parseID(buf, 2, 5);

        // end init network

        myframe.setVisible(true); // make the window visible
        myframe.requestFocus();

        // wait for magic packet

        while (true) {
            try { socketDown.receive(packet);
            } catch (IOException e) { }
            if (buf[0] == 0 && buf[1] == 0 && buf[2] == 0 && buf[3] == 0 && buf[4] == 0 && buf[5] == 0) break; // magic packet!
        }

        System.out.println(parseID(buf, 6, 9) + " players online");

        playerlist(parseID(buf, 6, 9));

        sleepx(1000);
        gameover.setText("3");
        sleepx(1000);
        gameover.setText("2");
        sleepx(1000);
        gameover.setText("1");
        sleepx(1000);
        gameover.setText("");


        System.out.println("Till next level: " + (level*(level+1)*5));

        Thread gameplay = new Thread() {

            public void sleept(long msec)
            {
                try { Thread.sleep(msec);
                } catch (InterruptedException ie) { }
            }

            public void run() {
                while( gameIsOver != true)
                {//{{{
                    lock = false;
                    if (dropFlag) sleept(10);
                    else if (level<7) sleept(58 - level*8);
                    else sleept(8);

                    lock = true;
                    while( lock2 )
                    { sleept(10); }
                    if( firstPlacing )
                    {
                        firstPlacing = false;
                        color1 = nextColor[0];
                        color2 = nextColor[1];
                        rotate = 0;
                        puyoX = 3; puyoY = 1; // initialPlace
                        puyoX2 = getRotatedPositionX(puyoX,rotate);
                        puyoY2 = getRotatedPositionY(puyoY,rotate);
                        if( getpuyo(puyoX,puyoY) == 0
                                && getpuyo(puyoX2,puyoY2) == 0 )
                        {
                            setpuyo(puyoX,puyoY,color1);
                            setpuyo(puyoX2,puyoY2,color2);
                            // System.out.print("next puyo is here.\n");
                            setNext((int)(Math.random()*3)+2,
                                    (int)(Math.random()*3)+2);
                        } else
                        {
                            // GAME OVER
                            setpuyo(puyoX,puyoY,color1);
                            setpuyo(puyoX2,puyoY2,color2);
                            // System.out.print("batan kyu-\n");
                            gameover.setText("Game Over!");

                            gameIsOver = true;

                            buf[0] = 1;
                            buf[1] = 0;

                            temp_player = myID;

                            buf[5] = (byte)(temp_player/8);
                            temp_player %= 8;
                            buf[4] = (byte)(temp_player/4);
                            temp_player %= 4;
                            buf[3] = (byte)(temp_player/2);
                            temp_player %= 2;
                            buf[2] = (byte)temp_player;

                            buf[6] = 0;
                            buf[7] = 0;

                            System.out.println();

                            for ( int n = 0; n < buf.length ; n++ ) System.out.print(buf[n]);

                            System.out.println();

                            try { socketUp.send(new DatagramPacket(buf, buf.length, server, SERVERPORT));
                                System.out.println("Sent gameover packet!");
                            } catch (IOException e) { }

                            break;

                        }
                    }
                    if( fallCount > 5 )
                    {
                        fallCount = 0; // counter reset;
                        puyoY++; // fall it for one block
                        puyoY2++;
                        if( (rotate == 0 || getpuyo(puyoX,puyoY) == 0)
                                && (rotate == 2 || getpuyo(puyoX2,puyoY2) == 0) )
                        {
                            setpuyonorepaint(puyoX,puyoY-1,0);
                            setpuyonorepaint(puyoX2,puyoY2-1,0);
                            setpuyonorepaint(puyoX,puyoY,color1);
                            setpuyonorepaint(puyoX2,puyoY2,color2);
                            myframe.repaint();
                        } else
                        {
                            firstPlacing = true;
                            // undo falling
                            puyoY--;
                            puyoY2--;
                            // fall each puyo
                            while(rotate != 0 && getpuyo(puyoX,puyoY+1) == 0)
                            {
                                setpuyonorepaint(puyoX,puyoY,0);
                                puyoY++;
                                setpuyonorepaint(puyoX,puyoY,color1);
                                sleept(20);
                                myframe.repaint();
                            }
                            while(getpuyo(puyoX2,puyoY2+1) == 0)
                            {
                                setpuyonorepaint(puyoX2,puyoY2,0);
                                puyoY2++;
                                setpuyonorepaint(puyoX2,puyoY2,color2);
                                sleept(20);
                                myframe.repaint();
                            }
                            while(rotate == 0 && getpuyo(puyoX,puyoY+1) == 0)
                            {
                                setpuyonorepaint(puyoX,puyoY,0);
                                puyoY++;
                                setpuyonorepaint(puyoX,puyoY,color1);
                                sleept(20);
                                myframe.repaint();
                            }

                            // clear connected puyos and falldown other puyos...
                            // clear connected puyos and falldown other puyos...
                            // !!--check here--!!
                            while( areConnectedPuyosCleared() )
                            {
                                rensa++;
                                fallPuyos();
                                sleept(150);
                            }

                            if (rensa > 1) System.out.println("Rensa: " + rensa);

                            if (tempscore!=0) System.out.println("Turn score: " + tempscore);

                            score+=tempscore*rensa;

                            rensacount+=rensa;

                            System.out.println(rensacount);

                            // System.out.println("Score: " + score);

                            scoreLabel.setText("Score: " + score);
                            playerlist[myID-1].setText(myID + ". " + score);

                            scoreLabel.repaint();
                            playerlist[myID-1].repaint();

                            // send own score to others

                            buf[0] = 0;
                            buf[1] = 0;

                            temp_player = myID;

                            buf[5] = (byte)(temp_player/8);
                            temp_player %= 8;
                            buf[4] = (byte)(temp_player/4);
                            temp_player %= 4;
                            buf[3] = (byte)(temp_player/2);
                            temp_player %= 2;
                            buf[2] = (byte)temp_player;

                            // 6-70 backward long byte

                            scoresBin = toBytes(Long.reverse(score));

                            for ( int n = 6; n <= buf.length ; n++ ) buf[n] = scoresBin[n-6];

                            // send packet

                            try { socketUp.send(new DatagramPacket(buf, buf.length, server, SERVERPORT));
                            } catch (IOException e) { }

                            rensa = 0;

                            tempscore = 0;

                            // !!--check here--!!
                        }

                    } else
                    {
                        fallCount++;
                    }
                    if (rensacount>=(level*(level+1)*5)) {

                        level++;

                        for ( int m = 1; m < 12 ; m++ ) {
                            for ( int n = 1; n < 7; n++ ) {
                                setpuyonorepaint(n,m,0);
                            }
                        }

                        myframe.repaint();

                        System.out.println("Level " + level);
                        System.out.println("Till next level: " + (level*(level+1)*5));

                        sleept(2000);

                    }
                }//}}}

                return;
            }
        };

        Thread networkDownstream = new Thread() {
            public void run() {
                while (!gameIsOver) {
                    try { socketDown.receive(packet);
                        for ( int n = 0; n < buf.length ; n++ ) System.out.print(buf[n]);
                    } catch (IOException e) { }

                    if (buf[0] == 0 && buf[1] == 0) playerlist[parseID(buf, 2, 5)].setText(parseID(buf, 2, 5) + ". " + parseID(buf, 6, 70));
                }
            }
        };

        gameplay.start();
        networkDownstream.start();

        try {
            gameplay.join();
            networkDownstream.join();
        } catch (InterruptedException e) { }

        socketDown.close();
        socketUp.close();

        return;

    }

    static void playerlist(int num)
    {
        int idx;

        playerlist = new JLabel[num];

        for (idx = 0; idx < num; idx++ ) {
            playerlist[idx] = new JLabel();
            myframe.add(playerlist[idx]);
            playerlist[idx].setBounds(300, 300+(idx*15), 30, 10);
            playerlist[idx].setText(""+(idx+1));
        }

        myframe.repaint();
    }
    public static int parseID(byte[] in, int start, int end) {
        int res = 0;

        for ( int n = start; n <= end; n++ ) {
            res += (int)in[n]*Math.pow(2, n-start);
        }

        return res;
    }
    static void setpuyo(int x, int y, int color)
    {
        puyomatrix[y][x] = color;
        mypanels[y][x].setBackground(colorList[color]);
        mypanels[y][x].repaint();
    }
    static void setpuyonorepaint(int x, int y, int color)
    {
        puyomatrix[y][x] = color;
        mypanels[y][x].setBackground(colorList[color]);
    }
    static void setNext(int color1,int color2)
    {
        nextColor[0] = color1;
        nextColor[1] = color2;
        nextpanel[0].setBackground(colorList[nextColor[0]]);
        nextpanel[1].setBackground(colorList[nextColor[1]]);
        nextpanel[0].repaint();
        nextpanel[1].repaint();
    }
    static int getpuyo(int x, int y)
    {
        return puyomatrix[y][x];
    }
    static int getRotatedPositionX(int x, int r)
    {
        int rx = 0;
        switch(r)
        {
            case 0:
                rx = x; break;
            case 1:
                rx = x-1; break;
            case 2:
                rx = x; break;
            case 3:
                rx = x+1; break;
        }
        return rx;
    }
    //   2
    // 1 o 3
    //   0
    static int getRotatedPositionY(int y, int r)
    {
        int ry = 0;
        switch(r)
        {
            case 0:
                ry = y+1; break;
            case 1:
                ry = y; break;
            case 2:
                ry = y-1; break;
            case 3:
                ry = y; break;
        }
        return ry;
    }

    static boolean rotate()
    {
        int nx,ny,nr;
        lock2 = true;
        if(lock==false && (puyoX+puyoX2+puyoY+puyoY2!=0)) // "back from dead" bug fix
        {
            nr = (rotate + 1)%4;
            nx = getRotatedPositionX(puyoX,nr);
            ny = getRotatedPositionY(puyoY,nr);
            if( getpuyo(nx,ny) == 0 )
            {
                rotate = nr;
                setpuyo(puyoX2,puyoY2,0);
                puyoX2 = nx;
                puyoY2 = ny;
                setpuyo(puyoX2,puyoY2,color2);
                lock2 = false;
                return true;
            }
        }
        lock2 = false;
        return false;
    }
    static boolean moveLeft()
    {
        int nx,ny,nr;
        lock2 = true;
        if(lock==false && (puyoX+puyoX2+puyoY+puyoY2!=0)) // "back from dead" bug fix
        {
            if( (rotate == 1 || getpuyo(puyoX-1,puyoY) == 0)
                    && (rotate == 3 || getpuyo(puyoX2-1,puyoY2) == 0) )
            {
                setpuyo(puyoX,puyoY,0);
                setpuyo(puyoX2,puyoY2,0);
                puyoX--;
                puyoX2--;
                setpuyo(puyoX,puyoY,color1);
                setpuyo(puyoX2,puyoY2,color2);
                lock2 = false;
                return true;
            }
        }
        lock2 = false;
        return false;
    }
    static boolean moveRight()
    {
        int nx,ny,nr;
        lock2 = true;
        if(lock==false && (puyoX+puyoX2+puyoY+puyoY2!=0)) // "back from dead" bug fix
        {
            if( (rotate == 3 || getpuyo(puyoX+1,puyoY) == 0)
                    && (rotate == 1 || getpuyo(puyoX2+1,puyoY2) == 0) )
            {
                setpuyo(puyoX,puyoY,0);
                setpuyo(puyoX2,puyoY2,0);
                puyoX++;
                puyoX2++;
                setpuyo(puyoX,puyoY,color1);
                setpuyo(puyoX2,puyoY2,color2);
                lock2 = false;
                return true;
            }
        }
        lock2 = false;
        return false;
    }
    static boolean areConnectedPuyosCleared()
    {
        boolean cleared = false;
        int x,y;
        java.util.Vector v;
        for( x = 1; x <= 6; x++ )
        {
            for( y = 0; y <= 12; y++ )
            {
                v = getConnectedPuyosFrom(x,y);
                // for debugging...
                // if( v.size() >= 1 )
                // {
                //    System.out.println("("+x+","+y+")=|"+v.size()+"|");
                // }
                if( v.size() >= 4 )
                {
                    int vi;
                    Point p;
                    for(vi = 0; vi < v.size(); vi++ )
                    {
                        p = (Point)v.elementAt(vi);
                        setpuyo(p.x, p.y, 0); // clear it
                        sleepx(100);
                    }
                    cleared = true;

                    puyoX=0;
                    puyoX2=0;
                    puyoY=0;
                    puyoY2=0;

                    tempscore += v.size()*50;
                }
            }
        }
        return cleared;
    }
    static java.util.Vector<Point> getConnectedPuyosFrom(int x, int y)
    {
        java.util.Vector<Point> v = new java.util.Vector<Point>();
        getConnectedPuyosFrom(x,y,v);
        return v;
    }
    // NOTE: an example of method overloading...
    static void getConnectedPuyosFrom(int x, int y, java.util.Vector<Point> v)
    {
        int vi;
        int color;
        Point p;

        if( x < 0 || x > 6 || y > 12 || y < 0 ) return;
        if( getpuyo(x,y) < 2 ) return;

        if( v.size() == 0 )
        {
            v.add(new Point(x,y));
            color = getpuyo(x,y);
        } else
        {
            p = (Point)v.elementAt(0);
            color = getpuyo(p.x,p.y);

            if( color != getpuyo(x,y) ) return; // not same color

            for( vi = 0; vi < v.size(); vi++ )
            {
                p = (Point)v.elementAt(vi);
                if( p.x == x && p.y == y )
                {
                    // this (x,y) has already been checked. do nothing...
                    return;
                }
            }

            // this (x,y) should be added to v
            v.add(new Point(x,y));
        }

        // Is the below OK?
        getConnectedPuyosFrom(x,y+1,v);
        getConnectedPuyosFrom(x+1,y,v);
        getConnectedPuyosFrom(x-1,y,v);
        getConnectedPuyosFrom(x,y-1,v);

        return;
    }
    static void fallPuyos()
    {
        int fallcount;
        do
        {
            fallcount = 0;
            int x,y;
            for( y = 10; y > 0; y-- )
            {
                for( x = 1; x <= 6; x++ )
                {
                    if( getpuyo(x,y) != 0 && getpuyo(x,y+1) == 0 )
                    {
                        setpuyonorepaint(x,y+1,getpuyo(x,y)); // fall the puyo
                        setpuyonorepaint(x,y,0); // the last position should be empty
                        fallcount++; // count-up fallout counter
                        sleepx(100);
                    }
                }

            }
            myframe.repaint();
        } while( fallcount != 0 );
    }
    static void sleepx(long msec)
    {
        try { Thread.sleep(msec);
        } catch (InterruptedException ie) { }
    }
    public static byte[] toBytes(long b)
    {
        byte[] temp = new byte[8];
        for(int i = 0; i < 8; i++)
        {
            temp[i]=(byte)(b >> (8 * (8 - (i+1))));
        }
        return temp;
    }
}
