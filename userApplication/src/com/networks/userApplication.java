package com.networks;

import ithakimodem.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class userApplication {
    public static void main(String[] args) {
        Modem modem = new Modem(8000);
        modem.setTimeout(2000);
        modem.open("ithaki");

        (new userApplication()).initialization(modem, "ATD2310ITHAKI\r");
        (new userApplication()).echo(modem, "E3865");

        modem.close();
    }

    public void initialization(Modem modem, String address) {
        int sym;

        modem.write(address.getBytes());
        for(;;) {
            try{
                sym = modem.read();
                if(sym == -1){
                    break;
                }
                System.out.print(((char) sym));

            } catch (Exception x) {
                break;
            }
        }
    }

    public void echo(Modem modem, String address){
        int sym;
        int packet_counter;
        double tic;
        double tac;
        int duration;
        String message = "";

        modem.write(address.getBytes());
        tic = System.currentTimeMillis();

        for(; ;){
            try{
                sym = modem.read();
                System.out.println("I'm here!");
                System.out.println("Sym =" + sym);
                if(sym == -1){
                    break;
                }
                message += (char)sym;
                System.out.println("Message = " + message);

                if(message.equals("PSTART ")){
                    message = "";
                }

                if(message.equals(" PSTOP")){
                    System.out.println(message);
                    message = "";
                    tac = System.currentTimeMillis();
                    duration = (int)(tac - tic);
                    System.out.println("Response time = " + duration + "ms");
                }
            }
            catch(Exception e) {
                break;
            }
        }


    }

}
