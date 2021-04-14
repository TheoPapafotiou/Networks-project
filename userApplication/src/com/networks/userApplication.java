package com.networks;

import ithakimodem.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

public class userApplication {

    private static final int BUFFER_SIZE = 4096; // 4KB
    public static void main(String[] args) throws IOException{
        Modem modem = new Modem(12000);
        modem.setTimeout(8000);
        modem.open("ithaki");

        (new userApplication()).initialization(modem, "ATD2310ITHAKI\r");
        //(new userApplication()).echo(modem, "E2449\r");
        //(new userApplication()).video(modem, "M3435\r");
        (new userApplication()).video(modem, "G5704\r");

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

            } 
            catch (Exception x) {
                break;
            }
        }
    }

    public void echo(Modem modem, String address) throws IOException{
        int sym;
        int packet_counter;
        double tic;
        double tac;
        int duration;
        long start;
        int[] delays = {};

        String message = "";

        start = System.currentTimeMillis();
        
        tic = 0;
        tac = 0;

        while((int)(tac - start) < 246000){
            modem.write(address.getBytes());
            for(;;){
                try{
                    sym = modem.read();
                    if(sym == -1){
                        break;
                    }
                    message += (char)sym;

                    if(message.contains("PSTART ")){
                        message = "";
                        tic = System.currentTimeMillis();
                        System.out.println("Tic = " + tic);
                    }

                    if(message.contains(" PSTOP")){
                        System.out.println(message);
                        message = "";
                        tac = System.currentTimeMillis();
                        duration = (int)(tac - tic);
                        delays = Arrays.copyOf(delays, delays.length + 1);
                        delays[delays.length - 1] = duration;
                        System.out.println("Response time = " + duration + "ms");
                    }
                }
                catch(Exception ex) {
                    break;
                }
            }
        }
        try (FileWriter pr = new FileWriter("Delays.csv")){
            for (int j = 0; j < delays.length; j++){
                pr.append(String.valueOf(delays[j]));
                pr.append("\n");
            }
            pr.close();
        }
    }

    public void video(Modem modem, String address) throws IOException{
        byte[] buffer = new byte[BUFFER_SIZE];
        int count_bytes = 0;
        String outputFile = "FrameErrors.jpg";

        modem.write(address.getBytes());
        try(OutputStream outputStream = new FileOutputStream(outputFile);)
        {
            int byteRead;
            while ((byteRead = modem.read()) != -1){
                //System.out.println("ByteRead = " + byteRead);
                outputStream.write(byteRead);
                count_bytes++;
            }
            System.out.println("Number of bytes: " + count_bytes);               
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
