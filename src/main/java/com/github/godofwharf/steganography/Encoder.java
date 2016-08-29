package com.github.godofwharf.steganography;

public class Encoder {
    private final static String CHARSET = " !\"#$%&\'()*+,-./0123456789:;<=>?@";

    private int[] CH = new int[9];
    private int[] CV = new int[9];
    private int[][] CB = new int[3][3];
    private int[] X = new int[3];
    private int[] Y = new int[3];


    private int[][] M = new int[27][27];
    private int[][] sudoku = new int[9][9];

    private int[] RGBArray = new int[786440];
    private int[] modifiedRGBArray = new int[786440];

    public Encoder(final int[][] sudoku, final int[][] M) {
        this.M = M;
        this.sudoku = sudoku;
    }

    private String reverse(String str) {
        return new StringBuilder(str).reverse().toString();
    }

    private int findIndex(char sc) {
        for (int i = 0; i < CHARSET.length(); i++) {
            if (sc == CHARSET.charAt(i))
                return i;
        }
        return -1;
    }

    private void findCandidates(int R, int G, int Si) {
        int g1, g2;
        g1 = (R % 9) + 9;
        g2 = (G % 9) + 9;
        int cnt = 0;
        if (G > 3 && G < 252) {
            int k = 0;
            for (int i = g2 - 4; i <= g2 + 4; i++) {
                CH[k] = M[g1][i];
                if (CH[k] == Si) {
                    X[cnt] = g1;
                    Y[cnt] = i;
                    cnt += 1;
                }
                k++;
            }
        } else if (G <= 3) {
            int k = 0;
            for (int i = 9; i <= 17; i++) {
                CH[k] = M[g1][i];
                if (CH[k] == Si) {
                    X[cnt] = g1;
                    Y[cnt] = i;
                    cnt += 1;
                }
                k++;
            }
        } else {
            int k = 0;
            for (int i = 4; i <= 12; i++) {
                CH[k] = M[g1][i];
                if (CH[k] == Si) {
                    X[cnt] = g1;
                    Y[cnt] = i;
                    cnt += 1;
                }
                k++;
            }
        }
        if (R > 3 && R < 252) {
            int k = 0;
            for (int i = g1 - 4; i <= g1 + 4; i++) {
                CV[k] = M[i][g2];
                if (CV[k] == Si) {
                    X[cnt] = i;
                    Y[cnt] = g2;
                    cnt += 1;
                }
                k++;
            }
        } else if (R <= 3) {
            int k = 0;
            for (int i = 9; i <= 17; i++) {
                CV[k] = M[i][g2];
                if (CV[k] == Si) {
                    X[cnt] = i;
                    Y[cnt] = g2;
                    cnt += 1;
                }
                k++;
            }
        } else {
            int k = 0;
            for (int i = 4; i <= 12; i++) {
                CV[k] = M[i][g2];
                if (CV[k] == Si) {
                    X[cnt] = i;
                    Y[cnt] = g2;
                    cnt += 1;
                }
                k++;
            }
        }
        if (R < 252 && G < 255) {
            int xb = (g1 / 3) * 3;
            int yb = (g2 / 3) * 3;
            int r, c;
            r = 0;
            for (int i = xb; i <= xb + 2; i++) {
                c = 0;
                for (int j = yb; j <= yb + 2; j++)
                    CB[r][c++] = M[i][j];
                r++;
            }
        } else {
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    CB[i][j] = 9;
        }
    }

    private int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private int[] findMinDistortion(int R, int G, int Si) {
        int g1, g2;
        g1 = (R % 9) + 9;
        g2 = (G % 9) + 9;

        int k = 2;

        if (R < 252 && G < 255) {
            int xb = (g1 / 3) * 3;
            int yb = (g2 / 3) * 3;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++)
                    if (CB[i][j] == Si) {
                        X[k] = xb + i;
                        Y[k] = yb + j;
                        k++;
                        break;
                    }
            }
        }
        int[] ret = new int[2];
        int idx = 0;
        int mn = (1 << 31);
        for (int i = 0; i < k; i++) {
            int cur;
            if ((cur = manhattan(X[i], Y[i], g1, g2)) < mn) {
                idx = i;
                mn = cur;
            }
        }
        ret[0] = X[idx];
        ret[1] = Y[idx];
        return ret;
    }

    private String ConvertBase9(String st) throws Exception {
        String ret = "";
        int[] Ascii = new int[st.length() + 1];
        for (int i = 0; i < st.length(); i++) {
            if (Character.isLetter(st.charAt(i)))
                Ascii[i] = (int) st.charAt(i);
            else {
                int idx = findIndex(st.charAt(i));
                Ascii[i] = 123 + idx;
            }
        }

        for (int i = 0; i < st.length(); i++) {
            int x = Ascii[i];
            String cur = "";
            char c;
            while (x > 8) {
                c = (char) ((x % 9) + '0');
                cur += c;
                x /= 9;
            }
            c = (char) (x + '0');
            cur += c;
            cur = reverse(cur);

            ret += cur;
        }
        return ret;
    }

    private String normalize(String st) throws Exception {
        st = st.trim();
        st = st.toLowerCase();
        return st;
    }

    public int[] encode(String message) throws Exception {
        message = normalize(message);
        message = ConvertBase9(message);
        int cnt = 0;
        for (int i = 0; i < RGBArray.length; i++) {

            int R = (RGBArray[i] >> 16) & 0xff;
            int G = (RGBArray[i] >> 8) & 0xff;
            int B = (RGBArray[i]) & 0xff;
            int newR, newG;
            if (i < message.length()) {
                int d = message.charAt(i) - '0';
                findCandidates(R, G, d);

                int[] newValues = findMinDistortion(R, G, d);
                newR = newValues[0];
                newG = newValues[1];

                modifiedRGBArray[i] = (newR << 16) | (newG << 8) | B;
            } else if (i == message.length())
                modifiedRGBArray[i] = (255 << 16) | (255 << 8) | 255;
            else
                modifiedRGBArray[i] = (R << 16) | (G << 8) | B;
        }
        return modifiedRGBArray;
    }

}
