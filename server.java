import java.net.*;
import java.io.*;

// packet flags
// mode 00
// player ID 0000 (backwards order)
// 00 0000

// modes
// 00 - scores
// 01 - ojama
// 10 - gameover
// 11 - player ID from server

// magic packets
// 00 0000 <- game start

class server {
	static final int MODELEN = 2;
	static final int PLAYERLEN = 4;
	static final int DATALEN = 70;
	static final int SERVERPORT = 2424;
	static final int CLIENTPORT = 2323;

	static int mode = 0;
	static InetAddress[] playersAddress = new InetAddress[8];
	static int playersNum = 0;
	static boolean endGame = false;
	static int timer = 10;
	static int player = 1;
	static int temp_player = 0;
	static byte[] buf = new byte[DATALEN];
	static DatagramPacket packet = new DatagramPacket(buf, buf.length);

	static boolean killed = false;

	static DatagramSocket socketUp = null;
	static DatagramSocket socketDown = null;

	static int playerAlive[] = new int[8];
    static int playerstates = 0;

	public static void main(String[] args) {

		try { socketUp = new DatagramSocket(CLIENTPORT);
			socketDown = new DatagramSocket(SERVERPORT);
		} catch (IOException e) { }



		// initialize, put players' addresses to InetAddress players[]

		try { System.out.println();
			System.out.println("PUYOSERVER v0.2 at " + InetAddress.getLocalHost());
			System.out.println();
		} catch (UnknownHostException e) { }

		Thread register = new Thread () {

		    public void run () {
		    	while ( player < 8 && killed != true) {

		    		try {
		    				// receive packet

		    				socketDown.receive(packet);

		    				if (buf[0] == 1 && buf[1] == 1) {
		    					System.out.println("New player: " + packet.getAddress() + " " + player);

		    					// give new number

		    					playersAddress[player] = packet.getAddress();

		    					// send it

		    					temp_player = player;

		    					buf[5] = (byte)(temp_player/8);
		    					temp_player %= 8;
		    					buf[4] = (byte)(temp_player/4);
		    					temp_player %= 4;
		    					buf[3] = (byte)(temp_player/2);
		    					temp_player %= 2;
		    					buf[2] = (byte)temp_player;

		    					socketUp.send(new DatagramPacket(buf, buf.length, packet.getAddress(), CLIENTPORT));

		    					player++; // tail

		    				}
		    		
		    		} catch (IOException e) { }
		    	}
		    }
		};

		register.start();

        sleep(20*1000);

        /* while (true) {
            if (System.console().readLine().equals("")) break;
        } */

        System.out.println("Registration is over");

        killed = true; // kill register thread

        player--; // decrement tail

        for ( int n = 0; n < 6 ; n++ ) buf[n] = 0; // Game start magic packet

        temp_player = player;

        buf[9] = (byte)(temp_player/8);
        temp_player %= 8;
        buf[8] = (byte)(temp_player/4);
        temp_player %= 4;
        buf[7] = (byte)(temp_player/2);
        temp_player %= 2;
        buf[6] = (byte)temp_player; // player count to binary

        System.out.println(player + " player(s) online");

        for ( int n = 1; n <= player ; n++ ) {
            System.out.println("Starting player " + playersAddress[n] + " ID: " + n);

            try { socketUp.send(new DatagramPacket(buf, buf.length, playersAddress[n], CLIENTPORT));
            } catch (IOException e) { }
        }

        try {
            socketDown.setSoTimeout(1000);
        } catch (SocketException e) {}

        for (int n = 1; n <= player; n++) playerAlive[n] = 0;

        while (!endGame) {
                    System.out.println("Listening...");
                    try {

                        socketDown.receive(packet);

                    } catch (IOException e) { }

            if (packet.getAddress()!=null) {
                for (int n = 0; n < buf.length; n++) System.out.print(buf[n]);

                System.out.println();

                for (int n = 1; n <= player; n++)

                    if (playersAddress[n] != packet.getAddress())
                        try {
                            socketUp.send(new DatagramPacket(buf, buf.length, playersAddress[n], CLIENTPORT));
                        } catch (IOException e) { }
            }

            if (buf[0] == 1 && buf[1] == 0) playerAlive[parseID(buf, 2, 5)] = 0;

            for (int n = 1; n <= player; n++) playerstates += playerAlive[n];

            if (playerstates==0) endGame = true;

            playerstates = 0;

            }



        socketDown.close();
        socketUp.close();

		// Game over, kill server
		return;


	}

	static void sleep(long msec) {
        try{
            Thread.sleep(msec);
        }catch(InterruptedException ie) {
        }
    }

    public static int parseID(byte[] in, int start, int end) {
        int res = 0;

        for ( int n = start; n <= end; n++ ) {
            res += (int)in[n]*Math.pow(2, n-start);
        }

        return res;
    }

}