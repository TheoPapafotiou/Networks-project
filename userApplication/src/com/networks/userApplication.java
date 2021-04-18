package src.com.networks;

import ithakimodem.*;
import java.io.*;
import java.util.Arrays;

public class userApplication {

    public static void main(String[] args) throws IOException{
        Modem modem = new Modem(12000);
        modem.setTimeout(8000);
        modem.open("ithaki");

        (new userApplication()).initialization(modem, "ATD2310ITHAKI\r");
        //(new userApplication()).echo(modem, "E2449\r");
        //(new userApplication()).video(modem, "M3435 CAM=PTZ\r");
        //(new userApplication()).video(modem, "G5704 CAM=PTZ\r");

        int numOfT = 60; // number of points
        (new userApplication()).gps_image(modem, (new userApplication()).gps_T((new userApplication()).gps(modem, "P0480"+"R=10044"+String.valueOf(numOfT)+"\r")), "P0480");

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
        // byte[] buffer = new byte[BUFFER_SIZE];
        int count_bytes = 0;
        String outputFile = "Frame.jpg";

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

    public StringBuilder gps(Modem modem, String address) throws IOException{
        modem.write(address.getBytes());
        int gps;
        StringBuilder message = new StringBuilder();
        for(;;)
        {
            try{
                gps = modem.read();
                if(gps == -1){
                    break;
                }
                message.append((char)gps); //append((char)gps);
                //System.out.println(message);

                if(message.toString().contains("START ITHAKI GPS TRACKING\r\n")){
                    message.delete(0, message.length());
                }

                if(message.toString().contains("STOP ITHAKI GPS TRACKING\r\n")){
                    message.delete(message.length()-28, message.length());
                    break;            
                }
            }
            catch(Exception ex) {
                break;
            }
        }
        System.out.println(message);
        return message;
    }

    public String[] gps_T(StringBuilder message) throws IOException{
        
        String T_messages[] = {};
        String messageN = "N";
        String messageE = "E";
        
        System.out.println(message.length());
        for (int i = 0; i < message.length(); i++)
        {
            if (message.charAt(i) == 'N' && message.charAt(i-1) == ',')
            {
                messageN = message.toString().substring(i-10, i-6);
                int sec = Integer.parseInt(message.toString().substring(i-5, i-1));
                System.out.println(sec);
                sec *= 0.006;
                messageN += String.valueOf(sec);
            }
            if (message.charAt(i) == 'E' && message.charAt(i-1) == ',')
            {
                messageE = message.toString().substring(i-10, i-6);
                int sec = Integer.parseInt(message.toString().substring(i-5, i-1));
                System.out.println(sec);
                sec *= 0.006;
                messageE += String.valueOf(sec);

                T_messages = Arrays.copyOf(T_messages, T_messages.length + 1);
                T_messages[T_messages.length - 1] = messageE+messageN;

                System.out.println(T_messages[T_messages.length - 1]);
            }
        }
        return T_messages;
    }
    
    public void gps_image(Modem modem, String[] T_messages,  String address)
    {
        
        int count_bytes = 0;
        String outputFile = "GPS.jpg";

        for (int i = 0; i < 54; i+= 6)
        {
            address += "T=";
            address += T_messages[i];
        }
        System.out.println(address);
        for (int i = 0; i < address.length(); i++)
        {
            if (address.charAt(i) == '.')
            {
                address = address.replace(".", "");
            }
        }
        address += "\r";
        System.out.println(address);
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
