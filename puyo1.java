import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class YPanel extends XPanel 
{
	JPanel innerPanel3;
	JPanel innerPanel4;
	Color bgcolor;

	public YPanel() 
	{
		setLayout(null);
		innerPanel3 = new JPanel();
		add(innerPanel3);
		innerPanel3.setBackground(Color.black);

		innerPanel4 = new JPanel();
		add(innerPanel4);
		innerPanel4.setBackground(Color.black);

		bgcolor = super.getBackground();
	}
	public void setBounds(int x, int y, int width, int height) 
	{
		super.setBounds(x,y,width,height);
		innerPanel3.setBounds(4,height-8,width-8,8);
		innerPanel4.setBounds(width-8,4,8,height-8);
	}
	public Color getBackground() 
	{
		return bgcolor;
	}
	public void setBackground(Color color) 
	{
		bgcolor = color;
		super.setBackground(color);
	}
}

class XPanel extends JPanel 
{
	JPanel innerPanel1;
	JPanel innerPanel2;

	public XPanel() 
	{
		setLayout(null);
		innerPanel1 = new JPanel();
		add(innerPanel1);
		innerPanel1.setBackground(Color.white);

		innerPanel2 = new JPanel();
		add(innerPanel2);
		innerPanel2.setBackground(Color.white);
	}
	public void setBounds(int x, int y, int width, int height) 
	{
		super.setBounds(x,y,width,height);
		innerPanel1.setBounds(0,0,width-8,8);
		innerPanel2.setBounds(0,0,8,height-8);
	}
}

class puyo1 
{
	static JFrame myframe;
	static JLabel scoreLabel;
	static JPanel mypanels[][];
	static int puyomatrix[][];
	static Color colorList[];
	static boolean dropFlag;
	static int rensa = 0;
	static long score = 0;
	static int tempscore = 0;
	static int puyoX = 3, puyoY = 1;
	static int puyoX2 = 3, puyoY2 = 2;
	static int color1 = 0, color2 = 0;
	static int rotate = 0;
	static boolean lock = false;
	static boolean lock2 = false;

	static JPanel nextpanel[];
	static int nextColor[];

	public static void main(String args[]) 
	{
		int i,x,y;
		final int puyoSize = 32;

		myframe =  new JFrame();
		myframe.setLayout(null); // does not use layout manager
		myframe.setSize(400,600); // window size : width = 400, height = 600
		
		myframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mypanels = new JPanel[13][8];
		puyomatrix = new int[13][8];
		colorList = new Color[5];

		nextpanel = new YPanel[2];
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
				JPanel p = new YPanel();
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
			JPanel p = new YPanel();
			nextpanel[y] = p;
			myframe.add(p);
			p.setBounds(30 + puyoSize*8 + 10,
						40 + puyoSize*y,
						puyoSize,
						puyoSize);
			nextpanel[y].setBackground(colorList[nextColor[y]]);
		}

		JPanel panel = new JPanel();
		panel.setBounds(40,470,600,40);

		scoreLabel = new JLabel();
		scoreLabel.setBounds(0, 0, 200, 40);
		scoreLabel.setText("Score: 0");

		panel.add(scoreLabel);

		scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		panel.setAlignmentX(Component.LEFT_ALIGNMENT);

		scoreLabel.setVisible(true);

		myframe.add(panel);

		myframe.setVisible(true); // make the window visible
		myframe.requestFocus();

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

		boolean firstPlacing = true;
		boolean gameIsOver = false;
		int fallCount = 0;

		
		while( gameIsOver != true) 
		{//{{{
			lock = false;
			if (dropFlag) sleep(10);
			else sleep(100);
			lock = true;
			while( lock2 ) 
			{ sleep(10); }
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
					System.out.print("batan kyu-\n");
					gameIsOver = true;
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
					setpuyo(puyoX,puyoY-1,0);
					setpuyo(puyoX2,puyoY2-1,0);
					setpuyo(puyoX,puyoY,color1);
					setpuyo(puyoX2,puyoY2,color2);
				} else 
				{
					firstPlacing = true;
					// undo falling
					puyoY--;
					puyoY2--;
					// fall each puyo
					while(rotate != 0 && getpuyo(puyoX,puyoY+1) == 0) 
					{
						setpuyo(puyoX,puyoY,0);
						puyoY++;
						setpuyo(puyoX,puyoY,color1);
						sleep(20);
					}
					while(getpuyo(puyoX2,puyoY2+1) == 0) 
					{
						setpuyo(puyoX2,puyoY2,0);
						puyoY2++;
						setpuyo(puyoX2,puyoY2,color2);
						sleep(20);
					}
					while(rotate == 0 && getpuyo(puyoX,puyoY+1) == 0) 
					{
						setpuyo(puyoX,puyoY,0);
						puyoY++;
						setpuyo(puyoX,puyoY,color1);
						sleep(20);
					}
					// clear connected puyos and falldown other puyos...
					// clear connected puyos and falldown other puyos...
					// !!--check here--!!
					while( areConnectedPuyosCleared() ) 
					{
						rensa++;
					   	fallPuyos();
						sleep(300);
					}

					if (rensa > 1) System.out.println("Rensa: " + rensa);

					System.out.println("Turn score: " + tempscore);

					score+=tempscore*rensa;

					// System.out.println("Score: " + score);

					scoreLabel.setText("Score: " + score);

					scoreLabel.repaint();

					rensa = 0;

					tempscore = 0;

					// !!--check here--!!
				}
			} else 
			{
				fallCount++;
			}
		}//}}}
	}
	
	public static void rotateBtnMouseReleased(MouseEvent e) 
	{
		rotate();
	}
	public static void leftBtnMouseReleased(MouseEvent e) 
	{
		moveLeft();
	}
	public static void rightBtnMouseReleased(MouseEvent e) 
	{
		moveRight();
	}
	static void setpuyo(int x, int y, int color) 
	{
		puyomatrix[y][x] = color;
		mypanels[y][x].setBackground(colorList[color]);
		mypanels[y][x].repaint();
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
	{ // why don't you use this!?
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
		if(lock==false) 
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
		if(lock==false) 
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
		if(lock==false) 
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
				if( v.size() >= 1 ) 
				{
				    System.out.println("("+x+","+y+")=|"+v.size()+"|");
				}
				if( v.size() >= 4 ) 
				{
					int vi;
					Point p;
					for(vi = 0; vi < v.size(); vi++ ) 
					{
						p = (Point)v.elementAt(vi);
						setpuyo(p.x, p.y, 0); // clear it
						sleep(100);
					}
					cleared = true;

					tempscore += v.size()*50;
				}
			}
		}
		return cleared;
	}
	static java.util.Vector getConnectedPuyosFrom(int x, int y) 
	{
		java.util.Vector v = new java.util.Vector();
		getConnectedPuyosFrom(x,y,v);
		return v;
	}
	// NOTE: an example of method overloading...
	static void getConnectedPuyosFrom(int x, int y, java.util.Vector v) 
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
						setpuyo(x,y+1,getpuyo(x,y)); // fall the puyo
						setpuyo(x,y,0); // the last position should be empty
						fallcount++; // count-up fallout counter
						sleep(100);
					}
				}
			}
		} while( fallcount != 0 );
	}
	static void sleep(long msec) 
	{
		try
		{
			Thread.sleep(msec);
		}catch(InterruptedException ie) 
		{
		}
	}
}
