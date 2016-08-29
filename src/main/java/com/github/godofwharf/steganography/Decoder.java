/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.godofwharf.steganography;

import java.io.*;
import java.net.Socket;

/**
 * @author godofwharf
 */
public class Decoder {
    private int[] pw9;
    private int[][] M;

    public Decoder(int[][] M) {
        this.pw9 = new int[4];
        this.pw9[0] = 1;
        this.pw9[1] = 9;
        this.pw9[2] = 81;
        this.pw9[3] = 729;
        this.M = M;
    }

    public String decode(int[] encodedMessage) throws Exception {
        String msg = decodeMessage(encodedMessage);
        System.out.println("Message decoded successfully");
        System.out.println(msg);
        return msg;
    }

    private int convertToDecimal(String base9) throws Exception {
        int ret = 0;
        for (int i = 0; i < base9.length(); i++) {
            ret += (base9.charAt(i) - '0') * pw9[2 - i];
        }
        return ret;
    }

    private String decodeMessage(int[] encodedMessage) throws Exception {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < encodedMessage.length; i++) {
            int R = (encodedMessage[i] >> 16) & 0xff;
            int G = (encodedMessage[i] >> 8) & 0xff;
            int B = (encodedMessage[i]) & 0xff;
            if (R == 255 && G == 255 && B == 255)
                break;

            R = R % 9;
            G = G % 9;
            int digit = M[R][G];
            sb.append(digit);
        }

        String s = sb.toString();
        String[] tokens = new String[500];
        int k = 0;
        for (int i = 0; i < s.length(); i += 3) {
            tokens[k++] = sb.substring(i, i + 3);
        }

        int[] values = new int[500];

        for (int i = 0; i < k; i++) {
            values[i] = convertToDecimal(tokens[i]);
        }

        sb = new StringBuilder("");
        for (int i = 0; i < k; i++) {
            char c;
            if (values[i] <= 122)
                c = (char) values[i];
            else
                c = (char) (values[i] - 91);
            sb.append(c);
        }
        return sb.toString();
    }
}
